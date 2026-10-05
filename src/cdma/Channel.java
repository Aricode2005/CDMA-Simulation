package cdma;

public class Channel {
    public static int[] combine(int[][] allChips) {
        if (allChips == null || allChips.length == 0) {
            return new int[0];
        }
        
        int numChips = allChips[0].length;
        int[] combined = new int[numChips];
        
        for (int[] chips : allChips) {
            for (int i = 0; i < numChips; i++) {
                combined[i] += chips[i];
            }
        }
        
        return combined;
    }
}
