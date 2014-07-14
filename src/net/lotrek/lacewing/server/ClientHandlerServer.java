package net.lotrek.lacewing.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.server.structure.ServerClient;

public class ClientHandlerServer extends Thread
{
	public final NavigableMap<Integer, ServerClient> clients = new TreeMap<Integer, ServerClient>();
	private ServerSocket serverSocket;
	private LacewingServer server;
	
	public void run()
	{
		while(!interrupted())
		{
			try {
				Socket cs = serverSocket.accept();
				ServerClient sc = new ServerClient(cs, server);
				
				if(clients.isEmpty())
				{
					clients.put(0, sc);
					sc.setID(0);
				}else for (int i = 0; i <= clients.lastKey() + 1; i++)
					if(!clients.containsKey(i))
					{
						clients.put(i, sc);
						sc.setID(i);
						break;
					}
				
			} catch (IOException e) {
//					e.printStackTrace();
			}
		}
	}
	
	public void interrupt()
	{
		super.interrupt();
		
		try {
			serverSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for(ServerClient sc : clients.values().toArray(new ServerClient[0]))
			try {
				sc.disconnectClient();
			} catch (IOException | LacewingException e) {
//				e.printStackTrace();
			}
		
		while(this.isAlive()) this.isAlive();
	}
	
	public void init(int port, LacewingServer ls) throws IOException
	{
		serverSocket = new ServerSocket(port);
		this.server = ls;
		super.start();
	}
}
