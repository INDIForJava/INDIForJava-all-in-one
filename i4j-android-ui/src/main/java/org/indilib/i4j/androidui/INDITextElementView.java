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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDITextElement;

/**
 * An class representing a View of a Text Element.
 * 
 * @version 1.32, April 20, 2012
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
