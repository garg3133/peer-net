package com.android.chatty.InitThreads;

import com.android.chatty.MainActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientInit extends Thread{
	private int serverPort;
	private InetAddress mServerAddr;
	public ClientInit(InetAddress serverAddr, int port){
		mServerAddr = serverAddr;
		serverPort = port;
	}

	@Override
	public void run() {
		Socket socket = new Socket();
		try {
			socket.bind(null);
			socket.connect(new InetSocketAddress(mServerAddr, serverPort),500);
//			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void interrupt() {
		super.interrupt();

	}
}
