/*
 *  This file is part of INDI for Java Server.
 * 
 *  INDI for Java Server is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Server is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Server.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.server.examples;

import java.net.Socket;
import laazotea.indi.INDIException;
import laazotea.indi.driver.examples.RandomNumberGeneratorDriver;
import laazotea.indi.server.DefaultINDIServer;

/**
 * An almost minimum INDI Server. It just has one working Driver:
 * RandomNumberGeneratorDriver (please check the INDI for Java Driver examples)
 * and it only accepts connections from 127.0.0.1 (localhost).
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.21, April 4, 2012
 */
public class MinimumINDIServer extends DefaultINDIServer {

  /**
   * Just loads the available driver.
   */
  public MinimumINDIServer() {
    super();

    // Loads the Java Driver. Please note that its class must be in the classpath.
    try {
      loadJavaDriver(RandomNumberGeneratorDriver.class);
    } catch (INDIException e) {
      e.printStackTrace();

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
