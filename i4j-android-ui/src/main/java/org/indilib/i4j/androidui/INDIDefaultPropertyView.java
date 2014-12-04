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
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.IOException;
import java.util.List;
import org.indilib.i4j.Constants;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIValueException;

/**
 * An class representing a View of a Property.
 * 
 * @version 1.32, April 20, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDIDefaultPropertyView extends INDIPropertyView {

    private TextView name;

    private ImageView state;

    private LinearLayout nameLayout;

    private LinearLayout elements;

    private LinearLayout setButtonPanel;

    private Button setButton;

    public INDIDefaultPropertyView(INDIProperty property) throws INDIException {
        super(property);

        Context context = I4JAndroidConfig.getContext();

        int color = Color.argb(255, 32, 32, 32);
        setBackgroundColor(color);

        boolean writable = false;
        if (property.getPermission() != Constants.PropertyPermissions.RO) {
            writable = true;
        }

        this.setOrientation(LinearLayout.VERTICAL);

        nameLayout = new LinearLayout(context);
        LayoutParams psnl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        psnl.setMargins(5, 5, 5, 5);
        nameLayout.setGravity(Gravity.CENTER_VERTICAL);
        nameLayout.setLayoutParams(psnl);

        state = new ImageView(context);
        state.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        nameLayout.addView(state);

        name = new TextView(context);
        LayoutParams psn = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        psn.setMargins(5, 0, 0, 0);
        name.setLayoutParams(psn);
        name.setTextSize(name.getTextSize() * 1.2f);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        nameLayout.addView(name);

        addView(nameLayout);

        // Add the Element Views
        elements = new LinearLayout(context);
        elements.setOrientation(LinearLayout.VERTICAL);
        LayoutParams pse = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        pse.setMargins(15, 0, 0, 0);
        elements.setLayoutParams(pse);

        List<INDIElement> elems = property.getElementsAsList();

        for (int i = 0; i < elems.size(); i++) {
            INDIElementView ev = null;

            try {
                ev = (INDIElementView) elems.get(i).getDefaultUIComponent();
            } catch (Exception e) { // Problem with library. Should not happen
                                    // unless errors in Client library
                e.printStackTrace();
                System.exit(-1);
            }

            ev.setINDIPropertyView(this);

            elements.addView(ev);
        }

        addView(elements);

        if (writable) {
            setButtonPanel = new LinearLayout(context);
            setButtonPanel.setGravity(Gravity.RIGHT);

            // Add the SET button
            setButton = new Button(context);
            setButton.setText("  Set  ");
            LayoutParams lpsb = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lpsb.setMargins(0, 0, 5, 0);
            setButton.setLayoutParams(lpsb);
            setButton.setEnabled(false);
            setButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    setActionPerformed();
                }
            });

            setButtonPanel.addView(setButton);
            addView(setButtonPanel);
        }

        LayoutParams ps = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ps.setMargins(5, 10, 5, 0);
        this.setLayoutParams(ps);

        updatePropertyData();
    }

    private void setActionPerformed() {
        for (int i = 0; i < elements.getChildCount(); i++) {
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

            for (int i = 0; i < elements.getChildCount(); i++) {
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
        for (int i = 0; i < elements.getChildCount(); i++) {
            INDIElementView elv = (INDIElementView) elements.getChildAt(i);

            elv.cleanDesiredValue();
        }
    }

    private void updatePropertyData() {
        name.setText(getProperty().getLabel());

        PropertyStates st = getProperty().getState();

        if (st == PropertyStates.IDLE) {
            state.setImageResource(R.drawable.ic_idle);
        } else if (st == PropertyStates.OK) {
            state.setImageResource(R.drawable.ic_ok);
        } else if (st == PropertyStates.BUSY) {
            state.setImageResource(R.drawable.ic_busy);
        } else if (st == PropertyStates.ALERT) {
            state.setImageResource(R.drawable.ic_alert);
        }
    }

    @Override
    public void propertyChanged(INDIProperty<?> property) {
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

        for (int i = 0; i < elements.getChildCount(); i++) {
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
