package harsh.trackpad;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    private static float mouse_x = 0, mouse_y = 0;
    private static boolean mouse_lt = false, mouse_rt = false;

    public static Thread mouseSenderThread;
    public static MouseTransmitter mouseTransmitterRunnable;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LinearLayout mouseArea = findViewById(R.id.area_mouse);
        Button btn_mouse_lt = findViewById(R.id.btn_mouse_left);
        Button btn_mouse_rt = findViewById(R.id.btn_mouse_right);

        mouseArea.setOnTouchListener((v, e) -> {
            //v.getParent().requestDisallowInterceptTouchEvent(true);
            float x = e.getX(), y = e.getY();
            float max_x = v.getWidth(), max_y = v.getHeight();
            mouse_x = x / max_x;
            mouse_y = y / max_y;
            if (mouse_x < 0) mouse_x = 0;
            if (mouse_x > 1) mouse_x = 1;
            if (mouse_y < 0) mouse_y = 0;
            if (mouse_y > 1) mouse_y = 1;
            if(e.getAction() == MotionEvent.ACTION_MOVE) System.out.println(mouse_x + " " + mouse_y);
            // Todo: add touch events
            return true;
        });

        btn_mouse_lt.setOnTouchListener((v, e) -> {
            mouse_lt = switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN -> true;
                case MotionEvent.ACTION_UP -> false;
                case MotionEvent.ACTION_MOVE -> mouse_lt;
                default -> throw new IllegalStateException("Unexpected value: " + e.getAction());
            };
            Log.d(getLocalClassName(), "MLT: " + mouse_lt);
            return true;
        });

        btn_mouse_rt.setOnTouchListener((v, e) -> {
            mouse_rt = switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN -> true;
                case MotionEvent.ACTION_UP -> false;
                case MotionEvent.ACTION_MOVE -> mouse_rt;
                default -> throw new IllegalStateException("Unexpected value: " + e.getAction());
            };
            Log.d(getLocalClassName(), "MRT: " + mouse_rt);
            return true;
        });

        mouseTransmitterRunnable = new MouseTransmitter();
        /*mouseSenderThread = new Thread(mouseSenderThread);
        mouseSenderThread.start();*/

        new Thread(mouseTransmitterRunnable).start();
        Log.d(getLocalClassName(), "Thread start() called");
    }

    private static class MouseTransmitter implements Runnable {
        private static final int PORT = 8889;
        public boolean keepRunning = true;

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                InetAddress hostAddress = InetAddress.getByName("192.168.15.254");
                Log.d(getClass().getName(), "Runnable Started");
                long startMillis = System.currentTimeMillis(), n = 0;
                while (keepRunning) {
                    byte[] data = new byte[10];
                    packMouseData(data);
                    DatagramPacket packet = new DatagramPacket(data, data.length, hostAddress, PORT);
                    socket.send(packet);
                    //System.out.println(n++);
                    while (System.currentTimeMillis() - startMillis < 34); // about 2 times a second
                    startMillis = System.currentTimeMillis();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void packMouseData(byte[] data) {
            int temp_x, temp_y;
            temp_x = Float.floatToIntBits(mouse_x);
            temp_y = Float.floatToIntBits(mouse_y);
            for (int i = 0; i < 4; i++) {
                data[i]   = (byte) (temp_x >>> (i * 8));
                data[i+4] = (byte) (temp_y >>> (i * 8));
            }
            data[9] = (byte) ((mouse_lt?2:0)|(mouse_rt?1:0));
        }
    }
}