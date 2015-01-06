package org.indilib.i4j.driver.telescope.simulator;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Date;

import net.sourceforge.novaforjava.JulianDay;
import net.sourceforge.novaforjava.api.LnDate;
import net.sourceforge.novaforjava.api.LnHrzPosn;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.telescope.INDIDirection;
import org.indilib.i4j.driver.telescope.INDITelescope;
import org.indilib.i4j.driver.telescope.INDITelescopeMoveImplementation;
import org.indilib.i4j.driver.telescope.INDITelescopeSyncInterface;
import org.indilib.i4j.driver.telescope.alignment.AlignmentDatabaseEntry;
import org.indilib.i4j.driver.telescope.alignment.DoubleRef;
import org.indilib.i4j.driver.telescope.alignment.MathPluginManagement;
import org.indilib.i4j.driver.telescope.alignment.MountAlignment;
import org.indilib.i4j.driver.telescope.alignment.TelescopeDirectionVector;
import org.indilib.i4j.driver.telescope.mount.AxisWithEncoder;
import org.indilib.i4j.driver.telescope.mount.Mount;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An telescope simulator that has "real" encoders and can sync. So a template
 * vor all real horizontal based scopes.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TelescopeAltAzSimulator extends INDITelescope implements INDITelescopeSyncInterface {

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
    private static final Logger LOG = LoggerFactory.getLogger(TelescopeAltAzSimulator.class);

    /**
     * the simulated axis, that will update its position itself.
     */
    class SimulatedAxisWithEncoder extends AxisWithEncoder {

        /**
         * when was the last update (in system millisecond time).
         */
        private long updateTime = -1;

        /**
         * update the encoder simulate the movement acourding to the speed.
         */
        public void update() {
            long now = System.currentTimeMillis();
            if (updateTime > 0) {
                double millisecondsPassed = now - updateTime;
                setPosition((long) (getPosition() + (speedInTicksPerSecond * millisecondsPassed / MILLISECONDS_PER_SECOND)));
            }
            updateTime = now;
        }

    }

    /**
     * Simulate a mount with two axis. It will run a thread as long as it is
     * connected.
     */
    class SimulatedMount extends Mount<SimulatedAxisWithEncoder> implements Runnable {

        /**
         * how many times will the update tread trigger during a scope update
         * interfall.
         */
        private static final long UPDATE_TIMES_PER_INTERFALL = 4L;

        /**
         * stop the thread.
         */
        private boolean stop = false;

        @Override
        public void run() {
            while (!stop) {
                try {
                    Thread.sleep(updateInterfall() / UPDATE_TIMES_PER_INTERFALL);
                    horizontalAxis.update();
                    verticalAxis.update();
                } catch (InterruptedException e) {
                    LOG.error("interrupted exception");
                }
            }

        }

        @Override
        protected SimulatedAxisWithEncoder createHorizontalAxis() {
            return new SimulatedAxisWithEncoder();
        }

        @Override
        protected SimulatedAxisWithEncoder createVerticalAxis() {
            return new SimulatedAxisWithEncoder();
        }
    }

    /**
     * the simulated mount.
     */
    private SimulatedMount mount = new SimulatedMount();

    /**
     * The math plugin for the calculation from eqn to hoziontal.
     */
    @InjectExtension
    private MathPluginManagement mathPluginManagement;

    /**
     * constructor for the altaz simulator.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public TelescopeAltAzSimulator(INDIConnection connection) {
        super(connection);
        mathPluginManagement.setApproximateAlignment(MountAlignment.ZENITH);
        mathPluginManagement.forceActive();
        mathPluginManagement.initialise();
        moveExtention.setMoveImpl(new INDITelescopeMoveImplementation(moveExtention));
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        super.driverConnect(timestamp);
        mathPluginManagement.connect();
        mount = new SimulatedMount();
        new Thread(mount, "Simulated Telescope Mount").start();
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        super.driverDisconnect(timestamp);
        mathPluginManagement.disconnect();
        mount.stop = true;
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
     * update the commands to the mount, according to the goto position (if it
     * is set).
     */
    protected void gotoUpdate() {
        if (gotoDirection == null && moveExtention.hasMoveRequest()) {
            gotoDirection = new INDIDirection(this.eqnRa.getValue(), this.eqnDec.getValue());
            if (trackState == TelescopeStatus.SCOPE_IDLE) {
                trackState = TelescopeStatus.SCOPE_SLEWING;
            }
        }
        if (gotoDirection != null) {
            moveExtention.update(gotoDirection);

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
    public boolean sync(double rightAscension, double declination) {
        LnHrzPosn actualAltAz = new LnHrzPosn();
        actualAltAz.az = mount.getHorizontalPosition();
        actualAltAz.alt = mount.getVerticalPosition();
        TelescopeDirectionVector vector = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualAltAz);
        AlignmentDatabaseEntry e = new AlignmentDatabaseEntry(rightAscension, declination, jdTimeFromCurrentMiliseconds(System.currentTimeMillis()), vector);
        this.mathPluginManagement.add(e);
        return true;
    }

    @Override
    public void isBeingDestroyed() {
        if (mount != null) {
            mount.stop();
        }
    }
}
