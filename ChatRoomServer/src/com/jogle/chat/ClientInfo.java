package com.jogle.chat;

import java.net.Socket;

public class ClientInfo {
	private int cid;
	private String name;
	private Socket socket;
	
	public ClientInfo(int i, Socket s) {
		cid = i;
		name = "";
		socket = s;
	}

	public int getCid() {
		return cid;
	}

	public String getName() {
		return name;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setName(String name) {
		this.name = name;
	}
}
