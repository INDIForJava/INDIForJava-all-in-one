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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.indilib.i4j.protocol.INDIProtocol;
import org.indilib.i4j.protocol.api.INDIInputStream;

/**
 * Input stream of INDIProtocol objects. deserialized from a xml stream.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIInputStreamImpl implements INDIInputStream {

    /**
     * The xstream object input stream that deserializes the INDIProtocol
     * objects.
     */
    private final ObjectInputStream in;

    /**
     * create an INDI inputstream over an object input stream.
     * 
     * @param in
     *            the object input stream
     * @throws IOException
     *             when something with the underlaying stream went wrong.
     */
    protected INDIInputStreamImpl(ObjectInputStream in) throws IOException {
        this.in = in;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public INDIProtocol<?> readObject() throws IOException {
        try {
            return (INDIProtocol<?>) in.readObject();
        } catch (EOFException e) {
            return null;
        } catch (ClassNotFoundException e) {
            throw new IOException("could not deserialize xml", e);
        }
    }
}
