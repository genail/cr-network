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
package pl.graniec.coralreef.network;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pl.graniec.coralreef.network.client.Client;
import pl.graniec.coralreef.network.exceptions.NetworkException;
import pl.graniec.coralreef.network.server.ConnectionListener;
import pl.graniec.coralreef.network.server.DisconnectReason;
import pl.graniec.coralreef.network.server.RemoteClient;
import pl.graniec.coralreef.network.server.Server;

/**
 * @author Piotr Korzuszek <piotr.korzuszek@gmail.com>
 *
 */
public class ModuleTest {

	final Mockery context = new JUnit4Mockery();
	
	/**
	 * 
	 */
	private static final int PORT = 9999;
	private Client client;
	private Server server;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		server = new Server("0.0.0.0", PORT);
		server.start();
		
		client = new Client();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void testConnection() throws NetworkException {
		
		final ConnectionListener cl = context.mock(ConnectionListener.class);
		
		context.checking(new Expectations(){{
			oneOf(cl).clientConnected(with(any(RemoteClient.class)));
			ignoring(cl).clientDisconnected(with(any(RemoteClient.class)), with(any(DisconnectReason.class)));
		}});
		
		server.addConnectionListener(cl);
		
		client.connect("127.0.0.1", PORT);
		
		assertTrue(client.isConnected());
		context.assertIsSatisfied();
	}
	
	@Test
	public void testDisconnection() throws NetworkException, InterruptedException {
		
		client.connect("127.0.0.1", PORT);
		
		final ConnectionListener cl = context.mock(ConnectionListener.class);
		
		context.checking(new Expectations(){{
			oneOf(cl).clientDisconnected(with(any(RemoteClient.class)), with(any(DisconnectReason.class)));
		}});
		
		server.addConnectionListener(cl);
		
		client.disconnect();
		
		Thread.sleep(50);
		
		assertFalse(client.isConnected());
		context.assertIsSatisfied();
	}

}