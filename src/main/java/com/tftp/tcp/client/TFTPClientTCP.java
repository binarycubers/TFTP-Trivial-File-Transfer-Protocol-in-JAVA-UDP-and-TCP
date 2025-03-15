package com.tftp.tcp.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TFTPClientTCP {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 9876;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("TFTP Client (TCP)");
        System.out.println("1. Download File (RRQ)");
        System.out.println("2. Upload File (WRQ)");
        System.out.print("Choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Filename: ");
        String filename = scanner.nextLine();

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            if (choice == 1) downloadFile(filename, out, in);
            else if (choice == 2) uploadFile(filename, out);
            else System.out.println("Invalid choice");

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private static void downloadFile(String filename, DataOutputStream out, DataInputStream in)
            throws IOException {
        out.writeInt(1); // RRQ
        out.writeUTF(filename);

        // Check for error
        if (in.readInt() == -1) {
            System.err.println("Error: " + in.readUTF());
            return;
        }

        try (FileOutputStream fos = new FileOutputStream("downloaded_" + filename)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            System.out.println("File downloaded successfully");
        }
    }

    private static void uploadFile(String filename, DataOutputStream out) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println("File not found");
            return;
        }

        out.writeInt(2); // WRQ
        out.writeUTF(filename);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("File uploaded successfully");
        }
    }
}