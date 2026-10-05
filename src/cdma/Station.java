package cdma;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Station {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== CDMA Station Client ===");
        System.out.print("Enter Channel Server IP (Leave empty for localhost): ");
        String ip = sc.nextLine().trim();
        if(ip.isEmpty()) ip = "localhost";
        
        Socket socket = new Socket(ip, 8080);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        
        int myId = dis.readInt();
        int n = dis.readInt();
        System.out.println("\nSuccessfully connected to Channel Server!");
        System.out.println("Assigned Station ID: " + myId);
        System.out.println("Total stations in network (N): " + n);
        
        int[][] walshMatrix = Walsh.generate(n);
        int m = walshMatrix.length;
        
        System.out.println("\nEnter payload string.");
        System.out.println("(Type 'FILE' to load input.txt, or leave empty for silence)");
        System.out.print("Payload: ");
        String input = sc.nextLine().trim();
        
        byte[] payloadBytes = new byte[0];
        if (input.equals("FILE")) {
            try {
                payloadBytes = Files.readAllBytes(Paths.get("input.txt"));
                System.out.println("Loaded " + payloadBytes.length + " bytes from file.");
            } catch (Exception e) {
                System.out.println("Error reading input.txt. Defaulting to silence.");
            }
        } else if (!input.isEmpty()) {
            payloadBytes = input.getBytes();
        }
        
        int[] payloadBits = new int[payloadBytes.length * 8];
        System.out.println("\n--- Binary Transformation at Sender (Station " + myId + ") ---");
        if (payloadBytes.length == 0) {
            System.out.println("(Silence)");
        } else {
            for (int i = 0; i < payloadBytes.length; i++) {
                System.out.print("Char '" + (char)payloadBytes[i] + "' -> ");
                for (int b = 7; b >= 0; b--) {
                    int bit = (payloadBytes[i] >> b) & 1;
                    payloadBits[i * 8 + (7 - b)] = bit;
                    System.out.print(bit);
                }
                System.out.println();
            }
        }
        
        int dest = myId;
        if (payloadBytes.length > 0) {
            System.out.print("\nEnter destination Station ID (0 to " + (n-1) + "): ");
            dest = sc.nextInt();
            if (dest < 0 || dest >= n) {
                System.out.println("Invalid destination. Defaulting to Receiver " + myId);
                dest = myId;
            }
        }
        
        dos.writeInt(payloadBits.length);
        dos.flush();
        
        System.out.println("\nWaiting for all other stations to join and configure...");
        int maxBits = dis.readInt();
        System.out.println("All stations ready. Starting synchronized transmission loop (" + maxBits + " time slots)...");
        System.out.println();
        
        int[] destCode = walshMatrix[dest];
        int[] myCode = walshMatrix[myId];
        
        List<Integer> receivedBits = new ArrayList<>();
        List<Byte> reconstructedBytes = new ArrayList<>();
        int currentByte = 0;
        int bitCount = 0;
        
        for (int k = 0; k < maxBits; k++) {
            System.out.println("\n--- Time Slot " + k + " ---");
            Integer bitToSend = (k < payloadBits.length) ? payloadBits[k] : null;
            int representation = 0;
            if (bitToSend != null) {
                representation = (bitToSend == 1) ? 1 : -1;
            } 
            
            int[] sentChips = new int[m];
            System.out.print("[Sender] Encoding bit: " + (bitToSend == null ? "Silence" : bitToSend) + 
                             " using destination code -> Chips: [");
            for (int j = 0; j < m; j++) {
                sentChips[j] = representation * destCode[j];
                dos.writeInt(sentChips[j]);
                System.out.print(sentChips[j] + (j < m - 1 ? ", " : ""));
            }
            System.out.println("]");
            dos.flush();
            
            int[] combined = new int[m];
            System.out.print("[Receiver] Received Combined Chips: [");
            for (int j = 0; j < m; j++) {
                combined[j] = dis.readInt();
                System.out.print(combined[j] + (j < m - 1 ? ", " : ""));
            }
            System.out.print("] -> ");
            
            int dotProduct = 0;
            for (int j = 0; j < m; j++) {
                dotProduct += combined[j] * myCode[j];
            }
            int result = dotProduct / m;
            
            Integer decodedBit = null;
            if (result <= -1) decodedBit = 0;
            else if (result >= 1) decodedBit = 1;
            
            System.out.println("Dot Product with my code = " + dotProduct + " -> Decoded Bit: " + 
                               (decodedBit == null ? "Silence" : decodedBit));
            
            if (decodedBit != null) {
                receivedBits.add(decodedBit);
                currentByte = (currentByte << 1) | decodedBit;
                bitCount++;
                
                System.out.println("           Buffer: " + 
                    String.format("%" + bitCount + "s", Integer.toBinaryString(currentByte & ((1<<bitCount)-1))).replace(' ', '0'));
                
                if (bitCount == 8) {
                    reconstructedBytes.add((byte) currentByte);
                    System.out.println("           => 8 bits collected! Reconstructed Char: '" + (char)currentByte + "'");
                    currentByte = 0;
                    bitCount = 0;
                }
            }
        }
        
        System.out.println("\n--- Final Reconstructed Data at Receiver " + myId + " ---");
        if (reconstructedBytes.isEmpty()) {
            System.out.println("[No valid data received or decoded as silence]");
        } else {
            byte[] finalData = new byte[reconstructedBytes.size()];
            for(int i = 0; i < reconstructedBytes.size(); i++) {
                finalData[i] = reconstructedBytes.get(i);
            }
            System.out.println("Output: " + new String(finalData));
        }
        
        socket.close();
        sc.close();
    }
}
