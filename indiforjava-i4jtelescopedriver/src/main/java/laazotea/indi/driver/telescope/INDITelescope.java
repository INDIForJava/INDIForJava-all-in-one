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
package laazotea.indi.driver.telescope;

import static laazotea.indi.Constants.PropertyPermissions.RW;
import static laazotea.indi.Constants.PropertyStates.IDLE;
import static laazotea.indi.Constants.PropertyStates.OK;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.driver.INDIBLOBElementAndValue;
import laazotea.indi.driver.INDIBLOBProperty;
import laazotea.indi.driver.INDIConnectionHandler;
import laazotea.indi.driver.INDIDriver;
import laazotea.indi.driver.INDIElement;
import laazotea.indi.driver.INDINumberElement;
import laazotea.indi.driver.INDINumberElementAndValue;
import laazotea.indi.driver.INDINumberProperty;
import laazotea.indi.driver.INDIProperty;
import laazotea.indi.driver.INDISwitchElement;
import laazotea.indi.driver.INDISwitchElementAndValue;
import laazotea.indi.driver.INDISwitchProperty;
import laazotea.indi.driver.INDITextElement;
import laazotea.indi.driver.INDITextElementAndValue;
import laazotea.indi.driver.INDITextProperty;
import laazotea.indi.INDIException;

/**
 * A class that acts as a abstract INDI for Java Driver for a Telescope.
 *
 * @author Richard van Nieuwenhoven [ritchie at gmx.at]
 */
public abstract class INDITelescope extends INDIDriver implements INDIConnectionHandler {

    private final class ScopeStaturUpdater implements Runnable {

        boolean running = true;

        @Override
        public void run() {
            while (running) {
                readScopeStatus();
                try {
                    Thread.sleep(updateInterfall());
                } catch (InterruptedException e) {
                    // ignore this
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
        MOTION_WEST,
        MOTION_EAST
    }

    protected enum TelescopeStatus {
        SCOPE_IDLE,
        SCOPE_SLEWING,
        SCOPE_TRACKING,
        SCOPE_PARKING,
        SCOPE_PARKED
    };

    private static Logger LOG = Logger.getLogger(INDITelescope.class.getName());;

    protected static final String MAIN_CONTROL_TAB = "Main Control";;

    /**
     * This is a variable filled in by the ReadStatus telescope low level code,
     * used to report current state are we slewing, tracking, or parked.
     */
    protected TelescopeStatus trackState;

    protected INDINumberProperty eqn;

    protected INDITextProperty time;

    protected INDINumberProperty location;

    protected INDISwitchProperty coord;

    protected INDISwitchProperty config;

    protected INDISwitchProperty park;

    protected INDISwitchProperty abort;

    protected INDITextProperty port;

    protected INDISwitchProperty movementNSS;

    protected INDISwitchProperty movementWES;

    protected INDINumberProperty scopeParameters;

    protected final INDINumberElement eqnRa;

    protected final INDINumberElement eqnDec;

    protected INDISwitchElement coordSync;

    protected final INDISwitchElement movementNSSNorth;

    protected final INDISwitchElement movementNSSSouth;

    protected final INDISwitchElement movementWESWest;

    protected final INDISwitchElement movementWESEast;

    protected INDISwitchElement parkElement;

    protected final INDITextElement timeutc;

    protected final INDITextElement timeOffset;

    protected final INDINumberElement locationLat;

    protected final INDINumberElement locationLong;

    protected final INDINumberElement locationElev;

    private PropertyStates last_state = null;

    private final SimpleDateFormat extractISOTimeFormat1 = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");

    private final SimpleDateFormat extractISOTimeFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    protected final INDINumberElement scopeParametersAperture;

    protected final INDINumberElement scopeParametersFocalLength;

    protected final INDINumberElement scopeParametersGuiderAperture;

    protected final INDINumberElement scopeParametersGuiderFocalLength;

    private ScopeStaturUpdater scopStatusUpdater;

    public INDITelescope(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        this.eqn = new INDINumberProperty(this, "EQUATORIAL_EOD_COORD", "Eq. Coordinates", INDITelescope.MAIN_CONTROL_TAB, IDLE, RW, 60);
        this.eqnRa = new INDINumberElement(this.eqn, "RA", "RA (hh:mm:ss)", 0d, 0d, 24d, 0d, "%010.6m");
        this.eqnDec = new INDINumberElement(this.eqn, "DEC", "DEC (dd:mm:ss)", 0d, -90d, 90d, 0d, "%010.6m");

        this.time = new INDITextProperty(this, "TIME_UTC", "UTC", "Site", IDLE, RW, 60);
        this.timeutc = new INDITextElement(this.time, "UTC", "UTC Time", "");
        this.timeOffset = new INDITextElement(this.time, "OFFSET", "UTC Offset", "");

        this.location = new INDINumberProperty(this, "GEOGRAPHIC_COORD", "Scope Location", "Site", OK, RW, 60);
        this.locationLat = new INDINumberElement(this.location, "LAT", "Lat (dd:mm:ss)", 0d, -90d, 90d, 0d, "%010.6m");
        this.locationLong = new INDINumberElement(this.location, "LONG", "Lon (dd:mm:ss)", 0d, 0d, 360d, 0d, "%010.6m");
        this.locationElev = new INDINumberElement(this.location, "ELEV", "Elevation (m)", 0d, -200d, 10000d, 0d, "%g");
        this.location.setSaveable(true);

        this.coord = new INDISwitchProperty(this, "ON_COORD_SET", "On Set", INDITelescope.MAIN_CONTROL_TAB, IDLE, RW, 60, laazotea.indi.Constants.SwitchRules.ONE_OF_MANY);
        new INDISwitchElement(this.coord, "TRACK", "Track", SwitchStatus.OFF);
        new INDISwitchElement(this.coord, "SLEW", "Slew", SwitchStatus.OFF);
        if (canSync()) {
            this.coordSync = new INDISwitchElement(this.coord, "SYNC", "Sync", SwitchStatus.OFF);
        }

        this.config = new INDISwitchProperty(this, "CONFIG_PROCESS", "Configuration", "Options", IDLE, RW, 60, laazotea.indi.Constants.SwitchRules.ONE_OF_MANY);
        new INDISwitchElement(this.config, "CONFIG_LOAD", "Load", SwitchStatus.OFF);
        new INDISwitchElement(this.config, "CONFIG_SAVE", "Save", SwitchStatus.OFF);
        new INDISwitchElement(this.config, "CONFIG_DEFAULT", "Default", SwitchStatus.OFF);

        if (canPark()) {
            this.park = new INDISwitchProperty(this, "TELESCOPE_PARK", "Park", INDITelescope.MAIN_CONTROL_TAB, IDLE, RW, 60, laazotea.indi.Constants.SwitchRules.ONE_OF_MANY);
            this.parkElement = new INDISwitchElement(this.park, "PARK", "Park", SwitchStatus.OFF);
        }
        this.abort =
                new INDISwitchProperty(this, "TELESCOPE_ABORT_MOTION", "Abort Motion", INDITelescope.MAIN_CONTROL_TAB, IDLE, RW, 60,
                        laazotea.indi.Constants.SwitchRules.ONE_OF_MANY);
        new INDISwitchElement(this.abort, "ABORT", "Abort", SwitchStatus.OFF);

        this.port = new INDITextProperty(this, "Ports", "Ports", "Options", IDLE, RW, 60);
        new INDITextElement(this.port, "PORT", "Port", "/dev/ttyUSB0");
        this.port.setSaveable(true);

        this.movementNSS = new INDISwitchProperty(this, "TELESCOPE_MOTION_NS", "North/South", "Motion", IDLE, RW, 60, laazotea.indi.Constants.SwitchRules.ONE_OF_MANY);
        this.movementNSSNorth = new INDISwitchElement(this.movementNSS, "MOTION_NORTH", "North", SwitchStatus.OFF);
        this.movementNSSSouth = new INDISwitchElement(this.movementNSS, "MOTION_SOUTH", "South", SwitchStatus.OFF);

        this.movementWES = new INDISwitchProperty(this, "TELESCOPE_MOTION_WE", "West/East", "Motion", IDLE, RW, 60, laazotea.indi.Constants.SwitchRules.ONE_OF_MANY);
        this.movementWESWest = new INDISwitchElement(this.movementWES, "MOTION_WEST", "West", SwitchStatus.OFF);
        this.movementWESEast = new INDISwitchElement(this.movementWES, "MOTION_EAST", "East", SwitchStatus.OFF);

        this.scopeParameters = new INDINumberProperty(this, "TELESCOPE_INFO", "Scope Properties", "Options", OK, RW, 60);
        this.scopeParametersAperture = new INDINumberElement(this.scopeParameters, "TELESCOPE_APERTURE", "Aperture (mm)", 50d, 50d, 4000d, 0d, "%g");
        this.scopeParametersFocalLength = new INDINumberElement(this.scopeParameters, "TELESCOPE_FOCAL_LENGTH", "Focal Length (mm)", 100d, 100d, 10000d, 0d, "%g");
        this.scopeParametersGuiderAperture = new INDINumberElement(this.scopeParameters, "GUIDER_APERTURE", "Guider Aperture (mm)", 50d, 50d, 4000d, 0d, "%g");
        this.scopeParametersGuiderFocalLength = new INDINumberElement(this.scopeParameters, "GUIDER_FOCAL_LENGTH", "Guider Focal Length (mm)", 100d, 100d, 10000d, 0d, "%g");
        this.scopeParameters.setSaveable(true);

        this.trackState = TelescopeStatus.SCOPE_PARKED;

        addProperty(this.eqn);
        addProperty(this.time);
        addProperty(this.location);
        if (canSync()) {
            addProperty(this.coord);
        }
        addProperty(this.config);
        if (canPark()) {
            addProperty(this.park);
        }
        addProperty(this.abort);
        addProperty(this.port);
        addProperty(this.movementNSS);
        addProperty(this.movementWES);
        addProperty(this.scopeParameters);

    }

    @Override
    public void addProperty(INDIProperty property) {
        super.addProperty(property);
    }

    protected boolean abort() {
        return false;
    }

    protected abstract boolean canSync();

    protected abstract boolean canPark();

    protected long updateInterfall() {
        return 1000L;
    }

    protected void doGoto(double ra, double dec) {

    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        new Thread(this.scopStatusUpdater = new ScopeStaturUpdater(), "Scope status").start();
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        scopStatusUpdater.stop();
    }

    private synchronized Date extractISOTime(String isoTime) {
        try {
            return this.extractISOTimeFormat1.parse(isoTime);
        } catch (ParseException e) {
            try {
                return this.extractISOTimeFormat2.parse(isoTime);
            } catch (ParseException e1) {
                INDITelescope.LOG.log(Level.SEVERE, "could not parse date: " + isoTime);
                return null;
            }
        }
    }

    protected boolean moveNS(TelescopeMotionNS dir) {
        INDITelescope.LOG.log(Level.SEVERE, "Mount does not support North/South motion.");
        resetSwitch(this.movementNSS);
        this.movementNSS.setState(IDLE);
        try {
            updateProperty(this.movementNSS);
        } catch (INDIException e) {
        }
        return false;
    }

    protected boolean moveWE(TelescopeMotionWE dir) {
        INDITelescope.LOG.log(Level.SEVERE, "Mount does not support West/East motion.");
        resetSwitch(this.movementWES);
        this.movementWES.setState(IDLE);
        try {
            updateProperty(this.movementWES);
        } catch (INDIException e) {
        }
        return false;
    }

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
        if (this.eqnRa.getValue() != ra || this.eqnDec.getValue() != dec || this.eqn.getState() != this.last_state) {
            this.eqnRa.setValue(ra);
            this.eqnDec.setValue(dec);
            this.last_state = this.eqn.getState();
            try {
                updateProperty(this.eqn);
            } catch (INDIException e) {
            }
        }

    }

    protected void park() {

    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty arg0, Date arg1, INDIBLOBElementAndValue[] arg2) {

    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date date, INDINumberElementAndValue[] elementsAndValues) {
        if (property == this.eqn) {
            // this is for us, and it is a goto
            double ra = -1;
            double dec = -100;

            for (INDINumberElementAndValue indiNumberElementAndValue : elementsAndValues) {
                if (indiNumberElementAndValue.getElement() == this.eqnRa) {
                    ra = indiNumberElementAndValue.getValue();
                } else if (indiNumberElementAndValue.getElement() == this.eqnDec) {
                    dec = indiNumberElementAndValue.getValue();
                }
            }

            if (ra >= 0d && ra <= 24d && dec >= -90d && dec <= 90d) {
                // we got an ra and a dec, both in range
                // And now we let the underlying hardware specific class
                // perform the goto
                // Ok, lets see if we should be doing a goto
                // or a sync
                if (canSync() && this.coordSync.getValue() == SwitchStatus.ON) {
                    sync(ra, dec);
                    return;
                }

                // Ensure we are not showing Parked status
                this.park.setState(IDLE);
                resetSwitch(this.park);
                try {
                    updateProperty(this.park);
                } catch (INDIException e) {
                }
                doGoto(ra, dec);
                return;
            } else {
                this.eqn.setState(PropertyStates.ALERT);
                INDITelescope.LOG.log(Level.SEVERE, "eqn data missing or corrupted.");
            }
        } else if (property == this.location) {

            Double targetLat = null;
            Double targetLong = null;
            Double targetElev = null;

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
                INDITelescope.LOG.log(Level.SEVERE, "Location data missing or corrupted.");
            } else {
                if (updateLocation(targetLat, targetLong, targetElev)) {
                    property.setValues(elementsAndValues);
                    this.location.setState(PropertyStates.OK);
                } else {
                    this.location.setState(PropertyStates.ALERT);
                }
            }
            try {
                updateProperty(this.location);
            } catch (INDIException e) {
            }
        }
        if (property == this.scopeParameters) {
            this.scopeParameters.setState(OK);
            property.setValues(elementsAndValues);

            try {
                updateProperty(this.scopeParameters);
            } catch (INDIException e) {
            }
        }
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date date, INDISwitchElementAndValue[] elementsAndValues) {
        if (this.coord == property) {
            this.coord.setState(OK);
            property.setValues(elementsAndValues);
        } else if (this.park == property) {
            park();
        } else if (this.movementNSS == property) {
            property.setValues(elementsAndValues);
            property.setState(PropertyStates.BUSY);
            if (this.movementNSSNorth.getValue() == SwitchStatus.ON) {
                moveNS(TelescopeMotionNS.MOTION_NORTH);
            } else {
                moveNS(TelescopeMotionNS.MOTION_SOUTH);
            }
        } else if (this.movementWES == property) {
            property.setValues(elementsAndValues);
            property.setState(PropertyStates.BUSY);
            if (this.movementWESWest.getValue() == SwitchStatus.ON) {
                moveWE(TelescopeMotionWE.MOTION_WEST);
            } else {
                moveWE(TelescopeMotionWE.MOTION_WEST);

            }

        } else if (this.abort == property) {
            resetSwitch(this.abort);
            if (abort()) {
                this.abort.setState(OK);
                if (this.park.getState() == PropertyStates.BUSY) {
                    this.park.setState(IDLE);
                }
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

    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date date, INDITextElementAndValue[] elementsAndValues) {
        if (property == this.port) {
            property.setValues(elementsAndValues);
            this.port.setState(PropertyStates.OK);

            try {
                updateProperty(this.port);
            } catch (INDIException e) {
            }
        } else if (property == this.time) {
            String utcString = "";
            String offsetString = "0";
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
                property.setValues(elementsAndValues);
                if (updateTime(utc, offset.doubleValue())) {
                    this.time.setState(PropertyStates.OK);

                } else {
                    this.time.setState(PropertyStates.ALERT);
                }
            } else {
                INDITelescope.LOG.log(Level.SEVERE, "Date/Time is invalid: " + utcString + " offset " + offsetString + ".");
                this.time.setState(PropertyStates.ALERT);
            }
            try {
                updateProperty(this.time);
            } catch (INDIException e) {
            }
        }
    }

    protected void readScopeStatus() {

    }

    private void resetSwitch(INDISwitchProperty property) {
        for (INDIElement element : property.getElementsAsList()) {
            element.setValue(SwitchStatus.OFF);
        }
    }

    protected boolean sync(double ra, double dec) {
        // if we get here, our mount doesn't support sync
        INDITelescope.LOG.log(Level.SEVERE, "Mount does not support Sync.");
        return false;
    }

    protected boolean updateLocation(double targetLat, double targetLong, double targetElev) {
        return true;
    }

    protected boolean updateTime(Date utc, double d) {
        return true;
    }

    @Override
    public void updateProperty(INDIProperty property) throws INDIException {
        super.updateProperty(property);
    }

    @Override
    public void updateProperty(INDIProperty property, String message) throws INDIException {
        super.updateProperty(property, message);
    }
}
