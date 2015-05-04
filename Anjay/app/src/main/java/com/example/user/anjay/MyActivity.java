package com.example.user.anjay;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;


public class MyActivity extends Activity {

    Button startBroadCast;
    Button stopBroadCast;

    TextView send_label;
    TextView receive_label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        /* start 按钮 和 stop 按钮 的 初始化 */
        startBroadCast = (Button) findViewById(R.id.start);
        stopBroadCast = (Button) findViewById(R.id.stop);

        send_label = (TextView) findViewById(R.id.send_information);
        receive_label = (TextView) findViewById(R.id.receive_information);

        send_label.append("\n\n");
        receive_label.append("\n\n");

        startBroadCast.setOnClickListener(listener);
        stopBroadCast.setOnClickListener(listener);


        Handler handler_for_udpReceiveAndtcpSend = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                /* 0x111: 在TextView "send_label" 上, append 发送tcp连接的信息 */
                if (msg.what == 0x111) {
                   send_label.append((msg.obj).toString());
                }
                /* 0x222: 在TextView上 "receive_label" 加上收到tcp连接的信息, udp多播的信息 */
                else if (msg.what == 0x222 ) {
                    receive_label.append((msg.obj).toString());
                }
            }
        };

        /* 接收tcp 连接*/
        new tcpReceive(handler_for_udpReceiveAndtcpSend).start();

        /*接收udp多播 并 发送tcp 连接*/
        new udpReceiveAndtcpSend(handler_for_udpReceiveAndtcpSend).start();
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == startBroadCast ) {
                startBroadCast.setEnabled(false);
                stopBroadCast.setEnabled(true);

                /* 发送 udp 多播 */
                new udpBroadCast("hi ~!").start();
            }
            else {
                startBroadCast.setEnabled(true);
                stopBroadCast.setEnabled(false);
            }
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}