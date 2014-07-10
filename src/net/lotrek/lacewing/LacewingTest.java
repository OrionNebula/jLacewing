package net.lotrek.lacewing;

import java.util.Scanner;

import net.lotrek.lacewing.client.LacewingClient;
import net.lotrek.lacewing.client.structure.Channel;

public class LacewingTest
{
	public static void main(String[] args) throws LacewingException
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
		Channel[] chs = lc.getChannelList();
		for(Channel ch : chs)
			System.out.println("- " + ch.getName() + " (" + ch.getPeers().length + " users)");
		System.out.println("* End of ChannelList *");
		
		System.out.print("Channel: ");
		Channel ch = Channel.getChannel(lc, in.nextLine());
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
