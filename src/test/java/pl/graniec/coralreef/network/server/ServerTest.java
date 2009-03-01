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

import static org.junit.Assert.*;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public class ServerTest {
	
	final int PORT = 12345;

	Server server;
	
	@Before
	public void setUp() throws SocketException, UnknownHostException {
		server = new Server("0.0.0.0", PORT);
	} 
	
	@After
	public void tearDown() {
		if (server.isRunning()) {
			server.stop();
		}
	}
	
	/**
	 * Test method for {@link pl.graniec.coralreef.network.server.Server#start()}.
	 * @throws SocketException 
	 */
	@Test
	public void testStart() throws SocketException {
		server.start();
		assertTrue(server.isRunning());
	}

	/**
	 * Test method for {@link pl.graniec.coralreef.network.server.Server#stop()}.
	 * @throws SocketException 
	 */
	@Test
	public void testStop() throws SocketException {
		server.start();
		assertTrue(server.isRunning());
		
		server.stop();
		assertFalse(server.isRunning());
	}

	/**
	 * Test method for {@link pl.graniec.coralreef.network.server.Server#setPort(int)}.
	 * @throws SocketException 
	 * @throws UnknownHostException 
	 */
	@Test
	public void testGetPort() throws SocketException, UnknownHostException {
		assertEquals(PORT, server.getPort());
		server.start();
		
		assertEquals(PORT, server.getPort());
		server.stop();
		
		// ephemeral port test
		Server otherServer = new Server("0.0.0.0", 0);
		assertEquals(0, otherServer.getPort());
		
		otherServer.start();
		assertFalse(otherServer.getPort() == 0);
		
		otherServer.stop();
	}

}
