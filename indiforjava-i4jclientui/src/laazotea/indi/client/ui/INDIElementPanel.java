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

import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.client.INDIElement;
import laazotea.indi.client.INDIElementListener;

/**
 * A panel to represent a <code>INDIElement</code>.
 *
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 * @version 1.3, April 9, 2012
 * @see INDIElement
 */
public abstract class INDIElementPanel extends javax.swing.JPanel implements INDIElementListener {
  private boolean changed;
  private boolean writable;
  private INDIPropertyPanel ipp;

  /**
   * Creates new form INDIElementPanel
   */
  protected INDIElementPanel(PropertyPermissions perm) {
    if (perm != PropertyPermissions.RO) {
      this.writable = true;
    } else {
      this.writable = false;
    }
    
    ipp = null;
    
    changed = false;
  }
  
  protected void setINDIPropertyPanel(INDIPropertyPanel ipp) {
    this.ipp = ipp;
  }
  
  protected void checkSetButton() {
    if (ipp != null) {
      ipp.checkSetButton(); 
    }
  }
  
  protected void setChanged(boolean changed) {
    this.changed = changed;
  }
  
  protected boolean isChanged() {
    return changed; 
  }

  protected boolean isWritable() {
    return writable;
  }
  
  protected abstract Object getDesiredValue();
  protected abstract INDIElement getElement();
  protected abstract void setError(boolean erroneous, String errorMessage);
  protected abstract boolean isDesiredValueErroneous();
  protected abstract void cleanDesiredValue();

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
