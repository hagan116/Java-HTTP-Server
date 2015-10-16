import java.io.*;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.net.ssl.*;

public class JHTTP {

  private static final Logger logger = Logger.getLogger(JHTTP.class.getCanonicalName());

  private final File rootDirectory;
  private final int port;
  private SSLServerSocket serverSocket;
    
  public JHTTP(File rootDirectory, int port) throws IOException {
    logger.addHandler(new ConsoleHandler());

	if (!rootDirectory.isDirectory()) {
		throw new IOException(rootDirectory + " does not exist as a directory"); 
    }
    this.rootDirectory = rootDirectory;
    this.port = port;
  	}

  public void start() throws IOException {
    ExecutorService pool = Executors.newCachedThreadPool();
    try {
    	SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    	serverSocket = (SSLServerSocket) factory.createServerSocket(port);
    	
    	logger.info("Accepting connections on port " + serverSocket.getLocalPort());
    	logger.info("Document Root: " + rootDirectory);
      
		
    	while (true) {
    		try {
	    		SSLSocket request = (SSLSocket) serverSocket.accept();
    			Runnable r = new RequestProcessor(rootDirectory, request);
    			pool.submit(r);
    		} catch (IOException ex) {
    			logger.log(Level.WARNING, "Error accepting connection", ex);
    		}   
    	}
    } finally { }
  }
  
  public static void main(String[] args) {

    // get the Document root
    File docroot;
    try {
    	docroot = new File(args[0]);
    } catch (ArrayIndexOutOfBoundsException ex) {
    	System.out.println("Usage: java JHTTP docroot port");
    	return;
    }
    
    // set the port to listen on
    int port;
    try {
    	port = Integer.parseInt(args[1]);
    	if (port < 0 || port > 65535) port = 80;
    } catch (RuntimeException ex) {
    	port = 80;
    }  
    
    try {            
    	JHTTP webserver = new JHTTP(docroot, port);
    	webserver.start();
    } 	catch (IOException ex) {
    	logger.log(Level.SEVERE, "Server could not start", ex);
    }
  }
  
  
}//END