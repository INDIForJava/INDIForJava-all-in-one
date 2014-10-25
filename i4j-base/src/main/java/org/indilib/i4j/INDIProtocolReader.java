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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A class that reads from a input stream and sends the read messages to a
 * parser.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.39, October 11, 2014
 */
public class INDIProtocolReader extends Thread {

    /**
     * A logger for the errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIProtocolReader.class);

    /**
     * The parser to which the messages will be sent.
     */
    private INDIProtocolParser parser;

    /**
     * Used to friendly stop the reader.
     */
    private boolean stop;

    /**
     * Creates the reader.
     * 
     * @param parser
     *            The parser to which the readed messages will be sent.
     */
    public INDIProtocolReader(final INDIProtocolParser parser) {
        this.parser = parser;
    }

    /**
     * The main body of the reader.
     */
    @Override
    public final void run() {
        DocumentBuilderFactory docBuilderFactory;

        DocumentBuilder docBuilder;
        try {
            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docBuilderFactory.newDocumentBuilder();
            docBuilder.setErrorHandler(new ErrorHandler() {

                @Override
                public void warning(final SAXParseException e) throws SAXException {
                }

                @Override
                public void fatalError(final SAXParseException e) throws SAXException {
                }

                @Override
                public void error(final SAXParseException e) throws SAXException {
                }
            });
        } catch (Exception e) {
            LOG.error("Could not parse doc", e);
            return;
        }

        StringBuffer bufferedInput = new StringBuffer();

        char[] buffer = new char[Constants.BUFFER_SIZE];

        stop = false;

        BufferedReader in = new BufferedReader(new InputStreamReader(parser.getInputStream(), Charsets.UTF_8));

        try {
            while (!stop) {
                int nReaded = in.read(buffer, 0, Constants.BUFFER_SIZE);

                if (nReaded != -1) {
                    bufferedInput.append(buffer, 0, nReaded); // Appending to
                    // the buffer

                    boolean errorParsing = false;

                    try {
                        String d = "<INDI>" + bufferedInput + "</INDI>";
                        // System.err.println(d);
                        d = d.replaceAll("\\<\\?xml version='...'\\?\\>", "");
                        d = d.replaceAll("\\<\\?xml version=\"...\"\\?\\>", "");
                        // System.err.println(d);

                        Document doc = docBuilder.parse(new InputSource(new StringReader(d)));

                        parser.parseXML(doc);
                    } catch (SAXException e) {
                        errorParsing = true;
                    }

                    if (!errorParsing) {
                        bufferedInput.setLength(0); // Empty the buffer because
                                                    // it has been already
                                                    // parsed
                    }
                } else { // If -1 readed, end
                    stop = true;
                }
            }
        } catch (IOException e) {
            LOG.error("could not parse doc", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LOG.error("Could not close Doc", e);
            }
        }

        parser.finishReader();
    }

    /**
     * Sets the stop parameter. If set to <code>true</code> the reader will
     * gracefully stop after the next read.
     * 
     * @param stop
     *            The stop parameter
     */
    public final void setStop(final boolean stop) {
        this.stop = stop;
    }
}
