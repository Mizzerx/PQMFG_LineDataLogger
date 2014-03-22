import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.jws.soap.SOAPBinding.Use;

public class ClientAlpha
{
	//Some Initial Class Variables
	private Socket ServerSocket = null;
	private InetAddress ServerIpAddress = null;
	private int ServerPortNum = 5005;
	
	//Default Constructor
	public ClientAlpha(int width,int Height)
	{
		
		/**
		 * Ask User To point At Server
		 * */
		
		//Create a Multi-line input box
		JOptionPaneMultiInput UserPrompt = new JOptionPaneMultiInput();
		NETAddress myAdress = UserPrompt.ask();// *NOTE: NET Adrees simple class i pout together for passing back and forth net addresses
		
		//Asighned returned shit to class variabls
		ServerIpAddress = myAdress.IP;
		ServerPortNum = myAdress.Port;
		
		/**
		 * Heres where we try and open up a connection to the server
		 * */
		try 
		{
			ServerSocket = new Socket(ServerIpAddress, ServerPortNum);
		} 
		catch (IOException e) {
			System.out.println("Could Not Open connection to Server At: "+ServerIpAddress+" "+Integer.toString(ServerPortNum));
		}
		

		
		
	}
	
	public static void main(String[] args) 
	{
		new ClientAlpha(0,0);
	}
}