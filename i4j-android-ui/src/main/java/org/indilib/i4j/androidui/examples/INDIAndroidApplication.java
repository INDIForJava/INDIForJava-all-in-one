
package org.indilib.i4j.androidui.examples;

/*
 * #%L
 * INDI for Java Android App
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

import android.app.Application;
import org.indilib.i4j.client.INDIServerConnection;

/**
 * An Android Application to persist the Server Connection between configuration
 * changes.
 *
 * @version 1.32, April 18, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDIAndroidApplication extends Application {

  private INDIServerConnection connection;
  private String selectedTab;
  
  public INDIServerConnection getConnection() {
    return connection;
  }

  public void setConnection(INDIServerConnection connection) {
    this.connection = connection;
  }

  public String getSelectedTab() {
    return selectedTab;
  }

  public void setSelectedTab(String selectedTab) {
    this.selectedTab = selectedTab;
  }
}