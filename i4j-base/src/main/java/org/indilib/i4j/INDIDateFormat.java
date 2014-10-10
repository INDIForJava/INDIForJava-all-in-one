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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple class to format and parse INDI timestamps.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.39, October 11, 2014
 */
public final class INDIDateFormat {

    /**
     * The first possible format for INDI timestamps.
     */
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * The second possible format for INDI timestamps.
     */
    private static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * A private constructor to avoid instantiating this utility class.
     */
    private INDIDateFormat() {
    }

    /**
     * Parses a timestamp expressed in the INDI format. If the timestamp does
     * not have the correct format it returns the current timestamp.
     * 
     * @param time
     *            the timestamp to be parsed
     * @return the parsed timestamp or the current timestamp if the format of
     *         the <code>time</code> is not correct.
     */
    public static Date parseTimestamp(final String time) {
        Date timestamp = new Date();

        String newTime = time.trim();

        if (!(newTime.length() == 0)) {
            try {
                timestamp = dateFormat.parse(newTime);
            } catch (ParseException e) {
                try {
                    timestamp = dateFormat2.parse(newTime);
                } catch (ParseException ee) {
                    timestamp = new Date(); // Not correct format, returning
                                            // current timestamp.
                }
            }
        }

        return timestamp;
    }

    /**
     * Formats a timestamp according to the INDI format.
     * 
     * @param timestamp
     *            the timestamp to be formmated
     * @return the formatted timestamp
     */
    public static String formatTimestamp(final Date timestamp) {
        return dateFormat.format(timestamp);
    }

    /**
     * Gets the current timestamp in form of a String according to the INDI
     * specification.
     * 
     * @return the current timestamp according to the INDI specification.
     */
    public static String getCurrentTimestamp() {
        return formatTimestamp(new Date());
    }
}
