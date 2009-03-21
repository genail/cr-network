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
package pl.graniec.coralreef.network.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import pl.graniec.coralreef.network.Network;
import pl.graniec.coralreef.network.exceptions.NetworkException;
import pl.graniec.coralreef.network.exceptions.TimeoutException;
import pl.graniec.coralreef.network.exceptions.UnexpectedReplyException;
import pl.graniec.coralreef.network.packets.ConnectPacketData;
import pl.graniec.coralreef.network.packets.DisconnectPacketData;
import pl.graniec.coralreef.network.packets.HeaderData;
import pl.graniec.coralreef.network.packets.PacketData;
import pl.graniec.coralreef.network.packets.PassportAssignData;
import pl.graniec.coralreef.network.packets.ClientData;

/**
 * The client of server on client-side.
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public class Client {
	
	private final static Logger logger = Logger.getLogger(Client.class.getName());
	
	/** Default read buffer size */
	private static final int BUFFOR_SIZE = 1024 * 32;
	/** Default connection timeout */
	private static final int CONNECTION_TIMEOUT = 10000;
	
	/** The socket */
	private final DatagramSocket socket;
	/** Passport of this client */
	private int passport;
	
	/**
	 * @throws SocketException 
	 * 
	 */
	public Client() throws SocketException {
		this.socket = new DatagramSocket();
	}
	
	/**
	 * Connects to the remote socket. See {@link #connect(String, int, int)}
	 * for more details.
	 * 
	 * @param host
	 * @param port
	 * @throws UnexpectedReplyException 
	 * @throws IOException 
	 * @see {@link #connect(String, int, int)}
	 */
	public void connect(String host, int port) throws NetworkException {
		connect(host, port, CONNECTION_TIMEOUT);
	}
	
	/**
	 * Connects to the remote socket.
	 * <p>
	 * Connection can failed in numerous reasons:
	 * <ul>
	 * <li>
	 * If hostname like "sun.com" is given, then sometimes it could not
	 * be resolved. Then this method will throw {@link UnknownHostException}.
	 * </li>
	 * <li>
	 * If connection procedure time is out then {@link TimeoutException} will
	 * be thrown.
	 * </li>
	 * <li>
	 * When data from server is totally different that expected one (server uses
	 * different protocol) then {@link NetworkException} is thrown. This can also
	 * mean that connection is phisically broken in some way.
	 * </li>
	 * <li>
	 * When data from server is recognized but it didn't reply with data that
	 * was expected then {@link UnexpectedReplyException}. This can also mean
	 * that server is using very different version of cr-network library.
	 * </li>
	 * </ul>
	 * 
	 * @param host Hostname of remote host.
	 * @param port Port of remote host.
	 * @param timeout Connection timeout in milliseconds.
	 */
	public void connect(String host, int port, int timeout) throws NetworkException {
		
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("parameters cannot be empty");
		}
		
		if (!Network.isPortValid(port)) {
			throw new IllegalArgumentException("number " + port + " is not valid network port value");
		}
		
		boolean succeed = false;
		
		try {
			InetAddress address = InetAddress.getByName(host);
			
			socket.connect(address, port);
			
			// connection procedure
		
			sendConnectPacket();
			waitForPassportAssign(timeout);
			
			succeed = true;
			
		} catch (SecurityException e) {
			
			throw new pl.graniec.coralreef.network.exceptions.SecurityException(e.getMessage());
			
		} catch (UnknownHostException e) {

			throw new pl.graniec.coralreef.network.exceptions.UnknownHostException(e.getMessage());

		} finally {
			if (!succeed && socket.isConnected()) {
				socket.disconnect();
			}
		}
		
	}
	
	
	
	
	/**
	 * @throws UnexpectedReplyException 
	 * @throws IOException 
	 * 
	 */
	private void waitForPassportAssign(int timeout) throws NetworkException {
		try {
			
			byte[] buffer = new byte[BUFFOR_SIZE];
			DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
			
			socket.setSoTimeout(timeout);
			socket.receive(receivedPacket);
			
			PacketData packet = unpack(receivedPacket);
			
			if (!(packet instanceof PassportAssignData)) {
				throw new UnexpectedReplyException("unexpected server reply: " + packet.getClass());
			}
			
			// got passport
			passport = ((PassportAssignData)packet).getPassport();
			
			logger.fine("passport assigned: " + passport);
			
		} catch (SocketTimeoutException e) {
			throw new TimeoutException(e.getMessage());
		} catch (IOException e) {
			throw new NetworkException(e.getMessage());
		}
	}
	
	private PacketData unpack(DatagramPacket packet) throws IOException {
		try {
			
			ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
			ObjectInputStream ois = new ObjectInputStream(bais);
		
			return (PacketData) ois.readObject();
			
		} catch (ClassNotFoundException e) {
			
			logger.warning("unpacking finished with unexpected error: " + e);
			throw new IOException("unexpected read error: " + e.getMessage());
			
		}
	}
	
	private DatagramPacket pack(PacketData data, boolean userData) throws IOException {
		
		PacketData toSendData;
		
		if (userData) {
			
			// if this is user data then I should send UserData class
			// that contains head of package and its body
			HeaderData header = new HeaderData(passport);
			toSendData = new ClientData(header, data);
			
		} else {
			
			toSendData = data;
			
		}
		
		byte[] bytes = packetDataToByteArray(toSendData);
		
		return new DatagramPacket(
				bytes,
				bytes.length,
				socket.getRemoteSocketAddress()
		);
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private void sendConnectPacket() throws NetworkException {
		PacketData data = new ConnectPacketData();
		send(data, false);
	}

	/**
	 * Sends the packet to remote server (if connected).
	 *  
	 * @param data Data to be sent.
	 * @throws IOException
	 */
	public void send(PacketData data) throws NetworkException {
		send(data, true);
	}
	
	private void send(PacketData data, boolean userData) throws NetworkException {
		if (data == null) {
			throw new IllegalArgumentException("cannot take null values");
		}
		
		if (!socket.isConnected()) {
			throw new IllegalStateException("client not connected");
		}
		try {
			socket.send(pack(data, userData));
		} catch (SecurityException e) {
			throw new pl.graniec.coralreef.network.exceptions.SecurityException(e);
		} catch (IOException e) {
			throw new NetworkException(e);
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

	/**
	 * @return
	 */
	public boolean isConnected() {
		return socket.isConnected();
	}

	/**
	 * 
	 */
	public void disconnect() {
		
		if (!isConnected()) {
			throw new IllegalStateException("client is not connected");
		}
		
		try {
			sendDisconnectPacket();
		} catch (NetworkException e) {
			logger.warning("error while sending disconnection packet " + e);
		}
		
		socket.disconnect();
		
	}

	/**
	 * @throws NetworkException 
	 * 
	 */
	private void sendDisconnectPacket() throws NetworkException {
		send(new DisconnectPacketData(), false);
	}
}
