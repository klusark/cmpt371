/**
 * HttpRequest - HTTP request container and parser
 *
 * $Id: HttpRequest.java,v 1.2 2003/11/26 18:11:53 kangasha Exp $
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest {
	/** Help variables */
	final static String CRLF = "\r\n";
	final static int HTTP_PORT = 80;
	/** Store the request parameters */
	String methodStr;
	String URI;
	String version;
	String headers = "";
	/** Server and port */
	private String host;
	private int port;

	// The supported methods
	public enum Method {
		GET, HEAD, UNSUPPORTED
	}

	// The method being used
	Method method;

	/** Create HttpRequest by reading it from the client socket */
	public HttpRequest(BufferedReader from) throws IOException {
		String firstLine = "";
		try {
			firstLine = from.readLine();
		} catch (IOException e) {
			System.out.println("Error reading request line: " + e);
		}
		if (firstLine == null) {
			throw new IOException();
		}
		System.out.println(firstLine);
		String[] tmp = firstLine.split(" ");
		methodStr = tmp[0];/* Fill in */;
		URI = tmp[1];/* Fill in */;
		version = tmp[2];/* Fill in */;

		determineMethod(methodStr);

		System.out.println("URI is: " + URI);

		if (method == Method.UNSUPPORTED) {
			throw new IOException("Error Method not supported: " + methodStr);
		}

		try {
			String line = from.readLine();
			while (line.length() != 0) {
				headers += line + CRLF;
				/* We need to find host header to know which server to
				 * contact in case the request URI is not complete. */
				if (line.startsWith("Host:")) {
					tmp = line.split(" ");
					host = getHostFromString(tmp[1]);
					port = getPortFromString(tmp[1]);
				}
				line = from.readLine();
			}
		} catch (IOException e) {
			System.out.println("Error reading from socket: " + e);
			return;
		}
		System.out.println("Host to contact is: " + host + " at port " + port);
	}

	void determineMethod(String str) {
		if (str.equals("GET")) {
			method = Method.GET;
		} else if (str.equals("HEAD")) {
			method = Method.HEAD;
		} else {
			method = Method.UNSUPPORTED;
		}
	}

	public Method getMethod() {
		return method;
	}

	String getHostFromString(String str) {
		if (str.indexOf(':') > 0) {
			String[] tmp2 = str.split(":");
			return tmp2[0];
		} else {
			return str;
		}
	}


	int getPortFromString(String str) {
		if (str.indexOf(':') > 0) {
			String[] tmp2 = str.split(":");
			return Integer.parseInt(tmp2[1]);
		} else {
			return HTTP_PORT;
		}
	}

	/** Return host for which this request is intended */
	public String getHost() {
		return host;
	}

	/** Return port for server */
	public int getPort() {
		return port;
	}

	/**
	 * Convert request into a string for easy re-sending.
	 */
	public String toString() {
		String req = "";

		req = method + " " + URI + " " + version + CRLF;
		req += headers;
		/* This proxy does not support persistent connections */
		req += "Connection: close" + CRLF;
		req += CRLF;

		return req;
	}
}
