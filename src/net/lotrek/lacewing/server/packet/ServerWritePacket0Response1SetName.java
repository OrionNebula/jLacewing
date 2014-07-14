package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public class ServerWritePacket0Response1SetName extends ServerWritePacket
{
	private String name;
	private boolean success;
	
	public ServerWritePacket0Response1SetName(String name, boolean success)
	{
		this.name = name;
		this.success = success;
	}
	
	public int getPacketLength()
	{
		return name.length() + 3;
	}

	public int getPacketType()
	{
		return 0;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(1);
			dos.write(success ? 1 : 0);
			dos.write(name.length());
			dos.write(name.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
