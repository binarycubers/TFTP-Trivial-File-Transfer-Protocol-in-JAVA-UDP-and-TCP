package com.tftp.udp.client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class TFTPClientUDP {
    private static final int TFTP_PORT = 1069;
    private static final int BUFFER_SIZE = 516;
    private static final short OP_RRQ = 1;
    private static final short OP_WRQ = 2;
    private static final short OP_DATA = 3;
    private static final short OP_ACK = 4;
    private static final short OP_ERROR = 5;
    private static final int MAX_RETRIES = 5;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("TFTP Client");
        System.out.println("1. Download File");
        System.out.println("2. Upload File");
        System.out.print("Choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Server Address: ");
        InetAddress serverAddress = InetAddress.getByName(scanner.nextLine());

        System.out.print("Filename: ");
        String filename = scanner.nextLine();

        if (choice == 1) {
            downloadFile(serverAddress, filename);
        } else if (choice == 2) {
            uploadFile(serverAddress, filename);
        } else {
            System.out.println("Invalid choice!");
        }
    }

    private static final String DOWNLOAD_DIRECTORY = "D:/Courses/New-Tasks/JAVA TFTP Task/TFTP-Implementation/TFTP-Implementation/tftp-download/";

    private static void downloadFile(InetAddress serverAddress, String filename) throws IOException {
        String outputFilename = DOWNLOAD_DIRECTORY + filename;
        File outputFile = new File(outputFilename);

        // Ensure parent directories exist
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            sendRequest(socket, serverAddress, OP_RRQ, filename);

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                int expectedBlock = 1;
                int retries = 0;
                final int MAX_RETRIES = 5;

                while (true) {
                    try {
                        DatagramPacket dataPacket = receivePacket(socket);
                        ByteBuffer buffer = ByteBuffer.wrap(dataPacket.getData());
                        short opcode = buffer.getShort();

                        if (opcode == OP_ERROR) {
                            handleError(buffer);
                            return;
                        }

                        if (opcode != OP_DATA) {
                            throw new IOException("Unexpected opcode: " + opcode);
                        }

                        short blockNumber = buffer.getShort();
                        if (blockNumber != (short) expectedBlock) {
                            throw new IOException("Unexpected block number: " + blockNumber);
                        }

                        // Write data to file
                        int dataLength = dataPacket.getLength() - 4;
                        fos.write(dataPacket.getData(), 4, dataLength);

                        // Send ACK to the server's port
                        InetAddress serverAddr = dataPacket.getAddress();
                        int serverPort = dataPacket.getPort();
                        sendAck(socket, serverAddr, serverPort, (short) expectedBlock);

                        if (dataLength < 512) break;
                        expectedBlock++;
                        retries = 0;
                    } catch (SocketTimeoutException e) {
                        if (++retries > MAX_RETRIES) {
                            throw new IOException("Max retries exceeded");
                        }
                        System.out.println("Timeout, retrying... (" + retries + "/" + MAX_RETRIES + ")");
                    }
                }
                System.out.println("File downloaded successfully: " + outputFile.getAbsolutePath());
            }
        }
    }

    private static void uploadFile(InetAddress serverAddress, String filename) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            File file = new File(filename);
            if (!file.exists() || !file.isFile()) {
                System.out.println("Error: File '" + filename + "' not found");
                return;
            }

            sendRequest(socket, serverAddress, OP_WRQ, filename);
            short blockNumber = 0;
            int retries = 0;
            int serverPort = -1; // New variable to track the server's port

            // Wait for initial ACK (block 0)
            while (true) {
                try {
                    DatagramPacket ackPacket = receivePacket(socket);
                    serverPort = ackPacket.getPort(); // Capture the server's port
                    if (validateAck(ackPacket, blockNumber)) break;
                } catch (SocketTimeoutException e) {
                    if (++retries > MAX_RETRIES) throw new IOException("Max retries exceeded");
                    sendRequest(socket, serverAddress, OP_WRQ, filename);
                }
            }

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[512];
                blockNumber = 1;

                while (true) {
                    int bytesRead = fis.read(buffer);
                    if (bytesRead == -1) bytesRead = 0;

                    // Send DATA to the server's port (not TFTP_PORT)
                    sendDataPacket(socket, serverAddress, serverPort, blockNumber, buffer, bytesRead);
                    retries = 0;

                    while (true) {
                        try {
                            DatagramPacket ackPacket = receivePacket(socket);
                            if (validateAck(ackPacket, blockNumber)) break;
                        } catch (SocketTimeoutException e) {
                            if (++retries > MAX_RETRIES) throw new IOException("Max retries exceeded");
                            // Resend to the same server port
                            sendDataPacket(socket, serverAddress, serverPort, blockNumber, buffer, bytesRead);
                        }
                    }

                    if (bytesRead < 512) break;
                    blockNumber++;
                }
            }
            System.out.println("File uploaded successfully!");
        }
    }

    private static void sendRequest(DatagramSocket socket, InetAddress server, short opcode, String filename) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(opcode);
        dos.write(filename.getBytes());
        dos.writeByte(0);
        dos.write("octet".getBytes());
        dos.writeByte(0);
        byte[] requestData = baos.toByteArray();

        DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, server, TFTP_PORT);
        socket.send(requestPacket);
    }

    private static void sendDataPacket(DatagramSocket socket, InetAddress server, int port,
                                       short blockNumber, byte[] data, int length) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(OP_DATA);
        dos.writeShort(blockNumber);
        dos.write(data, 0, length);
        byte[] packetData = baos.toByteArray();

        DatagramPacket dataPacket = new DatagramPacket(packetData, packetData.length, server, port);
        socket.send(dataPacket);
    }

    private static DatagramPacket receivePacket(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet;
    }

    private static boolean validateAck(DatagramPacket packet, short expectedBlock) {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
        return buffer.getShort() == OP_ACK && buffer.getShort() == expectedBlock;
    }

    private static void sendAck(DatagramSocket socket, InetAddress clientAddress, int port, short blockNumber) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(OP_ACK);
        dos.writeShort(blockNumber);
        byte[] ackData = baos.toByteArray();

        DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, clientAddress, port);
        socket.send(ackPacket);
    }

    private static void handleError(ByteBuffer buffer) {
        buffer.position(4); // Skip opcode and error code
        StringBuilder errorMsg = new StringBuilder();
        while (buffer.hasRemaining()) {
            char c = (char) buffer.get();
            if (c == 0) break;
            errorMsg.append(c);
        }
        System.out.println("Error received: " + errorMsg);
    }
}