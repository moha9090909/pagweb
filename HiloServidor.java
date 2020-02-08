import java.lang.Exception;
import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Date;

public class HiloServidor extends Thread {
	private Socket socketCliente;
	private String puerto;
	private String ip;
	static final File WEB_ROOT = new File(".");
	static final String ARCHIVO_POR_DEFECTO = "/index.html";
	static final String ARCHIVO_NO_ENCONTRADO = "/404.html";
	static final String METHOD_NOT_SUPPORTED = "/405.html";
	static final boolean verbose = true;
	
	public HiloServidor(Socket p_cliente,String puerto, String ip) {
		this.socketCliente = p_cliente;
		this.puerto=puerto;
		this.ip=ip;
	}
	
	public void run() {
		// we manage our particular client connection
		BufferedReader in = null; 
		PrintWriter out = null; 
		BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
		try {
			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
			// we get character output stream to client (for headers)
			out = new PrintWriter(socketCliente.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(socketCliente.getOutputStream());
			
			// get first line of the request from the client
			String input = in.readLine();
			
			// we parse the request with a string tokenizer
			StringTokenizer parse = new StringTokenizer(input);
			
			String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
			// we get file requested
			fileRequested = parse.nextToken().toLowerCase();
			
			
			// we support only GET and HEAD methods, we check
			if (!method.equals("GET")) {
				if (verbose) {
					System.out.println("501 Not Implemented : " + method + " method.");
				}
				
				// we return the not supported file to the client
				File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				//read content to return to client
				byte[] fileData = readFileData(file, fileLength);
					
				// we send HTTP Headers with data to client
				out.println("HTTP/1.1 501 Not Implemented");
				out.println("Server: Java HTTP Server from SSaurel : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + contentMimeType);
				out.println("Content-length: " + fileLength);
				out.println(); // blank line between headers and content, very important !
				out.flush(); // flush character output stream buffer
				// file
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
				
			} else {
				// GET or HEAD method
				if (fileRequested.equals("/")) {
					fileRequested += ARCHIVO_POR_DEFECTO;
				}
				File file;
				
				if (fileRequested.contains("controladorsd/")) {
					String response;
					response = ConnectController(fileRequested);
					out.println("HTTP/1.1 200 OK");
					out.println("Server: Java HTTP Server from SSaurel : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + "text/html");
					out.println("Content-length: " + response.length());
					out.println(); // blank line between headers and content, very important !
					out.flush();
					
					out.println(response);
					out.flush();
					
				} else {
					file = new File(WEB_ROOT, fileRequested);
					int fileLength = (int) file.length();
					String content = getContentType(fileRequested);
					
					if (method.equals("GET")) { // GET method so we return content
						byte[] fileData = readFileData(file, fileLength);
						
						// send HTTP Headers
						out.println("HTTP/1.1 200 OK");
						out.println("Server: Java HTTP Server from SSaurel : 1.0");
						out.println("Date: " + new Date());
						out.println("Content-type: " + content);
						out.println("Content-length: " + fileLength);
						out.println(); // blank line between headers and content, very important !
						out.flush(); // flush character output stream buffer
						
						dataOut.write(fileData, 0, fileLength);
						dataOut.flush();
					}
					if (verbose) {
						System.out.println("File " + fileRequested + " of type " + content + " returned");
					}
				}
				
				
				
				
			}
			
		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}
			
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				socketCliente.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 
			
			if (verbose) {
				System.out.println("Conexión cerrada.\n");
			}
		}		
	}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}
	
	// return supported MIME Types
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html")) {
			return "text/html";
		} else if(fileRequested.endsWith(".jpg") ||  fileRequested.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if(fileRequested.endsWith(".mp4")) {
			return "video/mp4";
		} else {
			return "text/plain";
		}
	}
	
	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WEB_ROOT, ARCHIVO_NO_ENCONTRADO);
		int fileLength = (int) file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);
		
		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: Java HTTP Server from SSaurel : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); // blank line between headers and content, very important !
		out.flush(); // flush character output stream buffer
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		
		if (verbose) {
			System.out.println("File " + fileRequested + " not found");
		}
	}
	private String ConnectController(String ruta) {
		String c_datos="";
		
		ClienteMyHTTPServer cl = new ClienteMyHTTPServer(ruta,puerto,ip);
		
		c_datos = cl.CreateConnection();
		
		return c_datos;
	}
}
