package com.jogle.chat;

public class PeerInfo {
	public int cid;
	public String name;
	
	public PeerInfo(int i, String n) {
		cid = i;
		name = n;
	}

	public int getCid() {
		return cid;
	}

	public String getName() {
		return name;
	}
}
