package net.lotrek.lacewing.client;

import net.lotrek.lacewing.client.structure.ClientChannel;
import net.lotrek.lacewing.client.structure.ClientPeer;

public interface ClientPacketActions
{
	public void onConnect(boolean success, String welcomeMessage);
	public void onChannelLeft(boolean success, ClientChannel channel);
	
	public void onBinaryServerMessage(int subchannel, byte[] message);
	public void onBinaryChannelMessage(int subChannel, ClientChannel channel, ClientPeer peer, byte[] message);
	public void onBinaryPeerMessage(int subchannel, ClientChannel channel, ClientPeer peer, byte[] message);
	public void onBinaryServerChannelMessage(int subchannel, ClientChannel channel, byte[] message);
	
	public void onObjectServerMessage(int subchannel, String json);
	public void onObjectChannelMessage(int subChannel, ClientChannel channel, ClientPeer peer,String json);
	public void onObjectPeerMessage(int subchannel, ClientChannel channel, ClientPeer peer, String json);
	public void onObjectServerChannelMessage(int subchannel, ClientChannel channel, String json);
	
	public void onPeerJoin(ClientChannel channel, ClientPeer peer);
	public void onPeerChanged(ClientChannel channel, ClientPeer peer, String newName, boolean isNowChannelMaster);
	public void onPeerLeft(ClientChannel channel, ClientPeer peer);
}
