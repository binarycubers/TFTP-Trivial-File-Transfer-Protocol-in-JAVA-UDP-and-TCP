package com.tftp.tcp.server;

import com.tftp.tcp.utils.ClientHandlerTCP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TFTPServerTCP {
    private static final int PORT = 9876;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TFTP Server (TCP) running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandlerTCP(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}