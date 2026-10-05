package cdma;

import java.util.ArrayList;
import java.util.List;

public class Receiver {
    private int id;
    private int[] walshCode;
    private List<Integer> receivedBits = new ArrayList<>();
    
    public Receiver(int id, int[] walshCode) {
        this.id = id;
        this.walshCode = walshCode;
    }
    
    public int getId() {
        return id;
    }
    
    public void receiveBit(Integer bit) {
        if (bit != null) {
            receivedBits.add(bit);
        }
    }
    
    public byte[] getReconstructedPayload() {
        int numBytes = receivedBits.size() / 8;
        byte[] data = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            int b = 0;
            for (int j = 0; j < 8; j++) {
                b = (b << 1) | receivedBits.get(i * 8 + j);
            }
            data[i] = (byte) b;
        }
        return data;
    }
    
    public Integer decode(int[] combinedChips) {
        int dotProduct = 0;
        for (int i = 0; i < walshCode.length; i++) {
            dotProduct += combinedChips[i] * walshCode[i];
        }
        int result = dotProduct / walshCode.length;
        
        if (result <= -1) return 0;
        if (result >= 1) return 1;
        return null; 
    }
}
