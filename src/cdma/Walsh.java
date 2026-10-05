package cdma;

public class Walsh {
    public static int[][] generate(int n) {
        int size = 1;
        while (size < n) {
            size *= 2;
        }
        
        int[][] matrix = new int[size][size];
        matrix[0][0] = 1;
        
        for (int k = 1; k < size; k *= 2) {
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < k; j++) {
                    matrix[i + k][j] = matrix[i][j];
                    matrix[i][j + k] = matrix[i][j];
                    matrix[i + k][j + k] = -matrix[i][j];
                }
            }
        }
        return matrix;
    }
}
