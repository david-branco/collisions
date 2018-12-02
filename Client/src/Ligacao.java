

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Ligacao {

	private Socket sock;
	private BufferedReader in;
	private PrintWriter pw;
	
	public Ligacao(){
	}
	/** Conecta-se ao servidor com o addr e a porta atraves de uma socket **/
	public boolean connect(String addr, int port) {
        try{
           	sock = new Socket(addr,port);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            pw = new PrintWriter(sock.getOutputStream());
            sock.setTcpNoDelay(true);
            return true;
        } catch(Exception e){return false;}
    }
	
	public Socket getSocket(){return sock;} 
	
	/** Escreve uma mensagem no socket.*/
    public void write(String s){
    	  System.out.println("Write: " + s);
    	  pw.println(s);
    	  pw.flush(); 
    }
    
        
    /** L� uma linha do socket. */
    public String read(){
      String str;
        try{
        	str = in.readLine();  
        	System.out.println("Read : " + str);
        } catch(Exception e){return null;}
        return str;
    }

    /** Desliga a conex�o */
    public void disconnect(){
        try{
            sock.close();
        } catch(Exception e){}
    }

}
