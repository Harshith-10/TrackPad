package harsh.trackpad.host;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MouseControl {
    public static float mouse_x = 0, mouse_y = 0;
    private static int screenWidth, screenHeight;
    private static boolean mouse_lt = false, mouse_rt = false, pmouse_lt = false, pmouse_rt = false;
    private static int lMouse_x, lMouse_y;

    private static Robot robot;

    public static void main(String[] args) {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = (int) size.getWidth(); lMouse_x = screenWidth / 2;
        screenHeight = (int) size.getHeight(); lMouse_y = screenHeight / 2;
        System.out.println(screenWidth + ", " + screenHeight);
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
        int x = (int) (screenWidth * mouse_x);
        int y = (int) (screenHeight * mouse_y);
        robot.mouseMove(x, y);

        if(mouse_lt){
            if(!pmouse_lt){
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                pmouse_lt = true;
            }
        } else {
            if(pmouse_lt){
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                pmouse_lt = false;
            }
        }

        if(mouse_rt){
            if(!pmouse_rt){
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                pmouse_rt = true;
            }
        } else {
            if(pmouse_rt){
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                pmouse_rt = false;
            }
        }
    }

    private static class MouseEventReceiver implements Runnable {
        private static final int PORT = 25744;
        public boolean keepRunning = true;

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                byte[] data = new byte[128];
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
            String dataStr = new String(packet.getData(), packet.getOffset(), packet.getLength());
            if(dataStr.isEmpty()) return;
            String[] data = dataStr.split(" ");
            mouse_x = Float.parseFloat(data[0]);
            mouse_y = Float.parseFloat(data[1]);
            mouse_lt = Boolean.parseBoolean(data[2]);
            mouse_rt = Boolean.parseBoolean(data[3]);
            updateMouse();
        }
    }
}