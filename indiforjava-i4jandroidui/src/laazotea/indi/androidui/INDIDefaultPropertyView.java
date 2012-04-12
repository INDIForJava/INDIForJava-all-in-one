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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import laazotea.indi.Constants;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIException;
import laazotea.indi.client.INDIElement;
import laazotea.indi.client.INDIProperty;
import laazotea.indi.client.INDIValueException;

/**
 * An class representing a View of a Property.
 *
 * @version 1.3, April 9, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDIDefaultPropertyView extends INDIPropertyView {

  private TextView name;
  private ImageView state;
  private LinearLayout nameLayout;
  private LinearLayout elements;
  private Button setButton;

  public INDIDefaultPropertyView(INDIProperty property) throws INDIException {
    super(property);

    Context context = I4JAndroidConfig.getContext();

    int rnd = (new Random()).nextInt(150);
    int color = Color.argb(255, rnd, rnd, rnd);
    setBackgroundColor(color);


    boolean writable = false;
    if (property.getPermission() != Constants.PropertyPermissions.RO) {
      writable = true;
    }

    this.setOrientation(LinearLayout.VERTICAL);

    nameLayout = new LinearLayout(context);

    state = new ImageView(context);
    state.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    nameLayout.addView(state);

    name = new TextView(context);
    name.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
    nameLayout.addView(name);

    addView(nameLayout);


    // Add the Element Views
    elements = new LinearLayout(context);
    elements.setOrientation(LinearLayout.VERTICAL);

    List<INDIElement> elems = property.getElementsAsList();

    for (int i = 0 ; i < elems.size() ; i++) {
      INDIElementView ev = null;

      try {
        ev = (INDIElementView) elems.get(i).getDefaultUIComponent();
      } catch (Exception e) { // Problem with library. Should not happen unless errors in Client library
        e.printStackTrace();
        System.exit(-1);
      }

      ev.setINDIPropertyView(this);

      elements.addView(ev);
    }

    addView(elements);


    // Add the SET button
    setButton = new Button(context);
    setButton.setText("Set");
    setButton.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
    setButton.setEnabled(false);
    setButton.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        setActionPerformed();
      }
    });

    if (writable) {
      addView(setButton);
    }

    updatePropertyData();
  }

  private void setActionPerformed() {
    for (int i = 0 ; i < elements.getChildCount() ; i++) {
      INDIElementView elv = (INDIElementView) elements.getChildAt(i);

      if (elv.isChanged()) {
        try {
          elv.getElement().setDesiredValue(elv.getDesiredValue());
        } catch (INDIValueException e) {
          e.printStackTrace();
          return;
        }
      }
    }

    try {
      getProperty().sendChangesToDriver();

      cleanDesiredValues();
    } catch (INDIValueException e) {
      INDIElement errorElement = e.getINDIElement();

      for (int i = 0 ; i < elements.getChildCount() ; i++) {
        INDIElementView elv = (INDIElementView) elements.getChildAt(i);

        if (errorElement == elv.getElement()) {
          elv.setError(true, e.getMessage());
        }
      }
    } catch (IOException e) {
      // System.out.println("Problem sending properties");
    }
  }

  private void cleanDesiredValues() {
    for (int i = 0 ; i < elements.getChildCount() ; i++) {
      INDIElementView elv = (INDIElementView) elements.getChildAt(i);

      elv.cleanDesiredValue();
    }
  }

  private void updatePropertyData() {
    name.setText(getProperty().getLabel());

    PropertyStates st = getProperty().getState();

    if (st == PropertyStates.IDLE) {
      state.setImageResource(R.drawable.light_idle);
    } else if (st == PropertyStates.OK) {
      state.setImageResource(R.drawable.light_ok);
    } else if (st == PropertyStates.BUSY) {
      state.setImageResource(R.drawable.light_busy);
    } else if (st == PropertyStates.ALERT) {
      state.setImageResource(R.drawable.light_alert);
    }
  }

  @Override
  public void propertyChanged(INDIProperty property) {
    if (property == getProperty()) {
      try {
        I4JAndroidConfig.postHandler(new Runnable() {

          @Override
          public void run() {
            updatePropertyData();
          }
        });
      } catch (INDIException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void checkSetButton() {
    boolean enabled = true;
    boolean changed = false;

    for (int i = 0 ; i < elements.getChildCount() ; i++) {
      INDIElementView elp = (INDIElementView) elements.getChildAt(i);

      if (elp.isDesiredValueErroneous()) {
        enabled = false;
      }

      if (elp.isChanged()) {
        changed = true;
      }
    }

    if (!changed) {
      enabled = false;
    }

    setButton.setEnabled(enabled);
  }
}
