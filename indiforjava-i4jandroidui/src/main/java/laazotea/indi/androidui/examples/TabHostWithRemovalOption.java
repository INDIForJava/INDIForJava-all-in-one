/*
 *  This file is part of INDI for Java Android UI.
 * 
 *  INDI for Java Android UI is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Android UI is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Android UI.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.androidui.examples;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import java.util.ArrayList;

/**
 * A class to handle tabs more easily.
 *
 * @version 1.32, April 20, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class TabHostWithRemovalOption extends TabHost {

  private TabWidget wid;
  private FrameLayout frameLayout;
  
  private ArrayList<TabSpec> specs;

  public TabHostWithRemovalOption(Context context) {
    super(context);

    this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    wid = new TabWidget(context);

    wid.setId(android.R.id.tabs);
    this.addView(wid, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    frameLayout = new FrameLayout(context);
    frameLayout.setId(android.R.id.tabcontent);
    frameLayout.setPadding(0, 65, 0, 0);
    this.addView(frameLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    this.setup();
    
    specs = new ArrayList<TabSpec>();
  }
  
  @Override
  public void addTab(TabSpec spec) {
    super.addTab(spec);
    
    specs.add(spec);
  }
  
  public void removeTab(String tag) {
    for (int i = specs.size() - 1 ; i >= 0 ; i--) {
      if (specs.get(i).getTag().equals(tag)) {
        specs.remove(i); 
      }
    }
    
    this.setCurrentTab(0); 
    this.clearAllTabs();
    
    for (int i = 0 ; i < specs.size() ; i++) {
      super.addTab(specs.get(i)); 
    }
  }
}
