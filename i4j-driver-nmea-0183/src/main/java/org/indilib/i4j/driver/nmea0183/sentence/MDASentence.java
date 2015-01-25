package org.indilib.i4j.driver.nmea0183.sentence;

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

import net.sf.marineapi.nmea.sentence.Sentence;

/**
 * Meteorological Composite - Barometric pressure, air and water temperature,
 * humidity, dew point and wind speed and direction relative to the surface of
 * the earth.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface MDASentence extends Sentence {

    /**
     * @return Barometric pressure, bars, to the nearest .001 bar. NaN if not
     *         available.
     */
    double getBarometricPressure();

    /**
     * @return Air temperature, degrees C, to the nearest 0,1 degree C. NaN if
     *         not available.
     */
    double getAirTemperature();

    /**
     * @return Water temperature, degrees C. NaN if not available.
     */
    double getWaterTemperature();

    /**
     * @return Relative humidity, percent, to the nearest 0,1 percent. NaN if
     *         not available.
     */
    double getRelativeHumidity();

    /**
     * @return Absolute humidity, percent, to the nearest 0,1 percent. NaN if
     *         not available.
     */
    double getAbsoluteHumidity();

    /**
     * @return Dew point, degrees C, to the nearest 0,1 degree C. NaN if not
     *         available.
     */
    double getDewPoint();

    /**
     * @return Wind direction, degrees True, to the nearest 0,1 degree. NaN if
     *         not available.
     */
    double getTrueWindDirection();

    /**
     * @return Wind direction, degrees True, to the nearest 0,1 degree. NaN if
     *         not available.
     */
    double getMagneticWindDirection();

    /**
     * @return Wind speed, meters per second, to the nearest 0,1 m/s. NaN if not
     *         available.
     */
    double getWindSpeed();
}
