import java.io.*;

import javax.net.ssl.*;

import java.net.URLEncoder;

public class HTTPSClientPost {
	
	public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Usage: java HTTPSClient host");
      return;
    }       
    
    int port = 80; // default https port
    String host = args[0];
    
    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    SSLSocket socket = null;

    try {         
      socket = (SSLSocket) factory.createSocket(host, port);

      //GET USER FILE INPUT + GET BYTES OF FILE
      BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("Authentication: Please enter login information.\nValid Username:");
      String username = userInput.readLine();
      System.out.println("Valid Password:");
      String password = userInput.readLine();
      
      String params = URLEncoder.encode("username", "UTF-8") + "="+ URLEncoder.encode(username, "UTF-8");
      		 params += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
      
      String path = "www/index.html/";
      BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
      wr.write("POST " + path + " HTTP/1.0\r\n");
      wr.write("Content-Length: " + params.length() + "\r\n");
      wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
      wr.write("\r\n");
      wr.write(params);
      wr.flush();

      //read response
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      //read the header
      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(line);
      }
      System.out.println();
      wr.close();
      in.close();
      
      
      // read the length
      String contentLength = in.readLine();
      @SuppressWarnings("unused")
	  int length = Integer.MAX_VALUE;
      try {
          length = Integer.parseInt(contentLength.trim(), 16);
      } catch (NumberFormatException ex) {
        // This server doesn't send the content-length
        // in the first line of the response body
      }
      System.out.println(contentLength);
      
      System.out.println();
    } catch (IOException ex) {
      System.err.println(ex);
    } finally {
        try {
          if (socket != null) socket.close();
        } catch (IOException e) {}
    }
  }
}