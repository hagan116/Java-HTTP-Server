import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;

import javax.net.ssl.*;
    
public class RequestProcessor implements Runnable {
  
  private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());

  private File rootDirectory;
  private SSLSocket connection;
  private String[] tokens;
  private String root;
  private String version;
  private String method;
  private OutputStream raw;
  private Writer out;
  private BufferedReader in;
  
  private static String username = "admin";
  private static String password = "12345";
  private String inputUsername;
  private String inputPassword;
  private String fileName;
  
  public RequestProcessor(File rootDirectory, SSLSocket connection) {
    //SET UP LOGGER
    logger.addHandler(new ConsoleHandler());

    if (rootDirectory.isFile()) {
      throw new IllegalArgumentException("rootDirectory must be a directory, not a file");   
    }
    try {
      rootDirectory = rootDirectory.getCanonicalFile();
    } catch (IOException ex) { ex.printStackTrace(); }
    this.rootDirectory = rootDirectory;
    
    this.connection = connection;
  }
  
  @Override
  public void run() {
    // for security checks
    root = rootDirectory.getPath();
    try {              
    	raw = new BufferedOutputStream(connection.getOutputStream());         
    	out = new OutputStreamWriter(raw);
    	in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
      
      	String get = in.readLine();
      
      	logger.info(connection.getRemoteSocketAddress() + " " + get);
      
      	tokens = get.split("\\s+");
      	method = tokens[0];
      	fileName = tokens[1];
      	version = "";
      	
      	if (method.equals("GET")) {
      		//logger.info("GET REQUEST");
          	getRequest(fileName);
      	}
      	else if (method.equals("POST")){
      		//logger.info("POST REQUEST");
          	postRequest();
      	}
      	else if (method.equals("HEAD")){
      		//logger.info("HEAD REQUEST");
       	    headRequest(fileName);
      	}
      	else { // method does not equal "GET"
        	requestNotImplemented();
      	}
    } catch (IOException ex) {
    	logger.log(Level.WARNING, 
        "Error talking to " + connection.getRemoteSocketAddress(), ex);
    } finally {
      	try {
        	connection.close();  
      	}
      	catch (IOException ex) { ex.printStackTrace(); } 
    }
  }

  	private void sendHeader(Writer out, String responseCode, String contentType, int length) throws IOException {
  		out.write(responseCode + "\r\n");
  		Date now = new Date();
  		out.write("Date: " + now + "\r\n");
  		out.write("Server: JHTTP 2.0\r\n");
  		out.write("Content-length: " + length + "\r\n");
  		out.write("Content-type: " + contentType + "\r\n\r\n");
  		out.flush();
  	}
    
  	public void getRequest(String fileNameParam){
        try {
        	fileName = fileNameParam;
        	//logger.info(fileName);
        	String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
        	
        	if (tokens.length > 2) {
        	    version = tokens[2];
        	}

		
        	File theFile = new File(rootDirectory,fileName);
        
        	// Don't let clients outside the document root
        	if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
            	byte[] theData = Files.readAllBytes(theFile.toPath());
            	if (version.startsWith("HTTP/")) { // send a MIME header
                	sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
            	}
            
            	// send the file; it may be an image or other binary data
            	// so use the underlying output stream
            	// instead of the writer
            	//logger.info("FILE IS BEING WRITTEN");
            	raw.write(theData);
            	raw.flush();
        	} else { // can't find the file
	        	logger.info("CANT FIND THE FILE");
            	fileNotFound();
        	}
        }
        catch (IOException ex){ }
    }
    
    //HTTP POST REQUEST METHOD
    public void postRequest(){  
    	try {
    		int length = 0;
    		int i=0; //USED AS COUNTER TO KNOW IF USERNAME HAS ALREADY BEEN READ
            if (tokens.length > 2) {
                version = tokens[2];
            }
            
            //READ THROUGH ALL THE POST REQUEST HEADER LINES
            String currentLine = null;
            while (in.ready()){
                currentLine = in.readLine();
                logger.info("NL:" + currentLine);
                
                //CHECK FOR CONTENT-DISPOSITION TYPE (HTML FORM POST REQUEST)
                if (currentLine.contains("Content-Disposition:")){
                    currentLine = in.readLine();
                    if (i==0){
                    	inputUsername = in.readLine();
                    	//logger.info("USERNAME FROM FROM IS:" + username);
                    	i++;
                    }else {
                    	inputPassword = in.readLine();
                    	//logger.info("PASSWORD FROM FROM IS:" + password);
                    	i=0;
                    }
                }
                
              //SET LENGTH OF POST
                if(currentLine.contains("Content-Length:")){
                	String strLength[] = currentLine.split(" ",2);
                	System.out.println("The length of post: " + strLength[1]);
                	length = Integer.parseInt(strLength[1]) + 2;
                }
                
                //CHECK FOR FORM-URLENCODED TYPE (HTTPSPOST) 
                if (currentLine.contains("Content-Type: application/x-www-form-urlencoded")){
                	//SET TYPE OF POST
                	String strType[] = currentLine.split(" ",2);
                	String type = strType[1];
                	System.out.println("The type of post: " + type);
              
                	//READ POST DATA
                	String postData = "";
                	if (length > 0) {
                        char[] charArray = new char[length];
                        in.read(charArray, 0, length);
                        postData = new String(charArray);
                    }
                	System.out.println(postData);
                	String params[] = postData.split("&",2);
                    String params0[] = params[0].split("=",2);
                    String params1[] = params[1].split("=",2);
                    inputUsername = params0[1];
                	inputPassword = params1[1];
                }
            }
            
            if(inputUsername.equals(username) && inputPassword.equals(password)){
            	out.write("Login Successful!\r\n");
            	getRequest("/www/page4.html");
            } else {
	            out.write("Login Failed..\r\n");
	            out.write("Post received though.\r\n");
	            out.write("/r/n");
	            out.flush();
            }
        }
        catch (IOException ex){ ex.printStackTrace(); } 
    	catch (Exception e) { e.printStackTrace(); }
    }
    
    //HTTP HEAD REQUEST METHOD
    public void headRequest(String fileNameParam){
        try {
            fileName = fileNameParam;
            String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
            if (tokens.length > 2) {
                version = tokens[2];
            }
            
            File theFile = new File(rootDirectory,fileName);
            
            if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
                byte[] theData = Files.readAllBytes(theFile.toPath());
                
                if (version.startsWith("HTTP/")) { // send a MIME header
                    sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
                }
            } else { // can't find the file
                fileNotFound();
            }
        }
        catch (IOException ex){ ex.printStackTrace(); }
    }
    
    
    public void fileNotFound(){
        try {
            String body = new StringBuilder("<HTML>\r\n")
            .append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
            .append("</HEAD>\r\n")
            .append("<BODY>")
            .append("<H1>HTTP Error 404: File Not Found</H1>\r\n")
            .append("</BODY></HTML>\r\n").toString();
            if (version.startsWith("HTTP/")) { // send a MIME header
                sendHeader(out, "HTTP/1.0 404 File Not Found",
                           "text/html; charset=utf-8", body.length());
            }
            out.write(body);
            out.flush();
        }
        catch (IOException ex){}
    }

    public void requestNotImplemented(){
        try {
            String body = new StringBuilder("<HTML>\r\n")
            .append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
            .append("</HEAD>\r\n")
            .append("<BODY>")
            .append("<H1>HTTP Error 501: Not Implemented</H1>\r\n")
            .append("</BODY></HTML>\r\n").toString();
            if (version.startsWith("HTTP/")) { // send a MIME header
                sendHeader(out, "HTTP/1.0 501 Not Implemented",
                           "text/html; charset=utf-8", body.length());
            }
            out.write(body);
            out.flush();
        }
        catch (IOException ex){}
    }
}