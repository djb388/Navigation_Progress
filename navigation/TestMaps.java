/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package navigation;

/**
 *
 * @author david
 */
public class TestMaps {
    
    public static void printMaps() {
        long[][] x = getXMap();
        long[][] y = getYMap();
        
        String[] xMap = new String[16];
        String[] yMap = new String[16];
        for(int i = 120; i < 136; i++) {
            for(int j = 120; j < 136; j++) {
                long nextX = x[i][j/64];
                long nextY = y[j][i/64];
            }
        }
    }
    
    public static long[][] getXMap() {
        long[][] x = new long[256][4];
        x[120] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000011111111L,0b1111111100000000000000000000000000000000000000000000000000000000L,0};
        x[121] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        x[122] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        x[123] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        x[124] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        x[125] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        x[126] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1000000100000000000000000000000000000000000000000000000000000000L,0};
        x[127] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1000000100000000000000000000000000000000000000000000000000000000L,0};
        x[128] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000011111111L,0b1000000100000000000000000000000000000000000000000000000000000000L,0};
        x[129] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1000000100000000000000000000000000000000000000000000000000000000L,0};
        x[130] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010111110L,0b1000000100000000000000000000000000000000000000000000000000000000L,0};
        x[131] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010100010L,0b1000000100000000000000000000000000000000000000000000000000000000L,0};
        x[132] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010101000L,0b1000000100000000000000000000000000000000000000000000000000000000L,0};
        x[133] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010111111L,0b1000000100000000000000000000000000000000000000000000000000000000L,0};
        x[134] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        x[135] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000011111111L,0b1111111100000000000000000000000000000000000000000000000000000000L,0};
        return x;
    }
    public static long[][] getYMap() {
        long[][] y = new long[256][4];
        y[120] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000011111111L,0b1111111100000000000000000000000000000000000000000000000000000000L,0};
        y[121] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1000000100000000000000000000000000000000000000000000000000000000L,0};
        y[122] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1011110100000000000000000000000000000000000000000000000000000000L,0};
        y[123] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1010010100000000000000000000000000000000000000000000000000000000L,0};
        y[124] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1010110100000000000000000000000000000000000000000000000000000000L,0};
        y[125] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1010010100000000000000000000000000000000000000000000000000000000L,0};
        y[126] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1011010100000000000000000000000000000000000000000000000000000000L,0};
        y[127] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b1000010100000000000000000000000000000000000000000000000000000000L,0};
        y[128] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000011L,0b1111110100000000000000000000000000000000000000000000000000000000L,0};
        y[129] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        y[130] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        y[131] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        y[132] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        y[133] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        y[134] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000010000000L,0b0000000100000000000000000000000000000000000000000000000000000000L,0};
        y[135] = new long[] { 0, 0b0000000000000000000000000000000000000000000000000000000011111111L,0b1111111100000000000000000000000000000000000000000000000000000000L,0};
        return y;
    }
}
