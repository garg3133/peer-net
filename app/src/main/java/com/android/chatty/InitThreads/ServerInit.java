package com.android.chatty.InitThreads;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

import com.android.chatty.MainActivity;

public class ServerInit extends Thread{
	private static final String TAG = "ServerInit";
	private static int SERVER_PORT;
	public static ArrayList<InetAddress> clients;
	private ServerSocket serverSocket;
	public ServerInit() {
		clients = new ArrayList<InetAddress>();
		try {
			serverSocket = new ServerSocket(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SERVER_PORT = serverSocket.getLocalPort();
	}

	public int getServerPort() {
		return SERVER_PORT;
	}

	@Override
	public void run() {
		clients.clear();

		try {
//			serverSocket = new ServerSocket(SERVER_PORT);
			// Collect client ip's
		    while(true) {
		       Socket clientSocket = serverSocket.accept();
		       if(!clients.contains(clientSocket.getInetAddress())){
		    	   clients.add(clientSocket.getInetAddress());
//		    	   Log.v(TAG, "New client: " + clientSocket.getInetAddress().getHostAddress());
		       }

		       clientSocket.close();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void interrupt() {
		super.interrupt();
		try {
			serverSocket.close();
			Log.v(TAG, "Server init process interrupted");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
