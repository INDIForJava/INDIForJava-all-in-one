package org.indilib.i4j.indi.nbp.integration;

/*
 * #%L
 * INDI for JAVA - NetBeans Platform Integration
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.openide.util.URLStreamHandlerRegistration;

/**
 * @author jhudziak
 */
@URLStreamHandlerRegistration(protocol = {
    "indi"
})
public class INDIURLStreamHandlerFactoryNBPRegistration extends URLStreamHandler {

    public INDIURLStreamHandlerFactoryNBPRegistration() {
        // #71 INDIURLStreamHandlerFactory and NetbeansPlatfrom application
        // http://sourceforge.net/p/indiforjava/tickets/71/
        System.out.println("INDIURLStreamHandlerFactoryNBPRegistration switching off INDIURLStreamHandlerFactory");
        System.setProperty("INDIURLStreamHandlerFactory.auto.register", "false");
        System.out.println("INDIURLStreamHandlerFactory.auto.register:" + System.getProperty("INDIURLStreamHandlerFactory.auto.register"));
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new NBIndiUrlConnection(u);
    }

}
