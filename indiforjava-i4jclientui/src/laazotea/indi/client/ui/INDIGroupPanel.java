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

import java.util.ArrayList;
import laazotea.indi.client.INDIProperty;

/**
 * A panel to join a group of
 * <code>INDIDefaultPropertyPanel</code>s.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.21, April 4, 2012
 * @see INDIDefaultPropertyPanel
 */
public class INDIGroupPanel extends javax.swing.JPanel {

  private ArrayList<INDIPropertyPanel> propertyPanels;

  /**
   * Creates new form INDIGroupPanel
   */
  public INDIGroupPanel() {
    initComponents();

    propertyPanels = new ArrayList<INDIPropertyPanel>();
  }

  public void addProperty(INDIProperty ip) {
    INDIPropertyPanel pp = null;
    try {
      pp = (INDIPropertyPanel) ip.getDefaultUIComponent();
    } catch (Exception e) { // Problem with library. Should not happen unless errors in Client library
      e.printStackTrace();
      System.exit(-1);
    }

    propertyPanels.add(pp);

    centralPanel.add(pp);

    revalidate();
  }

  public void removeProperty(INDIProperty ip) {
    for (int i = 0 ; i < propertyPanels.size() ; i++) {
      INDIPropertyPanel pp = propertyPanels.get(i);

      if (pp.getProperty() == ip) {
        centralPanel.remove(pp);
        propertyPanels.remove(pp);

        break;
      }
    }

    revalidate();
  }

  public int getPropertyCount() {
    return propertyPanels.size();
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scroll = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        centralPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        centralPanel.setLayout(new javax.swing.BoxLayout(centralPanel, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(centralPanel, java.awt.BorderLayout.NORTH);

        scroll.setViewportView(jPanel1);

        add(scroll, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centralPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane scroll;
    // End of variables declaration//GEN-END:variables
}
