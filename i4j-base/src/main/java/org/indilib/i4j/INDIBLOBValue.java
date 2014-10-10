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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.w3c.dom.Element;

/**
 * A class representing a INDI BLOB Value (some bytes and a format).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.39, October 11, 2014
 */
public class INDIBLOBValue {

    /**
     * The BLOB data.
     */
    private byte[] blobData;

    /**
     * The format of the data.
     */
    private String format;

    /**
     * The encoded data.
     */
    private String base64EncodedData;

    /**
     * Constructs a new BLOB Value from its coresponding bytes and format.
     * 
     * @param blobData
     *            the data for the BLOB
     * @param format
     *            the format of the data
     */
    public INDIBLOBValue(final byte[] blobData, final String format) {
        this.format = format;
        this.blobData = blobData;
        this.base64EncodedData = null;
    }

    /**
     * Constructs a new BLOB Value from a XML &lt;oneBLOB&gt; element.
     * 
     * @param xml
     *            the &lt;oneBLOB&gt; XML element
     * @throws IllegalArgumentException
     *             if the XML element is not correct.
     */
    public INDIBLOBValue(final Element xml) throws IllegalArgumentException {
        int size = 0;
        String f;

        try {
            String s = xml.getAttribute("size").trim();
            size = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Size number not correct");
        }

        if (!xml.hasAttribute("format")) {
            throw new IllegalArgumentException("No format attribute");
        }

        f = xml.getAttribute("format").trim();

        byte[] val;

        base64EncodedData = xml.getTextContent().trim();

        try {
            val = Base64.decode(base64EncodedData);
        } catch (IOException e) {
            base64EncodedData = null;
            throw new IllegalArgumentException("Not BASE64 coded data");
        }

        if (f.endsWith(".z")) { // gzipped. Decompress
            Inflater decompresser = new Inflater();
            decompresser.setInput(val);

            byte[] newvalue = new byte[size];

            try {
                decompresser.inflate(newvalue);

                val = newvalue;
            } catch (DataFormatException e) {
                throw new IllegalArgumentException("Not correctly GZIPped");
            }

            decompresser.end();

            f = f.substring(0, f.length() - 2);
        }

        if (val.length != size) {
            throw new IllegalArgumentException("Size of BLOB not correct");
        }

        format = f;
        blobData = val;
    }

    /**
     * Gets the BLOB data.
     * 
     * @return the BLOB data
     */
    public final byte[] getBLOBData() {
        return blobData;
    }

    /**
     * Gets the BLOB data in base64.
     * 
     * @return the BLOB data
     */
    public final String getBase64BLOBData() {
        if (base64EncodedData == null) {
            base64EncodedData = Base64.encodeBytes(getBLOBData());
        }

        return base64EncodedData;
    }

    /**
     * Gets the BLOB data format.
     * 
     * @return the BLOB data format
     */
    public final String getFormat() {
        return format;
    }

    /**
     * Gets the size of the BLOB data.
     * 
     * @return the size of the BLOB data
     */
    public final int getSize() {
        return blobData.length;
    }

    /**
     * Save the BLOB Data to a file.
     * 
     * @param file
     *            The file to which to save the BLOB data.
     * @throws IOException
     *             if there is some problem writting the file.
     */
    public final void saveBLOBData(final File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(blobData);
        }
    }
}
