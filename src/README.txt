CIS 400 - Web Server Design Final Project

OPTION 1
Testing the Java Web Server->


Compile:
javac JHTTP.java; javac RequestProcessor.java; javac HTTPSClientHead.java; javac HTTPSClient.java; javac HTTPSClientPost.java;

Server-Side:
To run the server execute the following command
	sudo java -Djavax.net.ssl.keyStore=serverKeystore -Djavax.net.ssl.keyStorePassword=123456 JHTTP $(pwd) 80
	(change so authorization happens within java)
	
Now we go client-side to test GET, HEAD, and POST requests.
	
Client-Side:
Get ->
	sudo java -Djavax.net.ssl.trustStore=serverKeystore -Djavax.net.ssl.trustStorePassword=123456 HTTPSClient localhost index.html
	Can run to whatever html file you want
	
Head ->
	sudo java -Djavax.net.ssl.trustStore=serverKeystore -Djavax.net.ssl.trustStorePassword=123456 HTTPSClientHead localhost index.html
	Can run whatever html file you want
	
Post ->
Login authentication is the purpose of this file.
	sudo java -Djavax.net.ssl.trustStore=serverKeystore -Djavax.net.ssl.trustStorePassword=123456 HTTPSClientPost localhost
	It will ask you for username and password and return result
	
ALSO:
	All these can be tested through google chrome
	https://localhost:80/www/page1.html
	(index.html) contains the login for POSTs
	
	

