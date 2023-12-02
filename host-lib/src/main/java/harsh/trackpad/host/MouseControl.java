package harsh.trackpad.host;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MouseControl {
    public static float mouse_x = 0, mouse_y = 0;
    private static boolean mouse_lt = false, mouse_rt = false, pmouse_lt = false, pmouse_rt = false;
    private static long lastMouseLt = 0, lastMouseRt = 0;

    private static Robot robot;

    public static void main(String[] args) {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        MouseEventReceiver eventReceiver = new MouseEventReceiver();
        Thread receiverThread = new Thread(eventReceiver);
        receiverThread.start();
    }

    public static void updateMouse(){
        if (robot == null) return;
        if(mouse_lt && (System.currentTimeMillis() - lastMouseLt) > 80){
            if(!pmouse_lt) {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                pmouse_lt = true;
            }
            lastMouseLt = System.currentTimeMillis();
        } else {
            if (pmouse_lt) {
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                pmouse_lt = false;
            }
        }

        if(mouse_rt && (System.currentTimeMillis() - lastMouseRt) > 80){
            if(!pmouse_rt) {
                robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                pmouse_rt = true;
            } else {
                robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
                pmouse_rt = false;
            }
            lastMouseRt = System.currentTimeMillis();
        }

        System.out.println(mouse_x + ", " + mouse_y);
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
            int temp_x = 0, temp_y = 0;
            for (int i = 4; i >= 0; i--) {
                temp_x <<= 8;
                temp_x |= data[i];
                temp_y <<= 8;
                temp_y |= data[i+4];
            }
            mouse_x = Float.intBitsToFloat(temp_x);
            mouse_y = Float.intBitsToFloat(temp_y);
            mouse_rt = ((data[9] & 1) == 1);
            mouse_lt = ((data[9] & 2) == 2);
            updateMouse();
        }
    }
}