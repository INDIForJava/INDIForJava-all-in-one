/*
 *  This file is part of INDI for Java Seletek Driver.
 * 
 *  INDI for Java Seletek Driver is free software: you can 
 *  redistribute it and/or modify it under the terms of the GNU General Public 
 *  License as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Seletek Driver is distributed in the hope that it
 *  will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Seletek Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.driver.seletek;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * INDI for Java Driver for the Seletek
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

/**
 * Some utility functions.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.35, November 11, 2013
 */
public final class Utils {

    /**
     * Utility class, prohibit instanciation.
     */
    private Utils() {
    }

    /**
     * logger to use.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    /**
     * Sleep for some time.
     * 
     * @param milis
     *            The number of miliseconds to sleep
     */
    protected static void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            LOG.error("sleep interupted!", e);
        }
    }

    /**
     * Get the portname for a port number.
     * 
     * @param seletekPort
     *            the port number.
     * @return the name of the port.
     */
    protected static String getSeletekPortName(int seletekPort) {
        if (seletekPort == 0) {
            return "Main";
        } else if (seletekPort == 1) {
            return "Exp";
        }

        return "Third";
    }
}
