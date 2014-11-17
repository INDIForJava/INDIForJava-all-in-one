package org.indilib.i4j.protocol.url;

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

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * A class to handle INDI Streams.
 * 
 * @author Richard van Nieuwenhoven [ritchie [at] gmx.at]
 * @version 1.39, October 11, 2014
 */
public class INDIURLStreamHandlerFactory implements URLStreamHandlerFactory {

    /**
     * DOC MISSING.
     */
    public static void init() {
        URL.setURLStreamHandlerFactory(new INDIURLStreamHandlerFactory());
    }

    @Override
    public final URLStreamHandler createURLStreamHandler(final String protocol) {
        if ("indi".equals(protocol)) {
            return new Handler();
        }
        return null;
    }

}
