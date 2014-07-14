package net.lotrek.lacewing;

import java.util.Scanner;

import net.lotrek.lacewing.client.ClientPacketActions;
import net.lotrek.lacewing.client.LacewingClient;
import net.lotrek.lacewing.client.structure.ClientChannel;
import net.lotrek.lacewing.client.structure.ClientPeer;
import net.lotrek.lacewing.server.LacewingServer;
import net.lotrek.lacewing.server.ServerPacketActions;
import net.lotrek.lacewing.server.structure.ServerChannel;
import net.lotrek.lacewing.server.structure.ServerClient;

public class LacewingTest
{
	public static void main(String[] args) throws LacewingException, InterruptedException
	{
		LacewingServer ls = new LacewingServer();
		ls.setPacketActions(new ServerPacketActions() {
			
			public String onSetNameRequest(ServerClient client, String newName)
			{
				System.err.println(client + " changed their name to \"" + newName + "\"");
				return newName;
			}
			
			public boolean onLeaveChannelRequest(ServerClient client, ServerChannel channel)
			{
				System.err.println(client + " left channel " + channel);
				return true;
			}
			
			public String onJoinChannelRequest(ServerClient client, boolean hide, boolean close, String channelName)
			{
				System.err.println(client + " joined channel " + channelName);
				return channelName;
			}
			
			public boolean onConnectRequest(ServerClient client)
			{
				System.err.println("Connection established from " + client.getAddress());
				return true;
			}
			
			public boolean onChannelListRequest(ServerClient client)
			{
				System.err.println(client + " requested the channel listing");
				return true;
			}
			
			public void onBinaryServerMessage(ServerClient client, int subchannel, byte[] message)
			{
				System.err.println(client + " sent the server \"" + new String(message) + "\" on subchannel " + subchannel);
			}
			
			public void onBinaryPeerMessage(ServerClient from, ServerClient to,	ServerChannel channel, int subchannel, byte[] message)
			{
				System.err.println(from + " sent " + to + " \"" + new String(message) + "\" on channel " + channel + " on subchannel " + subchannel);
			}
			
			public void onBinaryChannelMessage(ServerClient client,	ServerChannel channel, int subchannel, byte[] message)
			{
				System.err.println(client + " sent " + channel + " \"" + new String(message) + "\" on subchannel " + subchannel);
			}

			public void onObjectServerMessage(ServerClient client, int subchannel, String json)
			{
				
			}

			public void onObjectChannelMessage(ServerClient client, ServerChannel channel, int subchannel, String json)
			{
				
			}

			public void onObjectPeerMessage(ServerClient from, ServerClient to, ServerChannel channel, int subchannel, String json)
			{
				
			}

			public boolean onKickPeerRequest(ServerClient client, ServerChannel channel, ServerClient toKick)
			{
				System.err.println(client + " is attempting to kick " + toKick);
				return true;
			}
		});
		
		LacewingClient lc = new LacewingClient("localhost");
		ClientPacketActions actions = new ClientPacketActions() {
			
			public void onPeerLeft(ClientChannel channel, ClientPeer peer)
			{
				System.out.println(peer + "left " + channel);
			}
			
			public void onPeerJoin(ClientChannel channel, ClientPeer peer)
			{
				System.out.println(peer + " joined " + channel);
			}
			
			public void onPeerChanged(ClientChannel channel, ClientPeer peer, String newName, boolean isNowChannelMaster)
			{
				System.out.println(peer + " on channel " + channel + " changed their name to " + newName);
			}
			
			public void onObjectServerMessage(int subchannel, String json){}
			public void onObjectServerChannelMessage(int subchannel, ClientChannel channel, String json) {}
			public void onObjectPeerMessage(int subchannel, ClientChannel channel, ClientPeer peer, String json){}
			public void onObjectChannelMessage(int subChannel, ClientChannel channel, ClientPeer peer, String json) {}
			
			public void onConnect(boolean success, String welcomeMessage)
			{
				
			}
			
			public void onChannelLeft(boolean success, ClientChannel channel)
			{
				
			}
			
			public void onBinaryServerMessage(int subchannel, byte[] message)
			{
				
			}
			
			public void onBinaryServerChannelMessage(int subchannel, ClientChannel channel, byte[] message)
			{
				
			}
			
			public void onBinaryPeerMessage(int subchannel, ClientChannel channel, ClientPeer peer, byte[] message)
			{
				
			}
			
			public void onBinaryChannelMessage(int subChannel, ClientChannel channel, ClientPeer peer, byte[] message)
			{
				
			}
		};
		lc.setPacketActions(actions);
		
		lc.pair();
		lc.setName("jLacewing");
		ClientChannel.getChannel(lc, "jLacewing").joinChannel(lc, false, false);
		
		LacewingClient dc = new LacewingClient("localhost");
		dc.setPacketActions(actions);
		dc.pair();
		dc.setName("dc");
		ClientChannel.getChannel(dc, "jLacewing").joinChannel(dc, false, false);
		
		ClientChannel.getChannel(lc, "jLacewing").kickPeer(lc, ClientChannel.getChannel(lc, "jLacewing").getPeers()[0]);
		
		Thread.sleep(250);
		
		lc.disconnect();
		dc.disconnect();
		ls.stopServer();
	}

	public static void pylacewingExample() throws LacewingException
	{
		Scanner in = new Scanner(System.in);
		System.out.print("Host: ");
		String host = in.nextLine();
		System.out.print("Port: ");
		int port = Integer.parseInt(in.nextLine());
		System.out.print("Name: ");
		String name = in.nextLine();
		
		LacewingClient lc = new LacewingClient(host, port);
		System.out.println("MOTD: " + lc.pair());
		System.out.println("Connection accepted!");
		lc.setName(name);
		System.out.println("Logged in, " + lc.getName());
		System.out.println("* ChannelList *");
		ClientChannel[] chs = lc.getChannelList();
		for(ClientChannel ch : chs)
			System.out.println("- " + ch.getName() + " (" + ch.getPeers().length + " users)");
		System.out.println("* End of ChannelList *");
		
		System.out.print("Channel: ");
		ClientChannel ch = ClientChannel.getChannel(lc, in.nextLine());
		ch.joinChannel(lc, false, false);
		System.out.println(ch.isConnected() ? "Signed on!" : "Error!");
		
		System.out.print("Message: ");
		lc.sendBinaryServerMessage(0, in.nextLine().getBytes());
		System.out.println("(sending to Server)");
		
		System.out.print("Channel Message: ");
		ch.sendBinaryMessage(lc, 0, in.nextLine().getBytes());
		System.out.println("(sending to \"" + ch.getName() + "\")");
		
		System.out.print("New Username: ");
		lc.setName(in.nextLine());
		System.out.println("Changed name to " + lc.getName() + "!");
		
		ch.leaveChannel(lc);
		System.out.println("Signed off!");
		
		lc.disconnect();
		in.close();
		System.out.println("End of example");
	}
}
