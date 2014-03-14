import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Server to process ping requests over UDP.
 */
public class PingClient
{

	private static class SendTask extends TimerTask {
		DatagramSocket socket;
		InetAddress address;
		int port;
		int i = 0;
		Timer timer;
		int packets_received = 0;
		double min = 1000;
		double max = 0;
		double total = 0;
		int mdev = 0;
		long ping_start_time;
		long current_send_time = 0;
		int current_timeout = 1000;
		public SendTask(String host, int p, Timer t) {
			try {
				socket = new DatagramSocket();
				address = InetAddress.getByName(host);
				port = p;
				timer= t;
				ping_start_time = System.currentTimeMillis();
			} catch(Exception e) {
			}
		}
		private void getPacket() {
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(packet);
					double delta = (System.nanoTime() - current_send_time) / (double)(1000 * 1000);
					if (buf[5] - '0' != i) {
						current_timeout -= delta;
						socket.setSoTimeout(current_timeout);
						getPacket();
						return;
					}
					if (delta < min) {
						min = delta;
					}
					if (delta > max) {
						max = delta;
					}
					total += delta;
					System.out.println(packet.getLength() +" bytes from " +
										packet.getAddress().getHostAddress() + 
										": seq="+i+" time="+ delta+" ms");
					++packets_received;
				} catch(Exception e) {
					System.out.println("Timeout");
				}
		}
		public void run() {
			try {
				current_timeout = 1000;
				socket.setSoTimeout(current_timeout);
				byte[] buf;
				Date d = new Date();
				current_send_time = System.nanoTime();
				String data = "PING " + i + " "  +new Date()+"\r\n";
				buf = data.getBytes();
				// Create a datagram packet to hold incomming UDP packet.
				DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);

				// Block until the host receives a UDP packet.
				socket.send(packet);

				getPacket();
				// Print the recieved data.
				++i;
				if (i == 10) {
					System.out.println("--- ping statistics ---");
					int loss = ((10-packets_received)*100)/10;
					System.out.println("10 packets transmitted, "+packets_received+
										" received, "+loss+"% packet loss, time "+
										(System.currentTimeMillis() - ping_start_time) +" ms");
					if (packets_received != 0) {
						double avg = (total/packets_received);
						System.out.println("rtt min/avg/max = "+min+"/"+avg+
											"/"+max+" ms");
					}
					cancel();
					timer.cancel();
				}
			} catch(Exception e) {
			}

		}
	}

	public static void main(String[] args) throws Exception
	{
		// Get command line argument.
		if (args.length != 2) {
			System.out.println("Required arguments: host port");
			return;
		}
		String host = args[0];
		int port = Integer.parseInt(args[1]);

		System.out.println("PING " + host);
		// Create a datagram socket for receiving and sending UDP packets
		// through the port specified on the command line.

		Timer t = new Timer();
		SendTask t2 = new SendTask(host, port, t);
		t.schedule(t2, 1000, 1000);
	}
}
