package com.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
	private static final String sCleintServerHost = "192.168.1.6";
	private static Socket mSocket;
	private static PrintWriter cOut;
	private static BufferedReader cIn;
	private static Thread sReadThread, sWriteThread;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			mSocket = new Socket(sCleintServerHost, 6066);
			System.out.println("Connected to server " + sCleintServerHost
					+ " port @" + mSocket.getPort());
			setupStreams();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String describeUserCommands() {
		return "These are the commands available for now: \n(1)get_users \n(2)send_message \n(3)help"
				+ "\n (4)bye"
				+ "\n Usage: (A)get_users"
				+ "\n (B)send_message <host-name|host-address> <port> <message>\n"
				+ "\n (C)help"
				+ "\n Press enter after each command!"
				+ "\n (B)bye";
	}

	public static void setupStreams() {
		InputStream is;
		try {
			is = mSocket.getInputStream();
			OutputStream os = mSocket.getOutputStream();
			cOut = new PrintWriter(new OutputStreamWriter(os), true);
			cIn = new BufferedReader(new InputStreamReader(is));
			sReadThread = new Thread(new Runnable() {

				@Override
				public void run() {
					Scanner scanner = new Scanner(System.in);
					System.out.println(describeUserCommands());
					while (true) {
						System.out.println("User: ");
						String userInput = scanner.nextLine();
						if (userInput.equalsIgnoreCase("help"))
							System.out.println(describeUserCommands());
						else if (userInput.startsWith("bye")) {
							break;
						} else {
							cOut.println(userInput);
							System.out.println("Command sent: " + userInput);
						}
					}
					try {
						scanner.close();
						System.out.println("User closed connection!!");
						cOut.close();
						sWriteThread.interrupt();
						cIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			sWriteThread = new Thread(new Runnable() {

				@Override
				public void run() {
					String fromServer = null;
					try {
						while ((fromServer = cIn.readLine()) != null) {
							System.out.println("Server says: " + fromServer);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			sReadThread.start();
			sWriteThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
