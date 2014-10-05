
package org.indilib.i4j;

/*
 * #%L
 * INDI for Java Base Library
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

import java.io.File;

/**
 * A class to help dealing with Files and Directories.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.39, October 4, 2014
 */
public class FileUtils {
  
  /**
   * Gets the base directory while auxiliary files for the I4J library should
   * be stored. This directory is ~/.i4j . In case of that directory not 
   * existing, the directory is created. Every axiliary file produced by the 
   * library should be written in this directory.
   * 
   * @return The base directory for I4J auxiliary files.
   */
  public static File getI4JBaseDirectory() {
    String userDirName = System.getProperty("user.home");
    
    File userDir = new File(userDirName);
    
    File i4jDir = new File(userDir, ".i4j");
    
    if (!i4jDir.exists()) {
      i4jDir.mkdir();
    }
    
    return i4jDir;
  }
}
