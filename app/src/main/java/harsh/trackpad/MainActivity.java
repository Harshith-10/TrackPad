package harsh.trackpad;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mouseArea;
    private Button btn_mouse_left, btn_mouse_right;
    private static int mouse_x = 0, mouse_y = 0;
    private static boolean mouse_lt = false, mouse_rt = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mouseArea = findViewById(R.id.area_mouse);
        btn_mouse_left = findViewById(R.id.btn_mouse_left);
        btn_mouse_right = findViewById(R.id.btn_mouse_right);

        mouseArea.setOnTouchListener((v, event) -> {
            int x = (int) event.getX();
            int y = (int) event.getY();

            return false;
        });
    }

    private static class MouseTransmitter implements Runnable {
        private static final int PORT = 8889;
        public boolean keepRunning = true;

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                InetAddress hostAddress = InetAddress.getByName("add pc ip here");
                while (keepRunning) {
                    byte[] data = new byte[10];
                    packMouseData(data);
                    DatagramPacket packet = new DatagramPacket(data, data.length, hostAddress, PORT);
                    socket.send(packet);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void packMouseData(byte[] data) {
            for (int i = 0; i < 4; i++) {
                data[i]   = (byte) (mouse_x >>> (i * 8));
                data[i+4] = (byte) (mouse_y >>> (i * 8));
            }
            data[9] = (byte) ((mouse_lt?2:0)|(mouse_rt?1:0));
        }
    }
}