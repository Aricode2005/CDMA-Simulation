package cdma;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ChannelServer {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== CDMA Distributed Channel Server ===");
        System.out.print("Enter number of stations (N): ");
        int n = sc.nextInt();
        
        if (n <= 0) {
            System.out.println("N must be positive.");
            return;
        }

        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Channel Server started on port 8080.");
        System.out.println("Waiting for " + n + " stations to connect...");

        Socket[] sockets = new Socket[n];
        DataInputStream[] dis = new DataInputStream[n];
        DataOutputStream[] dos = new DataOutputStream[n];

        for (int i = 0; i < n; i++) {
            sockets[i] = serverSocket.accept();
            dis[i] = new DataInputStream(sockets[i].getInputStream());
            dos[i] = new DataOutputStream(sockets[i].getOutputStream());
            
            dos[i].writeInt(i);
            dos[i].writeInt(n);
            dos[i].flush();
            System.out.println("Station " + i + " connected from " + sockets[i].getRemoteSocketAddress());
        }

        int maxBits = 0;
        for (int i = 0; i < n; i++) {
            int len = dis[i].readInt();
            if (len > maxBits) {
                maxBits = len;
            }
        }

        System.out.println("\nAll stations ready.");
        System.out.println("Maximum bit sequence to transmit across network: " + maxBits);
        
        for (int i = 0; i < n; i++) {
            dos[i].writeInt(maxBits);
            dos[i].flush();
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

        System.out.println("\nStarting multiplexing over " + maxBits + " time slots...");

        for (int bitIndex = 0; bitIndex < maxBits; bitIndex++) {
            int[][] allChips = new int[n][m];
            
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    allChips[i][j] = dis[i].readInt();
                }
            }
            
            int[] combined = new int[m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    combined[j] += allChips[i][j];
                }
            }
            
            System.out.print("[Time Slot " + bitIndex + "] Combined Channel Signal: [");
            for (int j = 0; j < m; j++) {
                System.out.print(combined[j] + (j < m - 1 ? ", " : ""));
            }
            System.out.println("]");
            
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    dos[i].writeInt(combined[j]);
                }
                dos[i].flush();
            }
        }

        System.out.println("\nTransmission complete. Shutting down Channel Server.");
        for (int i = 0; i < n; i++) {
            sockets[i].close();
        }
        serverSocket.close();
        sc.close();
    }
}
