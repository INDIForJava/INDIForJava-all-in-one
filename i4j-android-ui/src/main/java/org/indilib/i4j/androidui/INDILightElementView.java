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
import android.widget.ImageView;
import android.widget.TextView;
import org.indilib.i4j.Constants.LightStates;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDILightElement;

/**
 * An class representing a View of a Light Element.
 * 
 * @version 1.32, April 20, 2012
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
        name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(name);

        currentValue = new ImageView(context);
        currentValue.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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
