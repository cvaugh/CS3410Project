package cs3410.project.filesystem;

import java.text.DecimalFormat;

public class Utils {
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#.00");

    /**
     * @return True if <tt>a</tt> contains the subarray <tt>b</tt> starting at
     *         <tt>startIndex</tt>, otherwise false.
     */
    public static boolean byteArrayContains(byte[] a, byte[] b, int startIndex) {
        try {
            for(int i = 0; i < b.length; i++) {
                if(b[i] != a[i + startIndex]) return false;
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    /**
     * @param array An array of up to 4 bytes representing an <tt>int</tt>
     * @return The integer represented by the byte array
     * @throws RuntimeException If the number of bytes in <tt>array</tt> is larger
     *         than the number of bytes in an integer
     */
    public static int bytesToInt(byte[] array) {
        if(array.length > Integer.BYTES) throw new RuntimeException(
                String.format("Byte array too large for integer (%d > %d)", array.length, Integer.BYTES));
        int i = 0;
        for(byte b : array) {
            i = (i << 8) + (b & 0xFF);
        }
        return i;
    }

    public static byte[] intToBytes(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n >>> 24);
        b[1] = (byte) (n >>> 16);
        b[2] = (byte) (n >>> 8);
        b[3] = (byte) n;
        return b;
    }

    public static String humanReadableSize(int bytes) {
        double b = (double) bytes;
        int magnitude = 0;
        while(b > 1000) {
            b /= 1000;
            magnitude++;
        }
        if(magnitude == 0) {
            return b + " B";
        } else {
            return String.format("%s %sB", SIZE_FORMAT.format(b), "kMGTPEZY".charAt(magnitude - 1));
        }
    }
}
