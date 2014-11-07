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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import net.sourceforge.novaforjava.JulianDay;
import net.sourceforge.novaforjava.api.LnDate;
import net.sourceforge.novaforjava.api.LnHrzPosn;
import net.sourceforge.novaforjava.api.LnLnlatPosn;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.INDISexagesimalFormatter;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.telescope.INDITelescope;
import org.indilib.i4j.driver.telescope.alignment.DoubleRef;
import org.indilib.i4j.driver.telescope.alignment.MathPluginManagement;
import org.indilib.i4j.driver.telescope.alignment.MountAlignment;
import org.indilib.i4j.driver.telescope.alignment.TelescopeDirectionVector;
import org.indilib.i4j.driver.telescope.mount.AxisWithEncoder;
import org.indilib.i4j.driver.telescope.mount.Mount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An telescope simulator that has "real" encoders and can sync. So a template
 * vor all real horizontal based scopes.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TelescopeAltAzSimulator extends INDITelescope {

    /**
     * The logger for any messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TelescopeAltAzSimulator.class);

    class SimulatedAxisWithEncoder extends AxisWithEncoder {

        private static final double MILLISECONDS_PER_SECOND = 1000d;

        private long updateTime = -1;

        public void update() {
            long now = System.currentTimeMillis();
            if (updateTime > 0) {
                double millisecondsPassed = now - updateTime;
                this.position = (long) (this.position + (speedInTicksPerSecond * millisecondsPassed / MILLISECONDS_PER_SECOND));
            }
            updateTime = now;
        }

    }

    class SimulatedMount extends Mount<SimulatedAxisWithEncoder> implements Runnable {

        private boolean stop = false;

        @Override
        public void run() {
            while (!stop) {
                try {
                    Thread.sleep(updateInterfall() / 4L);
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

    @InjectExtension
    private MathPluginManagement mathPluginManagement;

    /**
     * constructor for the altaz simulator.
     * 
     * @param inputStream
     *            the input stream.
     * @param outputStream
     *            the output stream
     */
    public TelescopeAltAzSimulator(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        mathPluginManagement.setApproximateAlignment(MountAlignment.ZENITH);

        LnLnlatPosn position = new LnLnlatPosn();
        INDISexagesimalFormatter indiSexagesimalFormatter = new INDISexagesimalFormatter("%010.6m");
        position.lng = indiSexagesimalFormatter.parseSexagesimal("16:22:00");
        position.lat = indiSexagesimalFormatter.parseSexagesimal("48:13:00");
        mathPluginManagement.setDatabaseReferencePosition(position.lat, position.lng);
        mathPluginManagement.initialise();
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
        gotoRa = Double.NaN;
        gotoDec = Double.NaN;
        mount.stop();
        return true;
    }

    private double gotoRa = Double.NaN;

    private double gotoDec = Double.NaN;

    @Override
    protected void doGoto(double ra, double dec) {
        gotoRa = ra;
        gotoDec = dec;
        gotoUpdate(gotoRa, gotoDec);
    }

    protected void gotoUpdate(double ra, double dec) {

        long now = System.currentTimeMillis();
        long doubleUpdateInterfall = updateInterfall() * 2;

        double jdNow = jdTimeFromCurrentMiliseconds(now);
        double jdTarget = jdTimeFromCurrentMiliseconds(now + doubleUpdateInterfall);

        TelescopeDirectionVector apparentTelescopeDirectionVector = new TelescopeDirectionVector();
        mathPluginManagement.transformCelestialToTelescope(ra, dec, jdTarget - jdNow, apparentTelescopeDirectionVector);
        LnHrzPosn actualAltAz = new LnHrzPosn();
        apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);

        mount.gotoWithSpeed(actualAltAz.az, actualAltAz.alt, ((double) doubleUpdateInterfall) / 1000d);
    }

    protected double jdTimeFromCurrentMiliseconds(long now) {
        LnDate dateNow = new LnDate();
        JulianDay.ln_get_date_from_UTC_milliseconds(dateNow, now);
        return JulianDay.ln_get_julian_day(dateNow);
    }

    @Override
    protected void readScopeStatus() {
        if (!Double.isNaN(gotoRa)) {
            gotoUpdate(gotoRa, gotoDec);
        }
        LnHrzPosn actualAltAz = new LnHrzPosn();
        actualAltAz.az = mount.getHorizontalPosition();
        actualAltAz.alt = mount.getVerticalPosition();

        DoubleRef rightAscensionRef = new DoubleRef();
        DoubleRef declinationRef = new DoubleRef();
        TelescopeDirectionVector vector = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualAltAz);
        mathPluginManagement.transformTelescopeToCelestial(vector, 0, rightAscensionRef, declinationRef);
        newRaDec(rightAscensionRef.getValue(), declinationRef.getValue());
    }

    @Override
    protected boolean updateLocation(double targetLat, double targetLong, double targetElev) {
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
}
