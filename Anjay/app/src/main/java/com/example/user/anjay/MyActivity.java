package com.example.user.anjay;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.user.anjay.R;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;


public class MyActivity extends Activity {

    private static String LOG_TAG = "WifiMulticastActivity";

    Button startBroadCast;
    Button stopBroadCast;

    TextView send_label;
    TextView receive_label;

    /* 用于 udpReceiveAndTcpSend 的3个变量 */
    Socket socket = null;
    MulticastSocket ms = null;
    DatagramPacket dp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        startBroadCast = (Button) findViewById(R.id.start);
        stopBroadCast = (Button) findViewById(R.id.stop);

        send_label = (TextView) findViewById(R.id.send_information);
        receive_label = (TextView) findViewById(R.id.receive_information);

        send_label.append("\n\n");
        receive_label.append("\n\n");

        startBroadCast.setOnClickListener(listener);
        stopBroadCast.setOnClickListener(listener);

        /* 接收tcp 连接*/
        new tcpReceive().start();

        /*接收udp多播 并 发送tcp 连接*/
        new udpReceiveAndtcpSend().start();
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

    /* 发送udp多播 */
    private  class udpBroadCast extends Thread {
        MulticastSocket sender = null;
        DatagramPacket dj = null;
        InetAddress group = null;

        byte[] data = new byte[1024];

        public udpBroadCast(String dataString) {
            data = dataString.getBytes();
        }

        @Override
        public void run() {
            try {
                sender = new MulticastSocket();
                group = InetAddress.getByName("224.0.0.1");
                dj = new DatagramPacket(data,data.length,group,6789);
                sender.send(dj);
                sender.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*接收udp多播 并 发送tcp 连接*/
    private class udpReceiveAndtcpSend extends  Thread {
        @Override
        public void run() {
            byte[] data = new byte[1024];
            try {
                InetAddress groupAddress = InetAddress.getByName("224.0.0.1");
                ms = new MulticastSocket(6789);
                ms.joinGroup(groupAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    dp = new DatagramPacket(data, data.length);
                    if (ms != null)
                       ms.receive(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (dp.getAddress() != null) {
                    final String quest_ip = dp.getAddress().toString();

                    /* 若udp包的ip地址 是 本机的ip地址的话，丢掉这个包(不处理)*/

                    //String host_ip = getLocalIPAddress();

                    String host_ip = getLocalHostIp();

                    System.out.println("host_ip:  --------------------  " + host_ip);
                    System.out.println("quest_ip: --------------------  " + quest_ip.substring(1));

                    if( (!host_ip.equals(""))  && host_ip.equals(quest_ip.substring(1)) ) {
                        continue;
                    }

                    final String codeString = new String(data, 0, dp.getLength());

                    receive_label.post(new Runnable() {
                        @Override
                        public void run() {
                            receive_label.append("收到来自: \n" + quest_ip.substring(1) + "\n" +"的udp请求\n");
                            receive_label.append("请求内容: " + codeString + "\n\n");
                        }
                    });
                    try {
                        final String target_ip = dp.getAddress().toString().substring(1);
                        send_label.post(new Runnable() {
                            @Override
                            public void run() {
                                send_label.append("发送tcp请求到: \n" + target_ip + "\n");
                            }
                        });
                        socket = new Socket(target_ip, 8080);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {

                        try {
                            if (socket != null)
                                socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }



    /* 接收tcp连接 */
    private class tcpReceive extends  Thread {
        ServerSocket serverSocket;
        Socket socket;
        BufferedReader in;
        String source_address;

        @Override
        public void run() {
            while(true) {
                serverSocket = null;
                socket = null;
                in = null;
                try {
                    Log.i("Tcp Receive"," new ServerSocket ++++++++++");
                    serverSocket = new ServerSocket(8080);

                    socket = serverSocket.accept();
                    Log.i("Tcp Receive"," get socket ++++++++++++++++");

                    if(socket != null) {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        sb.append(socket.getInetAddress().getHostAddress());

                        String line = null;
                        while ((line = in.readLine()) != null ) {
                            sb.append(line);
                        }

                        source_address = sb.toString().trim();
                        receive_label.post(new Runnable() {
                            @Override
                            public void run() {
                                receive_label.append("收到来自: "+"\n" +source_address+"\n"+"的tcp请求\n\n");
                            }
                        });
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        if (in != null)
                            in.close();
                        if (socket != null)
                            socket.close();
                        if (serverSocket != null)
                            serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip
                            .getHostAddress())) {
                        return ip.getHostAddress();
                    }
                }
            }
        }
        catch(SocketException e)
        {
                Log.e("feige", "获取本地ip地址失败");
                e.printStackTrace();
        }
        return ipaddress;
    }

    private String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(LOG_TAG, ex.toString());
        }
        return null;
    }

    // 按下返回键时，关闭 多播socket ms
    @Override
    public void onBackPressed() {
        ms.close();
        super.onBackPressed();
    }

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