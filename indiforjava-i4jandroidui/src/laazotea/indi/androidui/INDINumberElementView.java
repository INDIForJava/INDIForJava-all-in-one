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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.INDIException;
import laazotea.indi.client.INDIElement;
import laazotea.indi.client.INDINumberElement;
import laazotea.indi.client.INDIValueException;

/**
 * An class representing a View of a Number Element.
 *
 * @version 1.3, April 9, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDINumberElementView extends INDIElementView {

  private INDINumberElement ne;
  private boolean desiredValueErroneous;
  private TextView name;
  private TextView currentValue;
  private EditText desiredValue;
  private Drawable bgColor;

  public INDINumberElementView(INDINumberElement ne, PropertyPermissions perm) throws INDIException {
    super(perm);

    Context context = I4JAndroidConfig.getContext();

    this.ne = ne;

    name = new TextView(context);
    name.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    addView(name);

    currentValue = new TextView(context);
    currentValue.setSingleLine();
    currentValue.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    addView(currentValue);


    desiredValue = new EditText(context);
    desiredValue.setText("");
    desiredValue.setSingleLine();
    desiredValue.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    bgColor = desiredValue.getBackground();
    desiredValue.addTextChangedListener(new TextWatcher() {

      public void afterTextChanged(Editable s) {
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void onTextChanged(CharSequence s, int start, int before, int count) {
        desiredValueChanged();
      }
    });

    if (isWritable()) {
      addView(desiredValue);
    }



    updateElementData();

    desiredValueErroneous = false;
  }

  private void desiredValueChanged() {
    if (desiredValue.getText().toString().trim().length() > 0) {
      setChanged(true);

      try {
        boolean correct = ne.checkCorrectValue(desiredValue.getText().toString());

        if (!correct) {
          setError(true, "Unknown error parsing value");
        } else {
          setError(false, "");
        }
      } catch (INDIValueException e) {
        setError(true, e.getMessage());
      }
    } else {
      setChanged(false);
      setError(false, "");
    }

    checkSetButton();
  }

  private void updateElementData() {
    name.setText(ne.getLabel() + ":");

    currentValue.setText(ne.getValueAsString());
  }

  @Override
  protected Object getDesiredValue() {
    return desiredValue.getText().toString();
  }

  @Override
  protected INDINumberElement getElement() {
    return ne;
  }

  @Override
  protected void setError(boolean erroneous, String errorMessage) {
    this.desiredValueErroneous = erroneous;

    if (erroneous) {
      desiredValue.setBackgroundColor(Color.RED);
//      desiredValue.setToolTipText(errorMessage);
      desiredValue.requestFocus();
    } else {
      desiredValue.setBackgroundDrawable(bgColor);
//      desiredValue.setToolTipText(null);
    }
  }

  @Override
  protected boolean isDesiredValueErroneous() {
    return desiredValueErroneous;
  }

  @Override
  protected void cleanDesiredValue() {
    desiredValue.setText("");
  }

  @Override
  public void elementChanged(INDIElement element) {
    if (element == ne) {
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
