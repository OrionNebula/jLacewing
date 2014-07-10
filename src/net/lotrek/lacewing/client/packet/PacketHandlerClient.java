package net.lotrek.lacewing.client.packet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.MethodObjectPair;
import net.lotrek.lacewing.client.LacewingClient;
import net.lotrek.lacewing.client.structure.Peer;

public class PacketHandlerClient extends Thread
{
	private LacewingClient client;
	private HashMap<Class<? extends ReadPacket>, MethodObjectPair> packetTriggers = new HashMap<Class<? extends ReadPacket>, MethodObjectPair>();
	
	public void run()
	{
		try {
			registerPacketTrigger(ReadPacket11Ping.class, new MethodObjectPair(this.getClass().getMethod("handlePing", ReadPacket11Ping.class), this));
			registerPacketTrigger(ReadPacket9Peer.class, new MethodObjectPair(this.getClass().getMethod("handlePeer", ReadPacket9Peer.class), this));
		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		}
		
		while(!interrupted())
		{
			try {
				ReadPacket packet = ReadPacket.readPacketFromStream(client.getInputStream());
				
				if(packet == null)
					continue;
				
				if(packetTriggers.containsKey(packet.getClass()))
					packetTriggers.get(packet.getClass()).invoke(packet);
				else
					throw new LacewingException("Packet " + packet.getPacketType() + " has no handler");
				
			} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | LacewingException e) {
				client.handleOtherworldException(e);
			}
		}
	}
	
	public void interrupt()
	{
		super.interrupt();
		
		while(this.isAlive()) System.out.print("");
	}
	
	public void init(LacewingClient lc)
	{
		client = lc;
		start();
	}
	
	public LacewingClient getClient()
	{
		return client;
	}
	
	public void registerPacketTrigger(Class<? extends ReadPacket> packetType, MethodObjectPair toInvoke)
	{
		packetTriggers.put(packetType, toInvoke);
	}
	
	public void handlePing(ReadPacket11Ping packet)
	{
		try {
			WritePacket.writePacketToStream(client.getOutputStream(), new WritePacket9Pong());
		} catch (IOException | LacewingException e) {
			e.printStackTrace();
		}
	}
	
	public void handlePeer(ReadPacket9Peer packet)
	{
		int action = packet.getActionType(getClient());
		Peer peer = Peer.getPeer(getClient(), packet.getPeerID());
		packet.updatePeer(getClient());
		System.out.println(peer + " has been updated: " + action);
	}
	
	public static PacketHandlerClient getThreadAsThis()
	{
		return (PacketHandlerClient)Thread.currentThread();
	}
}
