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
package laazotea.indi.androidui;

import android.widget.LinearLayout;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.INDIException;
import laazotea.indi.client.INDIElement;
import laazotea.indi.client.INDIElementListener;

/**
 * An abstract class representing a View of a Element.
 *
 * @version 1.3, April 9, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public abstract class INDIElementView extends LinearLayout implements INDIElementListener {

  private boolean changed;
  private boolean writable;
  private INDIPropertyView ipv;

  protected INDIElementView(PropertyPermissions perm) throws INDIException {
    super(I4JAndroidConfig.getContext());

    if (perm != PropertyPermissions.RO) {
      this.writable = true;
    } else {
      this.writable = false;
    }

    ipv = null;

    changed = false;
  }

  protected void setINDIPropertyView(INDIPropertyView ipv) {
    this.ipv = ipv;
  }

  protected void checkSetButton() {
    if (ipv != null) {
      ipv.checkSetButton();
    }
  }

  protected void setChanged(boolean changed) {
    this.changed = changed;
  }

  protected boolean isChanged() {
    return changed;
  }

  protected boolean isWritable() {
    return writable;
  }

  protected abstract Object getDesiredValue();

  protected abstract INDIElement getElement();

  protected abstract void setError(boolean erroneous, String errorMessage);

  protected abstract boolean isDesiredValueErroneous();

  protected abstract void cleanDesiredValue();
}
