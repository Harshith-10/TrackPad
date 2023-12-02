package harsh.trackpad.host;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MouseControl {
    public static int mouse_x = 0, mouse_y = 0;
    public static boolean mouse_lt = false, mouse_rt = false;

    public static void main(String[] args) {
        MouseEventReceiver eventReceiver = new MouseEventReceiver();
        Thread receiverThread = new Thread(eventReceiver);
        receiverThread.start();
    }

    public static void updateMouse(){
        System.out.println(mouse_x + ", " + mouse_y + " cl: " + mouse_cl);
        // do something
    }

    private static class MouseEventReceiver implements Runnable {
        private static final int PORT = 8889;
        public boolean keepRunning = true;

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                byte[] data = new byte[10];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                while (keepRunning) {
                    socket.receive(packet);
                    parseMouseData(packet);
                    packet.setLength(data.length);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void parseMouseData(DatagramPacket packet) {
            byte[] data = packet.getData();
            mouse_x = mouse_y = 0;
            for (int i = 0; i < 4; i++) {
                mouse_x <<= 8;
                mouse_x |= data[i];
                mouse_y <<= 8;
                mouse_y |= data[i+4];
            }
            mouse_rt = ((data[9] & 1) == 1);
            mouse_lt = ((data[9] & 2) == 2);
            updateMouse();
        }
    }
}