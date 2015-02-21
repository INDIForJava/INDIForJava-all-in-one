package org.indilib.i4j.driver.nmea0183;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import net.sf.marineapi.nmea.sentence.MDASentence;
import net.sf.marineapi.nmea.sentence.MTASentence;
import net.sf.marineapi.nmea.sentence.MTWSentence;
import net.sf.marineapi.nmea.sentence.MWDSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.Measurement;

public class WeatherParser {

    private final Sentence sentence;

    public WeatherParser(Sentence sentence) {
        this.sentence = sentence;
    }

    public static boolean hasData(Sentence sentence) {
        return sentence instanceof MDASentence || sentence instanceof MWDSentence || sentence instanceof XDRSentence || sentence instanceof MTASentence
                || sentence instanceof MTWSentence;
    }

    public double getBarometricPressure() {
        if (sentence instanceof MDASentence) {
            double value = Double.NaN;
            MDASentence mdaSentence = (MDASentence) sentence;
            value = getPressureValue(mdaSentence, 'B');
            if (Double.isNaN(value)) {
                // check the value in pascal
                value = getPressureValue(mdaSentence, 'P');
                if (!Double.isNaN(value)) {
                    // 1 bar = 100000 pascal
                    value = value / 100000d;
                }
            }
            if (Double.isNaN(value)) {
                // check the value in inches of mercury
                value = getPressureValue(mdaSentence, 'I');
                if (!Double.isNaN(value)) {
                    // 1 bar = 29.53 inHg
                    value = value / 29.53d;
                }
            }
            return value;
        }
        Measurement measurement = getMesurment("P");
        if (measurement != null) {
            return measurement.getValue();
        }
        return Double.NaN;
    }

    private double getPressureValue(MDASentence mdaSentence, char unit) {
        double value = Double.NaN;
        if (mdaSentence.getSecondaryBarometricPressureUnit() == unit) {
            value = mdaSentence.getSecondaryBarometricPressure();
        }
        if (Double.isNaN(value) && mdaSentence.getPrimaryBarometricPressureUnit() == unit) {
            value = mdaSentence.getPrimaryBarometricPressure();
        }
        return value;
    }

    private Measurement getMesurment(String type) {
        if (sentence instanceof XDRSentence) {
            for (Measurement mesurement : ((XDRSentence) sentence).getMeasurements()) {
                if (type.equals(mesurement.getType())) {
                    return mesurement;
                }
            }
        }
        return null;
    }

    public double getAirTemperature() {
        if (sentence instanceof MDASentence) {
            return ((MDASentence) sentence).getAirTemperature();
        } else if (sentence instanceof MTASentence) {
            return ((MTASentence) sentence).getTemperature();
        }
        Measurement measurement = getMesurment("C");
        if (measurement != null) {
            return measurement.getValue();
        }
        return Double.NaN;
    }

    public double getWaterTemperature() {
        if (sentence instanceof MDASentence) {
            return ((MDASentence) sentence).getWaterTemperature();
        } else if (sentence instanceof MTWSentence) {
            return ((MTWSentence) sentence).getTemperature();
        }
        return Double.NaN;
    }

    public double getRelativeHumidity() {
        if (sentence instanceof MDASentence) {
            return ((MDASentence) sentence).getRelativeHumidity();
        }
        return Double.NaN;
    }

    public double getAbsoluteHumidity() {
        if (sentence instanceof MDASentence) {
            return ((MDASentence) sentence).getAbsoluteHumidity();
        }
        Measurement measurement = getMesurment("H");
        if (measurement != null) {
            return measurement.getValue();
        }
        return Double.NaN;
    }

    public double getDewPoint() {
        if (sentence instanceof MDASentence) {
            return ((MDASentence) sentence).getDewPoint();
        }
        return Double.NaN;
    }

    public double getTrueWindDirection() {
        if (sentence instanceof MDASentence) {
            return ((MDASentence) sentence).getTrueWindDirection();
        } else if (sentence instanceof MWDSentence) {
            return ((MWDSentence) sentence).getTrueWindDirection();
        }
        return Double.NaN;
    }

    public double getMagneticWindDirection() {
        if (sentence instanceof MDASentence) {
            return ((MDASentence) sentence).getMagneticWindDirection();
        } else if (sentence instanceof MWDSentence) {
            return ((MWDSentence) sentence).getMagneticWindDirection();
        }
        return Double.NaN;
    }

    public double getWindSpeed() {
        if (sentence instanceof MDASentence) {
            return ((MDASentence) sentence).getWindSpeed();
        } else if (sentence instanceof MWDSentence) {
            return ((MWDSentence) sentence).getWindSpeed();
        }
        return Double.NaN;
    }
}
