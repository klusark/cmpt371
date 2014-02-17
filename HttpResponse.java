
/**
 * HttpResponse - Handle HTTP replies
 *
 * $Id: HttpResponse.java,v 1.2 2003/11/26 18:12:42 kangasha Exp $
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpResponse {
	final static String CRLF = "\r\n";
	/** How big is the buffer used for reading the object */
	final static int BUF_SIZE = 8192;
	/** Maximum size of objects that this proxy can handle. For the
	 * moment set to 100 KB. You can adjust this as needed. */
	/** Reply status and headers */
	String version;
	int status;
	String statusLine = "";
	String headers = "";
	/* Length of the object */
	int length = -1;
	HttpRequest request;

	/** Read response from server. */
	public HttpResponse(DataInputStream fromServer, HttpRequest r) {
		request = r;
		boolean gotStatusLine = false;

		/* First read status line and response headers */
		try {
			String line = fromServer.readLine()/* Fill in */;
			while (line != null && line.length() != 0) {
				if (!gotStatusLine) {
					statusLine = line;
					gotStatusLine = true;
				} else {
					headers += line + CRLF;
				}

				/* Get length of content as indicated by
				 * Content-Length header. */

				// Convert it to lower case so we only have to check it once
				String clen = line.toLowerCase();
				if (clen.startsWith("content-length")) {
					String[] tmp = line.split(" ");
					length = Integer.parseInt(tmp[1]);
				}
				line = fromServer.readLine();
			}
		} catch (IOException e) {
			System.out.println("Error reading headers from server: " + e);
			return;
		}

		// if it's head there is nothing to read, regardless of what the server
		// says
		if (request.getMethod() == HttpRequest.Method.HEAD) {
			length = 0;
		}

	}

	public void stream(DataInputStream from, DataOutputStream to) {
		try {
			int bytesRead = 0;
			byte buf[] = new byte[BUF_SIZE];
			boolean loop = false;

			/* If we didn't get Content-Length header, just loop until
			 * the connection is closed. */
			if (length == -1) {
				loop = true;
			}

			// read BUF_SIZE chunks into buf and immediately send them to the
			// client
			while (bytesRead < length || loop) {
				/* Read it in as binary data */
				int res = from.read(buf, 0, BUF_SIZE);/* Fill in */;
				if (res == -1) {
					break;
				}
				to.write(buf, 0, res);
				bytesRead += res;
			}
		} catch (IOException e) {
			System.out.println("Error reading response body: " + e);
			return;
		}

	}

	/**
	 * Convert response into a string for easy re-sending. Only
	 * converts the response headers, body is not converted to a
	 * string.
	 */
	public String toString() {
		String res = "";

		res = statusLine + CRLF;
		res += headers;
		res += CRLF;

		return res;
	}
}
