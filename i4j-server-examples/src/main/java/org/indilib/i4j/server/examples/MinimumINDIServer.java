
package org.indilib.i4j.server.examples;

/*
 * #%L
 * INDI for Java Server examples
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.net.Socket;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.examples.RandomNumberGeneratorDriver;
import org.indilib.i4j.server.DefaultINDIServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An almost minimum INDI Server. It just has one working Driver:
 * RandomNumberGeneratorDriver (please check the INDI for Java Driver examples)
 * and it only accepts connections from 127.0.0.1 (localhost).
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.21, April 4, 2012
 */
public class MinimumINDIServer extends DefaultINDIServer {

    private static Logger LOG = LoggerFactory.getLogger(MinimumINDIServer.class);
  /**
   * Just loads the available driver.
   */
  public MinimumINDIServer() {
    super();

    // Loads the Java Driver. Please note that its class must be in the classpath.
    try {
      loadJavaDriver(RandomNumberGeneratorDriver.class);
    } catch (INDIException e) {
        LOG.error("indi exception",e);

      System.exit(-1);
    }
  }

  /**
   * Accepts the client if it is 127.0.0.1 (localhost).
   *
   * @param socket
   * @return <code>true</code> if it is the 127.0.0.1 host.
   */
  @Override
  protected boolean acceptClient(Socket socket) {
    byte[] address = socket.getInetAddress().getAddress();

    if ( (address[0] == 127) && (address[1] == 0) && (address[2] == 0) && (address[3] == 1) ) {
      return true;
    }
    
    return false;
  }

  /**
   * Just creates one instance of this server.
   * @param args 
   */
  public static void main(String[] args) {
    MinimumINDIServer s = new MinimumINDIServer();  
  }
}