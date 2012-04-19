/*
 *  This file is part of INDI for Java Android UI.
 * 
 *  INDI for Java Android UI is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Android UI is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Android UI.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.androidui.examples;

import android.app.Application;
import laazotea.indi.client.INDIServerConnection;

/**
 * An Android Application to persist the Server Connection between configuration
 * changes.
 *
 * @version 1.32, April 18, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDIAndroidApplication extends Application {

  private INDIServerConnection connection;

  public INDIServerConnection getConnection() {
    return connection;
  }

  public void setConnection(INDIServerConnection connection) {
    this.connection = connection;
  }
}
