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

import android.content.Context;
import android.widget.TextView;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.INDIBLOBValue;
import laazotea.indi.INDIException;
import laazotea.indi.client.INDIBLOBElement;
import laazotea.indi.client.INDIElement;

/**
 * An class representing a View of a BLOB Element.
 *
 * @version 1.32, April 20, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDIBLOBElementView extends INDIElementView {

  private INDIBLOBElement be;
  private TextView name;
  private TextView currentValue;

  public INDIBLOBElementView(INDIBLOBElement be, PropertyPermissions perm) throws INDIException {
    super(perm);

    Context context = I4JAndroidConfig.getContext();

    this.be = be;

    name = new TextView(context);
    name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    addView(name);

    currentValue = new TextView(context);
    currentValue.setSingleLine();
    currentValue.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    addView(currentValue);


    updateElementData();
  }

  private void updateElementData() {
    name.setText(be.getLabel());

    INDIBLOBValue val = be.getValue();

    if (val != null) {
      currentValue.setText(val.getFormat() + "(" + val.getSize() + " bytes)");
    }
  }

  @Override
  protected Object getDesiredValue() {
    return null;
  }

  @Override
  protected INDIBLOBElement getElement() {
    return be;
  }

  @Override
  protected void setError(boolean erroneous, String errorMessage) {
  }

  @Override
  protected boolean isDesiredValueErroneous() {
    return false;
  }

  @Override
  protected void cleanDesiredValue() {
  }

  @Override
  public void elementChanged(INDIElement element) {
    if (element == be) {
      try {
        I4JAndroidConfig.postHandler(new Runnable() {

          @Override
          public void run() {
            updateElementData();
          }
        });
      } catch (INDIException e) {
        e.printStackTrace();
      }
    }
  }
}
