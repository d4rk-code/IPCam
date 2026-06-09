package com.ma.ipcam;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpServer extends Thread {

    private volatile byte[] latestFrame;

    public void setFrame(byte[] frame) {
        this.latestFrame = frame;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(8000);
            System.out.println("HTTP server running on port 8000");

            while (true) {

                Socket socket = serverSocket.accept();

                new Thread(() -> handleClient(socket)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (Exception ignored) {}
        }
    }

    private void handleClient(Socket socket) {

        try {
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);

            pw.println("HTTP/1.0 200 OK");
            pw.println("Connection: close");
            pw.println("Max-Age: 0");
            pw.println("Expires: 0");
            pw.println("Cache-Control: no-cache, private");
            pw.println("Pragma: no-cache");
            pw.println("Content-Type: multipart/x-mixed-replace; boundary=frame");
            pw.println();
            pw.flush();

            while (!socket.isClosed()) {

                byte[] frame = latestFrame;

                if (frame == null) {
                    Thread.sleep(10);
                    continue;
                }

                try {
                    pw.println("--frame");
                    pw.println("Content-Type: image/jpeg");
                    pw.println("Content-Length: " + frame.length);
                    pw.println();
                    pw.flush();

                    os.write(frame);
                    os.write("\r\n".getBytes());
                    os.flush();

                    Thread.sleep(100);

                } catch (IOException e) {
                    break; // client disconnected
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }
}
