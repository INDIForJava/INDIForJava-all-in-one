package org.indilib.i4j.driver.telescope.simulator;

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
import static org.indilib.i4j.Constants.PropertyStates.BUSY;
import static org.indilib.i4j.Constants.PropertyStates.IDLE;
import static org.indilib.i4j.Constants.PropertyStates.OK;

import java.util.Date;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.NumberEvent;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.driver.telescope.INDIDirection;
import org.indilib.i4j.driver.telescope.INDITelescope;
import org.indilib.i4j.driver.telescope.INDITelescopeParkInterface;
import org.indilib.i4j.driver.telescope.INDITelescopeSyncInterface;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the scope simulator class, basicly a copy of the c++ version. it will
 * simulate a scopes behavior for any indi-client.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TelescopeSimulator extends INDITelescope implements INDITelescopeParkInterface, INDITelescopeSyncInterface {

    /**
     * how many milliseconds per second.
     */
    private static final double MILLISECONDS_PER_SECOND = 1000d;

    /**
     * multiplier to create 360 degrees from 24 hours.
     */
    private static final double HOUR_TO_DEGREE = 15d;

    /**
     * the simulatet focal length.
     */
    private static final double DEFAULT_FOCAL_LENGTH = 2000d;

    /**
     * the simulated aperture.
     */
    private static final double DEFAULT_APERTURE = 203d;

    /**
     * the tab where the motion control properties will be displayed.
     */
    public static final String MOTION_TAB = "Motion Control";

    /**
     * The logger for any messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TelescopeSimulator.class);

    /**
     * slew rate, degrees/s.
     */
    private static final double GOTO_RATE = 2d;

    /**
     * slew rate, degrees/s.
     */
    private static final double SLEW_RATE = 0.5d;

    /**
     * slew rate, degrees/s.
     */
    private static final double FINE_SLEW_RATE = 0.1d;

    /**
     * sidereal rate, degrees/s.
     */
    private static final double SID_RATE = 0.004178d;

    /**
     * Move at GOTO_RATE until distance from target is GOTO_LIMIT degrees.
     */
    private static final double GOTO_LIMIT = 5d;

    /**
     * Move at SLEW_LIMIT until distance from target is SLEW_LIMIT degrees.
     */
    private static final double SLEW_LIMIT = 2d;

    /**
     * poll period, ms.
     */
    private static final long POLLMS = 250L;

    /**
     * Guide north direction.<br/>
     * TODO: replace with something mode java like.
     */
    private static final int GUIDE_NORTH = 0;

    /**
     * Guide south direction.<br/>
     * TODO: replace with something mode java like.
     */
    private static final int GUIDE_SOUTH = 1;

    /**
     * Guide west direction.<br/>
     * TODO: replace with something mode java like.
     */
    private static final int GUIDE_WEST = 0;

    /**
     * Guide east direction.<br/>
     * TODO: replace with something mode java like.
     */
    private static final int GUIDE_EAST = 1;

    /**
     * Guide north south values.<br/>
     * TODO: replace with something mode java like.
     */
    private double[] guiderNSTarget = new double[2];

    /**
     * Guide west east values.<br/>
     * TODO: replace with something mode java like.
     */
    private double[] guiderEWTarget = new double[2];

    /**
     * The current pointing coordinates.
     */
    private final INDIDirection current = new INDIDirection();

    /**
     * The target pointing coordinates.
     */
    private final INDIDirection target = new INDIDirection();

    /**
     * Simulated periodic error in RA, DEC.
     */
    @InjectProperty(name = "EQUATORIAL_PE", label = "Periodic Error", group = MOTION_TAB, permission = PropertyPermissions.RO)
    private INDINumberProperty eqPen;

    /**
     * Simulated periodic error in right ascension.
     */
    @InjectElement(name = "RA_PE", label = "RA (hh:mm:ss)", numberValue = 0.15d, maximum = 24d, numberFormat = "%010.6m")
    private INDINumberElement eqPenRa;

    /**
     * Simulated periodic error in declination.
     */
    @InjectElement(name = "DEC_PE", label = "DEC (dd:mm:ss)", numberValue = 0.15d, minimum = MIN_DECLINATION_DEGREES, maximum = 90d, numberFormat = "%010.6m")
    private INDINumberElement eqPenDec;

    /**
     * Enable client to manually add periodic error northward or southward for
     * simulation purposes.
     */
    @InjectProperty(name = "PE_NS", label = "PE N/S", group = MOTION_TAB)
    private INDISwitchProperty periodicErrorNS;

    /**
     * manually add periodic error northward simulation purposes.
     */
    @InjectElement(name = "PE_N", label = "North")
    private INDISwitchElement periodicErrorNSNorth;

    /**
     * manually add periodic error southward simulation purposes.
     */
    @InjectElement(name = "PE_S", label = "South")
    private INDISwitchElement periodicErrorNSSouth;

    /**
     * Enable client to manually add periodic error westward or easthward for
     * simulation purposes.
     */
    @InjectProperty(name = "PE_WE", label = "PE W/E", group = MOTION_TAB)
    private INDISwitchProperty periodicErrorWE;

    /**
     * manually add periodic error westward for simulation purposes.
     */
    @InjectElement(name = "PE_W", label = "West")
    private INDISwitchElement periodicErrorWEWest;

    /**
     * manually add periodic error easthward for simulation purposes.
     */
    @InjectElement(name = "PE_E", label = "East")
    private INDISwitchElement periodicErrorWEEast;

    /**
     * How fast do we guide compared to sidereal rate.
     */
    @InjectProperty(name = "GUIDE_RATE", label = "Guiding Rate", group = MOTION_TAB, timeout = 0)
    private INDINumberProperty guideRate;

    /**
     * How fast do we guide compared to sidereal rate in the west/east axis.
     */
    @InjectElement(name = "GUIDE_RATE_WE", label = "W/E Rate", numberValue = 0.3d, maximum = 1d, step = 0.1d, numberFormat = "%g")
    private INDINumberElement guideRateWE;

    /**
     * How fast do we guide compared to sidereal rate in the north/south axis.
     */
    @InjectElement(name = "GUIDE_RATE_NS", label = "N/S Rate", numberValue = 0.3d, maximum = 1d, step = 0.1d, numberFormat = "%g")
    private INDINumberElement guideRateNS;

    /**
     * last time value (last pol interval.
     */
    private long lastSystime = -1;

    /**
     * delta of the last update.
     */
    private final INDIDirection lastDelta = new INDIDirection();

    /**
     * Standard constructor for the simulated telescope driver.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public TelescopeSimulator(INDIConnection connection) {
        super(connection);
        current.set(0, MAX_DECLINATION_DEGREES);
        parkExtension.setParked(false);

        // Let's simulate it to be an F/10 8" telescope
        scopeParametersAperture.setValue(DEFAULT_APERTURE);
        scopeParametersFocalLength.setValue(DEFAULT_FOCAL_LENGTH);
        scopeParametersGuiderAperture.setValue(DEFAULT_APERTURE);
        scopeParametersGuiderFocalLength.setValue(DEFAULT_FOCAL_LENGTH);

        trackState = TelescopeStatus.SCOPE_IDLE;
        guideRate.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newGuideRateValue(elementsAndValues);
            }
        });
        periodicErrorNS.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newPErrNSValue(elementsAndValues);
            }
        });
        periodicErrorWE.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newPErrWEValue(elementsAndValues);
            }
        });
    }

    /**
     * the client send a new value for the Guide rate.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newGuideRateValue(INDINumberElementAndValue[] elementsAndValues) {
        guideRate.setValues(elementsAndValues);
        guideRate.setState(OK);
        updateProperty(guideRate);
    };

    /**
     * the client send a new value for the periodic error in the north south
     * axis.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newPErrNSValue(INDISwitchElementAndValue[] elementsAndValues) {
        periodicErrorNS.setValues(elementsAndValues);
        periodicErrorNS.setState(OK);

        if (periodicErrorNSNorth.isOn()) {
            eqPenDec.setValue(eqPenDec.getValue() + SID_RATE * guideRateNS.getValue());
            LOG.info(String.format("Simulating PE in NORTH direction for value of %g", SID_RATE));
        } else {
            eqPenDec.setValue(eqPenDec.getValue() - SID_RATE * guideRateNS.getValue());
            LOG.info(String.format("Simulating PE in SOUTH direction for value of %g", SID_RATE));
        }
        periodicErrorNS.resetAllSwitches();
        updateProperty(periodicErrorNS);
        updateProperty(eqPen);
    }

    /**
     * the client send a new value for the periodic error in the west east axis.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newPErrWEValue(INDISwitchElementAndValue[] elementsAndValues) {
        periodicErrorWE.setValues(elementsAndValues);
        periodicErrorWE.setState(OK);

        if (periodicErrorWEWest.isOn()) {
            eqPenRa.setValue(eqPenRa.getValue() - SID_RATE / HOUR_TO_DEGREE * guideRateWE.getValue());
            LOG.info(String.format("Simulator PE in WEST direction for value of %g", SID_RATE));
        } else {
            eqPenRa.setValue(eqPenRa.getValue() + SID_RATE / HOUR_TO_DEGREE * guideRateWE.getValue());
            LOG.info(String.format("Simulator PE in EAST direction for value of %g", SID_RATE));
        }

        periodicErrorWE.resetAllSwitches();
        updateProperty(periodicErrorWE);
        updateProperty(eqPen);
    }

    @Override
    protected boolean abort() {
        moveExtention.abort();
        if (parkExtension.isBusy()) {
            parkExtension.setIdle();
        }
        if (eqn.getState() == BUSY) {
            eqn.setState(IDLE);
            updateProperty(eqn);
        }
        trackState = TelescopeStatus.SCOPE_IDLE;
        abort.setState(OK);
        abort.resetAllSwitches();
        updateProperty(abort);
        LOG.info("Telescope aborted.");

        return true;
    }

    @Override
    protected void doGoto(double ra, double dec) {
        // IDLog("ScopeSim Goto\n");
        target.set(ra, dec);
        String formattedRaString, formattedDecString;

        formattedRaString = target.getRaString();
        formattedDecString = target.getDecString();

        parkExtension.setParked(false);
        trackState = TelescopeStatus.SCOPE_SLEWING;

        eqn.setState(BUSY);

        LOG.info(String.format("Slewing to RA: %s - DEC: %s", formattedRaString, formattedDecString));
    }

    @Override
    public void park() {
        target.set(0, MAX_DECLINATION_DEGREES);
        trackState = TelescopeStatus.SCOPE_PARKING;
        LOG.info("Parking telescope in progress...");
    }

    @Override
    protected void readScopeStatus() {
        INDIDirection da = new INDIDirection();
        INDIDirection delta = new INDIDirection();

        double dt = 0d;
        int nsGuideDir = -1, weGuideDir = -1;

        /* update elapsed time since last poll, don't presume exactly POLLMS */
        long now = System.currentTimeMillis();
        if (lastSystime == -1) {
            lastSystime = now;
        }
        dt = (now - lastSystime) / MILLISECONDS_PER_SECOND;
        lastSystime = now;

        if (Math.abs(target.getRa() - current.getRa()) * HOUR_TO_DEGREE >= GOTO_LIMIT) {
            da.setRa(GOTO_RATE * dt);
        } else if (Math.abs(target.getRa() - current.getRa()) * HOUR_TO_DEGREE >= SLEW_LIMIT) {
            da.setRa(SLEW_RATE * dt);
        } else {
            da.setRa(FINE_SLEW_RATE * dt);
        }
        if (Math.abs(target.getDec() - current.getDec()) >= GOTO_LIMIT) {
            da.setDec(GOTO_RATE * dt);
        } else if (Math.abs(target.getDec() - current.getDec()) >= SLEW_LIMIT) {
            da.setDec(SLEW_RATE * dt);
        } else {
            da.setDec(FINE_SLEW_RATE * dt);
        }
        moveExtention.update(current);

        /*
         * Process per current state. We check the state of
         * EQUATORIAL_EOD_COORDS_REQUEST and act acoordingly
         */
        switch (trackState) {
            case SCOPE_SLEWING:
            case SCOPE_PARKING:
                readScopeStatusParking(da, delta);
                break;
            case SCOPE_IDLE:
                eqn.setState(IDLE);
                break;
            case SCOPE_TRACKING:
                readScopeStatusTracking(delta, dt, nsGuideDir, weGuideDir);
                break;
            default:
                break;
        }
        LOG.info(String.format("Current RA: %s Current DEC: %s", current.getRaString(), current.getDecString()));
        newRaDec(current.getRa(), current.getDec());
    }

    /**
     * Ok, we read the scope status and simulate the tracking state.
     * 
     * @param delta
     *            the delta direction
     * @param dt
     *            the delta in time
     * @param nsGuideDir
     *            the north south guid direction
     * @param weGuideDir
     *            the west east guide direction
     */
    private void readScopeStatusTracking(INDIDirection delta, double dt, int nsGuideDir, int weGuideDir) {

        INDIDirection guideDt = new INDIDirection();
        /* tracking */

        dt *= MILLISECONDS_PER_SECOND;

        if (guiderNSTarget[GUIDE_NORTH] > 0) {
            LOG.info("Commanded to GUIDE NORTH for " + guiderNSTarget[GUIDE_NORTH] + " ms");
            nsGuideDir = GUIDE_NORTH;
        } else if (guiderNSTarget[GUIDE_SOUTH] > 0) {
            LOG.info("Commanded to GUIDE SOUTH for " + guiderNSTarget[GUIDE_SOUTH] + " ms");
            nsGuideDir = GUIDE_SOUTH;
        }

        // WE Guide Selection
        if (guiderEWTarget[GUIDE_WEST] > 0) {
            weGuideDir = GUIDE_WEST;
            LOG.info("Commanded to GUIDE WEST for " + guiderEWTarget[GUIDE_WEST] + " ms");
        } else if (guiderEWTarget[GUIDE_EAST] > 0) {
            weGuideDir = GUIDE_EAST;
            LOG.info("Commanded to GUIDE EAST for " + guiderEWTarget[GUIDE_EAST] + " ms");
        }

        if (nsGuideDir != -1) {

            guideDt.setDec(SID_RATE * guideRateNS.getValue() * guiderNSTarget[nsGuideDir] / MILLISECONDS_PER_SECOND * (nsGuideDir == GUIDE_NORTH ? 1 : -1));

            // If time remaining is more that dt, then decrement and
            if (guiderNSTarget[nsGuideDir] >= dt) {
                guiderNSTarget[nsGuideDir] -= dt;
            } else {
                guiderNSTarget[nsGuideDir] = 0;
            }
            eqPenDec.setValue(eqPenDec.getValue() + guideDt.getDec());

        }

        if (weGuideDir != -1) {

            guideDt.setRa(SID_RATE / HOUR_TO_DEGREE * guideRateWE.getValue() * guiderEWTarget[weGuideDir] / MILLISECONDS_PER_SECOND * (weGuideDir == GUIDE_WEST ? -1 : 1));

            if (guiderEWTarget[weGuideDir] >= dt) {
                guiderEWTarget[weGuideDir] -= dt;
            } else {
                guiderEWTarget[weGuideDir] = 0;
            }
            eqPenRa.setValue(eqPenRa.getValue() + guideDt.getRa());

        }

        // Mention the followng:
        // Current RA displacemet and direction
        // Current DEC displacement and direction
        // Amount of RA GUIDING correction and direction
        // Amount of DEC GUIDING correction and direction

        delta.setRa(eqPenRa.getValue() - target.getRa());
        delta.setDec(eqPenDec.getValue() - target.getDec());

        INDIDirection pe = new INDIDirection(eqPenRa.getValue(), eqPenDec.getValue());

        if (!eq(delta.getRa(), lastDelta.getRa()) || !eq(delta.getDec(), lastDelta.getDec()) || !eq(guideDt.getRa(), 0) || !eq(guideDt.getDec(), 0)) {
            lastDelta.set(delta.getRa(), delta.getDec());
            LOG.info(String.format("dt is %g", dt));
            LOG.info(String.format("RA Displacement (%s) %s -- %s of target RA %s", delta.getRaString(), pe.getRaString(), eqPenRa.getValue() - target.getRa() > 0 ? "East"
                    : "West", target.getRaString()));
            LOG.info(String.format("DEC Displacement (%s) %s -- %s of target RA %s", delta.getDecString(), pe.getDecString(), eqPenDec.getValue() - target.getDec() > 0
                    ? "North" : "South", target.getDecString()));
            LOG.info(String.format("RA Guide Correction (%g) %s -- Direction %s", guideDt.getRa(), guideDt.getRaString(), guideDt.getRa() > 0 ? "East" : "West"));
            LOG.info(String.format("DEC Guide Correction (%g) %s -- Direction %s", guideDt.getDec(), guideDt.getDecString(), guideDt.getDec() > 0 ? "North" : "South"));
        }

        if (nsGuideDir != -1 || weGuideDir != -1) {
            updateProperty(eqPen);
        }
    }

    /**
     * Ok, we read the scope status and simulate the paring state.
     * 
     * @param da
     *            do not know really
     * @param delta
     *            the delta direction
     */
    private void readScopeStatusParking(INDIDirection da, INDIDirection delta) {
        int nlocked = 0;
        /* slewing - nail it when both within one pulse @ SLEWRATE */

        delta.setRa(target.getRa() - current.getRa());

        if (Math.abs(delta.getRa()) * HOUR_TO_DEGREE <= da.getRa()) {
            current.setRa(target.getRa());
            nlocked++;
        } else if (delta.getRa() > 0) {
            current.addRa(da.getRa() / HOUR_TO_DEGREE);
        } else {
            current.addRa(-da.getRa() / HOUR_TO_DEGREE);
        }
        delta.setDec(target.getDec() - current.getDec());
        if (Math.abs(delta.getDec()) <= da.getDec()) {
            current.setDec(target.getDec());
            nlocked++;
        } else if (delta.getDec() > 0) {
            current.addDec(da.getDec());
        } else {
            current.addDec(-da.getDec());
        }
        eqn.setState(BUSY);

        if (nlocked == 2) {
            if (trackState == TelescopeStatus.SCOPE_SLEWING) {

                // Initially no PE in both axis.
                eqnRa.setValue(current.getRa());
                eqnDec.setValue(current.getDec());
                updateProperty(eqn);
                trackState = TelescopeStatus.SCOPE_TRACKING;

                eqn.setState(OK);
                LOG.info("Telescope slew is complete. Tracking...");
            } else {
                trackState = TelescopeStatus.SCOPE_PARKED;
                eqn.setState(IDLE);
                LOG.info("Telescope parked successfully.");
            }
        }
    }

    @Override
    public boolean sync(double ra, double dec) {
        current.set(ra, dec);

        eqPenRa.setValue(ra);
        eqPenDec.setValue(dec);
        updateProperty(eqPen);

        LOG.info("Sync is successful.");

        trackState = TelescopeStatus.SCOPE_IDLE;
        eqn.setState(OK);

        newRaDec(current.getRa(), current.getDec());

        return true;
    }

    @Override
    protected long updateInterfall() {
        return POLLMS;
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        super.driverConnect(timestamp);
        LOG.info("Telescope simulator is online.");
        addProperty(guideRate);
        addProperty(eqPen);
        addProperty(periodicErrorNS);
        addProperty(periodicErrorWE);
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        super.driverDisconnect(timestamp);
        LOG.info("Telescope simulator is offline.");
        removeProperty(guideRate);
        removeProperty(eqPen);
        removeProperty(periodicErrorNS);
        removeProperty(periodicErrorWE);
    }

    @Override
    public String getName() {
        return "Telescope Simulator";
    }

    @Override
    protected boolean updateLocation(double targetLat, double targetLong, double targetElev) {
        // we will ignore the setting of the current location in the simulator
        return true;
    }

    @Override
    protected boolean updateTime(Date utc, double d) {
        // we will ignore the setting of current time in the simulator
        return true;
    }

}
