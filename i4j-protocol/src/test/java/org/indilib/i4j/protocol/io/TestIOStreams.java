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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.indilib.i4j.protocol.INDIProtocol;
import org.indilib.i4j.protocol.OneBlob;
import org.indilib.i4j.protocol.OneText;
import org.indilib.i4j.protocol.SetTextVector;
import org.indilib.i4j.protocol.api.INDIInputStream;
import org.indilib.i4j.protocol.api.INDIOutputStream;
import org.junit.Test;

/**
 * Test the encode and decode indi protokol streams.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TestIOStreams {

    /**
     * Test the encode and decode streams.
     * 
     * @throws Exception
     */
    @Test
    public void testEncodeDecode() throws Exception {
        String expected =
                "<oneBLOB name=\"hugo\" size=\"200\">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</oneBLOB><oneText name=\"hugo\">XXX</oneText><oneText name=\"hugo\"></oneText><oneText n"
                        + "ame=\"hugo\"></oneText><setTextVector device=\"ZZ\"><oneBLOB name=\"hugo\" size=\"200\">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</oneBLOB><oneText name=\"hugo\">XXX</on"
                        + "eText><oneText name=\"hugo\"></oneText><oneText name=\"hugo\"></oneText></setTextVector>";
        OneBlob elem1 = new OneBlob().setByteContent(new byte[200]).setName("hugo");
        OneText elem2 = new OneText().setTextContent("XXX").setName("hugo");
        OneText elem3 = new OneText().setName("hugo");
        OneText elem4 = new OneText().setName("hugo");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        INDIOutputStream out = INDIProtocolFactory.createINDIOutputStream(bytes);
        out.writeObject(elem1);
        out.writeObject(elem2);
        out.writeObject(elem3);
        out.writeObject(elem4);

        SetTextVector list = new SetTextVector()//
                .setDevice("ZZ")//
                .addElement(elem1)//
                .addElement(elem2)//
                .addElement(elem3)//
                .addElement(elem4);
        out.writeObject(list);
        out.close();

        INDIInputStream in = INDIProtocolFactory.createINDIInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        INDIProtocol<?> object;
        out = INDIProtocolFactory.createINDIOutputStream(System.out);
        while ((object = in.readObject()) != null) {
            out.writeObject(object);
        }
        in.close();
    }
}
