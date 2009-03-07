/**
 * Copyright (c) 2009, Coral Reef Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of the Coral Reef Project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package pl.graniec.coralreef.network.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import pl.graniec.coralreef.network.Network;
import pl.graniec.coralreef.network.packets.ConnectPacketData;
import pl.graniec.coralreef.network.packets.ControllPacketData;
import pl.graniec.coralreef.network.packets.DisconnectPacketData;
import pl.graniec.coralreef.network.packets.HeaderData;
import pl.graniec.coralreef.network.packets.Packet;
import pl.graniec.coralreef.network.packets.PacketData;
import pl.graniec.coralreef.network.packets.PassportAssignData;
import pl.graniec.coralreef.network.packets.PongPacketData;
import pl.graniec.coralreef.network.packets.UserData;

/**
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public class Server {
	
	/** Thread that receives all packets */
	private class ReceiverThread extends Thread {
		
		private final static int BUFFER_SIZE = 1024 * 32;
		
		@Override
		public void run() {
			try {
				Packet packet;
				DatagramPacket datagramPacket;
				byte[] buffer = new byte[BUFFER_SIZE];
				
				while (!isInterrupted()) {
				
					datagramPacket = new DatagramPacket(buffer, BUFFER_SIZE);
					socket.receive(datagramPacket);
					
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
					BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);
					ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
					
					// decode object
					byteArrayInputStream.reset();
					PacketData packetData = (PacketData)objectInputStream.readObject();
					
					// build a packet object and handle this packet
					packet = new Packet(
							datagramPacket.getAddress().getHostAddress(),
							datagramPacket.getPort(),
							packetData
					);
					
					handlePacket(packet);
				}
				
			} catch (SocketException e) {
				if (socket.isClosed()) {
					return;
				}
				
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private final static Logger logger = Logger.getLogger(Server.class.getName());
	
	/** The socket */
	private DatagramSocket socket;
	/** Port to work on */
	private final int port;
	/** Host to work on */
	private final InetAddress host;
	
	/** Receiver thread */
	private ReceiverThread receiverThread;
	
	/** Server clients */
	private Map<Integer, RemoteClient> clients = new HashMap<Integer, RemoteClient>();
	
	/** Connection/disconnection listeners */
	private List<ConnectionListener> connectionListeners = new LinkedList<ConnectionListener>();
	
	/**
	 * Creates a new server that will bind on given host and port number.
	 * <p>
	 * Valid port number is a value between 0 and 65535 inclusively. If you pass
	 * <code>0</code> as port number then a ephemeral port will be chosen by
	 * operating system during the {@link #start()} method invocation. After then
	 * you can use {@link #getPort()} to determine which port has been used.
	 * @throws SocketException 
	 * @throws UnknownHostException 
	 * 
	 */
	public Server(String host, int port) throws SocketException, UnknownHostException {
		
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("values cannot be empty");
		}
		
		if (!Network.isPortValid(port)) {
			throw new IllegalArgumentException("number " + port + " is not valid network port value");
		}

		
		this.host = InetAddress.getByName(host);
		this.port = port;
		
	}
	
	public void addConnectionListener(ConnectionListener l) {
		synchronized (connectionListeners) {
			connectionListeners.add(l);
		}
	}
	
	private void addNewClient(String host, int port, int passport) {
		
		RemoteClient client = new RemoteClient(this, host, port, passport);
		
		synchronized (clients) {
			clients.put(passport, client);
		}
		
		notifyClientConnected(client);
	}
	
	private int createNewPassport() {
		int passport;
		
		do {
			passport = (int)(Math.random() * Integer.MAX_VALUE);
		} while (isPassportInUse(passport));
		
		return passport;
	}
	
	/**
	 * Provides a port on which the server is/would be running.
	 * <p>
	 * Keep on mind that when you're used <code>0</code> as port then you can receive
	 * your ephemeral port only after {@link #start()} method call. Before then
	 * the <code>0</code> value will be returned.
	 * 
	 * @return the port
	 */
	public int getPort() {
		if (!isRunning()) {
			return port;
		} else {
			return socket.getLocalPort();
		}
	}
	
	/**
	 * @param data
	 */
	private void handleConnectPacket(Packet packet) {
		final int passport = createNewPassport();
		
		// send back passport info
		try {
			
			final PacketData answer = new PassportAssignData(passport);
			send(answer, packet.getSenderHost(), packet.getSenderPort());
			
			addNewClient(packet.getSenderHost(), packet.getSenderPort(), passport);
			
		} catch (IOException e) {
			logger.warning("cannot send back passport to client: " + e);
		}
	}
	
	/**
	 * @param packet
	 */
	private void handleControllPacket(Packet packet) {
		final PacketData data = packet.getData();
		
		if (data instanceof ConnectPacketData) {
			handleConnectPacket(packet);
		} else
		if (data instanceof DisconnectPacketData) {
			handleDisconnectPacket(packet);
		} else
		if (data instanceof PongPacketData) {
			handlePongPacket(packet);
		} else {
			logger.info("unexpected controll packet income: " + packet.getData().getClass());
		}
		
	}
	
	/**
	 * @param packet
	 */
	private void handlePongPacket(Packet packet) {
		//FIXME: implement me!
	}

	/**
	 * @param packet
	 */
	private void handleDisconnectPacket(Packet packet) {
		
	}

	private void handlePacket(Packet packet) {
		
		/*
		 * Packets falls into two groups:
		 * 1) Special control packets
		 * 2) User custom packets.
		 * 
		 * The first one is used only by server to connect, disconnect,
		 * examine and keep alive server clients. User programmer doesn't
		 * need to be aware of this packets at all.
		 * 
		 * The second one are packets that are only user defined data
		 * packets. Its is group into UserData object that held HeaderData
		 * object and actual user data.
		 */
		
		final PacketData data = packet.getData();
		
		if (data instanceof ControllPacketData) {
			handleControllPacket(packet);
		} else
		if (data instanceof UserData){
			handleUserDataPacket(packet);
		} else {
			logger.severe("unknown top-level packet data class: " + data.getClass().getName());
		}
	}
	
	/**
	 * @param packet
	 */
	private void handleUserDataPacket(Packet packet) {
		/*
		 * Regular packets are build from two parts like this:
		 * 
		 *  ____________________
		 * |        |           |
		 * | HEADER | USER DATA |
		 * |________|___________|
		 * 
		 * 
		 * Header contains important information that helps to
		 * identify sender (passport). After this there is a
		 * data send by user that server should handle externally
		 * ( by packet handling event ).
		 */
		
		final HeaderData header = ((UserData)packet.getData()).getHeader();
		final PacketData body   = ((UserData)packet.getData()).getBody();
		
		RemoteClient client;
		
		synchronized (clients) {
			// check passport correctness
			client = clients.get(header.getPassport());
			
			if (client == null) {
				logger.warning("received packet " + packet + " from unknown client");
				return;
			}
			
		}
		
		// notify client about new data
		client.notifyPacketReceived(body);
		
	}

	/**
	 * @param result
	 * @return
	 */
	private boolean isPassportInUse(int result) {
		synchronized (clients) {
			return clients.containsKey(result);
		}
	}

	public boolean isRunning() {
		return socket != null && !socket.isClosed();
	}

	/**
	 * @param client
	 */
	private void notifyClientConnected(RemoteClient client) {
		
		// make listeners copy
		ConnectionListener[] copy;
		
		synchronized (connectionListeners) {
			copy = new ConnectionListener[connectionListeners.size()];
			connectionListeners.toArray(copy);
		}
		
		// notify all
		for (ConnectionListener l : copy) {
			l.clientConnected(client);
		}
	}
	
	private byte[] packetDataToByteArray(PacketData data) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		
		objectOutputStream.writeObject(data);
		
		objectOutputStream.close();
		byteArrayOutputStream.close();
		
		return byteArrayOutputStream.toByteArray();
	}
	
	public boolean removeConnectionListener(ConnectionListener l) {
		synchronized (connectionListeners) {
			return connectionListeners.remove(l);
		}
	}
	
	protected void send(PacketData data, String host, int port) throws IOException {
		
		if (data == null || host == null || host.isEmpty()) {
			throw new IllegalArgumentException("cannot take null/empty values");
		}
		
		if (!Network.isPortValid(port)) {
			throw new IllegalArgumentException("number " + port + " is not valid port number");
		}
		
		if (!isRunning()) {
			throw new IllegalStateException("server is not running");
		}

		// construct packet (for sending)
		byte[] buffer = packetDataToByteArray(data);
		
		DatagramPacket packet = new DatagramPacket(
				buffer,
				buffer.length,
				InetAddress.getByName(host),
				port
		);
		
		// send it forward
		socket.send(packet);
	}

	/**
	 * Starts the server on local machine using previously given port number.
	 * @throws SocketException
	 */
	public void start() throws SocketException {
		
		if (isRunning()) {
			throw new IllegalStateException("server is already running");
		}
		
		socket = new DatagramSocket(port, host);
		
		// running receiver thread
		receiverThread = new ReceiverThread();
		receiverThread.start();
	}

	public void stop() throws InterruptedException {
		if (!isRunning()) {
			throw new IllegalStateException("server is not running");
		}
		
		socket.close();
//		socket = null;
		
		receiverThread.interrupt();
		receiverThread.join();
	}
	
}
