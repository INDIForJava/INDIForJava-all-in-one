package org.indilib.i4j.protocol.api;

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

import org.indilib.i4j.protocol.INDIProtocol;

import java.io.IOException;

/**
 * INDI Input stream interface to read indi protocol object from a stream.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface INDIInputStream {

    /**
     * closes the underlying input stream.
     * 
     * @throws IOException
     *             when something went wrong with the underlying input stream.
     */
    void close() throws IOException;

    /**
     * @return the next indi protocol object from the stream. (blocking till the
     *         next is available or null at end of stream)
     * @throws IOException
     *             if something went wrong with the deserialization or the
     *             underlying stream.
     */
    INDIProtocol<?> readObject() throws IOException;
}
