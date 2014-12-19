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
import android.view.View;
import android.widget.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;

/**
 * An class representing a View of a Device.
 * 
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDIDeviceView extends LinearLayout implements INDIDeviceListener {

    private INDIDevice device;

    private LinearLayout main;

    private LinearLayout bottomControls;

    private EditText messages;

    private ScrollView scroll;

    private LinearLayout properties;

    private RadioGroup blobButtons;

    private RadioButton neverBLOB;

    private RadioButton alsoBLOB;

    private RadioButton onlyBLOB;

    public INDIDeviceView(INDIDevice device) throws INDIException {
        super(I4JAndroidConfig.getContext());

        this.device = device;

        Context context = I4JAndroidConfig.getContext();

        setOrientation(LinearLayout.VERTICAL);
        main = new LinearLayout(context);
        main.setOrientation(LinearLayout.VERTICAL);

        properties = new LinearLayout(context);
        properties.setOrientation(LinearLayout.VERTICAL);
        properties.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
        main.addView(properties);

        bottomControls = new LinearLayout(context);
        LayoutParams ps = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ps.setMargins(5, 10, 5, 0);
        bottomControls.setLayoutParams(ps);

        neverBLOB = new RadioButton(context);
        neverBLOB.setText("BLOBs Never");
        neverBLOB.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                setBLOBNever();
            }
        });

        alsoBLOB = new RadioButton(context);
        alsoBLOB.setText("BLOBs Also");
        alsoBLOB.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                setBLOBAlso();
            }
        });

        onlyBLOB = new RadioButton(context);
        onlyBLOB.setText("BLOBs Only");
        onlyBLOB.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                setBLOBOnly();
            }
        });

        blobButtons = new RadioGroup(context);
        blobButtons.addView(neverBLOB);
        blobButtons.addView(alsoBLOB);
        blobButtons.addView(onlyBLOB);
        blobButtons.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        neverBLOB.toggle();
        bottomControls.addView(blobButtons);

        messages = new EditText(context);
        messages.setMaxLines(5);
        messages.setMinLines(5);
        LayoutParams psm = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        psm.setMargins(5, 0, 0, 0);
        messages.setLayoutParams(psm);
        bottomControls.addView(messages);

        main.addView(bottomControls);

        scroll = new ScrollView(context);
        scroll.setVerticalScrollBarEnabled(true);
        scroll.addView(main);

        addView(scroll);

        List<INDIProperty<?>> props = device.getPropertiesAsList();

        for (int i = 0; i < props.size(); i++) {
            newProperty(device, props.get(i));
        }
    }

    public void newProperty(INDIDevice device, final INDIProperty<?> property) {
        if (device == this.device) {

            try {
                I4JAndroidConfig.postHandler(new Runnable() {

                    @Override
                    public void run() {
                        addP(property);
                    }
                });
            } catch (INDIException e) {
                e.printStackTrace();
            }
        }
    }

    private void addP(INDIProperty property) {
        INDIPropertyView pv = null;

        try {
            pv = (INDIPropertyView) property.getDefaultUIComponent();
        } catch (Exception e) { // Problem with library. Should not happen
                                // unless errors in Client library
            e.printStackTrace();
            System.exit(-1);
        }

        properties.addView(pv);
    }

    public void removeProperty(INDIDevice device, final INDIProperty<?> property) {
        if (device == this.device) {
            try {
                I4JAndroidConfig.postHandler(new Runnable() {

                    @Override
                    public void run() {
                        removeP(property);
                    }
                });
            } catch (INDIException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeP(INDIProperty property) {
        for (int i = 0; i < properties.getChildCount(); i++) {
            INDIPropertyView pp = (INDIPropertyView) properties.getChildAt(i);

            if (pp.getProperty() == property) {
                properties.removeView(pp);

                break;
            }
        }
    }

    public void messageChanged(final INDIDevice device) {
        if (this.device == device) {
            try {
                I4JAndroidConfig.postHandler(new Runnable() {

                    @Override
                    public void run() {
                        setMessage(device.getLastMessage());
                    }
                });
            } catch (INDIException e) {
                e.printStackTrace();
            }
        }
    }

    private void setMessage(String message) {
        message = message.trim();

        Date d = device.getTimestamp();

        messages.setText(d + ": " + message);
    }

    private void setBLOBNever() {
        try {
            setMessage("Setting BLOBs Never");
            device.blobsEnableNever();
        } catch (IOException e) {
            setMessage("Problem setting BLOBs Never");
        }
    }

    private void setBLOBAlso() {
        try {
            setMessage("Setting BLOBs Also");
            device.blobsEnableAlso();
        } catch (IOException e) {
            setMessage("Problem setting BLOBs Also");
        }
    }

    private void setBLOBOnly() {
        try {
            setMessage("Setting BLOBs Only");
            device.blobsEnableOnly();
        } catch (IOException e) {
            setMessage("Problem setting BLOBs Only");
        }
    }

    public INDIDevice getDevice() {
        return device;
    }
}
