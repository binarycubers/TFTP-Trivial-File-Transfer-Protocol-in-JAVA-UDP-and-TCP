# TFTP Implementation using UDP/TCP in Java

This project implements the **Trivial File Transfer Protocol (TFTP)** using **UDP** in Java. It includes a client and server application that can transfer files between them. The implementation follows a simplified version of the TFTP protocol as specified in [RFC 1350](https://tools.ietf.org/html/rfc1350).

---

## **Features**
- **File Upload**: Clients can upload files to the server.
- **File Download**: Clients can download files from the server.
- **Error Handling**: Supports basic error handling (e.g., file not found).
- **Simultaneous Transfers**: The server supports multiple clients simultaneously.
- **Customizable Paths**: Files are saved to specific directories on both the client and server.

---

## **Requirements**
- **Java Development Kit (JDK)**: Version 8 or higher.
- **Maven**: For building the project (optional).

---

## **Description of Key Files and Directories**

### **1. `src/`**
- Contains the source code for the project.
- **`main/java/com.tttp/`**:
    - **`tcp/`**: Implementation of TFTP over TCP.
        - **`client/`**: Client-side code for TCP.
        - **`server/`**: Server-side code for TCP.
        - **`utils/`**: Utility classes for TCP (e.g., packet packing/unpacking).
    - **`udp/`**: Implementation of TFTP over UDP.
        - **`client/`**: Client-side code for UDP.
        - **`server/`**: Server-side code for UDP.
        - **`utils/`**: Utility classes for UDP (e.g., packet packing/unpacking).

### **2. `target/`**
- Contains compiled classes and JAR files.
- **`tftp-protocols-1.0-SNAPSHOT.jar`**: Main JAR file.
- **`tftp-protocols-1.0-SNAPSHOT-tcp-client.jar`**: JAR for TCP client.
- **`tftp-protocols-1.0-SNAPSHOT-tcp-server.jar`**: JAR for TCP server.
- **`tftp-protocols-1.0-SNAPSHOT-udp-client.jar`**: JAR for UDP client.
- **`tftp-protocols-1.0-SNAPSHOT-udp-server.jar`**: JAR for UDP server.

### **3. `tftp-download/`**
- Directory where downloaded files are saved by the client.

### **4. `tftp-uploads/`**
- Directory where uploaded files are saved by the server.

### **5. Configuration Files**
- **`.gitignore`**: Specifies files and directories to ignore in Git.
- **`pom.xml`**: Maven build configuration file.
- **`README.md`**: Project documentation.

### **6. `External Libraries/`**
- Contains external dependencies used in the project.


---
## TCP Implementation

### Features
- Supports RRQ (Read Request) and WRQ (Write Request).
- Handles file not found errors.
- Uses TCP for reliable file transfer.

## Running the TFTP Project
This guide explains how to run the TFTP Server and Client using the JAR files generated by Maven.

### Option 1: Using Separate JAR Files

#### 1. Build the Project
Run the following command in the root directory of your project (where the pom.xml file is located):
```bash
mvn clean package
```
This will generate two JAR files in the target directory:

- tftp-protocols-1.0-SNAPSHOT-tcp-server.jar (for the server).
- tftp-protocols-1.0-SNAPSHOT-tcp-client.jar (for the client).


#### 2. Run the Server

1. Open a terminal or command prompt.

2. Navigate to the ```target``` directory:

```bash
cd target
```
3. Run the server using the following command:

```bash
java -jar tftp-protocols-1.0-SNAPSHOT-tcp-server.jar
```
4. The server will start and display:
```bash
TFTP Server (TCP) running on port 9876
```
#### 3. Run the Client
   
1. Open another terminal or command prompt.

2. Navigate to the ```target``` directory:

```bash
cd target
```
3. Run the client using the following command:

```bash
java -jar tftp-protocols-1.0-SNAPSHOT-tcp-client.jar
```
4. The client will display a menu:

```bash
TFTP Client (TCP)
1. Download File (RRQ)
2. Upload File (WRQ)
   Choice:
```
5. Follow the prompts to upload or download files.

### Option 2: Using the Main JAR File

#### 1. Build the Project

Run the following command in the root directory of your project (where the pom.xml file is located):

```bash
mvn clean package
```
This will generate a main JAR file in the target directory:

 - tftp-tcp-1.0-SNAPSHOT.jar.

#### 2. Run the Server

1. Open a terminal or command prompt.

2. Navigate to the ```target``` directory:

```bash
cd target
```
3. Run the server using the following command:

```bash
java -cp tftp-tcp-1.0-SNAPSHOT.jar com.tftp.tcp.server.TFTPServerTCP
```
4. The server will start and display:
```
TFTP Server (TCP) running on port 9876
```
#### 3. Run the Client
1. Open another terminal or command prompt.

2. Navigate to the ```target``` directory:

```bash
cd target
```
3. Run the client using the following command:

```bash
java -cp tftp-tcp-1.0-SNAPSHOT.jar com.tftp.tcp.client.TFTPClientTCP
```
4. The client will display a menu:

```
TFTP Client (TCP)
1. Download File (RRQ)
2. Upload File (WRQ)
   Choice:
```   
5. Follow the prompts to upload or download files.

## Testing the Project
### 1. Upload a File
- Choose option 2 in the client.

- Enter the filename of a file you want to upload (e.g., testfile.txt).

- The file will be uploaded to the server.

### 2. Download a File
   
- Choose option 1 in the client.

- Enter the filename of the file you want to download (e.g., testfile.txt).

- The file will be downloaded from the server.

### 3. Check the Server Directory

- Files uploaded by the client will be saved in the directory where the server is running.

- Files downloaded by the client will be saved in the directory where the client is running.

## Troubleshooting
### 1. Class Not Found

- Ensure the pom.xml file is correctly configured.

- Rebuild the project using mvn clean package.

### 2. Port Already in Use

- If the server fails to start, ensure no other application is using port 9876.

- You can change the port in the TFTPServerTCP.java file.

### 3. File Not Found

- Ensure the file you are trying to upload or download exists in the correct directory.

## Summary

- Use Option 1 if you want separate JAR files for the server and client.

- Use Option 2 if you want to use the main JAR file and specify the main class explicitly.

---
## UDP Implementation

## **How It Works**
### **Protocol Overview**
- The client and server communicate using UDP.
- The server listens on port `1069` by default.
- The client sends **Read Request (RRQ)** or **Write Request (WRQ)** packets to the server.
- The server responds with **DATA** or **ACK** packets.

### **File Paths**
- **Server Upload Directory**: Files uploaded by clients are saved to:
  *D:/Courses/New-Tasks/JAVA TFTP Task/TFTP-Implementation/TFTP-Implementation/tftp-uploads/*

- **Client Download Directory**: Files downloaded by clients are saved to: *D:/Courses/New-Tasks/JAVA TFTP Task/TFTP-Implementation/TFTP-Implementation/tftp-download/*


---

## **Usage**
### **1. Build the Project**
If using Maven, run:
```bash
mvn clean package
```
This will generate the JAR files in the `target/` directory.

### **2. Run the Server**
   Start the TFTP server:
   ```bash
   java -jar tftp-protocols-1.0-SNAPSHOT-udp-server.jar
```
The server will listen on port `1069`.

### **3. Run the Client**
   Start the TFTP client:

```bash
java -jar tftp-protocols-1.0-SNAPSHOT-udp-client.jar
```
Follow the on-screen prompts to upload or download files.
---
## Commands
### Client Menu
```bash
TFTP Client
1. Download File
2. Upload File
3. Exit
   Choice:
```   
- **Download File:** Enter the filename to download (e.g., resources/testfile.txt).

- **Upload File:** Enter the filename to upload (e.g., resources/testfile.txt).

---
## Example
## Download a File
1. Start the server:
```bash
java -jar tftp-protocols-1.0-SNAPSHOT-udp-server.jar
```
2. Start the client:

```bash
java -jar tftp-protocols-1.0-SNAPSHOT-udp-client.jar
```
3. Choose `"Download File"` and enter the filename:
```bash
Filename: resources/testfile.txt
```
4. The file will be saved to:
```bash
D:/.../tftp-download/resources/testfile.txt
```

## Upload a File

1. Start the server and client as above.

2. Choose `"Upload File"` and enter the filename:

```bash
Filename: resources/testfile.txt
```
3. The file will be saved to:

```bash
D:/.../tftp-uploads/resources/testfile.txt
```
---
## Notes
- Ensure the **tftp-download/** and **tftp-uploads/** directories exist before running the client or server.
- Customize the paths in the code if you want to use different directories.
