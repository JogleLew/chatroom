package com.jogle.chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;

public class ChatClientSurface extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Socket socket;
	private int cid;
	private String username;
	private DataInputStream istream;
	private DataOutputStream ostream;
	private ArrayList<PeerInfo> list;
	private JTextField inputField;
	private JTextArea messageField;
	private JButton sendButton;
	private JList peerList;
	private JLabel userProfile;
	private DefaultListModel defaultListModel;
	private int index;
	private String message;
	
	public ChatClientSurface(Socket s, String n) {
		super();
		socket = s;
		try {
			istream = new DataInputStream(socket.getInputStream());
			ostream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "连接服务器失败，请检查网络", "提示", JOptionPane.ERROR_MESSAGE);
			return;
		}
		cid = -1;
		username = n;
		list = new ArrayList<PeerInfo>();
		BackgroundThread t = new BackgroundThread();
		t.start();
		this.setSize(700, 600);
		this.getContentPane().setLayout(null);
		
		JLabel label1 = new JLabel();
		label1.setBounds(23, 15, 100, 25);
		label1.setText("聊天内容");
		this.add(label1);
		
		messageField = new JTextArea();
		messageField.setLineWrap(true);
		messageField.setEditable(false);
		
		JScrollPane scroll = new JScrollPane(messageField);
		scroll.setHorizontalScrollBarPolicy( 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
				scroll.setVerticalScrollBarPolicy( 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		scroll.setBounds(20, 45, 450, 385);
		this.add(scroll, null);
		
		JLabel label2 = new JLabel();
		label2.setBounds(483, 15, 100, 25);
		label2.setText("聊天对象");
		this.add(label2);
		
		peerList = new JList();
		peerList.setBounds(483, 45, 200, 385);
		peerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.add(peerList);
		
		JLabel label3 = new JLabel();
		label3.setBounds(23, 445, 200, 25);
		label3.setText("要发送的消息");
		this.add(label3);
		
		inputField = new JTextField();
		inputField.setBounds(20, 480, 450, 40);
		this.add(inputField, null);
		
		sendButton = new JButton();
		sendButton.setBounds(483, 480, 200, 40);
		sendButton.setText("发 送");
		sendButton.addActionListener(this);
		this.add(sendButton, null);
		
		userProfile = new JLabel();
		userProfile.setBounds(23, 520, 600, 40);
		userProfile.setText("您的用户名为：请等待……");
		this.add(userProfile, null);
	}
	
	public String readFromStream() throws IOException{
		String s = istream.readUTF();
		return ChatEncoder.decode(s);
	}
	
	public void writeToStream(String s) throws IOException{
		String news = ChatEncoder.encode(s);
		ostream.writeUTF(news);
	}
	
	class BackgroundThread extends Thread {
		public BackgroundThread() {
			try {
				writeToStream("USER_NAME|" + username);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "连接服务器失败，请检查网络", "提示", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			String s = "";
			while (true) {
				try {
					s = readFromStream();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "连接服务器失败，请检查网络", "提示", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return;
				}
				if (s.startsWith("USER_ID|")) {
					cid = Integer.parseInt(s.substring(8));
					SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                        	userProfile.setText("您的用户名为：" + username + "#" + cid);
                        }
                    });
				}
				else if (s.startsWith("USERS_LIST|")) {
					String listString = s.substring(11);
					String[] clientString = listString.split(",");
					for (int i = 0; i < clientString.length; i++) {
						String[] sp = clientString[i].split(":");
						int ncid = Integer.parseInt(sp[0]);
						PeerInfo p = new PeerInfo(ncid, sp[1]);
						if (ncid != cid)
							list.add(p);
					}
					defaultListModel = new DefaultListModel();
					for (int i = 0; i < list.size(); i++)
						defaultListModel.add(i, list.get(i).getName() + "#" + list.get(i).getCid());
					SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                        	peerList.setModel(defaultListModel);
                        }
                    });
				}
				else if (s.startsWith("USER_JOIN|")) {
					String userString = s.substring(10);
					String[] sp = userString.split(":");
					int cid = Integer.parseInt(sp[0]);
					final PeerInfo p = new PeerInfo(cid, sp[1]);
					list.add(p);
					SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                        	defaultListModel.add(list.size() - 1, p.getName() + "#" + p.getCid());
                        }
                    });
				}
				else if (s.startsWith("USER_EXIT|")) {
					String userString = s.substring(10);
					String[] sp = userString.split(":");
					int cid = Integer.parseInt(sp[0]);
					index = -1;
					for (int i = 0; i < list.size(); i++)
						if (list.get(i).getCid() == cid) {
							index = i;
							break;
						}
					if (index >= 0) {
						SwingUtilities.invokeLater(new Runnable(){
	                        @Override
	                        public void run() {
	                        	defaultListModel.remove(index);
	                        }
	                    });
						list.remove(index);
					}
				}
				else if (s.startsWith("MSG_RCV")) {
					message = "";
					int senderId = Integer.parseInt(s.split("\\|")[0].split(":")[1]); 
					String senderInfo = "";
					for (int i = 0; i < list.size(); i++)
						if (list.get(i).getCid() == senderId) {
							senderInfo = list.get(i).getName() + "#" + list.get(i).getCid();
							break;
						}
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					message = senderInfo + " (" + df.format(new Date()) + "):\n" + s.substring(s.split("\\|")[0].length() + 1) + "\n\n";
					
					SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                        	messageField.setText(messageField.getText() + message);
                        }
                    });
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int[] p = peerList.getSelectedIndices();
		if (p == null || p.length == 0) {
			JOptionPane.showMessageDialog(null, "请选择一个或多个聊天对象", "提示", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String s = "" + list.get(p[0]).getCid();
		for (int i = 1; i < p.length; i++)
			s += "," + list.get(p[i]).getCid();
		try {
			writeToStream("MSG_SEND:" + s + "|" + inputField.getText());
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "消息发送失败，请检查网络", "提示", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		message = username + "#" + cid + " (" + df.format(new Date()) + "):\n" + inputField.getText() + "\n\n";
		messageField.setText(messageField.getText() + message);
		
		inputField.setText("");
	}
}
