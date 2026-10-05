package cdma;

public class Sender {
    private int id;
    private int[] payloadBits;
    private int bitIndex = 0;
    private int destination = -1;
    
    public Sender(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
    
    public void setDestination(int dest) {
        this.destination = dest;
    }
    
    public int getDestination() {
        return destination;
    }
    
    public void setPayload(byte[] data) {
        payloadBits = new int[data.length * 8];
        for (int i = 0; i < data.length; i++) {
            for (int b = 7; b >= 0; b--) {
                payloadBits[i * 8 + (7 - b)] = (data[i] >> b) & 1;
            }
        }
        bitIndex = 0;
    }
    
    public boolean hasMoreData() {
        return payloadBits != null && bitIndex < payloadBits.length;
    }
    
    public Integer getNextBit() {
        if (!hasMoreData()) {
            return null;
        }
        return payloadBits[bitIndex++];
    }
    
    public int[] encode(Integer dataBit, int[] destinationCode) {
        int representation = 0;
        if (dataBit != null) {
            if (dataBit == 0) representation = -1;
            else if (dataBit == 1) representation = 1;
        } 
        
        int[] chips = new int[destinationCode.length];
        for (int i = 0; i < destinationCode.length; i++) {
            chips[i] = representation * destinationCode[i];
        }
        return chips;
    }
}
