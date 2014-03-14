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
		public SendTask(String host, int p, Timer t) {
			try {
				socket = new DatagramSocket();
				address = InetAddress.getByName(host);
				port = p;
				timer= t;
				socket.setSoTimeout(1000);
			} catch(Exception e) {
			}
		}
		public void run() {
			try {
				byte[] buf;
				Date d = new Date();
				long send_time = System.currentTimeMillis();
				String data = "PING " + i + " "  +send_time+"\r\n";
				System.out.println("Sending: "+ data);
				buf = data.getBytes();
				// Create a datagram packet to hold incomming UDP packet.
				DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);

				// Block until the host receives a UDP packet.
				socket.send(packet);

				buf = new byte[1024];
				packet = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(packet);
					long delta = System.currentTimeMillis() - send_time;
					System.out.println(delta);
					printData(packet);
				} catch(Exception e) {
				}
				// Print the recieved data.
				++i;
				if (i == 10) {
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

		// Create a datagram socket for receiving and sending UDP packets
		// through the port specified on the command line.

		Timer t = new Timer();
		SendTask t2 = new SendTask(host, port, t);
		t.schedule(t2, 1000, 1000);
	}

	/*
	 * Print ping data to the standard output stream.
	 */
	private static void printData(DatagramPacket request) throws Exception
	{
		// Obtain references to the packet's array of bytes.
		byte[] buf = request.getData();

		// Wrap the bytes in a byte array input stream,
		// so that you can read the data as a stream of bytes.
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);

		// Wrap the byte array output stream in an input stream reader,
		// so you can read the data as a stream of characters.
		InputStreamReader isr = new InputStreamReader(bais);

		// Wrap the input stream reader in a bufferred reader,
		// so you can read the character data a line at a time.
		// (A line is a sequence of chars terminated by any combination of \r and \n.)
		BufferedReader br = new BufferedReader(isr);

		// The message data is contained in a single line, so read this line.
		String line = br.readLine();

		// Print host address and data received from it.
		System.out.println(
			"Received from " +
			request.getAddress().getHostAddress() +
			": " +
			new String(line) );
	}
}
