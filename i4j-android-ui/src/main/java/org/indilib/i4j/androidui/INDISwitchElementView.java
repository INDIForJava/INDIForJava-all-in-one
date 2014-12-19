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
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDISwitchElement;

/**
 * An class representing a View of a Switch Element.
 * 
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDISwitchElementView extends INDIElementView {

    private INDISwitchElement se;

    private TextView name;

    private TextView currentValue;

    private Drawable bgColor;

    private ToggleButton desiredValue;

    public INDISwitchElementView(INDISwitchElement se, PropertyPermissions perm) throws INDIException {
        super(perm);

        Context context = I4JAndroidConfig.getContext();

        this.se = se;

        this.setGravity(Gravity.CENTER_VERTICAL);

        name = new TextView(context);
        name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(name);

        currentValue = new TextView(context);
        currentValue.setSingleLine();
        currentValue.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        bgColor = currentValue.getBackground();
        addView(currentValue);

        desiredValue = new ToggleButton(context);
        desiredValue.setTextOn("Set Selected");
        desiredValue.setTextOff("Set Deselected");
        desiredValue.setChecked(false);
        desiredValue.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        desiredValue.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                desiredValueChanged();
            }
        });

        if (isWritable()) {
            addView(desiredValue);
        }

        updateElementData();
    }

    private void updateElementData() {
        name.setText(se.getLabel() + ":");

        SwitchStatus ss = (SwitchStatus) se.getValue();

        if (ss == SwitchStatus.OFF) {
            currentValue.setText("DESELECTED");
            currentValue.setBackgroundDrawable(bgColor);
        } else {
            currentValue.setText("SELECTED");
            currentValue.setBackgroundColor(Color.GREEN);
        }
    }

    private void desiredValueChanged() {
        checkSetButton();
    }

    @Override
    protected boolean isChanged() {
        return true; // Always changed: all will be send
    }

    @Override
    protected Object getDesiredValue() {
        if (desiredValue.isChecked()) {
            return SwitchStatus.ON;
        }

        return SwitchStatus.OFF;
    }

    @Override
    protected INDISwitchElement getElement() {
        return se;
    }

    @Override
    protected void setError(boolean erroneous, String errorMessage) {
        // A single switch element cannot be erroneous
    }

    @Override
    protected boolean isDesiredValueErroneous() {
        return false; // A single switch element cannot be erroneous
    }

    @Override
    protected void cleanDesiredValue() {
        desiredValue.setChecked(false);
    }

    @Override
    public void elementChanged(INDIElement element) {
        if (element == se) {
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
