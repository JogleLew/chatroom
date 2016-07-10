package com.jogle.chat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ChatServer extends Thread {
	private static int newCid;
	private static ArrayList<ClientInfo> clientList;
	private ClientInfo currentInfo;
	private Socket socket;
	private DataInputStream istream;
	private DataOutputStream ostream;
	
	public ChatServer(ClientInfo c) {
		currentInfo = c;
		socket = currentInfo.getSocket();
		try
		{
			istream = new DataInputStream(socket.getInputStream());
			ostream = new DataOutputStream(socket.getOutputStream());
		}
		catch(IOException e)
		{
			System.out.println("获取流信息失败。");
		}
	}
	
	public String readFromStream() throws IOException{
		String s = istream.readUTF();
		return ChatEncoder.decode(s);
	}

	public void writeToStream(String s) throws IOException{
		String news = ChatEncoder.encode(s);
		ostream.writeUTF(news);
	}
	
	public String getClientsInfo() {
		if (clientList == null || clientList.size() == 0)
			return "";
		String s = clientList.get(0).getCid() + ":" + clientList.get(0).getName();
		for (int i = 1; i < clientList.size(); i++) {
			s += "," + clientList.get(i).getCid() + ":" + clientList.get(i).getName();
		}
		return s;
	}
	
	@Override
	public void run() {
		while (true) {
			String s = null;
			try {
				s = readFromStream();
			} catch (Exception e) {
				System.out.println(currentInfo.getName() + "#" + currentInfo.getCid() + " Closed.");
				clientListOperation("remove", currentInfo);
				clientListOperation("notifyUserExit", currentInfo);
				return;
			}
			if (s.startsWith("USER_NAME|")) {
				currentInfo.setName(s.substring(10));
				try {
					clientListOperation("notifyNewUserJoin", currentInfo);
					writeToStream("USER_ID|" + currentInfo.getCid());
					writeToStream("USERS_LIST|" + getClientsInfo());
				} catch (IOException e) {
					System.out.println(currentInfo.getName() + "#" + currentInfo.getCid() + " Closed.");
					clientListOperation("remove", currentInfo);
					clientListOperation("notifyUserExit", currentInfo);
					return;
				}
			}
			else if (s.startsWith("MSG_SEND:")) {
				String idString = s.split("\\|")[0];
				idString = idString.split(":")[1];
				String[] idss = idString.split(",");
				for (int i = 0; i < idss.length; i++) {
					int ncid = Integer.parseInt(idss[i]);
					for (ClientInfo ci : clientList) {
						if (ci.getCid() == ncid) {
							try {
								String news = ChatEncoder.encode("MSG_RCV:" + currentInfo.getCid() + "|" + s.substring(10 + idString.length()));
								DataOutputStream out = new DataOutputStream(ci.getSocket().getOutputStream());
								out.writeUTF(news);
							} catch (Exception e) {
								System.out.println(currentInfo.getName() + "#" + currentInfo.getCid() + " Closed.");
								clientListOperation("remove", currentInfo);
								clientListOperation("notifyUserExit", currentInfo);
								return;
							}
						}
					}
				}
			}
			System.out.println(s);
		}
	}
	
	@SuppressWarnings("resource")
	public static void main(String args[]) {
		ServerSocket server = null;
		Socket socket = null;
		newCid = 1;
		clientList = new ArrayList<ClientInfo>();
		
		while (true) {
			try {
				server = new ServerSocket(6666);
			}
			catch(IOException e1) {
				// now listening
			} catch (Exception e2) {
				System.out.println("监听端口失败，服务端异常退出。");
				return;
			}
			
			try {
				socket = server.accept();
				InetAddress address = socket.getInetAddress();
				System.out.println("用户的IP："+address);
			}	
			catch (IOException e) {
				continue;
			}
			ClientInfo ci = new ClientInfo(newCid++, socket);
			clientList.add(ci);
			ChatServer serverThread = new ChatServer(ci);
			serverThread.start();
		}
	}

	private void notifyNewUserJoin(ClientInfo ci) {
		for (int i = 0; i < clientList.size(); i++) {
			if (clientList.get(i) == ci)
				continue;
			DataOutputStream out = null;
			try {
				out = new DataOutputStream(clientList.get(i).getSocket().getOutputStream());
			} catch (IOException e) {
				continue;
			}
			if (out == null)
				continue;
			String news = ChatEncoder.encode("USER_JOIN|" + ci.getCid() + ":" + ci.getName());
			try {
				out.writeUTF(news);
			} catch (IOException e) {
				continue;
			}
		}
	}
	
	private void notifyUserExit(ClientInfo ci) {
		for (int i = 0; i < clientList.size(); i++) {
			if (clientList.get(i) == ci)
				continue;
			DataOutputStream out = null;
			try {
				out = new DataOutputStream(clientList.get(i).getSocket().getOutputStream());
			} catch (IOException e) {
				continue;
			}
			if (out == null)
				continue;
			String news = ChatEncoder.encode("USER_EXIT|" + ci.getCid() + ":" + ci.getName());
			try {
				out.writeUTF(news);
			} catch (IOException e) {
				continue;
			}
		}
	}
	
	private synchronized void clientListOperation(String op, ClientInfo info) {
		if (op.equals("add")) {
			clientList.add(info);
		}
		else if (op.equals("remove")) {
			clientList.remove(info);
		}
		else if (op.equals("notifyNewUserJoin")) {
			notifyNewUserJoin(info);
		}
		else if (op.equals("notifyUserExit")) {
			notifyUserExit(info);
		}
	}
}
