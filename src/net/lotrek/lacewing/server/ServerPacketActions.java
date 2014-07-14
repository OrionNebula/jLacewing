package net.lotrek.lacewing.server;

import net.lotrek.lacewing.server.structure.ServerChannel;
import net.lotrek.lacewing.server.structure.ServerClient;

public interface ServerPacketActions
{
	public boolean onConnectRequest(ServerClient client);
	public String onSetNameRequest(ServerClient client, String newName);
	public String onJoinChannelRequest(ServerClient client, boolean hide, boolean close, String channelName);
	public boolean onLeaveChannelRequest(ServerClient client, ServerChannel channel);
	public boolean onChannelListRequest(ServerClient client);
	
	public void onBinaryServerMessage(ServerClient client, int subchannel, byte[] message);
	public void onBinaryChannelMessage(ServerClient client, ServerChannel channel, int subchannel, byte[] message);
	public void onBinaryPeerMessage(ServerClient from, ServerClient to, ServerChannel channel, int subchannel, byte[] message);
	
	public void onObjectServerMessage(ServerClient client, int subchannel, String json);
	public void onObjectChannelMessage(ServerClient client, ServerChannel channel, int subchannel, String json);
	public void onObjectPeerMessage(ServerClient from, ServerClient to, ServerChannel channel, int subchannel, String json);
	
	public boolean onKickPeerRequest(ServerClient client, ServerChannel channel, ServerClient toKick);
}
