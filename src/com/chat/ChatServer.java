package com.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
	private static final int SERVER_PORT = 6066;
	private static ArrayList<Socket> sClientSockets = null;
	static int mCurrentId = 1;
	static int[][] mGrid = new int[3][3];
	static int totalMarks=0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AcceptConnectionsThread acceptConnectionsThread = new AcceptConnectionsThread();
		acceptConnectionsThread.start();
	}

	private static class AcceptConnectionsThread extends Thread {
		
		static int counter = 0;
		
		@Override
		public void run() {
			super.run();
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(SERVER_PORT);
				while (true) {
					System.out.println("Waiting to accept client connection @"
							+ SERVER_PORT);
					Socket clientSocket = serverSocket.accept();
					if (sClientSockets == null)
						sClientSockets = new ArrayList<>();
					sClientSockets.add(clientSocket);
					ClientHandler clientHandler = new ClientHandler(
							clientSocket,counter++);
					clientHandler.start();
					System.out.println("Client connected @" + SERVER_PORT
							+ " and client socket port = "
							+ clientSocket.getPort());

				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}

	private static class ClientHandler extends Thread {
		private Socket mClientSocket = null;
		private PrintWriter out;
		private BufferedReader in;
		private int id;

		public ClientHandler(Socket clientSocket,int id) {
			mClientSocket = clientSocket;
			this.id = id;
		}

		@Override
		public void run() {
			super.run();
			setupStreams();
		}

		private void setupStreams() {
			try {
				InputStream is = mClientSocket.getInputStream();
				OutputStream os = mClientSocket.getOutputStream();
				out = new PrintWriter(os, true);
				in = new BufferedReader(new InputStreamReader(is));
				out.println("Hi "
						+ mClientSocket.getInetAddress().getHostName() + "!!");
				String inputLine = null;
				if (!in.ready()) {
					System.out.println("Input not ready!!");
				}
				while ((inputLine = in.readLine()) != null) {
					System.out.println("Client" + mClientSocket + " says: "
							+ inputLine);
					String[] words = inputLine.split(" ");
					String command = words[0];
					if (command.contains(COMMANDS.GET_USERS)) {
						out.println(sClientSockets.size());
						for (int i = 0; i < sClientSockets.size(); i++) {
							out.println(sClientSockets.get(i).getInetAddress()
									.getHostAddress()
									+ "_" + sClientSockets.get(i).getPort());
						}
					} else if (command.contains(COMMANDS.SEND_MESSAGE) && id == ChatServer.mCurrentId) {
						if (words.length >= 4) {
							
							int x = Integer.valueOf(words[0]);
							int y = Integer.valueOf(words[1]);
                            
							ChatServer.mGrid[x][y] = id;
							ChatServer.mCurrentId = (ChatServer.mCurrentId+1)%2;
							ChatServer.totalMarks++;
							
							boolean hasWon = checkForWinner(x,y,id);
							// send message to the correct recipient.
							for (Socket s : sClientSockets) {
									PrintWriter pw = new PrintWriter(
											s.getOutputStream(), true);
									if(hasWon) {
										pw.println(mClientSocket+ " says: "+ id + " Won!!");
									}
									else if(ChatServer.totalMarks >= 9) {
										pw.println(mClientSocket+ " says: "+ x + " "+ y + " No body won the game!! ");
									}
									else {
										pw.println(mClientSocket+ " says: "+ x + " "+ y);
									}
							}
							
							
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				out.close();
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private boolean checkForWinner(int x,int y,int id) {
			
			boolean flag = true;
			
			for(int j=0;j < 3; j++ ) {
				if(ChatServer.mGrid[x][j]!=id) {
					flag = false;
					break;
				}
			}
			
			if(flag)return true;
			
			flag=true;
			
			for(int j=0;j < 3; j++ ) {
				if(ChatServer.mGrid[j][y]!=id) {
					flag=false;
					break;
				}
			}
			
			if(flag)
				return true;
			
			return false;
		}
	}

	public static class COMMANDS {
		public static final String GET_USERS = "get_users";
		public static final String SEND_MESSAGE = "send_message";
	}

}
