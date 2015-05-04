package com.example.user.anjay;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by user on 15-5-4.
 */

/* 接收tcp连接 */
public class tcpReceive extends  Thread {
    ServerSocket serverSocket;
    Socket socket;
    BufferedReader in;
    String source_address;
    Handler handler = new Handler();

    public tcpReceive(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        while(true) {
            serverSocket = null;
            socket = null;
            in = null;
            try {
                Log.i("Tcp Receive", " new ServerSocket ++++++++++");
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

                    Message msg = new Message();
                    msg.what = 0x222;
                    String information = "收到来自: "+"\n" +source_address+"\n"+"的tcp请求\n\n";
                    msg.obj = information;
                    handler.sendMessage(msg);
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
