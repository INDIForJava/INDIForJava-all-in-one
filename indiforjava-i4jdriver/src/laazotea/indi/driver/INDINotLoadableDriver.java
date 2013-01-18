/*
 *  This file is part of INDI for Java Driver.
 * 
 *  INDI for Java Driver is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Driver is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.driver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class representing a Not Loadable Driver in the INDI Protocol. 
 * INDI Drivers which should not be directly instantiated by a server should
 * extend this class. It is in charge of stablishing the connection to the
 * clients and parsing / formating any incoming / leaving messages.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.32, January 18, 2013
 */
public abstract class INDINotLoadableDriver extends INDIDriver {
  public INDINotLoadableDriver(InputStream inputStream, OutputStream outputStream) {
    super(inputStream, outputStream);
  }
}
