package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;

public class ServerWritePacket0Response0Connect extends ServerWritePacket
{
	private String motd;
	private int clientID;
	private boolean success;
	
	public ServerWritePacket0Response0Connect(String motd, int clientID, boolean success)
	{
		this.motd = motd;
		this.clientID = clientID;
		this.success = success;
	}
	
	public int getPacketLength()
	{
		return motd.length() + 4;
	}

	public int getPacketType()
	{
		return 0;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(0);
			dos.write(success ? 1 : 0);
			DataTools.writeInversedShort(dos, clientID);
			dos.write(motd.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
