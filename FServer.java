import java.net.*;
import java.io.*;
import java.util.*;
import java.time.*;

public class FServer {
    private DatagramSocket sock_et;
    private byte[] response;
    private byte[] reply;

    private DatagramPacket rec_packet, send_packet;
    private InetAddress rec_ip;
    private int rec_port;

    private String rec_data;
    private String send_data;

    private int prev_seq_no = -1;
    private int curr_seq = 0;

    private FileInputStream clientFile;
    private byte[] currentChunk;
    private byte[] nextChunk;
    public boolean End=false;
    private int result=512;

    public FServer(int serverPort) throws SocketException {
        this.sock_et = new DatagramSocket(serverPort);
        // this.sock_et.setSoTimeout(30);
        this.response = new byte[100];

        this.currentChunk=new byte[512];
        this.nextChunk=new byte[512];
        

    }

    public int getSequenceNo(String recData) {
        String[] splittedData = recData.split(" ");
        int seqNo = Integer.parseInt(splittedData[1]);
        return seqNo;
    }

    private int readChunk() throws IOException {
        this.nextChunk=new byte[512];
        return this.clientFile.read(this.nextChunk);
    }

    public int sendReply() throws IOException {
      
        
        if (curr_seq <= prev_seq_no) {
            send_data = new String(this.currentChunk);
            if (result==-1){
                send_data = "RDT " + this.curr_seq + " " + send_data.trim()  + " END \r\n";
                if(curr_seq<prev_seq_no){
                    this.End=true;
                }
            }
            else{
            send_data = "RDT " + this.curr_seq + " " + send_data + " \r\n";}
        } else {
            this.currentChunk=new byte[512];
            this.currentChunk = this.nextChunk.clone();
            result = this.readChunk();
            send_data = new String(this.currentChunk);
            if (result == -1) {
                send_data = "RDT " + this.curr_seq + " " + send_data.trim() + " END \r\n";
                this.clientFile.close();
            } else {
                send_data = "RDT " + this.curr_seq + " " + send_data + " \r\n";
            }
            reply=send_data.getBytes();
            this.prev_seq_no = this.curr_seq;
        }
        this.send_packet = new DatagramPacket(reply, reply.length, this.rec_ip, this.rec_port);
        this.sock_et.send(send_packet);
        return result;
    }

    public void getRespose() throws Exception {
        this.response = new byte[100];
        this.rec_packet = new DatagramPacket(response, response.length);
        this.sock_et.receive(this.rec_packet);
        
        this.rec_ip = rec_packet.getAddress();
        this.rec_port = rec_packet.getPort();
      
        this.rec_data = new String(rec_packet.getData());
        
        this.rec_data=rec_data.trim();
        

        if (prev_seq_no== -1) {
            System.out.println("Client IP Address = " + rec_ip);
            System.out.println("Client port = " + rec_port);
    
            this.clientFile = new FileInputStream(rec_data);
            this.sock_et.setSoTimeout(160);
            this.readChunk();
            System.out.println("Filename "+rec_data+"\r\n");

        } else {
            
            this.curr_seq = this.getSequenceNo(rec_data);
            if(this.prev_seq_no!=this.curr_seq){
                System.out.println("Client IP Address = " + rec_ip);
                System.out.println("Client port = " + rec_port);
        
            System.out.println(rec_data+"\r\n");
        }
            // System.out.println(" RECEIVE  -------- "+this.curr_seq);
        }

    }

    public static void main(String[] args) {

        int serverPort = Integer.parseInt(args[0]);
        int result = 0;
        try {
            FServer fs = new FServer(serverPort);
            System.out.println("Server is up");
            boolean end=fs.End;
            while (true && !fs.End) {
                try {
                    fs.getRespose();

                } catch (SocketTimeoutException e) {
                    System.out.println("TimeOut Occur\r\n");
                } catch (IOException e) {
                    System.out.println("Respond Not Received Or given FileName not exist\r\n");
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    System.out.println("other Exception\r\n");
                    System.out.println(e.getMessage());
                }
                try {
                    result = fs.sendReply();
                } catch (Exception e) {
                    
                }

            }
            try {
                fs.clientFile.close();
            } catch (IOException ex) {
                System.out.println("Failed to close file,reason may be file pointing to null object \r\n");
                System.out.println(ex.getMessage());
            }

        } catch (SocketException ex) {
            System.out.println("Socket Exception Occur\r\n");
        } catch (Exception ex) {
            System.out.println("Other Exception Occur\r\n");
            System.out.println(ex.getMessage());
        }
    }

}