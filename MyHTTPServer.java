import java.net.*;


public class MyHTTPServer {
	
	public static void main(String[] args) {
		
		String puerto = "";
		String puerto_controller = "";
		String ip_controller="";
		try {
			if (args.length < 3) {
				System.out.println("ERROR");
				System.out.println("Indica el puerto de escucha de el Servidor y el puerto y la ip de controller");
				System.exit(1);
			}
			
			puerto = args[0];
			puerto_controller = args[1];
			ip_controller= args[2];
			ServerSocket socketServidor = new ServerSocket(Integer.parseInt(puerto));
			System.out.println("Escuchando el puerto " + puerto + ".....");
			
			for(;;) {
				Socket socketCliente = socketServidor.accept();
				System.out.println("Sirviendo cliente...");
				
				Thread t = new HiloServidor(socketCliente,puerto_controller,ip_controller);  //Abrir HiloServidor.java
				t.start();
			}
		}
		catch(Exception e) {
			System.out.println("Error en el Servidor :" + e.toString());
		}
	}	
}