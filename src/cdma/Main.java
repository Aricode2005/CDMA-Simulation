package cdma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== CDMA Simulation with Walsh Codes (Payload Support) ===");
        System.out.print("Enter the number of stations (N): ");
        int n = Integer.parseInt(scanner.nextLine().trim());
        
        if (n <= 0) {
            System.out.println("Number of stations must be positive.");
            return;
        }

        int[][] walshMatrix = Walsh.generate(n);
        int m = walshMatrix.length;
        System.out.println("\nGenerated Walsh Matrix of size " + m + "x" + m + ":");
        for (int i = 0; i < m; i++) {
            System.out.print("W[" + i + "]: ");
            for (int j = 0; j < m; j++) {
                System.out.printf("%3d ", walshMatrix[i][j]);
            }
            System.out.println();
        }

        Sender[] senders = new Sender[n];
        Receiver[] receivers = new Receiver[n];
        
        for (int i = 0; i < n; i++) {
            senders[i] = new Sender(i);
            receivers[i] = new Receiver(i, walshMatrix[i]);
        }
        
        System.out.println("\nConfigure transmissions.");
        System.out.println("Type 'FILE' to load 'input.txt' as payload.");
        System.out.println("Type any other text to send it as a string.");
        System.out.println("Leave empty (press Enter) for silence.");
        
        for (int i = 0; i < n; i++) {
            System.out.print("Sender " + i + " payload: ");
            String input = scanner.nextLine().trim();
            
            if (input.equals("FILE")) {
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get("input.txt"));
                    senders[i].setPayload(bytes);
                    System.out.println("   -> Loaded " + bytes.length + " bytes from input.txt");
                } catch (IOException e) {
                    System.out.println("   -> Error: input.txt not found. Setting silence.");
                }
            } else if (!input.isEmpty()) {
                senders[i].setPayload(input.getBytes());
            }
            
            if (senders[i].hasMoreData()) {
                System.out.print("Sender " + i + " destination receiver (0 to " + (n - 1) + "): ");
                int dest = Integer.parseInt(scanner.nextLine().trim());
                if (dest >= 0 && dest < n) {
                    senders[i].setDestination(dest);
                } else {
                    System.out.println("   -> Invalid receiver. Defaulting to Receiver " + i);
                    senders[i].setDestination(i);
                }
            }
        }

        System.out.println("\n--- Starting Bit-by-Bit CDMA Simulation over Time Slots ---");
        int timeSlot = 0;
        
        while (true) {
            boolean anyActive = false;
            for (int i = 0; i < n; i++) {
                if (senders[i].hasMoreData()) {
                    anyActive = true;
                    break;
                }
            }
            
            if (!anyActive) {
                break; 
            }
            
            int[][] allChips = new int[n][m];
            
            
            for (int i = 0; i < n; i++) {
                Integer bit = senders[i].getNextBit();
                int destCodeIndex = senders[i].getDestination();
                if (destCodeIndex == -1) destCodeIndex = i;                
                allChips[i] = senders[i].encode(bit, walshMatrix[destCodeIndex]);
            }
            
            int[] combinedChips = Channel.combine(allChips);
            
            for (int i = 0; i < n; i++) {
                Integer decoded = receivers[i].decode(combinedChips);
                receivers[i].receiveBit(decoded);
            }
            
            timeSlot++;
        }

        System.out.println("Simulation finished in " + timeSlot + " time slots (" + timeSlot + " bits).");
        
        System.out.println("\n--- Decoded Payloads at Receivers ---");
        for (int i = 0; i < n; i++) {
            byte[] data = receivers[i].getReconstructedPayload();
            if (data.length > 0) {
                System.out.println("Receiver " + i + " output: " + new String(data));
            } else {
                System.out.println("Receiver " + i + " output: [Silence / No Data]");
            }
        }
        
        scanner.close();
    }
}
