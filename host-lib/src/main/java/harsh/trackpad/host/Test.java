package harsh.trackpad.host;

public class Test {
    public static void main(String[] args) {
        int a = 0b00000100000000110000001000000001;

        byte b4 = (byte) a;
        byte b3 = (byte) (a >>> 8);
        byte b2 = (byte) (a >>> 16);
        byte b1 = (byte) (a >>> 24);

        System.out.printf("%d %d %d %d", b1, b2, b3, b4);
    }
}
