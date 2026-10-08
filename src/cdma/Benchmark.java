package cdma;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class Benchmark {
    public static void main(String[] args) throws IOException {
        int[] ns = {4, 8, 16, 32};
        
        byte[] payloadBytes;
        try {
            payloadBytes = Files.readAllBytes(Paths.get("input.txt"));
        } catch (Exception e) {
            System.out.println("input.txt not found, using dummy payload.");
            payloadBytes = "Benchmarking CDMA simulation payload!".getBytes();
        }
        
        System.out.println("=== Starting High-Precision Automated Benchmark ===");
        
        FileWriter csv = new FileWriter("benchmark_results.csv");
        csv.write("N,TotalTimeSlots,ActiveTimeSlots,ExecutionTimeMs,Throughput\n");
        
        Random rand = new Random();
        
        int TARGET_TIME_SLOTS = 50000; 
        
        for (int n : ns) {
            System.out.println("Running simulation for N = " + n);
            int[][] walshMatrix = Walsh.generate(n);
            int m = walshMatrix.length;
            
            Sender[] senders = new Sender[n];
            Receiver[] receivers = new Receiver[n];
            
            for (int i = 0; i < n; i++) {
                senders[i] = new Sender(i);
                senders[i].setPayload(payloadBytes);
                senders[i].setDestination(rand.nextInt(n));
                
                receivers[i] = new Receiver(i, walshMatrix[i]);
            }
            
           
            int maxBits = Math.max(payloadBytes.length * 8, TARGET_TIME_SLOTS);
            int activeTimeSlots = 0;
            
            long startTime = System.nanoTime();
            
            for (int k = 0; k < maxBits; k++) {
                int[][] allChips = new int[n][m];
                boolean slotActive = false;
                
                for (int i = 0; i < n; i++) {
                    Integer bit = senders[i].getNextBit(); 
                    int dest = senders[i].getDestination();
                    allChips[i] = senders[i].encode(bit, walshMatrix[dest]);
                }
                
                int[] combined = Channel.combine(allChips);
                
                for (int val : combined) {
                    if (val != 0) {
                        slotActive = true;
                        break;
                    }
                }
                if (slotActive) {
                    activeTimeSlots++;
                }
                
                for (int i = 0; i < n; i++) {
                    receivers[i].receiveBit(receivers[i].decode(combined));
                }
            }
            
            long endTime = System.nanoTime();
            // Convert to milliseconds with decimal precision
            double durationMs = (endTime - startTime) / 1_000_000.0;
            
            // Throughput = Total network bits processed per second 
            // (N stations * maxBits) / (duration in seconds)
            double throughput = (maxBits * n * 1000.0) / durationMs;
            
            csv.write(n + "," + maxBits + "," + activeTimeSlots + "," + String.format("%.3f", durationMs) + "," + String.format("%.2f", throughput) + "\n");
            System.out.println(" -> Done. Exec Time: " + String.format("%.3f", durationMs) + "ms | Throughput: " + String.format("%.2f", throughput) + " bits/sec");
        }
        
        csv.close();
        System.out.println("Benchmark finished. Results written to benchmark_results.csv.");
    }
}
