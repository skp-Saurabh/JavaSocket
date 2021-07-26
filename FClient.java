import java.net.*;
import java.io.*;
import java.util.*;
 
public class FClient {
	private static final double LOSS_RATE = 0.3;
	private static final int AVERAGE_DELAY = 100;
 
	public static void main(String[] args) throws InterruptedException {
	 
	    DatagramSocket cs = null;
		FileOutputStream fos = null;
		Random random = new Random();

		try {

	    	cs = new DatagramSocket();
			Scanner sc = new Scanner(System.in);
			int flag=0;
			byte[] rd, sd;
			String reply,ack,data;
			DatagramPacket sp,rp;
			int count=1;
			int rseq,i,j;
			boolean end = false;
			String req = "";
			String reqArray[]=null;
			if (flag == 0) {
				System.out.println("Send File Request:");
				req = sc.nextLine();
				reqArray = req.split(" ");
				flag++;
			}

			if (reqArray[0].equals("REQUEST")) {
				// write received html data
				fos = new FileOutputStream(reqArray[1].substring(0, reqArray[1].length() - 5) + "_recived.html");
				sd = reqArray[1].getBytes();

				sp = new DatagramPacket(sd, sd.length, InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
				
                cs.send(sp);

				System.out.println("Requesting " + reqArray[1] + " from server"+ InetAddress.getByName(args[0]) + ":" + Integer.parseInt(args[1]) + " serverport");
				while(!end)
				{
					// delay
					if (random.nextDouble() < LOSS_RATE)
					{
							System.out.println(" No frame received \r\n");
							continue;
					}
					Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
					// get next consignment
					rd=new byte[528];
					rp=new DatagramPacket(rd,rd.length); 
					cs.receive(rp);
					reply=new String(rp.getData());
					i=reply.indexOf(" ");
					j=reply.indexOf(" ",reply.indexOf(" ") + 1);
					rseq=Integer.parseInt(reply.substring(i+1,j));
					if(count-rseq==2){
						System.out.println("Frame no. "+rseq+" already previously received.So discarding to avoid duplicacy.\r\n");
						ack = "ACK " + (count-1)+" \r\n";
						// send ACK      
						sd=ack.getBytes();	
						sp=new DatagramPacket(sd,sd.length,InetAddress.getByName(args[0]),Integer.parseInt(args[1]));	  
						cs.send(sp);
						continue;
					}
					else{
                         System.out.println(reply);
						// concat consignment
						if (reply.trim().contains("END")){// if last consignment
                            reply=reply.trim();
							count=0;
							data=reply.substring(j+1,reply.length()-4);
						}
						else{
							data=reply.substring(j+1,reply.length()-9);
						}
						fos.write(data.getBytes());
						ack = "ACK " + count+" \r\n";
						// send ACK      
						sd=ack.getBytes();	
						sp=new DatagramPacket(sd,sd.length,InetAddress.getByName(args[0]),Integer.parseInt(args[1]));	  
						cs.send(sp);
						if(count==0){
							Thread.sleep(500);
							end=true;
						}
						count++;
					}
					 	 
					
				}
			}
			else {
				System.out.println("Invalid REQUEST FORMAT\n Give input in format of REQUEST fileName CRLF");
			}
		} catch (IOException ex) {
			System.out.println(ex.getMessage());

		} finally {

			try {
				if (fos != null)
					fos.close();
				if (cs != null)
					cs.close();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
}
