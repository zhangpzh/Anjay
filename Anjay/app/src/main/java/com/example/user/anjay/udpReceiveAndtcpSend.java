package com.example.user.anjay;

import android.os.Message;
import android.util.Log;
import org.apache.http.conn.util.InetAddressUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import android.os.Handler;

/**
 * Created by user on 15-5-4.
 */

/*接收udp多播 并 发送tcp 连接*/
public class udpReceiveAndtcpSend extends  Thread {

    Socket socket = null;
    MulticastSocket ms = null;
    DatagramPacket dp;
    Handler handler = new Handler();

    public udpReceiveAndtcpSend(Handler handler) {
        this.handler = handler;
    }
    @Override
    public void run() {
        Message msg;
        String information;

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

                String host_ip = getLocalHostIp();

                System.out.println("host_ip:  --------------------  " + host_ip);
                System.out.println("quest_ip: --------------------  " + quest_ip.substring(1));

                /* 若udp包的ip地址 是 本机的ip地址的话，丢掉这个包(不处理)*/

                if( (!host_ip.equals(""))  && host_ip.equals(quest_ip.substring(1)) ) {
                    continue;
                }

                final String codeString = new String(data, 0, dp.getLength());

                msg = new Message();
                msg.what = 0x222;
                information = "收到来自: \n" + quest_ip.substring(1) + "\n" +"的udp请求\n"
                        + "请求内容: " + codeString + "\n\n";
                msg.obj = information;
                handler.sendMessage(msg);

                try {
                    final String target_ip = dp.getAddress().toString().substring(1);

                    msg = new Message();
                    msg.what = 0x111;
                    information = "发送tcp请求到: \n" + target_ip + "\n";
                    msg.obj = information;
                    handler.sendMessage(msg);

                    socket = new Socket(target_ip, 8080);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    try {
                        if (socket != null)
                            socket.close();
                        if (ms != null)
                            ms.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getLocalHostIp() {
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
}
