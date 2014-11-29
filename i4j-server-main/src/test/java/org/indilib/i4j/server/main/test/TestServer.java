package org.indilib.i4j.server.main.test;

import org.indilib.i4j.server.main.INDIBasicServer;

/*
 * #%L
 * INDI for Java Server Examples
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

/**
 * This is a test server to make an easy start from the developing tools
 * possible.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class TestServer {

    /**
     * private constructor to prohibit instanciation.
     */
    private TestServer() {
    }

    /**
     * std main method to start the test server. The server will be started in
     * the interactive mode.
     * 
     * @param args
     *            ignored arguments.
     */
    public static void main(String[] args) {
        INDIBasicServer.main(new String[]{
            "-i"
        });
    }
}
