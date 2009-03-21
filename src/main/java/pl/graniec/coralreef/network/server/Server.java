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

import pl.graniec.coralreef.network.client.Client;

/**
 * Server is a service that listens for incoming connection on specified
 * port and interface of host. When connection from a {@link Client} is
 * establilished then server can either receive or send data to it using
 * the {@link RemoteClient} interface.
 * 
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public interface Server {
	/**
	 * Makes a listener object to listen for incoming connections
	 * on this server. Also every disconnect event will be notified
	 * too.
	 * <p>
	 * If the same listener object is already added then the result
	 * of this method will be <code>false</code>.
	 * 
	 * @param l Listener object.
	 * 
	 * @return <code>true</code> if listener is successfully added.
	 * 
	 * @see #removeConnectionListener(ConnectionListener)
	 */
	boolean addConnectionListener(ConnectionListener l);
	
	/**
	 * Closes the opened server. Closing will disconnect all currently
	 * connected clients, This will also trigger disconnection event for
	 * all connection listeners.
	 * 
	 * @see #open(int)
	 */
	void close();
	
	/**
	 * Provides a port number on which the server is running on.
	 * 
	 * @return Port number or <code>0</code> if server is not open.
	 */
	int getPort();
	
	/**
	 * Tells if server is open for new connections.
	 * 
	 * @see #open(int)
	 */
	boolean isOpen();
	
	/**
	 * Opens the non-opened server on current port. If server is already
	 * opened then it must be closed before another open procedure can
	 * occur.
	 * <p>
	 * You can specify the port number from 0 to 65 535. 0 will open
	 * the server on random available port. Range from 1 to 1023 can
	 * be used only by privileged users (Unix).
	 * 
	 * @param port Port number on which server will be opened.
	 * 
	 * @see #close()
	 */
	void open(int port);
	
	/**
	 * Removes a previously added listener from connection listening.
	 * <p>
	 * If there is not such listener registered then the result of
	 * this method will be <code>false</code>.
	 * 
	 * @param l Listener object.
	 * 
	 * @return <code>true</code> if listener is successfully removed.
	 * 
	 * @see #addConnectionListener(ConnectionListener)
	 */
	boolean removeConnectionListener(ConnectionListener l);
}
