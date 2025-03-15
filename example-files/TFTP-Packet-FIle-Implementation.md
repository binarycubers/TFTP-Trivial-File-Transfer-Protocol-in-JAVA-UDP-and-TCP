# How to Use TFTPPacket.java

This guide explains how to use the `TFTPPacket.java` class to create and handle TFTP packets, including RRQ/WRQ, DATA, ACK, and ERROR packets. It also demonstrates how to integrate this class into a TCP implementation.

---

## RRQ/WRQ Packets

To create a Read Request (RRQ) or Write Request (WRQ) packet, use the constructor:

```java
java tftp.tcp.TFTPClientTCP
```
### Example:

```java
TFTPPacket rrqPacket = new TFTPPacket(TFTPPacket.OP_RRQ, "testfile.txt");
```

## Data Packets

To create a DATA packet, use the constructor:

```java
TFTPPacket(int opcode, int blockNumber, byte[] data)**
```
### Example

```java
byte[] fileData = Files.readAllBytes(Paths.get("testfile.txt"));
TFTPPacket dataPacket = new TFTPPacket(TFTPPacket.OP_DATA, 1, fileData);
```
## ACK Packets

To create an Acknowledgment (ACK) packet, use the constructor:

```java
TFTPPacket(int opcode, int blockNumber);
```
### Example:

```java
TFTPPacket ackPacket = new TFTPPacket(TFTPPacket.OP_ACK, 1);
```

## ERROR Packets

To create an ERROR packet, use the constructor:

```java
TFTPPacket(int opcode, int errorCode, String errorMessage);
```
### Example:

```java
TFTPPacket errorPacket = new TFTPPacket(TFTPPacket.OP_ERROR, TFTPPacket.ERR_FILE_NOT_FOUND, "File not found");
```
## Integration with TCP Implementation

The TFTPPacket class can be used to encapsulate packet data in a TCP-based implementation. Below is an example of how to handle a Read Request (RRQ) on the server side.

### Server Handling RRQ:

```java
private void handleReadRequest(String filename, DataOutputStream out) {
    File file = new File(filename);
    if (!file.exists()) {
        TFTPPacket errorPacket = TFTPPacket.createErrorPacket(
                TFTPPacket.ERR_FILE_NOT_FOUND, "File not found");
        sendPacket(out, errorPacket);
        return;
    }

    try (FileInputStream fis = new FileInputStream(file)) {
        byte[] buffer = new byte[512];
        int blockNumber = 1;
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            TFTPPacket dataPacket = TFTPPacket.createDataPacket(blockNumber, buffer);
            sendPacket(out, dataPacket);
            blockNumber++;
        }
    } catch (IOException e) {
        TFTPPacket errorPacket = TFTPPacket.createErrorPacket(
                TFTPPacket.ERR_ILLEGAL_OPERATION, "Error reading file");
        sendPacket(out, errorPacket);
    }
}

private void sendPacket(DataOutputStream out, TFTPPacket packet) {
    try {
        out.writeInt(packet.getOpcode());
        if (packet.getOpcode() == TFTPPacket.OP_DATA) {
            out.writeInt(packet.getBlockNumber());
            out.write(packet.getData());
        } else if (packet.getOpcode() == TFTPPacket.OP_ERROR) {
            out.writeInt(packet.getErrorCode());
            out.writeUTF(packet.getErrorMessage());
        }
    } catch (IOException e) {
        System.err.println("Failed to send packet: " + e.getMessage());
    }
}
```

## Summary

The TFTPPacket.java file provides a structured and efficient way to handle TFTP packets, even in a TCP-based implementation. By using the constructors and methods provided, you can easily create and manage RRQ, WRQ, DATA, ACK, and ERROR packets for your TFTP server or client.

---