package com.example.user.anjay;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by user on 15-5-4.
 */

/* 发送udp多播 */
public  class udpBroadCast extends Thread {
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