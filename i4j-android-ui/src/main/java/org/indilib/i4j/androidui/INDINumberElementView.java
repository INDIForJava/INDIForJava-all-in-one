package org.indilib.i4j.androidui;

/*
 * #%L INDI for Java Android App %% Copyright (C) 2013 - 2014 indiforjava %%
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Lesser Public License for more details. You should have received a copy of
 * the GNU General Lesser Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>. #L%
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDINumberElement;
import org.indilib.i4j.client.INDIValueException;

/**
 * An class representing a View of a Number Element.
 * 
 * @version 1.32, April 20, 2012
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
        name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(name);

        currentValue = new TextView(context);
        currentValue.setSingleLine();
        currentValue.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        addView(currentValue);

        desiredValue = new EditText(context);
        desiredValue.setText("");
        desiredValue.setSingleLine();
        desiredValue.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
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
            // desiredValue.setToolTipText(errorMessage);
            desiredValue.requestFocus();
        } else {
            desiredValue.setBackgroundDrawable(bgColor);
            // desiredValue.setToolTipText(null);
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
