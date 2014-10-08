/*
 *  This file is part of INDI for Java Client UI.
 * 
 *  INDI for Java Client UI is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Client UI is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Client UI.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.client.ui;

/*
 * #%L
 * INDI for Java Client UI Library
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;

import org.indilib.i4j.Constants;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDISwitchElement;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.client.INDIValueException;
import org.indilib.i4j.client.ui.examples.SimpleINDIFrameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel to represent a the standard CONNECTION Property.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.2, March 26, 2012
 * @see INDIProperty
 */
public class INDIConnectionPropertyPanel extends INDIPropertyPanel implements INDIElementListener {

    private static final Logger LOG = LoggerFactory.getLogger(INDIConnectionPropertyPanel.class);

    private INDISwitchElement connectedE;

    private INDISwitchElement disconnectedE;

    /**
     * Creates new form <code>INDIConnectionPropertyPanel</code>.
     * 
     * @param property
     *            the CONNECTION property.
     * @throws INDIException
     *             if the <code>property</code> is not a correct CONNECTION
     *             Property.
     */
    public INDIConnectionPropertyPanel(INDISwitchProperty property) throws INDIException {
        super(property);

        connectedE = property.getElement("CONNECT");
        if (connectedE == null) {
            throw new INDIException("CONNECT Element not present");
        }
        connectedE.addINDIElementListener(this);

        disconnectedE = property.getElement("DISCONNECT");
        if (disconnectedE == null) {
            throw new INDIException("DISCONNECT Element not present");
        }
        disconnectedE.addINDIElementListener(this);

        initComponents();

        setButtonStatus();
        updatePropertyData();
    }

    /**
     * Updates the selected and text of the button
     */
    private void setButtonStatus() {
        SwitchStatus ss = connectedE.getValue();

        if (ss == SwitchStatus.ON) {
            button.setSelected(true);
            button.setText("Connected");
        } else {
            button.setSelected(false);
            button.setText("Disconnected");
        }
    }

    private void updatePropertyData() {
        Constants.PropertyStates st = getProperty().getState();

        if (st == Constants.PropertyStates.IDLE) {
            state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_idle.png"))); // NOI18N
        } else if (st == Constants.PropertyStates.OK) {
            state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_ok.png"))); // NOI18N
        } else if (st == Constants.PropertyStates.BUSY) {
            state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_busy.png"))); // NOI18N
        } else if (st == Constants.PropertyStates.ALERT) {
            state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_alert.png"))); // NOI18N
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        button = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        state = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new java.awt.BorderLayout(5, 0));

        button.setText("Disconnected");
        button.setToolTipText("CONNECTION");
        button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });
        add(button, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_idle.png"))); // NOI18N
        jPanel1.add(state, java.awt.BorderLayout.WEST);

        add(jPanel1, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_buttonActionPerformed
        try {
            if (button.isSelected()) {
                connectedE.setDesiredValue(SwitchStatus.ON);
                disconnectedE.setDesiredValue(SwitchStatus.OFF);
            } else {
                connectedE.setDesiredValue(SwitchStatus.OFF);
                disconnectedE.setDesiredValue(SwitchStatus.ON);
            }
            getProperty().sendChangesToDriver();
        } catch (INDIValueException e) {
            LOG.error("value exception", e);
        } catch (IOException e) {
            LOG.error("io exception", e);
        }

        updatePropertyData();
    }// GEN-LAST:event_buttonActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables

    private javax.swing.JToggleButton button;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JLabel state;

    // End of variables declaration//GEN-END:variables

    @Override
    public void elementChanged(INDIElement element) {
        // setButtonStatus();
    }

    @Override
    public void propertyChanged(INDIProperty property) {
        setButtonStatus();
        updatePropertyData();
    }

    @Override
    protected void checkSetButton() {
    }
}
