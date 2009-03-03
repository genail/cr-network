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

import java.util.LinkedList;
import java.util.List;

import pl.graniec.coralreef.network.PacketListener;
import pl.graniec.coralreef.network.packets.Packet;
import pl.graniec.coralreef.network.packets.PacketData;

/**
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public class RemoteClient {
	
	/** Parent server */
	private final Server server;
	
	/** Host name */
	private final String host;
	/** Remote port */
	private final int port;
	/** Passport (connection id) */
	private final int passport;
	
	/** Last ping value */
	private int ping;
	
	/** Packet listeners */
	private final List<PacketListener> packetListeners = new LinkedList<PacketListener>();

	/**
	 * @param server
	 * @param host
	 * @param port
	 * @param passport
	 */
	protected RemoteClient(Server server, String host, int port, int passport) {
		super();
		this.server = server;
		this.host = host;
		this.port = port;
		this.passport = passport;
	}
	
	public void addPacketListener(PacketListener l) {
		synchronized (packetListeners) {
			packetListeners.add(l);
		}
	}
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * @return the ping
	 */
	public int getPing() {
		return ping;
	}
	
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	void notifyPacketReceived(PacketData packet) {
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
	 * @return the passport
	 */
	public int getPassport() {
		return passport;
	}
	
}
