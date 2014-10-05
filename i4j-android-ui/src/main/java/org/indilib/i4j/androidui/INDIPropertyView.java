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
package org.indilib.i4j.androidui;

import android.widget.LinearLayout;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;

/**
 * An abstract class representing a View of a Property.
 *
 * @version 1.32, April 20, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public abstract class INDIPropertyView extends LinearLayout implements INDIPropertyListener {

  private INDIProperty property;

  protected INDIPropertyView(INDIProperty property) throws INDIException {
    super(I4JAndroidConfig.getContext());

    this.property = property;
    
    this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
  }

  public INDIProperty getProperty() {
    return property;
  }

  protected abstract void checkSetButton();
}
