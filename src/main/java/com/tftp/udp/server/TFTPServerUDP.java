package com.tftp.udp.server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TFTPServerUDP {
    private static final int TFTP_PORT = 1069;
    private static final int BUFFER_SIZE = 516;
    private static final short OP_RRQ = 1;
    private static final short OP_WRQ = 2;
    private static final short OP_DATA = 3;
    private static final short OP_ACK = 4;
    private static final short OP_ERROR = 5;
    private static final short MAX_BLOCK_SIZE = 512;

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(TFTP_PORT);
        System.out.println("TFTP Server listening on port " + TFTP_PORT);

        while (true) {
            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            threadPool.execute(new RequestHandler(receivePacket));
        }
    }

    private static class RequestHandler implements Runnable {
        private final DatagramPacket requestPacket;

        RequestHandler(DatagramPacket packet) {
            this.requestPacket = packet;
        }

        @Override
        public void run() {
            try {
                ByteBuffer buffer = ByteBuffer.wrap(requestPacket.getData());
                short opcode = buffer.getShort();
                String filename = readNullTerminatedString(buffer);
                String mode = readNullTerminatedString(buffer).toLowerCase();

                if (!mode.equals("octet")) {
                    sendError(requestPacket.getAddress(), requestPacket.getPort(), "Unsupported mode");
                    return;
                }

                InetAddress clientAddress = requestPacket.getAddress();
                int clientPort = requestPacket.getPort();

                try (DatagramSocket transferSocket = new DatagramSocket()) {
                    transferSocket.setSoTimeout(3000);

                    if (opcode == OP_RRQ) {
                        handleReadRequest(transferSocket, clientAddress, clientPort, filename);
                    } else if (opcode == OP_WRQ) {
                        handleWriteRequest(transferSocket, clientAddress, clientPort, filename);
                    } else {
                        sendError(clientAddress, clientPort, "Invalid operation");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling request: " + e.getMessage());
            }
        }

        private void handleReadRequest(DatagramSocket socket, InetAddress clientAddress, int clientPort, String filename) throws IOException {
            File file = new File(filename);
            if (!file.exists() || !file.isFile()) {
                sendError(clientAddress, clientPort, "File not found");
                return;
            }

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] fileBuffer = new byte[MAX_BLOCK_SIZE];
                short blockNumber = 1;
                final int MAX_RETRIES = 5;

                while (true) {
                    int bytesRead = fis.read(fileBuffer);
                    if (bytesRead == -1) bytesRead = 0;

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(baos);
                    dos.writeShort(OP_DATA);
                    dos.writeShort(blockNumber);
                    dos.write(fileBuffer, 0, bytesRead);
                    byte[] packetData = baos.toByteArray();

                    DatagramPacket dataPacket = new DatagramPacket(packetData, packetData.length, clientAddress, clientPort);

                    int retries = 0;
                    boolean ackReceived = false;

                    while (!ackReceived && retries < MAX_RETRIES) {
                        try {
                            socket.send(dataPacket);
                            ackReceived = waitForAck(socket, blockNumber);
                        } catch (SocketTimeoutException e) {
                            retries++;
                            System.out.println("[SERVER] Retrying DATA packet for block " + blockNumber + " (" + retries + "/" + MAX_RETRIES + ")");
                        }
                    }

                    if (!ackReceived) {
                        throw new IOException("Max retries exceeded for block " + blockNumber);
                    }

                    if (bytesRead < MAX_BLOCK_SIZE) break;
                    blockNumber++;
                }
            }
        }

        private void handleWriteRequest(DatagramSocket socket, InetAddress clientAddress, int clientPort, String filename) throws IOException {
            System.out.println("[SERVER] Handling WRQ for file: " + filename);

            // Extract the filename from the path (if any)
            String simpleFilename = new File(filename).getName();

            // Define the upload directory
            File uploadDir = new File("D:/Courses/New-Tasks/JAVA TFTP Task/TFTP-Implementation/TFTP-Implementation/tftp-uploads/");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs(); // Create the upload directory if it doesn't exist
            }

            // Create the file in the upload directory
            File file = new File(uploadDir, simpleFilename);
            if (file.exists()) {
                System.out.println("[SERVER] File already exists: " + file.getAbsolutePath());
                sendError(clientAddress, clientPort, "File already exists");
                return;
            }

            System.out.println("[SERVER] Sending ACK for block 0");
            sendAck(socket, clientAddress, clientPort, (short) 0);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                short expectedBlock = 1;

                while (true) {
                    byte[] receiveData = new byte[BUFFER_SIZE];
                    DatagramPacket dataPacket = new DatagramPacket(receiveData, receiveData.length);
                    System.out.println("[SERVER] Waiting for DATA packet for block " + expectedBlock);
                    socket.receive(dataPacket);

                    System.out.println("[SERVER] Received DATA packet from " + dataPacket.getAddress() + ":" + dataPacket.getPort());

                    ByteBuffer buffer = ByteBuffer.wrap(dataPacket.getData());
                    short opcode = buffer.getShort();

                    if (opcode == OP_ERROR) {
                        String errorMsg = readNullTerminatedString(buffer);
                        throw new IOException("Server error: " + errorMsg);
                    }

                    if (opcode != OP_DATA) {
                        throw new IOException("Unexpected opcode: " + opcode);
                    }

                    short blockNumber = buffer.getShort();
                    if (blockNumber != expectedBlock) {
                        throw new IOException("Unexpected block number: " + blockNumber);
                    }

                    int dataLength = dataPacket.getLength() - 4;
                    fos.write(dataPacket.getData(), 4, dataLength);

                    System.out.println("[SERVER] Sending ACK for block " + blockNumber);
                    sendAck(socket, clientAddress, clientPort, blockNumber);

                    if (dataLength < MAX_BLOCK_SIZE) break;
                    expectedBlock++;
                }
            }
            System.out.println("[SERVER] File upload completed: " + file.getAbsolutePath());
        }


        private boolean waitForAck(DatagramSocket socket, short blockNumber) throws IOException {
            try {
                byte[] ackBuffer = new byte[4];
                DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
                socket.receive(ackPacket);

                ByteBuffer buffer = ByteBuffer.wrap(ackPacket.getData());
                return buffer.getShort() == OP_ACK && buffer.getShort() == blockNumber;
            } catch (SocketTimeoutException e) {
                return false;
            }
        }

        private void sendAck(DatagramSocket socket, InetAddress clientAddress, int clientPort, short blockNumber) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeShort(OP_ACK);
            dos.writeShort(blockNumber);
            byte[] ackData = baos.toByteArray();

            DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, clientAddress, clientPort);
            socket.send(ackPacket);
        }

        private void sendError(InetAddress clientAddress, int clientPort, String message) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeShort(OP_ERROR);
            dos.writeShort(0); // Error code
            dos.write(message.getBytes());
            dos.writeByte(0);
            byte[] errorData = baos.toByteArray();

            try (DatagramSocket socket = new DatagramSocket()) {
                DatagramPacket errorPacket = new DatagramPacket(errorData, errorData.length, clientAddress, clientPort);
                socket.send(errorPacket);
            }
        }

        private String readNullTerminatedString(ByteBuffer buffer) {
            StringBuilder sb = new StringBuilder();
            while (buffer.hasRemaining()) {
                char c = (char) buffer.get();
                if (c == 0) break;
                sb.append(c);
            }
            return sb.toString();
        }
    }
}