package org.indilib.i4j.driver.telescope.nexstar.gt;

/*
 * #%L
 * INDI for Java Driver for the NexStar GT Mount
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

import static org.indilib.i4j.Constants.PropertyStates.IDLE;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import net.sourceforge.novaforjava.JulianDay;
import net.sourceforge.novaforjava.api.LnDate;
import net.sourceforge.novaforjava.api.LnHrzPosn;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.serial.INDISerialPortExtension;
import org.indilib.i4j.driver.serial.INDISerialPortInterface;
import org.indilib.i4j.driver.telescope.INDIDirection;
import org.indilib.i4j.driver.telescope.INDITelescope;
import org.indilib.i4j.driver.telescope.INDITelescopeSyncInterface;
import org.indilib.i4j.driver.telescope.alignment.AlignmentDatabaseEntry;
import org.indilib.i4j.driver.telescope.alignment.DoubleRef;
import org.indilib.i4j.driver.telescope.alignment.MathPluginManagement;
import org.indilib.i4j.driver.telescope.alignment.MountAlignment;
import org.indilib.i4j.driver.telescope.alignment.TelescopeDirectionVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexStarGt extends INDITelescope implements INDITelescopeSyncInterface, INDISerialPortInterface {

    /**
     * the right assertion pression when a goto is defined as position reached.
     */
    private static final double RA_GOTO_PRECISION = 0.004;

    /**
     * the declination pression when a goto is defined as position reached.
     */
    private static final double DEC_GOTO_PRECISION = 0.015;

    /**
     * the number of milliseconds per second.
     */
    private static final double MILLISECONDS_PER_SECOND = 1000d;

    /**
     * the number of arc minutes per degree.
     */
    private static final double ARCMINUTES_PER_DEGREE = 60d;

    /**
     * The logger for any messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NexStarGt.class);

    /**
     * the nexstar gt mount interface.
     */
    private NexStarGtMount mount;

    /**
     * The math plugin for the calculation from eqn to hoziontal.
     */
    @InjectExtension
    private MathPluginManagement mathPluginManagement;

    public NexStarGt(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        mathPluginManagement.setApproximateAlignment(MountAlignment.ZENITH);
        mathPluginManagement.forceActive();
        mathPluginManagement.initialise();
    }

    protected INDISerialPortExtension getSerial() {
        return this.serialPortExtension;
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        super.driverConnect(timestamp);
        serialPortExtension.connect();
        mathPluginManagement.connect();
        mount = new NexStarGtMount(this.serialPortExtension);
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        mount.stop();
        this.mathPluginManagement.disconnect();
        this.serialPortExtension.disconnect();
        super.driverDisconnect(timestamp);
    }

    @Override
    protected boolean abort() {
        gotoDirection = null;
        trackState = TelescopeStatus.SCOPE_IDLE;
        mount.stop();
        return true;
    }

    /**
     * Current goto or tracking position.
     */
    private INDIDirection gotoDirection = null;

    @Override
    protected void doGoto(double ra, double dec) {
        parkExtension.setParked(false);
        trackState = TelescopeStatus.SCOPE_SLEWING;

        gotoDirection = new INDIDirection(ra, dec);
        gotoUpdate();
    }

    /**
     * update the commands to the mount, acording to the goto position (if it is
     * set).
     */
    protected void gotoUpdate() {
        if (gotoDirection != null) {
            long now = System.currentTimeMillis();
            long doubleUpdateInterfall = updateInterfall() * 2;

            double jdNow = jdTimeFromCurrentMiliseconds(now);
            double jdTarget = jdTimeFromCurrentMiliseconds(now + doubleUpdateInterfall);

            TelescopeDirectionVector apparentTelescopeDirectionVector = new TelescopeDirectionVector();
            mathPluginManagement.transformCelestialToTelescope(gotoDirection.getRa(), gotoDirection.getDec(), jdTarget - jdNow, apparentTelescopeDirectionVector);
            LnHrzPosn actualAltAz = new LnHrzPosn();
            apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);

            mount.gotoWithSpeed(actualAltAz.az, actualAltAz.alt, ((double) doubleUpdateInterfall) / MILLISECONDS_PER_SECOND);
        }
    }

    /**
     * convert the millisecond system time in julian date.
     * 
     * @param now
     *            the system time in milliseconds.
     * @return the julian date.
     */
    protected double jdTimeFromCurrentMiliseconds(long now) {
        LnDate dateNow = new LnDate();
        JulianDay.ln_get_date_from_UTC_milliseconds(dateNow, now);
        return JulianDay.ln_get_julian_day(dateNow);
    }

    @Override
    protected void readScopeStatus() {
        gotoUpdate();

        LnHrzPosn actualAltAz = new LnHrzPosn();
        actualAltAz.az = mount.getHorizontalPosition();
        actualAltAz.alt = mount.getVerticalPosition();

        DoubleRef rightAscensionRef = new DoubleRef();
        DoubleRef declinationRef = new DoubleRef();
        TelescopeDirectionVector vector = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualAltAz);
        mathPluginManagement.transformTelescopeToCelestial(vector, 0, rightAscensionRef, declinationRef);
        newRaDec(rightAscensionRef.getValue(), declinationRef.getValue());
        if (gotoDirection != null && trackState == TelescopeStatus.SCOPE_SLEWING) {
            double raDiff = Math.abs(rightAscensionRef.getValue() - gotoDirection.getRa());
            double decDiff = Math.abs(declinationRef.getValue() - gotoDirection.getDec());
            if (decDiff < DEC_GOTO_PRECISION && raDiff < RA_GOTO_PRECISION) {
                // target reached!
                if (coordTrack.isOn()) {
                    LOG.info("target reached, switching to tracking");
                    trackState = TelescopeStatus.SCOPE_TRACKING;
                } else {
                    LOG.info("target reached, going idle");
                    trackState = TelescopeStatus.SCOPE_IDLE;
                    gotoDirection = null;
                    mount.stop();
                }
            }
        }
    }

    @Override
    protected boolean updateLocation(double targetLat, double targetLong, double targetElev) {
        mathPluginManagement.setDatabaseReferencePosition(targetLat, targetLong);
        mathPluginManagement.initialise();
        return true;
    }

    @Override
    protected boolean updateTime(Date utc, double d) {
        return true;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    protected boolean moveNS(TelescopeMotionNS dir) {
        this.movementNSS.resetAllSwitches();
        this.movementNSS.setState(IDLE);
        String message = null;
        double rate;
        if (dir == TelescopeMotionNS.MOTION_NORTH) {
            rate = 1d / ARCMINUTES_PER_DEGREE;
        } else {
            rate = -1d / ARCMINUTES_PER_DEGREE;
        }
        if (trackState == TelescopeStatus.SCOPE_IDLE) {
            trackState = TelescopeStatus.SCOPE_SLEWING;
            gotoDirection = new INDIDirection(eqnRa.getValue(), eqnDec.getValue() + rate);
        } else if (trackState == TelescopeStatus.SCOPE_TRACKING || trackState == TelescopeStatus.SCOPE_SLEWING) {
            gotoDirection.addDec(rate);
        } else {
            message = "can only move if the scope is idle or tracking";
        }
        updateProperty(this.movementNSS, message);

        return true;
    }

    @Override
    protected boolean moveWE(TelescopeMotionWE dir) {
        this.movementWES.resetAllSwitches();
        this.movementWES.setState(IDLE);
        String message = null;
        double rate;
        if (dir == TelescopeMotionWE.MOTION_WEST) {
            rate = 1d / ARCMINUTES_PER_DEGREE;
        } else {
            rate = -1d / ARCMINUTES_PER_DEGREE;
        }
        if (trackState == TelescopeStatus.SCOPE_IDLE) {
            trackState = TelescopeStatus.SCOPE_SLEWING;
            gotoDirection = new INDIDirection(eqnRa.getValue() + rate, eqnDec.getValue());
        } else if (trackState == TelescopeStatus.SCOPE_TRACKING || trackState == TelescopeStatus.SCOPE_SLEWING) {
            gotoDirection.addRa(rate);
        } else {
            message = "can only move if the scope is idle or tracking";
        }
        updateProperty(this.movementWES, message);

        return true;
    }

    @Override
    public boolean sync(double rightAscension, double declination) {
        LnHrzPosn actualAltAz = new LnHrzPosn();
        actualAltAz.az = mount.getHorizontalPosition();
        actualAltAz.alt = mount.getVerticalPosition();
        TelescopeDirectionVector vector = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualAltAz);
        AlignmentDatabaseEntry e = new AlignmentDatabaseEntry(rightAscension, declination, jdTimeFromCurrentMiliseconds(System.currentTimeMillis()), vector);
        this.mathPluginManagement.add(e);
        return true;
    }

}
