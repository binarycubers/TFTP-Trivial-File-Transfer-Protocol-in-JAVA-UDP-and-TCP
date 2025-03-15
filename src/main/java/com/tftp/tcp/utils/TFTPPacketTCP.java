package com.tftp.tcp.utils;

public class TFTPPacketTCP {
    public static final int OP_RRQ = 1; // Read Request
    public static final int OP_WRQ = 2; // Write Request
    public static final int OP_DATA = 3; // Data Packet
    public static final int OP_ACK = 4; // Acknowledgment
    public static final int OP_ERROR = 5; // Error Packet

    private int opcode;
    private String filename;
    private int blockNumber;
    private byte[] data;
    private int errorCode;
    private String errorMessage;

    // Constructor for RRQ/WRQ packets
    public TFTPPacketTCP(int opcode, String filename) {
        this.opcode = opcode;
        this.filename = filename;
    }

    // Constructor for DATA packets
    public TFTPPacketTCP(int opcode, int blockNumber, byte[] data) {
        this.opcode = opcode;
        this.blockNumber = blockNumber;
        this.data = data;
    }

    // Constructor for ACK packets
    public TFTPPacketTCP(int opcode, int blockNumber) {
        this.opcode = opcode;
        this.blockNumber = blockNumber;
    }

    // Constructor for ERROR packets
    public TFTPPacketTCP(int opcode, int errorCode, String errorMessage) {
        this.opcode = opcode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    // Getters
    public int getOpcode() {
        return opcode;
    }

    public String getFilename() {
        return filename;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public byte[] getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}