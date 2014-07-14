package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.server.structure.ServerChannel;
import net.lotrek.lacewing.server.structure.ServerClient;

public class ServerWritePacket0Response2JoinChannel extends ServerWritePacket
{
	private String name;
	private ServerChannel channel;
	private boolean success, isMaster;
	private ServerClient toExclude;
	
	public ServerWritePacket0Response2JoinChannel(String name)
	{
		this.name = name;
		this.success = false;
	}
	
	public ServerWritePacket0Response2JoinChannel(ServerChannel channel, boolean isMaster, ServerClient toExclude)
	{
		this.channel = channel;
		this.isMaster = isMaster;
		this.toExclude = toExclude;
		this.success = true;
	}
	
	public int getPacketLength()
	{
		if(success)
		{
			int clientLength = 0;
			for(ServerClient c : channel.getClients())
				if(c != toExclude)
					clientLength += c.getLengthInJoinChannel();
			
			return 6 + channel.getName().length() + clientLength;
		}else return (3 + name.length());
	}

	public int getPacketType()
	{
		return 0;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			dos.write(2);
			dos.write(success ? 1 : 0);
			if(success)
			{
				dos.write(isMaster ? 1 : 0);
				dos.write(channel.getName().length());
				dos.write(channel.getName().getBytes());
				DataTools.writeInversedShort(dos, channel.getID());
				for(ServerClient c : channel.getClients())
					if(c != toExclude)
						c.writeToJoinChannel(dos, channel);
			}else
			{
				dos.write(name.length());
				dos.write(name.getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
