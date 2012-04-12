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
import android.widget.ImageView;
import android.widget.TextView;
import laazotea.indi.Constants.LightStates;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.INDIException;
import laazotea.indi.client.INDIElement;
import laazotea.indi.client.INDILightElement;

/**
 * An class representing a View of a Light Element.
 *
 * @version 1.3, April 9, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDILightElementView extends INDIElementView {

  private INDILightElement le;
  private TextView name;
  private ImageView currentValue;

  public INDILightElementView(INDILightElement le) throws INDIException {
    super(PropertyPermissions.RO);

    Context context = I4JAndroidConfig.getContext();

    this.le = le;

    name = new TextView(context);
    name.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
    addView(name);

    currentValue = new ImageView(context);
    currentValue.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    addView(currentValue);

    updateElementData();
  }

  private void updateElementData() {
    name.setText(le.getLabel() + ":");

    LightStates s = (LightStates) le.getValue();

    if (s == LightStates.ALERT) {
      currentValue.setImageResource(R.drawable.light_alert_big);
    } else if (s == LightStates.BUSY) {
      currentValue.setImageResource(R.drawable.light_busy_big);
    } else if (s == LightStates.OK) {
      currentValue.setImageResource(R.drawable.light_ok_big);
    } else if (s == LightStates.IDLE) {
      currentValue.setImageResource(R.drawable.light_idle_big);
    }
  }

  @Override
  protected Object getDesiredValue() {
    return null; // There is no desired value for a light
  }

  @Override
  protected INDILightElement getElement() {
    return le;
  }

  @Override
  protected void setError(boolean erroneous, String errorMessage) {
    // No thing to do, a light cannot be erroneous
  }

  @Override
  protected boolean isDesiredValueErroneous() {
    return false; // Cannot be erroneous
  }

  @Override
  protected void cleanDesiredValue() {
    // There is no desired value for a light
  }

  @Override
  public void elementChanged(INDIElement element) {
    if (element == le) {
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
