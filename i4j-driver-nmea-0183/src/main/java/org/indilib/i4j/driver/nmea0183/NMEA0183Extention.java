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

import static org.indilib.i4j.Constants.PropertyStates.OK;
import static org.indilib.i4j.properties.INDIStandardProperty.ATMOSPHERE;
import static org.indilib.i4j.properties.INDIStandardElement.ELEV;
import static org.indilib.i4j.properties.INDIStandardProperty.GEOGRAPHIC_COORD;
import static org.indilib.i4j.properties.INDIStandardElement.HUMIDITY;
import static org.indilib.i4j.properties.INDIStandardElement.LAT;
import static org.indilib.i4j.properties.INDIStandardElement.LONG;
import static org.indilib.i4j.properties.INDIStandardElement.PRESSURE;
import static org.indilib.i4j.properties.INDIStandardElement.TEMPERATURE;
import static org.indilib.i4j.properties.INDIStandardProperty.TIME_UTC;
import static org.indilib.i4j.properties.INDIStandardElement.UTC;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TimeSentence;
import net.sf.marineapi.nmea.util.Time;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;

public class NMEA0183Extention<DRIVER extends INDIDriver> extends INDIDriverExtension<DRIVER> {

    /**
     * First variant of the iso time format.
     */
    private final SimpleDateFormat extractISOTimeFormat = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");

    /**
     * Property for the utc time of the nmea stream.
     */
    @InjectProperty(std = TIME_UTC, label = "UTC", group = "Main Control", permission = PropertyPermissions.RO)
    protected INDITextProperty time;

    /**
     * The UTC time of the nmea stream.
     */
    @InjectElement(std = UTC, label = "UTC Time")
    protected INDITextElement timeutc;

    /**
     * The geographic coordinates of the telescope location on earth.
     */
    @InjectProperty(std = GEOGRAPHIC_COORD, label = "Location", state = OK, group = "Main Control", permission = PropertyPermissions.RO)
    protected INDINumberProperty location;

    /**
     * the latitude of the coordinates.
     */
    @InjectElement(std = LAT, label = "Lat (dd:mm:ss)", numberFormat = "%010.6m")
    protected INDINumberElement locationLat;

    /**
     * the longtitude of the coordinates.
     */
    @InjectElement(std = LONG, label = "Lon (dd:mm:ss)", numberFormat = "%010.6m")
    protected INDINumberElement locationLong;

    /**
     * The elevation of the coordinates.
     */
    @InjectElement(std = ELEV, label = "Elevation (m)", numberFormat = "%5.1f")
    protected INDINumberElement locationElev;

    /**
     * Weather conditions.
     */
    @InjectProperty(std = ATMOSPHERE, label = "Weather conditions", state = OK, group = "Main Control", permission = PropertyPermissions.RO)
    protected INDINumberProperty weatherConditions;

    /**
     * The temperature in Kelvin.
     */
    @InjectElement(std = TEMPERATURE, label = "Temperature in Kelvin", numberFormat = "%5.1f")
    protected INDINumberElement temperature;

    /**
     * The temperature in Kelvin.
     */
    @InjectElement(std = PRESSURE, label = "pressure in hPa", numberFormat = "%5.1f")
    protected INDINumberElement pressure;

    /**
     * The absolute humidity in %.
     */
    @InjectElement(std = HUMIDITY, label = "absolute humidity in %", numberFormat = "%5.1f")
    protected INDINumberElement absoluteHumidity;

    /**
     * The wind speed in m/s.
     */
    @InjectElement(name = "WIND", label = "wind speed in m/s", numberFormat = "%5.1f")
    protected INDINumberElement windSpeed;

    /**
     * is the driver connected.
     */
    private boolean connected = false;

    /**
     * was any nmea time received.
     */
    private boolean timeReceived = false;

    /**
     * was any nmea location information received.
     */
    private boolean locationReceived = false;

    /**
     * was any nmea weather information received.
     */
    private boolean weatherReceived = false;

    private final class NMEAProcessor implements Runnable {

        private boolean stop = false;

        @Override
        public void run() {
            SentenceFactory factory = SentenceFactory.getInstance();

            String readLine = reader.readLine();
            while (readLine != null && !stop) {
                Sentence sentence = factory.createParser(readLine);
                analyseSentence(sentence);
                readLine = reader.readLine();
            }
            stop = true;
        }

        public void stop() {
            stop = true;
        }
    }

    public interface ILineReader {

        String readLine();

        void close();
    }

    private ILineReader reader;

    private NMEAProcessor nmeaProcessor;

    public void setReader(ILineReader reader) {
        this.reader = reader;
    }

    public NMEA0183Extention(DRIVER driver) {
        super(driver);
    }

    @Override
    public void connect() {
        super.connect();
        if (reader != null) {
            nmeaProcessor = new NMEAProcessor();
            new Thread(nmeaProcessor, "NMEAProcessor").start();
        }
        connected = true;
        addRemoveFields(false);
    }

    @Override
    public void disconnect() {
        super.disconnect();
        nmeaProcessor.stop();
        if (reader != null) {
            reader.close();
        }
        connected = false;
        addRemoveFields(false);
    }

    protected void analyseSentence(Sentence sentence) {
        if (sentence instanceof PositionSentence) {
            PositionSentence s = (PositionSentence) sentence;
            locationElev.setValue(s.getPosition().getAltitude());
            locationLat.setValue(s.getPosition().getLatitude());
            locationLong.setValue(s.getPosition().getLongitude());
            locationReceived = true;
        }
        if (sentence instanceof TimeSentence) {
            TimeSentence s = (TimeSentence) sentence;
            timeutc.setValue(extractISOTimeFormat.format(getUTCTime(s).getTime()));
            timeReceived = true;
        }
        if (WeatherParser.hasData(sentence)) {
            WeatherParser mda = new WeatherParser(sentence);
            double value = mda.getAbsoluteHumidity();
            if (Double.isNaN(value)) {
                absoluteHumidity.setValue(value);
                weatherReceived = true;
            }
            value = mda.getAirTemperature();
            if (Double.isNaN(value)) {
                temperature.setValue(celcsiusToKelvin(value));
                weatherReceived = true;
            }
            value = mda.getBarometricPressure();
            if (Double.isNaN(value)) {
                pressure.setValue(barTohPa(value));
                weatherReceived = true;
            }
            value = mda.getWindSpeed();
            if (Double.isNaN(value)) {
                windSpeed.setValue(value);
                weatherReceived = true;
            }
        }
        addRemoveFields(true);
    }

    private void addRemoveFields(boolean update) {
        if (connected) {
            if (timeReceived) {
                addProperty(time);
                if (update) {
                    updateProperty(time);
                }
            }
            if (locationReceived) {
                addProperty(location);
                if (update) {
                    updateProperty(location);
                }
                if (weatherReceived) {
                    addProperty(weatherConditions);
                    if (update) {
                        updateProperty(weatherConditions);
                    }
                }
            } else {
                removeProperty(time);
                removeProperty(location);
                removeProperty(weatherConditions);
            }
        }
    }

    protected Calendar getUTCTime(TimeSentence sentence) {
        Time time = sentence.getTime();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // check if are just switching the day, assuming the stream is not
        // historic
        if (cal.get(Calendar.HOUR_OF_DAY) > 12) {
            // afternoon
            if (time.getHour() < 12) {
                // morning so our clock is off we must correct the day
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
            }
        }
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMinutes());

        BigDecimal[] vals = BigDecimal.valueOf(time.getSeconds()).divideAndRemainder(BigDecimal.valueOf(1));

        cal.set(Calendar.SECOND, vals[0].intValue());
        cal.set(Calendar.MILLISECOND, (int) (vals[1].doubleValue() * 1000d));
        cal.getTimeInMillis();
        return cal;
    }

    private double barTohPa(double value) {
        return value * 100d;
    }

    private double celcsiusToKelvin(double value) {
        return value + 273.15d;
    }
}
