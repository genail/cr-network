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

import java.io.Externalizable;
import java.io.NotSerializableException;
import java.io.Serializable;

import pl.graniec.coralreef.network.PacketListener;
import pl.graniec.coralreef.network.exceptions.NetworkException;

/**
 * 
 * Client that residents on other side of the connection.
 * 
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public interface RemoteClient {
	
	/**
	 * Add a packet listener object that will listen for all
	 * data sent by this remote client. If this listener object
	 * is already listening on this client, then <code>false</code>
	 * is returned.
	 * 
	 * @param Listener object.
	 * 
	 * @return <code>true</code> if this listener was successfully added. 
	 * 
	 * @see #removePacketListener(PacketListener)
	 */
	boolean addPacketListener(PacketListener l);
	
	/**
	 * Disconnects remote client from server (only if connected). The action
	 * is immediately and no further packets will be delivered to this client
	 * nor received from it.
	 * <p>
	 * If you have a connection listener enabled then you will receive
	 * disconnection event from this action.
	 */
	void disconnect();
	
	/**
	 * Tells if this remote client is still connected to the server.
	 * 
	 * @return <code>true</code> if this client is connected to the server.
	 */
	boolean isConnected();
	
	/**
	 * Removes previously added packet listener from further listening
	 * for packets of this remote client. If this listener haven't been
	 * listening then <code>false</code> is returned.
	 * 
	 * @param l Listener object.
	 * 
	 * @return <code>true</code> if this object was successfully removed.
	 * 
	 * @see #addPacketListener(PacketListener)
	 */
	boolean removePacketListener(PacketListener l);
	
	/**
	 * Sends data to remote client (if still connected).
	 * <p>
	 * Given data must implements be either {@link Serializable} or
	 * {@link Externalizable} interface in order to be sent through the
	 * network. It's recommended to use the second one because of
	 * high ability of data organization.
	 * 
	 * @throws NotSerializableException When given object does not implements
	 * {@link Serializable} or {@link Externalizable} interface.
	 * 
	 * @throws NetworkException When an error occurred while sending this data.
	 * This usually means that client have just disconnected and data were not
	 * sent. 
	 */
	void send(Object data) throws NotSerializableException, NetworkException;
	
}
