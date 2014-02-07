import java.util.*;
import java.io.*;


public class HttpHeaders {
	final static String CRLF = "\r\n";
	Map<String, String> _headers = new HashMap<String, String>();
	public HttpHeaders(BufferedReader headers) throws IOException {
		String line = headers.readLine();
		while (line.length() != 0) {
			String[] split = line.split(":", 2);
			_headers.put(split[0], split[1].trim());
			line = headers.readLine();
		}
	}

	public String getHeader(String header) {
		return _headers.get(header);
	}

	public String toString() {
		String out = "";
		for (Map.Entry<String, String> entry : _headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			out += key + ":" + value;
		}
		return out;
	}
}
