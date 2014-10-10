/*
 *  This file is part of INDI for Java Driver.
 *
 *  INDI for Java Driver is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  INDI for Java Driver is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Driver.  If not, see
 *  <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.driver.telescope;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
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

import static org.indilib.i4j.Constants.PropertyStates.IDLE;
import static org.indilib.i4j.Constants.PropertyStates.OK;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIBLOBElementAndValue;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIConnectionHandler;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIElement;
import org.indilib.i4j.driver.INDIElementAndValue;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.NumberEvent;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.driver.event.TextEvent;
import org.indilib.i4j.driver.serial.INDISerialPortExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that acts as a abstract INDI for Java Driver for any Telescope. All
 * telescope drivers should subclass this abstract drivser. The initali version
 * was a port of the indilib c++ version.
 * 
 * @author Richard van Nieuwenhoven [ritchie at gmx.at]
 */
public abstract class INDITelescope extends INDIDriver implements INDIConnectionHandler {

    /**
     * When two doubles are compared, at what precision are they interpreted as
     * equal.
     */
    private static final double EQUALITY_PRECISION = 0.000000000001;

    /**
     * how many milliseconds in a second.
     */
    private static final long ONE_SECOND_IN_MILLISECONDS = 1000L;

    /**
     * The minimal value for right ascension in hours.
     */
    private static final double MIN_RIGHT_ACENSION_HOURS = 0d;

    /**
     * The maximal value for right ascension in hours.
     */
    private static final double MAX_RIGHT_ACENSION_HOURS = 24d;

    /**
     * The minimal value for declination in degrees.
     */
    private static final double MAX_DECLINATION_DEGREES = 90d;

    /**
     * The maximal value for declination in degrees.
     */
    private static final double MIN_DECLINATION_DEGREES = -90d;

    private final class ScopeStaturUpdater implements Runnable {

        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                readScopeStatus();
                try {
                    Thread.sleep(updateInterfall());
                } catch (InterruptedException e) {
                    LOG.error("thread interupted", e);
                }
            }
        }

        public void stop() {
            running = false;
        }
    }

    protected enum TelescopeMotionNS {
        MOTION_NORTH,
        MOTION_SOUTH
    }

    protected enum TelescopeMotionWE {
        MOTION_EAST,
        MOTION_WEST
    }

    protected enum TelescopeStatus {
        SCOPE_IDLE,
        SCOPE_PARKED,
        SCOPE_PARKING,
        SCOPE_SLEWING,
        SCOPE_TRACKING
    };

    /**
     * The logger for any messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDITelescope.class);

    protected static final String MAIN_CONTROL_TAB = "Main Control";

    protected static final String SITE_TAB = "Site";

    protected static final String OPTIONS_TAB = "Options";

    protected static final String MOTION_TAB = "Motion";

    private final SimpleDateFormat extractISOTimeFormat1 = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");

    private final SimpleDateFormat extractISOTimeFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private PropertyStates lastState = null;

    private ScopeStaturUpdater scopStatusUpdater;

    @InjectProperty(name = "EQUATORIAL_EOD_COORD", label = "Eq. Coordinates", group = INDITelescope.MAIN_CONTROL_TAB)
    protected INDINumberProperty eqn;

    @InjectElement(name = "RA", label = "RA (hh:mm:ss)", maximum = 24d, numberFormat = "%010.6m")
    protected INDINumberElement eqnRa;

    @InjectElement(name = "DEC", label = "DEC (dd:mm:ss)", minimum = MIN_DECLINATION_DEGREES, maximum = 90d, numberFormat = "%010.6m")
    protected INDINumberElement eqnDec;

    @InjectProperty(name = "TIME_UTC", label = "UTC", group = SITE_TAB)
    protected INDITextProperty time;

    @InjectElement(name = "UTC", label = "UTC Time")
    protected INDITextElement timeutc;

    @InjectElement(name = "OFFSET", label = "UTC Offset")
    protected INDITextElement timeOffset;

    @InjectProperty(name = "GEOGRAPHIC_COORD", label = "Scope Location", state = OK, group = INDITelescope.SITE_TAB, saveable = true)
    protected INDINumberProperty location;

    @InjectElement(name = "LAT", label = "Lat (dd:mm:ss)", minimum = MIN_DECLINATION_DEGREES, maximum = 90d, numberFormat = "%010.6m")
    protected INDINumberElement locationLat;

    @InjectElement(name = "LONG", label = "Lon (dd:mm:ss)", maximum = 360d, numberFormat = "%010.6m")
    protected INDINumberElement locationLong;

    @InjectElement(name = "ELEV", label = "Elevation (m)", minimum = -200d, maximum = 10000d)
    protected INDINumberElement locationElev;

    @InjectProperty(name = "ON_COORD_SET", label = "On Set", group = INDITelescope.MAIN_CONTROL_TAB)
    protected INDISwitchProperty coord;

    @InjectElement(name = "TRACK", label = "Track")
    protected INDISwitchElement coordTrack;

    @InjectElement(name = "SLEW", label = "Slew")
    protected INDISwitchElement coordSlew;

    @InjectProperty(name = "CONFIG_PROCESS", label = "Configuration", group = INDITelescope.OPTIONS_TAB)
    protected INDISwitchProperty config;

    @InjectElement(name = "CONFIG_LOAD", label = "Load")
    protected INDISwitchElement configLoad;

    @InjectElement(name = "CONFIG_SAVE", label = "Save")
    protected INDISwitchElement configSave;

    @InjectElement(name = "CONFIG_DEFAULT", label = "Default")
    protected INDISwitchElement configDefault;

    protected INDITelescopeParkExtension parkExtension;

    @InjectProperty(name = "TELESCOPE_ABORT_MOTION", label = "Abort Motion", group = INDITelescope.MAIN_CONTROL_TAB)
    protected INDISwitchProperty abort;

    @InjectElement(name = "ABORT", label = "Abort")
    protected INDISwitchElement abordElement;

    @InjectExtension(group = OPTIONS_TAB)
    protected INDISerialPortExtension serialPortExtension;

    @InjectProperty(name = "TELESCOPE_MOTION_NS", label = "North/South", group = MOTION_TAB)
    protected INDISwitchProperty movementNSS;

    @InjectElement(name = "MOTION_NORTH", label = "North")
    protected INDISwitchElement movementNSSNorth;

    @InjectElement(name = "MOTION_SOUTH", label = "South")
    protected INDISwitchElement movementNSSSouth;

    @InjectProperty(name = "TELESCOPE_MOTION_WE", label = "West/East", group = MOTION_TAB)
    protected INDISwitchProperty movementWES;

    @InjectElement(name = "MOTION_WEST", label = "West")
    protected INDISwitchElement movementWESWest;

    @InjectElement(name = "MOTION_EAST", label = "East")
    protected INDISwitchElement movementWESEast;

    @InjectProperty(name = "TELESCOPE_INFO", label = "Scope Properties", group = OPTIONS_TAB, state = OK, saveable = true)
    protected INDINumberProperty scopeParameters;

    @InjectElement(name = "TELESCOPE_APERTURE", label = "Aperture (mm)", numberValue = 50d, minimum = 50d, maximum = 4000d, numberFormat = "%g")
    protected INDINumberElement scopeParametersAperture;

    @InjectElement(name = "TELESCOPE_FOCAL_LENGTH", label = "Focal Length (mm)", numberValue = 100d, minimum = 100d, maximum = 10000d, numberFormat = "%g")
    protected INDINumberElement scopeParametersFocalLength;

    @InjectElement(name = "GUIDER_APERTURE", label = "Guider Aperture (mm)", numberValue = 50d, minimum = 50d, maximum = 4000d, numberFormat = "%g")
    protected INDINumberElement scopeParametersGuiderAperture;

    @InjectElement(name = "GUIDER_FOCAL_LENGTH", label = "Guider Focal Length (mm)", numberValue = 100d, minimum = 100d, maximum = 10000d, numberFormat = "%g")
    protected INDINumberElement scopeParametersGuiderFocalLength;

    protected INDITelescopeSyncExtension syncExtension;

    /**
     * This is a variable filled in by the ReadStatus telescope low level code,
     * used to report current state are we slewing, tracking, or parked.
     */
    protected TelescopeStatus trackState;

    public INDITelescope(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        this.eqn.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newEqnValue(elementsAndValues);
            }
        });
        this.location.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newLocationValue(elementsAndValues);
            }
        });
        this.time.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                newTimeValue(elementsAndValues);
            }
        });
        this.coord.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                property.setState(OK);
                property.setValues(elementsAndValues);
            }
        });
        this.abort.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newAbortValue();
            }
        });
        this.scopeParameters.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newScopeParameter(elementsAndValues);
            }
        });
        this.movementNSS.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newMovementNSSValue(elementsAndValues);
            }
        });
        this.movementWES.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newMovementWESValue(elementsAndValues);
            }
        });
        this.trackState = TelescopeStatus.SCOPE_PARKED;

    }

    /**
     * convert an iso time string to a java date.
     * 
     * @param isoTime
     *            the String representing the date.
     * @return the java date representation of the isoTime String.
     */
    private synchronized Date extractISOTime(String isoTime) {
        try {
            return this.extractISOTimeFormat1.parse(isoTime);
        } catch (ParseException e) {
            try {
                return this.extractISOTimeFormat2.parse(isoTime);
            } catch (ParseException e1) {
                INDITelescope.LOG.error("could not parse date: " + isoTime);
                return null;
            }
        }
    }

    /**
     * the indi client send an abort of the current operation.
     */
    private void newAbortValue() {
        this.abort.resetAllSwitches();
        if (abort()) {
            this.abort.setState(OK);
            parkExtension.setNotBussy();
            if (this.eqn.getState() == PropertyStates.BUSY) {
                this.eqn.setState(IDLE);
            }
            if (this.movementWES.getState() == PropertyStates.BUSY) {
                this.movementWES.setState(IDLE);
            }
            if (this.movementNSS.getState() == PropertyStates.BUSY) {
                this.movementNSS.setState(IDLE);
            }
            this.trackState = TelescopeStatus.SCOPE_IDLE;
        } else {
            this.abort.setState(PropertyStates.ALERT);
        }
    }

    /**
     * neu goto values received from the indi client.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newEqnValue(INDIElementAndValue<INDINumberElement, Double>[] elementsAndValues) {
        // this is for us, and it is a goto
        double ra = -1;
        double dec = -100;

        for (INDIElementAndValue<INDINumberElement, Double> indiNumberElementAndValue : elementsAndValues) {
            if (indiNumberElementAndValue.getElement() == this.eqnRa) {
                ra = indiNumberElementAndValue.getValue();
            } else if (indiNumberElementAndValue.getElement() == this.eqnDec) {
                dec = indiNumberElementAndValue.getValue();
            }
        }

        if (ra >= MIN_RIGHT_ACENSION_HOURS && ra <= MAX_RIGHT_ACENSION_HOURS && dec >= MIN_DECLINATION_DEGREES && dec <= MAX_DECLINATION_DEGREES) {
            // we got an ra and a dec, both in range
            // And now we let the underlying hardware specific class
            // perform the goto
            // Ok, lets see if we should be doing a goto
            // or a sync
            if (syncExtension.doSync(ra, dec)) {
                return;
            }

            // Ensure we are not showing Parked status
            parkExtension.setIdle();
            doGoto(ra, dec);
        } else {
            this.eqn.setState(PropertyStates.ALERT);
            INDITelescope.LOG.error("eqn data missing or corrupted.");
        }
    }

    /**
     * the client send new values for the telescope location properties. process
     * the values and call the scope implementation.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newLocationValue(INDINumberElementAndValue[] elementsAndValues) {
        Double targetLat = null;
        Double targetLong = null;
        Double targetElev = null;
        // TODO: check if this for loop can be eliminated.
        for (INDINumberElementAndValue indiNumberElementAndValue : elementsAndValues) {
            if (this.locationLat == indiNumberElementAndValue.getElement()) {
                targetLat = indiNumberElementAndValue.getValue();
            } else if (this.locationLong == indiNumberElementAndValue.getElement()) {
                targetLong = indiNumberElementAndValue.getValue();
            } else if (this.locationElev == indiNumberElementAndValue.getElement()) {
                targetElev = indiNumberElementAndValue.getValue();
            }
        }
        if (targetLat == null || targetLong == null || targetElev == null) {
            this.location.setState(PropertyStates.ALERT);
            INDITelescope.LOG.error("Location data missing or corrupted.");
        } else {
            if (updateLocation(targetLat, targetLong, targetElev)) {
                this.location.setValues(elementsAndValues);
                this.location.setState(PropertyStates.OK);
            } else {
                this.location.setState(PropertyStates.ALERT);
            }
        }
        updateProperty(this.location);
    }

    /**
     * new values where send from the client for the move north/south property
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newMovementNSSValue(INDISwitchElementAndValue[] elementsAndValues) {
        movementNSS.setValues(elementsAndValues);
        movementNSS.setState(PropertyStates.BUSY);
        if (this.movementNSSNorth.getValue() == SwitchStatus.ON) {
            moveNS(TelescopeMotionNS.MOTION_NORTH);
        } else {
            moveNS(TelescopeMotionNS.MOTION_SOUTH);
        }
    }

    /**
     * new values where send from the client for the move west/east property
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newMovementWESValue(INDISwitchElementAndValue[] elementsAndValues) {
        movementWES.setValues(elementsAndValues);
        movementWES.setState(PropertyStates.BUSY);
        if (this.movementWESWest.getValue() == SwitchStatus.ON) {
            moveWE(TelescopeMotionWE.MOTION_WEST);
        } else {
            moveWE(TelescopeMotionWE.MOTION_EAST);

        }
    }

    /**
     * The scope parameters where changes by the client.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newScopeParameter(INDINumberElementAndValue[] elementsAndValues) {
        this.scopeParameters.setState(OK);
        this.scopeParameters.setValues(elementsAndValues);

        updateProperty(this.scopeParameters);
    }

    /**
     * The current time was changed by the client.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newTimeValue(INDITextElementAndValue[] elementsAndValues) {
        String utcString = "";
        String offsetString = "0";
        // TODO: is this loop removeable?
        for (INDITextElementAndValue indiTextElementAndValue : elementsAndValues) {
            if (this.timeutc == indiTextElementAndValue.getElement()) {
                utcString = indiTextElementAndValue.getValue();
            } else if (this.timeOffset == indiTextElementAndValue.getElement()) {
                offsetString = indiTextElementAndValue.getValue();
            }
        }
        Double offset;
        try {
            offset = Double.valueOf(NumberFormat.getNumberInstance().parse(offsetString).doubleValue());
        } catch (ParseException e1) {
            offset = null;
        }
        Date utc = extractISOTime(utcString);
        if (utc != null && offset != null) {
            this.time.setValues(elementsAndValues);
            if (updateTime(utc, offset.doubleValue())) {
                this.time.setState(PropertyStates.OK);

            } else {
                this.time.setState(PropertyStates.ALERT);
            }
        } else {
            INDITelescope.LOG.error("Date/Time is invalid: " + utcString + " offset " + offsetString + ".");
            this.time.setState(PropertyStates.ALERT);
        }
        updateProperty(this.time);
    }

    /**
     * The driver implementation should try to halt the current operation. And
     * return true if the operation succeeded.
     * 
     * @return if the operation succeeded .
     */
    abstract protected boolean abort();

    /**
     * The driver implementation should now goto the specified coordinates.
     * 
     * @param ra
     *            the right ascension of the goto point in space
     * @param dec
     *            the declination of the point in space
     */
    abstract protected void doGoto(double ra, double dec);

    /**
     * this is the default implementation if the scope does not support
     * North/South motion. Subclasses should override this method to support it.
     * 
     * @param dir
     *            The direction to move to
     * @return true is successful
     */
    protected boolean moveNS(TelescopeMotionNS dir) {
        INDITelescope.LOG.error("Mount does not support North/South motion.");
        this.movementNSS.resetAllSwitches();
        this.movementNSS.setState(IDLE);
        updateProperty(this.movementNSS);
        return false;
    }

    /**
     * this is the default implementation if the scope does not support
     * West/East motion. Subclasses should override this method to support it.
     * 
     * @param dir
     *            The direction to move to
     * @return true is successful
     */
    protected boolean moveWE(TelescopeMotionWE dir) {
        INDITelescope.LOG.error("Mount does not support West/East motion.");
        this.movementWES.resetAllSwitches();
        this.movementWES.setState(IDLE);
        updateProperty(this.movementWES);
        return false;
    }

    /**
     * The scope is now pointing here, resport the pointing position back to the
     * indi client. Subclasses should call this method to send the new pointing
     * data to the indi client.
     * 
     * @param ra
     *            the right ascension of the goto point in space
     * @param dec
     *            the declination of the point in space
     */
    protected void newRaDec(double ra, double dec) {
        // Lets set our eq values to these numbers
        // which came from the hardware
        switch (this.trackState) {
            case SCOPE_PARKED:
            case SCOPE_IDLE:
                this.eqn.setState(IDLE);
                break;

            case SCOPE_SLEWING:
                this.eqn.setState(PropertyStates.BUSY);
                break;

            case SCOPE_TRACKING:
                this.eqn.setState(PropertyStates.OK);
                break;

            default:
                break;
        }

        // IDLog("newRA DEC RA %g - DEC %g --- EqN[0] %g --- EqN[1] %g --- EqN.state %d\n",
        // ra, dec, EqN[0].value, EqN[1].value, EqNP.s);
        if (!eq(this.eqnRa.getValue(), ra) || !eq(this.eqnDec.getValue(), dec) || this.eqn.getState() != this.lastState) {
            this.eqnRa.setValue(ra);
            this.eqnDec.setValue(dec);
            this.lastState = this.eqn.getState();
            updateProperty(this.eqn);
        }

    }

    /**
     * equal doubles are two doubles equal? Subclasses my use this helper
     * method.
     * 
     * @param double1
     *            the first value
     * @param double2
     *            the other value
     * @return true if the 2 doubles are equal up to the precision
     *         {@link EQUALITY_PRECISION}.
     */
    protected boolean eq(double double1, double double2) {
        return Math.abs(double1 - double2) < EQUALITY_PRECISION;
    }

    abstract protected void readScopeStatus();

    protected long updateInterfall() {
        return ONE_SECOND_IN_MILLISECONDS;
    }

    abstract protected boolean updateLocation(double targetLat, double targetLong, double targetElev);

    abstract protected boolean updateTime(Date utc, double d);

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        new Thread(this.scopStatusUpdater = new ScopeStaturUpdater(), "Scope status").start();
        addProperty(this.eqn);
        addProperty(this.time);
        addProperty(this.location);
        addProperty(this.coord);
        addProperty(this.config);
        parkExtension.connect();
        addProperty(this.abort);
        serialPortExtension.connect();

        addProperty(this.movementNSS);
        addProperty(this.movementWES);
        addProperty(this.scopeParameters);

    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        scopStatusUpdater.stop();
        removeProperty(this.eqn);
        removeProperty(this.time);
        removeProperty(this.location);
        removeProperty(this.coord);
        removeProperty(this.config);
        parkExtension.disconnect();
        removeProperty(this.abort);
        serialPortExtension.disconnect();
        newAbortValue();

        removeProperty(this.movementNSS);
        removeProperty(this.movementWES);
        removeProperty(this.scopeParameters);
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date date, INDISwitchElementAndValue[] elementsAndValues) {
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
    }
}
