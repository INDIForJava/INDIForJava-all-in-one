package org.indilib.i4j.protocol.io;

/*
 * #%L
 * INDI Protocol implementation
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.CustomObjectOutputStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StatefulWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.indilib.i4j.protocol.*;
import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.protocol.api.INDIOutputStream;

import java.io.*;
import java.util.Enumeration;
import java.util.Map;

/**
 * Xstream definitions factory for the indi protocol.
 *
 * @author Richard van Nieuwenhoven
 */
public final class INDIProtocolFactory {

    /**
     * Buffer to use for the core in and output streams.
     */
    private static final int BUFFER_SIZE = 16 * 1024;

    /**
     * bytes for a dummy root close tag.
     */
    private static final byte[] CLOSE_BYTES = "</X>".getBytes();

    /**
     * bytes for a dummy root open tag.
     */
    private static final byte[] OPEN_BYTES = "<X>".getBytes();

    /**
     * the XSTREAM driver to use.
     */
    private static final HierarchicalStreamDriver STREAM_DRIVER;

    /**
     * static cache of the XSTREAM INDI protokol instance.
     */
    private static final XStream XSTREAM;

    static {
        STREAM_DRIVER = new XppDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new Printwriter(out);
            }
        };
        XSTREAM = new XStream(STREAM_DRIVER);
        XSTREAM.processAnnotations(new Class<?>[]{
                DefBlob.class,
                DefBlobVector.class,
                DefElement.class,
                DefLight.class,
                DefLightVector.class,
                DefNumber.class,
                DefNumberVector.class,
                DefSwitch.class,
                DefSwitchVector.class,
                DefText.class,
                DefTextVector.class,
                DefVector.class,
                DelProperty.class,
                EnableBLOB.class,
                GetProperties.class,
                INDIProtocol.class,
                Message.class,
                NewBlobVector.class,
                NewLightVector.class,
                NewNumberVector.class,
                NewSwitchVector.class,
                NewTextVector.class,
                NewVector.class,
                OneBlob.class,
                OneElement.class,
                OneLight.class,
                OneNumber.class,
                OneText.class,
                OneSwitch.class,
                SetBlobVector.class,
                SetLightVector.class,
                SetNumberVector.class,
                SetSwitchVector.class,
                SetTextVector.class,
                SetVector.class
        });
    }

    /**
     * untility class never instanciated.
     */
    private INDIProtocolFactory() {
    }

    /**
     * create an indi protocol input stream around an input stream.
     *
     * @param in the underlaying input stream where the xml will be read.
     * @return the resultung indi input stream
     * @throws IOException when something went wrong with the underlaying intput stream.
     */
    public static INDIInputStream createINDIInputStream(InputStream in) throws IOException {
        return new INDIInputStreamImpl(XSTREAM.createObjectInputStream(inputStreamWithRootTag(new BufferedInputStream(new MinimalBlockinInputStream(in), BUFFER_SIZE))));
    }

    /**
     * create an indi protocol output stream around an output stream.
     *
     * @param out the underlaying output stream where the xml will be written.
     * @return the resultung indi output stream
     * @throws IOException when something went wrong with the underlaying output stream.
     */
    public static INDIOutputStream createINDIOutputStream(OutputStream out) throws IOException {
        final StatefulWriter statefulWriter = new StatefulWriter(STREAM_DRIVER.createWriter(new BufferedOutputStream(out, BUFFER_SIZE)));
        return new INDIOutputStreamImpl(new CustomObjectOutputStream(new CustomObjectOutputStream.StreamCallback() {
            @Override
            public void close() {
                if (statefulWriter.state() != StatefulWriter.STATE_CLOSED) {
                    statefulWriter.close();
                }
            }

            @Override
            public void defaultWriteObject() throws NotActiveException {
                throw new NotActiveException("not in call to writeObject");
            }

            @Override
            public void flush() {
                statefulWriter.flush();
            }

            @Override
            public void writeFieldsToStream(Map fields) throws NotActiveException {
                throw new NotActiveException("not in call to writeObject");
            }

            @Override
            public void writeToStream(Object object) {
                XSTREAM.marshal(object, statefulWriter);
            }
        }));
    }

    /**
     * create an combined input stream around the input stream parameter that
     * creates a root tag around the xml parts in the input stream.
     *
     * @param in the underlying input stream where the xml's will be read.
     * @return the resulting input stream
     */
    private static InputStream inputStreamWithRootTag(final InputStream in) {
        Enumeration<InputStream> streamEnum = new Enumeration<InputStream>() {

            private final InputStream[] streams = {
                    new ByteArrayInputStream(OPEN_BYTES), in,
                    new ByteArrayInputStream(CLOSE_BYTES)};
            private int index = 0;

            @Override
            public boolean hasMoreElements() {
                return index < streams.length;
            }

            @Override
            public InputStream nextElement() {
                return streams[index++];
            }
        };
        return new SequenceInputStream(streamEnum);
    }

    /**
     * nice toString for the protocol objects.
     *
     * @param protocol the protocol object.
     * @return the toString.
     */
    public static String toString(INDIProtocol<?> protocol) {
        return protocol.getClass().getSimpleName() + " " + XSTREAM.toXML(protocol);
    }

    /**
     * simple toXml for the protocol objects.
     *
     * @param protocol the protocol object.
     * @return the toString.
     */
    public static String toXml(INDIProtocol<?> protocol) {
        return XSTREAM.toXML(protocol);
    }
}