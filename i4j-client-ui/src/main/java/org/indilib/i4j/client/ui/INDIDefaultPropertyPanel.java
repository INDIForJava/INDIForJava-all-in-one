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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.client.INDIValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default panel to represent a <code>INDIProperty</code>.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.36, November 18, 2013
 * @see INDIProperty
 */
public class INDIDefaultPropertyPanel extends INDIPropertyPanel {

    /**
     * serial Version UID.
     */
    private static final long serialVersionUID = 3224097519454125427L;

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIDefaultPropertyPanel.class);

    /**
     * the info dialog for messages.
     */
    private INDIPropertyInformationDialog infoDialog;

    /**
     * Creates new form INDIDefaultPropertyPanel.
     * 
     * @param ip
     *            property behind the pannel.
     */
    public INDIDefaultPropertyPanel(INDIProperty<?> ip) {
        super(ip);

        initComponents();

        VerticalLayout v = new VerticalLayout(5, VerticalLayout.CENTER, VerticalLayout.CENTER);
        buttons.setLayout(v);
        buttons.validate();

        updatePropertyData();

        boolean writable = false;
        if (ip.getPermission() != PropertyPermissions.RO) {
            writable = true;
        }

        if (!writable) {
            set.setVisible(false);
            buttons.remove(set);
            buttons.revalidate();
        }

        List<INDIElement> elems = (List<INDIElement>) ip.getElementsAsList();

        // In case that we have a switch property we may need a button group
        ButtonGroup bg = null;

        if (ip instanceof INDISwitchProperty) {
            INDISwitchProperty isp = (INDISwitchProperty) ip;

            if (isp.getRule() == SwitchRules.ONE_OF_MANY) {
                bg = new ButtonGroup();
            } else if (isp.getRule() == SwitchRules.AT_MOST_ONE) {
                bg = new ButtonGroupZeroOrOne();
            }
        }

        for (int i = 0; i < elems.size(); i++) {
            INDIElementPanel ep = null;

            try {
                ep = (INDIElementPanel) elems.get(i).getDefaultUIComponent();
            } catch (Exception e) {
                LOG.error("Problem with library. Should not happen unless errors in Client library", e);
                System.exit(-1);
            }

            if (bg != null) {
                ((INDISwitchElementPanel) ep).setButtonGroup(bg);
            }

            ep.setINDIPropertyPanel(this);

            addElementPanel(ep);
        }
    }

    @Override
    protected void checkSetButton() {
        boolean enabled = true;
        boolean changed = false;

        for (int i = 0; i < elements.getComponentCount(); i++) {
            INDIElementPanel elp = (INDIElementPanel) elements.getComponent(i);

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

        set.setEnabled(enabled);
    }

    /**
     * update the representation of the property.
     */
    private void updatePropertyData() {
        name.setText(getProperty().getLabel());
        name.setToolTipText(getProperty().getName());

        PropertyStates st = getProperty().getState();

        if (st == PropertyStates.IDLE) {
            state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_idle.png"))); // NOI18N
        } else if (st == PropertyStates.OK) {
            state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_ok.png"))); // NOI18N
        } else if (st == PropertyStates.BUSY) {
            state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_busy.png"))); // NOI18N
        } else if (st == PropertyStates.ALERT) {
            state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_alert.png"))); // NOI18N
        }
    }

    /**
     * add a pannel for an element.
     * 
     * @param panel
     *            the pannel to add.
     */
    private void addElementPanel(INDIElementPanel panel) {
        elements.add(panel);

        elements.validate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        state = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        name = new javax.swing.JLabel();
        elements = new javax.swing.JPanel();
        buttons = new javax.swing.JPanel();
        set = new javax.swing.JButton();
        information = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        setLayout(new java.awt.BorderLayout(10, 0));

        jPanel1.setLayout(new java.awt.BorderLayout(5, 0));

        state.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/light_idle.png"))); // NOI18N
        jPanel1.add(state, java.awt.BorderLayout.WEST);

        jPanel5.setLayout(new java.awt.BorderLayout());
        jPanel5.add(name, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel5, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.WEST);

        elements.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        elements.setLayout(new javax.swing.BoxLayout(elements, javax.swing.BoxLayout.Y_AXIS));
        add(elements, java.awt.BorderLayout.CENTER);

        buttons.setLayout(new java.awt.BorderLayout(0, 5));

        set.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/tick.png"))); // NOI18N
        set.setText("Set");
        set.setEnabled(false);
        set.setMargin(new java.awt.Insets(1, 14, 1, 14));
        set.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setActionPerformed(evt);
            }
        });
        buttons.add(set, java.awt.BorderLayout.CENTER);

        information.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/indilib/i4j/client/ui/images/information.png"))); // NOI18N
        information.setText("Info");
        information.setToolTipText("Information about the property");
        information.setMargin(new java.awt.Insets(0, 0, 0, 0));
        information.setPreferredSize(new java.awt.Dimension(50, 16));
        information.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                informationActionPerformed(evt);
            }
        });
        buttons.add(information, java.awt.BorderLayout.SOUTH);

        add(buttons, java.awt.BorderLayout.EAST);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * open the info dialog.
     * 
     * @param evt
     *            event triggering this action.
     */
    private void informationActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_informationActionPerformed
        if (infoDialog == null) {
            infoDialog = new INDIPropertyInformationDialog((JFrame) SwingUtilities.getWindowAncestor(this), false, getProperty());
        }
        infoDialog.showDialog();
    } // GEN-LAST:event_informationActionPerformed

    /**
     * send the desired values.
     * 
     * @param evt
     *            the event triggering the action.
     */
    private void setActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_setActionPerformed

        for (int i = 0; i < elements.getComponentCount(); i++) {
            INDIElementPanel elp = (INDIElementPanel) elements.getComponent(i);

            if (elp.isChanged()) {
                try {
                    elp.getElement().setDesiredValue(elp.getDesiredValue());
                } catch (INDIValueException e) {
                    LOG.error("value exception", e);
                    return;
                }
            }
        }

        try {
            getProperty().sendChangesToDriver();

            cleanDesiredValues();
        } catch (INDIValueException e) {
            INDIElement errorElement = e.getINDIElement();

            for (int i = 0; i < elements.getComponentCount(); i++) {
                INDIElementPanel elp = (INDIElementPanel) elements.getComponent(i);

                if (errorElement == elp.getElement()) {
                    elp.setError(true, e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.error("io exception", e);
        }
    } // GEN-LAST:event_setActionPerformed

    /**
     * the form was resized, rearage the element names.
     * 
     * @param evt
     *            the event triggereing the change.
     */
    private void formComponentResized(java.awt.event.ComponentEvent evt) { // GEN-FIRST:event_formComponentResized
        resizeElementNames();
    } // GEN-LAST:event_formComponentResized

    /**
     * resize the element names to the max size of all elements.
     */
    private void resizeElementNames() {
        int size = 0;

        for (int i = 0; i < elements.getComponentCount(); i++) {
            INDIElementPanel ep = (INDIElementPanel) elements.getComponent(i);
            if (size < ep.getNameSize()) {
                size = ep.getNameSize();
            }
        }
        for (int i = 0; i < elements.getComponentCount(); i++) {
            INDIElementPanel ep = (INDIElementPanel) elements.getComponent(i);
            ep.setNameSize(size);
        }
    }

    /**
     * clean the desired values.
     */
    private void cleanDesiredValues() {
        for (int i = 0; i < elements.getComponentCount(); i++) {
            INDIElementPanel elp = (INDIElementPanel) elements.getComponent(i);

            elp.cleanDesiredValue();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttons;

    private javax.swing.JPanel elements;

    private javax.swing.JButton information;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JLabel name;

    private javax.swing.JButton set;

    private javax.swing.JLabel state;

    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChanged(INDIProperty<?> property) {
        if (property == getProperty()) {
            updatePropertyData();
        }
    }
}
