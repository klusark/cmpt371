
import java.net.*;
import java.io.*;
import java.util.*;

public class ProxyCacheThread implements Runnable {
	Socket client = null;
	Socket server = null;
	HttpRequest request = null;
	HttpResponse response = null;
	Thread t = null;

	ProxyCacheThread(Socket c) {
		client = c;
		t = new Thread(this, "Proxy Thread");
		t.start();
	}

	public void run() {
		System.out.println("New connection!\n");
		try {
			readRequest();
			sendRequest();
			readWriteResponse();
		} catch(Exception e) {
			System.out.println("Exception: " + e);
		}
		try {
			if (client != null) {
				client.close();
			}
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			System.out.println("Error closing the connections: " + e);
		}
		System.out.println("Connection close");
	}

	void readRequest() throws Exception {
		/* Process request. If there are any exceptions, then simply
		 * return and end this request. This unfortunately means the
		 * client will hang for a while, until it timeouts. */
		System.out.println("Read request");
		/* Read request */
		try {
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()))/* Fill in */;
			request = new HttpRequest(fromClient)/* Fill in */;
		} catch (IOException e) {
			System.out.println("Error reading request from client: " + e);
			throw e;
		}
	}

	void sendRequest() throws Exception {
		System.out.println("Send request");
		/* Send request to server */
		try {
			/* Open socket and write request to socket */
			server = new Socket(request.getHost(), request.getPort())/* Fill in */;
			DataOutputStream toServer = new DataOutputStream(server.getOutputStream())/* Fill in */;
			/* Fill in */
			toServer.writeBytes(request.toString());
		} catch (UnknownHostException e) {
			System.out.println("Unknown host: " + request.getHost());
			System.out.println(e);
			throw e;
		} catch (IOException e) {
			System.out.println("Error writing request to server: " + e);
			throw e;
		}
	}

	void readWriteResponse() throws Exception {
		System.out.println("Read/write response");
		/* Read response and forward it to client */
		try {
			DataInputStream fromServer = new DataInputStream(server.getInputStream())/* Fill in */;
			response = new HttpResponse(fromServer, request);/* Fill in */;
			DataOutputStream toClient = new DataOutputStream(client.getOutputStream())/* Fill in */;
			/* Fill in */
			/* Write response to client. First headers, then body */
			System.out.println("Write header");
			toClient.writeBytes(response.toString());
			System.out.println("Write data");
			response.stream(fromServer, toClient);

			/* Code to insert object into the cache goes here. */
			/* NOT required for this assignment */
		} catch (IOException e) {
			System.out.println("Error writing response to client: " + e);
			throw e;
		}
	}

}
