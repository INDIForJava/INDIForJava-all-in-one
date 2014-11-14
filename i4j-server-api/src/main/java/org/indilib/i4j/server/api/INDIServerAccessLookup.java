package org.indilib.i4j.server.api;

/*
 * #%L
 * INDI for Java Server API
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
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

import java.util.ServiceLoader;

/**
 * Utility class to load the server access interface from the service loader
 * system.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class INDIServerAccessLookup {

    /**
     * the curremt service loader.
     */
    private static ServiceLoader<INDIServerAccess> serviceLoader;

    /**
     * private constructor to protect utility classes.
     */
    private INDIServerAccessLookup() {
    }

    /**
     * @return the global indi server access interface.
     */
    public static INDIServerAccess indiServerAccess() {
        if (serviceLoader == null) {
            serviceLoader = ServiceLoader.load(INDIServerAccess.class);
        }
        return serviceLoader.iterator().next();
    }
}
