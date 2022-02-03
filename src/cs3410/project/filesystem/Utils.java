package cs3410.project.filesystem;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Utils {
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

    public static List<byte[]> splitByteArray(byte[] array, byte[] delimiter) {
        List<byte[]> list = new LinkedList<>();
        if(delimiter.length == 0) return list;
        int start = 0;
        outer:
        for(int i = 0; i < array.length - delimiter.length + 1; i++) {
            for(int j = 0; j < delimiter.length; j++) {
                if(array[i + j] != delimiter[j]) {
                    continue outer;
                }
            }
            list.add(Arrays.copyOfRange(array, start, i));
            start = i + delimiter.length;
        }
        list.add(Arrays.copyOfRange(array, start, array.length));

        return list;
    }

    public static int bytesToInt(byte[] array) {
        int i = 0;
        for(byte b : array) {
            i = (i << 8) + (b & 0xFF);
        }
        return i;
    }
}
