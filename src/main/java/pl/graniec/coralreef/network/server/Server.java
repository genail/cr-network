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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import pl.graniec.coralreef.network.Network;
import pl.graniec.coralreef.network.PacketListener;
import pl.graniec.coralreef.network.packets.Packet;
import pl.graniec.coralreef.network.packets.PacketData;

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
				
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				
				while (!isInterrupted()) {
				
					datagramPacket = new DatagramPacket(buffer, BUFFER_SIZE);
					socket.receive(datagramPacket);
					
					// decode object
					byteArrayInputStream.reset();
					PacketData packetData = (PacketData)objectInputStream.readObject();
					
					// build a packet object and notify the listeners
					packet = new Packet(
							datagramPacket.getAddress().getHostAddress(),
							datagramPacket.getPort(),
							packetData
					);
					
					notifyPacketReceived(packet);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** The socket */
	private DatagramSocket socket;
	/** Port to work on */
	private final int port;
	/** Host to work on */
	private final InetAddress host;
	
	/** The packet listeners */
	private final List<PacketListener> packetListeners = new LinkedList<PacketListener>();
	
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
	
	public void addPacketListener(PacketListener l) {
		synchronized (packetListeners) {
			packetListeners.add(l);
		}
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
	
	public boolean isRunning() {
		return socket != null && socket.isBound();
	}
	
	private void notifyPacketReceived(Packet packet) {
		// make a copy of the list
		PacketListener[] copy;
		
		synchronized (packetListeners) {
			copy = new PacketListener[packetListeners.size()];
			packetListeners.toArray(copy);
		}
		
		// notify all
		for (PacketListener l : copy) {
			l.packetReceived(packet);
		}
	}
	
	public boolean removePacketListener(PacketListener l) {
		synchronized (packetListeners) {
			return packetListeners.remove(l);
		}
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
	}
	
	public void stop() {
		if (!isRunning()) {
			throw new IllegalStateException("server is not running");
		}
		
		socket.close();
		socket = null;
	}
	
}
