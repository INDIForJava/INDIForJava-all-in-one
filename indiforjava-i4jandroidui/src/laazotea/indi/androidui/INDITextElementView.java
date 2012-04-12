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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.INDIException;
import laazotea.indi.client.INDIElement;
import laazotea.indi.client.INDITextElement;

/**
 * An class representing a View of a Text Element.
 *
 * @version 1.3, April 9, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDITextElementView extends INDIElementView {

  private INDITextElement te;
  private TextView name;
  private TextView currentValue;
  private EditText desiredValue;

  public INDITextElementView(INDITextElement te, PropertyPermissions perm) throws INDIException {
    super(perm);

    Context context = I4JAndroidConfig.getContext();

    this.te = te;

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
  }

  private void desiredValueChanged() {
    if (!(desiredValue.getText().length() == 0)) {
      setChanged(true);
    } else {
      setChanged(false);
    }

    checkSetButton();
  }

  private void updateElementData() {
    name.setText(te.getLabel() + ":");

    currentValue.setText((String) te.getValue());
  }

  @Override
  protected Object getDesiredValue() {
    return desiredValue.getText().toString();
  }

  @Override
  protected INDITextElement getElement() {
    return te;
  }

  @Override
  protected void setError(boolean erroneous, String errorMessage) {
    // Does nothing, text elements cannot be erroneous
  }

  @Override
  protected boolean isDesiredValueErroneous() {
    return false; // Never erroneous
  }

  @Override
  protected void cleanDesiredValue() {
    desiredValue.setText("");
  }

  @Override
  public void elementChanged(INDIElement element) {
    if (element == te) {
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
