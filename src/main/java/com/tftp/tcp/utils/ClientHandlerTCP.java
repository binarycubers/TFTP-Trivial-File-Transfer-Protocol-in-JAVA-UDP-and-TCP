package com.tftp.tcp.utils;

import java.io.*;
import java.net.Socket;

public class ClientHandlerTCP implements Runnable {
    private final Socket clientSocket;

    public ClientHandlerTCP(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            // Read request type (1 = RRQ, 2 = WRQ)
            int requestType = in.readInt();
            String filename = in.readUTF();

            if (requestType == 1) handleReadRequest(filename, out);
            else if (requestType == 2) handleWriteRequest(filename, in);
            else sendError(out, "Invalid request type");

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private void handleReadRequest(String filename, DataOutputStream out) {
        File file = new File(filename);
        if (!file.exists()) {
            sendError(out, "File not found");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            sendError(out, "Error reading file");
        }
    }

    private void handleWriteRequest(String filename, DataInputStream in) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    private void sendError(DataOutputStream out, String message) {
        try {
            out.writeInt(-1); // Error indicator
            out.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Failed to send error: " + e.getMessage());
        }
    }
}