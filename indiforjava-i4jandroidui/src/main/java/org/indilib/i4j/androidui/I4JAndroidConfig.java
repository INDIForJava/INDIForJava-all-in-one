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
package org.indilib.i4j.androidui;

import android.content.Context;
import android.os.Handler;
import org.indilib.i4j.INDIException;

/**
 * A class to centralize a Handler and Context for all INDI Android Views.
 *
 * @version 1.3, April 9, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public class I4JAndroidConfig {

  private static Context context = null;
  private static Handler handler = null;

  /**
   * Sets the global Context for all INDI Views.
   *
   * @param newContext the global Context.
   */
  public static void setContext(Context newContext) {
    context = newContext;
  }

  /**
   * Sets the global Hanlder for all views being updated from different Threads.
   *
   * @param newHandler the global Handler.
   */
  public static void setHandler(Handler newHandler) {
    handler = newHandler;
  }

  /**
   * Gets the global Context.
   *
   * @return the global Context.
   * @throws INDIException if the global Context has not yet been set.
   */
  public static Context getContext() throws INDIException {
    if (context == null) {
      throw new INDIException("I4JAndroidConfig context not set");
    }

    return context;
  }

  /**
   * Gets the global Handler.
   *
   * @return the global Handler
   * @throws INDIException if the global Handler has not yet been set.
   */
  public static Handler getHandler() throws INDIException {
    if (handler == null) {
      throw new INDIException("I4JAndroidConfig handler not set");
    }

    return handler;
  }

  /**
   * Post a Runnable object to the global Handler.
   *
   * @param run the Runnable to post.
   * @throws INDIException if the global Handler has not yet been set.
   */
  public static void postHandler(Runnable run) throws INDIException {
    Handler h = getHandler();

    h.post(run);
  }
}
