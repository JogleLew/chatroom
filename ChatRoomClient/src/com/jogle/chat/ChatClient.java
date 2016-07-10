package com.jogle.chat;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

import javax.swing.*;

public class ChatClient extends JFrame {
	private static final long serialVersionUID = 1L;
	private JButton submitButton;
	private JTextField hostBox;
    private JTextField usernameBox;
    private ChatClient self;
    private Socket socket;
    
	public ChatClient() {
		super();
		self = this;
		this.setSize(300, 300);
		this.getContentPane().setLayout(null);
		
		JLabel title = new JLabel();
		title.setBounds(110, 20, 100, 40);
		title.setText("聊天室");
		title.setFont(new Font(Font.DIALOG, Font.BOLD, 25));
		this.add(title,null);
		
		JLabel label = new JLabel();
		label.setBounds(20, 70, 200, 18);
		label.setText("要连接到的服务器IP地址：");
		this.add(label,null);
		
		hostBox = new JTextField();
		hostBox.setBounds(20, 90, 260, 25);
		hostBox.setText("127.0.0.1");
		this.add(hostBox, null);
		
		JLabel label2 = new JLabel();
		label2.setBounds(20, 130, 200, 18);
		label2.setText("您的用户名：");
		this.add(label2,null);
		
		usernameBox = new JTextField();
		usernameBox.setBounds(20, 150, 260, 25);
		this.add(usernameBox, null);
		
		submitButton = new JButton();
		submitButton.setBounds(100, 200, 100, 25);
		submitButton.setText("连 接");
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new Socket(hostBox.getText(), 6666);
				} catch (UnknownHostException e1) {
					JOptionPane.showMessageDialog(null, "请输入一个正确的服务器IP地址", "提示", JOptionPane.ERROR_MESSAGE);
					return;
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "连接服务器失败，请检查网络", "提示", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (socket == null) {
					JOptionPane.showMessageDialog(null, "连接服务器失败，请检查网络", "提示", JOptionPane.ERROR_MESSAGE);
					return;
				}
				ChatClientSurface ccs = new ChatClientSurface(socket, usernameBox.getText());
				ccs.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				ccs.setResizable(false);
				ccs.setLocationRelativeTo(null);
				ccs.setVisible(true);
				self.dispose();
			}
			
		});
		this.add(submitButton, null);
	}
	
	public static void main(String[] args) {
		ChatClient cc = new ChatClient();
        cc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cc.setResizable(false);
        cc.setLocationRelativeTo(null);
        cc.setVisible(true);
	}
}
