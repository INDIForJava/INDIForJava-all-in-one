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

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/**
 * A class to allow Radio Buttons to have zero elements selected.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.10, March 19, 2012
 */
public class ButtonGroupZeroOrOne extends ButtonGroup {

  @Override
  public void setSelected(ButtonModel m, boolean b) {
    if (b && m != null && m != getSelection()) {
      super.setSelected(m, b);
    } else if (!b && m == getSelection()) {
      clearSelection();
    }
  }
}
