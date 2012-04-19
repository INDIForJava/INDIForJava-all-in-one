/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package laazotea.indi.androidui.examples;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import java.util.ArrayList;

/**
 *
 * @author zerjillo
 */
public class TabHostWithRemovalOption extends TabHost {

  private TabWidget wid;
  private FrameLayout frameLayout;
  
  private ArrayList<TabSpec> specs;

  public TabHostWithRemovalOption(Context context) {
    super(context);

    this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

    wid = new TabWidget(context);

    wid.setId(android.R.id.tabs);
    this.addView(wid, new LinearLayout.LayoutParams(
            LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

    frameLayout = new FrameLayout(context);
    frameLayout.setId(android.R.id.tabcontent);
    frameLayout.setPadding(0, 65, 0, 0);
    this.addView(frameLayout, new LinearLayout.LayoutParams(
            LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

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
