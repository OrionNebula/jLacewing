package net.lotrek.lacewing.server.packet;

import java.io.DataOutputStream;
import java.io.IOException;

import net.lotrek.lacewing.DataTools;
import net.lotrek.lacewing.server.structure.ServerChannel;
import net.lotrek.lacewing.server.structure.ServerClient;

public class ServerWritePacket9Peer extends ServerWritePacket
{
	private ServerClient peer;
	private int channel, peerID = -1;
	
	public ServerWritePacket9Peer(ServerClient peer, int channel)
	{
		this.peer = peer;
		this.channel = channel;
	}
	
	public ServerWritePacket9Peer(int peer, int channel)
	{
		this.peerID = peer;
		this.channel = channel;
	}
	
	public int getPacketLength()
	{
		return peerID != -1 ? 4 : 5 + peer.getName().length();
	}

	public int getPacketType()
	{
		return 9;
	}

	public void writePacketData(DataOutputStream dos)
	{
		try {
			if(peerID == -1)
			{
				DataTools.writeInversedShort(dos, channel);
				DataTools.writeInversedShort(dos, peer.getID());
				dos.write(ServerChannel.getChannel(peer.getPacketHandler().getServer(), channel).getChannelMaster() == peer ? 1 : 0);
				dos.write(peer.getName().getBytes());
			}else
			{
				DataTools.writeInversedShort(dos, channel);
				DataTools.writeInversedShort(dos, peerID);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
