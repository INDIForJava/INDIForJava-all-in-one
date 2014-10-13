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
/**
 * A convenience class to parse special INDI URIs in the form:
 * indi://[host][:port][/device[/property[/element]]] When using it, ALWAYS
 * check if the URI is correct with <code>isCorrect()</code>. The values
 * obtained from the class when the URI is not correct should not be used.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.39, October 11, 2014
 */
public class INDIURI {

    /**
     * The host in the URI. <code>127.0.0.1</code> as default value.
     */
    private String host;

    /**
     * The port in the URI. <code>7624</code> as default value.
     */
    private int port;

    /**
     * The device in the URI. <code>null</code> as default value.
     */
    private String device;

    /**
     * The property in the URI. <code>null</code> as default value.
     */
    private String property;

    /**
     * The element in the URI. <code>null</code> as default value.
     */
    private String element;

    /**
     * The correctness of the constructor URI.
     */
    private boolean correct;

    /**
     * Maximum number of parts in the URI separated by /.
     */
    private static final int MAX_PARTS = 4;

    /**
     * Creates the INDIURI and parses it. The method <code>isCorrect()</code>
     * should be called to check if the URI is correct:
     * <code>indi://[host][:port][/device[/property[/element]]]</code>
     * 
     * @param uri
     *            The URI to be parsed.
     */
    public INDIURI(final String uri) {
        host = "127.0.0.1";
        port = Constants.INDI_DEFAULT_PORT;
        device = null;
        property = null;
        element = null;

        String newUri = uri.trim();

        String prefix = "indi://";

        if (!newUri.startsWith(prefix)) {
            correct = false;
            return;
        }

        newUri = newUri.substring(prefix.length());

        String[] parts = newUri.split("/", -1);

        if (parts.length > MAX_PARTS) {
            correct = false;
            return;
        }

        String[] hostPort = parts[0].split(":", -1);

        if (hostPort.length > 2) {
            correct = false;
            return;
        }

        if (!hostPort[0].isEmpty()) {
            host = hostPort[0];
        }

        if (hostPort.length == 2) {
            if (!hostPort[1].isEmpty()) {
                try {
                    port = Integer.parseInt(hostPort[1]);
                } catch (NumberFormatException e) {
                    correct = false;
                    return;
                }
            }

            if (port <= 0) {
                correct = false;
                return;
            }
        }

        if (parts.length > 1) {
            if (!parts[1].isEmpty()) {
                device = parts[1];

                if (parts.length > 2) {
                    if (!parts[2].isEmpty()) {
                        property = parts[2];

                        if (parts.length > 3) {
                            if (!parts[3].isEmpty()) {
                                element = parts[3];
                            }
                        }
                    }
                }
            }
        }

        correct = true;
    }

    /**
     * Returns if the pared URI is correct.
     * 
     * @return <code>true</code> if the parsed URI is correct.
     *         <code>false</code> otherwise.
     */
    public final boolean isCorrect() {
        return correct;
    }

    /**
     * Gets the Host of the URI.
     * 
     * @return The Host of the URI.
     */
    public final String getHost() {
        return host;
    }

    /**
     * Gets the Port of the URI.
     * 
     * @return The Port of the URI.
     */
    public final int getPort() {
        return port;
    }

    /**
     * Gets the Device of the URI.
     * 
     * @return The Device of the URI.
     */
    public final String getDevice() {
        return device;
    }

    /**
     * Gets the Property of the URI.
     * 
     * @return The Property of the URI.
     */
    public final String getProperty() {
        return property;
    }

    /**
     * Gets the Element of the URI.
     * 
     * @return The Element of the URI.
     */
    public final String getElement() {
        return element;
    }
}
