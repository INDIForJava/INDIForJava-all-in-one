
package org.indilib.i4j.androidui;

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
