package net.lotrek.lacewing.server.structure;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.LacewingException;
import net.lotrek.lacewing.server.LacewingServer;
import net.lotrek.lacewing.server.packet.ServerWritePacket;
import net.lotrek.lacewing.server.packet.ServerWritePacket4BinaryServerChannelMessage;

public class ServerChannel
{
	private LacewingServer server;
	private String name;
	private int id;
	private ServerClient channelMaster;
	private boolean isHidden, willClose;
	private ArrayList<ServerClient> clients = new ArrayList<ServerClient>();
	
	private ServerChannel(LacewingServer ls, String name)
	{
		server = ls;
		this.name = name;
		
		if(ls.globalChannelsByID.isEmpty())
		{
			ls.globalChannelsByID.put(0, this);
			this.id = 0;
		}else for (int i = 0; i <= ls.globalChannelsByID.lastKey() + 1; i++)
			if(!ls.globalChannelsByID.containsKey(i))
			{
				ls.globalChannelsByID.put(i, this);
				this.id = i;
				break;
			}
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getID()
	{
		return id;
	}
	
	public ServerClient[] getClients()
	{
		return clients.toArray(new ServerClient[0]);
	}
	
	public void clearClients()
	{
		clients.clear();
	}
	
	public void removeClient(ServerClient c)
	{
		clients.remove(c);
	}
	
	public void addClient(ServerClient c)
	{
		clients.add(c);
	}
	
	public void setChannelMaster(ServerClient c)
	{
		channelMaster = c;
	}
	
	public ServerClient getChannelMaster()
	{
		return channelMaster;
	}
	
	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isWillClose() {
		return willClose;
	}

	public void setWillClose(boolean willClose) {
		this.willClose = willClose;
	}

	public LacewingServer getServer()
	{
		return server;
	}
	
	public int predictLengthInChannelList()
	{
		return isHidden ? 0 : 3 + getName().length();
	}
	
	public void writeToChannelList(DataOutputStream dos, ServerClient toExclude) throws IOException
	{
		if(isHidden)
			return;
		
		DataTools.writeInversedShort(dos, clients.size() - (clients.contains(toExclude) ? 1 : 0));
		dos.write(getName().length());
		dos.write(getName().getBytes());
	}
	
	public void sendBinaryServerChannelMessage(int subchannel, byte[] data) throws IOException, LacewingException
	{
		for(ServerClient c : this.getClients())
			ServerWritePacket.writePacketToStream(c.getOutputStream(), new ServerWritePacket4BinaryServerChannelMessage(subchannel, this.getID(), data));
	}
	
	public String toString()
	{
		return "ServerChannel { name: \"" + this.getName() + "\"" + (this.getID() == -1 ? "" : (" id: " + this.getID())) + " clients: " + clients.size() + " }";
	}
	
	public static ServerChannel getChannel(LacewingServer ls, String name)
	{
		if(!ls.globalChannelList.containsKey(name))
			ls.globalChannelList.put(name, new ServerChannel(ls, name));
		
		return ls.globalChannelList.get(name);
	}
	
	public static ServerChannel getChannel(LacewingServer ls, int id)
	{
		return ls.globalChannelsByID.get(id);
	}
}
