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
package laazotea.indi.client.ui;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.ButtonGroup;
import javax.swing.UIManager;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.client.INDIElement;
import laazotea.indi.client.INDISwitchElement;

/**
 * A panel to represent a
 * <code>INDISwitchElement</code>.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.3, April 9, 2012
 * @see INDISwitchElement
 */
public class INDISwitchElementPanel extends INDIElementPanel {

  private INDISwitchElement se;

  /**
   * Creates new form INDISwitchElementPanel
   */
  public INDISwitchElementPanel(INDISwitchElement se, PropertyPermissions perm) {
    super(perm);

    initComponents();

    if (!isWritable()) {
      desiredValue.setVisible(false);
      ((GridLayout) mainPanel.getLayout()).setColumns(2);
      mainPanel.remove(desiredValue);
    }

    this.se = se;

    updateElementDataInitial();
  }

  protected void setButtonGroup(ButtonGroup buttonGroup) {
    buttonGroup.add(desiredValue);
  }

  private void updateElementDataInitial() {
    name.setText(se.getLabel());
    name.setToolTipText(se.getName());

    SwitchStatus ss = (SwitchStatus) se.getValue();

    if (ss == SwitchStatus.OFF) {
      currentValue.setText("");
      currentValue.setBackground(UIManager.getColor("Label.background"));
      desiredValue.setSelected(false);
    } else {
      currentValue.setText("SELECTED");
      currentValue.setBackground(Color.GREEN);
      desiredValue.setSelected(true);
    }
  }

  private void updateElementData() {
    name.setText(se.getLabel());
    name.setToolTipText(se.getName());

    SwitchStatus ss = (SwitchStatus) se.getValue();

    if (ss == SwitchStatus.OFF) {
      currentValue.setText("");
      currentValue.setBackground(UIManager.getColor("Label.background"));
    } else {
      currentValue.setText("SELECTED");
      currentValue.setBackground(Color.GREEN);
    }
  }

  @Override
  protected boolean isChanged() {
    return true; // Always changed: all will be send 
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        name = new javax.swing.JLabel();
        currentValue = new javax.swing.JTextField();
        desiredValue = new javax.swing.JToggleButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        name.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        mainPanel.add(name);

        currentValue.setEditable(false);
        currentValue.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        currentValue.setToolTipText("Current Value");
        currentValue.setMinimumSize(new java.awt.Dimension(4, 16));
        mainPanel.add(currentValue);

        desiredValue.setText("Select");
        desiredValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                desiredValueActionPerformed(evt);
            }
        });
        mainPanel.add(desiredValue);

        add(mainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

  private void desiredValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_desiredValueActionPerformed
    setChanged(true);

    checkSetButton();
  }//GEN-LAST:event_desiredValueActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField currentValue;
    private javax.swing.JToggleButton desiredValue;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel name;
    // End of variables declaration//GEN-END:variables

  @Override
  protected Object getDesiredValue() {
    if (desiredValue.isSelected()) {
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
    // desiredValue.setSelected(false);
  }

  @Override
  public void elementChanged(INDIElement element) {
    if (element == se) {
      updateElementData();
    }
  }
}
