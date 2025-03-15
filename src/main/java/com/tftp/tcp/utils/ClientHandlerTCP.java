package com.tftp.tcp.utils;

import java.io.*;
import java.net.Socket;

public class ClientHandlerTCP implements Runnable {
    private static final String UPLOAD_DIRECTORY = "D:/Courses/New-Tasks/JAVA TFTP Task/TFTP-Implementation/TFTP-Implementation/tftp-tcp-upload/";
    private final Socket clientSocket;

    public ClientHandlerTCP(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            // Read request type (1 = RRQ, 2 = WRQ)
            int requestType = in.readInt();
            String filePathOrName = in.readUTF(); // Full path for download, filename for upload

            if (requestType == 1) {
                handleDownload(filePathOrName, out); // Handle download request
            } else if (requestType == 2) {
                handleUpload(filePathOrName, in, out); // Handle upload request
            } else {
                sendError(out, "Invalid request type");
            }

        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void handleDownload(String filePath, DataOutputStream out) throws IOException {
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            sendError(out, "File not found: " + filePath);
            return;
        }

        out.writeInt(0); // Success indicator

        // Send the file to the client
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File sent to client: " + file.getAbsolutePath());
    }

    private void handleUpload(String filename, DataInputStream in, DataOutputStream out) throws IOException {
        File file = new File(UPLOAD_DIRECTORY + filename);

        // Ensure parent directories exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        out.writeInt(0); // Success

        // Save the file received from the client
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File received from client: " + file.getAbsolutePath());
    }

    private void sendError(DataOutputStream out, String message) throws IOException {
        out.writeInt(-1); // Error indicator
        out.writeUTF(message);
    }
}