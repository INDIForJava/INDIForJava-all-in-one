package org.indilib.i4j.androidui.examples;

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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost.TabSpec;
import android.widget.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.indilib.i4j.androidui.I4JAndroidConfig;
import org.indilib.i4j.androidui.INDIDeviceView;
import org.indilib.i4j.androidui.R;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.client.INDIServerConnectionListener;
import org.indilib.i4j.properties.INDIGeneralProperties;
import static org.indilib.i4j.properties.INDIGeneralProperties.CONNECTION;
import static org.indilib.i4j.properties.INDIGeneralProperties.CONNECT;
import static org.indilib.i4j.properties.INDIGeneralProperties.DISCONNECT;

/**
 * An Android Activity that implements a INDI Client.
 * 
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class INDIAndroidActivity extends Activity implements INDIServerConnectionListener {

    private Handler handler;

    private TabHostWithRemovalOption tabs;

    private LinearLayout connectionTab;

    private TextView hostText;

    private EditText host;

    private TextView portText;

    private EditText port;

    private Button connectionButton;

    private Button disconnectionButton;

    private ArrayList<INDIDevice> devices;

    private INDIAndroidApplication app;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (INDIAndroidApplication) this.getApplication();

        final Context context = this.getApplicationContext();
        handler = new Handler();

        I4JAndroidConfig.setContext(context);
        I4JAndroidConfig.setHandler(handler);

        tabs = new TabHostWithRemovalOption(context);

        devices = new ArrayList<INDIDevice>();

        createConnectionView();

        // We need to check if the connection was already created (maybe the
        // activity has reestarded due to some configuration change)

        INDIServerConnection conn = app.getConnection();

        if (conn != null) { // Update the interface
            String h = conn.getURL().getHost();
            int p = conn.getURL().getPort();
            if (p <= 0) {
                p = conn.getURL().getDefaultPort();
            }

            host.setText(h); // We update the connection tab interface
            port.setText("" + p);
            connectionButton.setEnabled(false);
            disconnectionButton.setEnabled(true);

            List<INDIDevice> dds = conn.getDevicesAsList();

            for (int i = 0; i < dds.size(); i++) {
                INDIDevice d = dds.get(i);

                addD(d); // We add the interface for this device
            }

            String tabName = app.getSelectedTab();

            if (tabName != null) {
                tabs.setCurrentTabByTag(tabName);
            }

            conn.addINDIServerConnectionListener(this);
        }

        setContentView(tabs);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        app.setSelectedTab(tabs.getCurrentTabTag());
        app.getConnection().removeINDIServerConnectionListener(this);
    }

    private void createConnectionView() {
        connectionTab = new LinearLayout(this);
        connectionTab.setOrientation(LinearLayout.VERTICAL);

        hostText = new TextView(this);
        hostText.setText("Host:");
        host = new EditText(this);
        host.setSingleLine();
        host.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LinearLayout l = new LinearLayout(this);
        l.addView(hostText);
        l.addView(host);
        connectionTab.addView(l);

        portText = new TextView(this);
        portText.setText("Port:");
        port = new EditText(this);
        port.setSingleLine();
        port.setText("7624");
        port.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        l = new LinearLayout(this);
        l.addView(portText);
        l.addView(port);
        connectionTab.addView(l);

        connectionButton = new Button(this);
        connectionButton.setText("Connect");
        connectionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                connect();
            }
        });
        connectionTab.addView(connectionButton);

        disconnectionButton = new Button(this);
        disconnectionButton.setText("Disconnect");
        disconnectionButton.setEnabled(false);
        disconnectionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                disconnect();
            }
        });
        connectionTab.addView(disconnectionButton);

        TabSpec tspec = tabs.newTabSpec(CONNECTION).setIndicator("Connection", getResources().getDrawable(R.drawable.ic_network));
        tspec.setContent(new TabHost.TabContentFactory() {

            public View createTabContent(String tag) {
                return connectionTab;
            }
        });

        tabs.addTab(tspec);
    }

    private void connect() {
        String hostName = host.getText().toString();
        String portString = port.getText().toString();

        int portNumber = 7624;

        try {
            portNumber = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            return;
        }

        INDIServerConnection sc = new INDIServerConnection("xXx", hostName, portNumber);
        try {
            sc.connect();

            sc.addINDIServerConnectionListener(this);

            sc.askForDevices(); // Ask for all the devices.

            app.setConnection(sc); // We save the connection into the
                                   // application class
        } catch (IOException e) {
            return;
        }

        connectionButton.setEnabled(false);
        disconnectionButton.setEnabled(true);
    }

    public void disconnect() {
        if (app.getConnection() != null) {
            app.getConnection().disconnect();
        }
    }

    public void newDevice(INDIServerConnection connection, final INDIDevice device) {
        try {
            I4JAndroidConfig.postHandler(new Runnable() {

                @Override
                public void run() {
                    addD(device);
                }
            });
        } catch (INDIException e) {
            e.printStackTrace();
        }
    }

    private void addD(INDIDevice device) {
        try {
            final INDIDeviceView view = (INDIDeviceView) device.getDefaultUIComponent();

            TabSpec spec = tabs.newTabSpec(device.getName()).setIndicator(device.getName(), getResources().getDrawable(R.drawable.ic_gear));
            spec.setContent(new TabHost.TabContentFactory() {

                public View createTabContent(String tag) {
                    return view;
                }
            });

            tabs.addTab(spec);
        } catch (INDIException ee) {
            ee.printStackTrace();
        }

        devices.add(device);
    }

    public void removeDevice(INDIServerConnection connection, final INDIDevice device) {
        try {
            I4JAndroidConfig.postHandler(new Runnable() {

                @Override
                public void run() {
                    removeD(device);
                }
            });
        } catch (INDIException e) {
            e.printStackTrace();
        }
    }

    private void removeD(INDIDevice device) {
        devices.remove(device);
        tabs.removeTab(device.getName());
    }

    public void connectionLost(INDIServerConnection connection) {
        try {
            I4JAndroidConfig.postHandler(new Runnable() {

                @Override
                public void run() {
                    connectionHasBeenLost();
                }
            });
        } catch (INDIException e) {
            e.printStackTrace();
        }
    }

    private void connectionHasBeenLost() {
        while (!devices.isEmpty()) {
            INDIDevice d = devices.get(0);

            removeD(d);
        }

        connectionButton.setEnabled(true);
        disconnectionButton.setEnabled(false);
    }

    public void newMessage(INDIServerConnection connection, Date date, String message) {
        // Here we should include the code to handle messages
    }
}
