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

import java.io.Externalizable;
import java.io.NotSerializableException;
import java.io.Serializable;

import pl.graniec.coralreef.network.PacketListener;
import pl.graniec.coralreef.network.exceptions.NetworkException;
import pl.graniec.coralreef.network.server.Server;

/**
 * Client is a host that connects to the {@link Server} in order
 * to exchange data.
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public interface Client {

	/**
	 * Add a connection listener object that will listen for
	 * all connection events done by this client. This includes
	 * connection as well as disconnections. If this listener object
	 * is already listening on this client then <code>false</code>
	 * is returned
	 * 
	 * @param l Listener object.
	 * 
	 * @return <code>true</code> if this listener was successfully added.
	 * 
	 * @see #removeConnectionListener(ConnectionListener)
	 */
	boolean addConnectionListener(ConnectionListener l);
	
	/**
	 * Add a packet listener object that will listen for all
	 * data sent by this client. If this listener object
	 * is already listening on this client then <code>false</code>
	 * is returned.
	 * 
	 * @param l Listener object.
	 * 
	 * @return <code>true</code> if this listener was successfully added. 
	 * 
	 * @see #removePacketListener(PacketListener)
	 */
	boolean addPacketListener(PacketListener l);
	
	/**
	 * Connects not-connected client to the remote host on given port.
	 * From now on any data can be transfered.
	 * 
	 * @param host Hostname of target machine. Can be host as
	 * <code>sun.com</code> or textual representation od IP address.
	 * @param port Port on which the server is running on.
	 * 
	 * @see #disconnect()
	 * 
	 */
	void connect(String host, int port) throws NetworkException;
	
	/**
	 * Disconnects connected client from server. From this point
	 * no data can be sent or received.
	 * 
	 * @see #connect(String, int)
	 */
	void disconnect();
	
	/**
	 * Tells if this client is currently connected to remote server.
	 * 
	 * @see #connect(String, int)
	 */
	boolean isConnected();

	/**
	 * Removes previously added connection listener from further listening
	 * for connection events of this client. If this listener haven't been
	 * listening until now then <code>false</code> is returned.
	 * 
	 * @param l Listener object.
	 * 
	 * @return <code>true</code> if this object was successfully removed.
	 * 
	 * @see #addConnectionListener(ConnectionListener)
	 */
	boolean removeConnectionListener(ConnectionListener l);
	
	/**
	 * Removes previously added packet listener from further listening
	 * for packets of this client. If this listener haven't been
	 * listening until now then <code>false</code> is returned.
	 * 
	 * @param l Listener object.
	 * 
	 * @return <code>true</code> if this object was successfully removed.
	 * 
	 * @see #addPacketListener(PacketListener)
	 */
	boolean removePacketListener(PacketListener l);
	
	/**
	 * Sends data to remote server (if still connected).
	 * <p>
	 * Given data must implements be either {@link Serializable} or
	 * {@link Externalizable} interface in order to be sent through the
	 * network. It's recommended to use the second one because of
	 * high ability of data organization.
	 */
	void send(Object data) throws NotSerializableException, NetworkException;
	
	
}
