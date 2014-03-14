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
		int current_seq = 0;
		Timer timer;
		int packets_received = 0;
		double min = 1000;
		double max = 0;
		double total = 0;
		int mdev = 0;
		long ping_start_time;
		long current_send_time = 0;
		int current_timeout = 1000;
		public SendTask(InetAddress addr, int p, Timer t) throws SocketException {
			socket = new DatagramSocket();
			address = addr;
			port = p;
			timer= t;
			ping_start_time = System.currentTimeMillis();
		}
		private void getPacket() {
				byte[] buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(packet);
					double delta = (System.nanoTime() - current_send_time) / (double)(1000 * 1000);

					// Make sure that this packet has the same seq as the one that was sent
					if (buf[5] - '0' != current_seq) {
						// If it isn't valid, we need to change the timeout and receive some more
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
										": seq="+current_seq+" time="+ delta+" ms");
					++packets_received;
				} catch(IOException e) {
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
				String data = "PING " + current_seq + " "  +new Date()+"\r\n";
				buf = data.getBytes();
				// Create a datagram packet to hold incomming UDP packet.
				DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);

				socket.send(packet);

				// Block for up to a second waiting for a packet
				getPacket();
				++current_seq;
				if (current_seq == 10) {
					// All 10 have been sent, print stats
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

					// No need for the timer anymore
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
		int port = 0;

		// Check to make sure the port makes sense
		try {
			port = Integer.parseInt(args[1]);
			if (port < 1 || port > 65535) {
				throw new Exception();
			}
		} catch (Exception e) {
			System.out.println("Invalid port");
			return;
		}

		// Check the host as well
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			System.out.println("Invalid host");
			return;
		}

		System.out.println("PING " + host);

		// Create and setup timer
		Timer t = new Timer();
		SendTask t2 = new SendTask(address, port, t);
		t.schedule(t2, 1000, 1000);
	}
}
