package net.sf.marineapi.nmea.parser;

/*
 * #%L
 * INDI for Java NMEA 0183 stream driver
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.Measurement;

/**
 * Workaround bug https://github.com/ktuukkan/marine-api/issues/31
 * 
 * @author Richard van Nieuwenhoven
 */
public class XDRParserFixed extends XDRParser {

    /**
     * length of each data set is 4 fields
     */
    private static int DATA_SET_LENGTH = 4;

    /**
     * Creates new instance of XDRParser.
     * 
     * @param nmea
     *            XDR sentence string
     */
    public XDRParserFixed(String nmea) {
        super(nmea);
    }

    /**
     * Creates XDR parser with empty sentence.
     * 
     * @param talker
     *            TalkerId to set
     */
    public XDRParserFixed(TalkerId talker) {
        super(talker);
    }

    @Override
    public List<Measurement> getMeasurements() {
        List<Measurement> result = new ArrayList<Measurement>();
        for (int i = 0; i <= getFieldCount(); i += DATA_SET_LENGTH) {
            Measurement value = fetchValues(i);
            if (!value.isEmpty()) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Fetch data set starting at given index.
     * 
     * @param i
     *            Start position of data set, i.e. index of first data field.
     * @return XDRValue object
     */
    private Measurement fetchValues(int i) {
        try {
            Method method = XDRParser.class.getDeclaredMethod("fetchValues", int.class);
            method.setAccessible(true);
            return (Measurement) method.invoke(this, i);
        } catch (Exception e) {
            return null;
        }
    }

}
