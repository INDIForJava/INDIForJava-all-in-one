package org.indilib.i4j.driver.telescope.alignment;

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

import java.io.File;

import net.sourceforge.novaforjava.JulianDay;
import net.sourceforge.novaforjava.Transform;
import net.sourceforge.novaforjava.api.LnDate;
import net.sourceforge.novaforjava.api.LnEquPosn;
import net.sourceforge.novaforjava.api.LnHrzPosn;
import net.sourceforge.novaforjava.api.LnLnlatPosn;

import org.indilib.i4j.INDISexagesimalFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildInMathPluginTest {

    /**
     * The logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BuiltInMathPlugin.class);

    private INDISexagesimalFormatter indiSexagesimalFormatter = new INDISexagesimalFormatter("%010.6m");

    @Test
    public void testMathPluginTransformCelestialToTelescopeWithZeroTo4PointsInDb() {
        System.setProperty("user.home", new File("target").getAbsolutePath());
        IMathPlugin plugin = new BuiltInMathPlugin();
        plugin.create();
        InMemoryDatabase inMemoryDatabase = new InMemoryDatabase();
        plugin.initialise(inMemoryDatabase);
        plugin.setApproximateAlignment(MountAlignment.ZENITH);

        LnLnlatPosn position = new LnLnlatPosn();
        position.lng = indiSexagesimalFormatter.parseSexagesimal("16:22:00");
        position.lat = indiSexagesimalFormatter.parseSexagesimal("48:13:00");
        inMemoryDatabase.setDatabaseReferencePosition(position.lat, position.lng);

        double rightAscension = indiSexagesimalFormatter.parseSexagesimal("16:41:41");
        double declination = indiSexagesimalFormatter.parseSexagesimal("36:27:36");
        double expectedAlt = indiSexagesimalFormatter.parseSexagesimal("12:12:06");
        double expectedAz = indiSexagesimalFormatter.parseSexagesimal("47:56:00");

        LnDate date = new LnDate(); // UTC
        date.years = 2014;
        date.months = 11;
        date.days = 2;
        date.hours = 5;
        date.minutes = 10;
        double jd = JulianDay.ln_get_julian_day(date);
        double jdNow = JulianDay.ln_get_julian_from_sys();
        double julianOffset = jd - jdNow;

        printSimpleAltAzValues(position, rightAscension, declination, julianOffset, expectedAlt, expectedAz);

        TelescopeDirectionVector apparentTelescopeDirectionVector = new TelescopeDirectionVector();
        plugin.transformCelestialToTelescope(rightAscension, declination, julianOffset, apparentTelescopeDirectionVector);

        LnHrzPosn actualAltAz = new LnHrzPosn();
        apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);
        printAltAz(actualAltAz, expectedAlt, expectedAz);

        DoubleRef rightAscensionRef = new DoubleRef();
        DoubleRef declinationRef = new DoubleRef();
        TelescopeDirectionVector vector = new TelescopeDirectionVector();
        vector.copyFrom(apparentTelescopeDirectionVector);
        plugin.transformTelescopeToCelestial(vector, julianOffset, rightAscensionRef, declinationRef);
        Assert.assertEquals(rightAscensionRef.getValue(), rightAscension, 0.01d);
        Assert.assertEquals(declinationRef.getValue(), declination, 0.01d);

        // -----------------------------------------------------------
        AlignmentDatabaseEntry e = new AlignmentDatabaseEntry();
        e.declination = declination;
        e.rightAscension = rightAscension;
        e.telescopeDirection = apparentTelescopeDirectionVector;
        e.observationJulianDate = jd;
        inMemoryDatabase.getAlignmentDatabase().add(e);
        plugin.initialise(inMemoryDatabase);

        // -----------------------------------------------------------
        rightAscension = indiSexagesimalFormatter.parseSexagesimal("16:30:13");
        declination = indiSexagesimalFormatter.parseSexagesimal("21:29:22");
        expectedAlt = indiSexagesimalFormatter.parseSexagesimal("02:09:44");
        expectedAz = indiSexagesimalFormatter.parseSexagesimal("59:31:04");
        date.minutes = 11;
        jd = JulianDay.ln_get_julian_day(date);
        jdNow = JulianDay.ln_get_julian_from_sys();
        julianOffset = jd - jdNow;

        printSimpleAltAzValues(position, rightAscension, declination, julianOffset, expectedAlt, expectedAz);

        apparentTelescopeDirectionVector = new TelescopeDirectionVector();
        plugin.transformCelestialToTelescope(rightAscension, declination, julianOffset, apparentTelescopeDirectionVector);

        actualAltAz = new LnHrzPosn();
        apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);
        printAltAz(actualAltAz, expectedAlt, expectedAz);

        vector = new TelescopeDirectionVector();
        vector.copyFrom(apparentTelescopeDirectionVector);
        plugin.transformTelescopeToCelestial(vector, julianOffset, rightAscensionRef, declinationRef);
        Assert.assertEquals(rightAscensionRef.getValue(), rightAscension, 0.01d);
        Assert.assertEquals(declinationRef.getValue(), declination, 0.01d);

        // -----------------------------------------------------------
        e = new AlignmentDatabaseEntry();
        e.declination = declination;
        e.rightAscension = rightAscension;
        e.telescopeDirection = apparentTelescopeDirectionVector;
        e.observationJulianDate = jd;
        inMemoryDatabase.getAlignmentDatabase().add(e);
        plugin.initialise(inMemoryDatabase);
        // -----------------------------------------------------------

        rightAscension = indiSexagesimalFormatter.parseSexagesimal("14:44:59");
        declination = indiSexagesimalFormatter.parseSexagesimal("27:04:29");
        expectedAlt = indiSexagesimalFormatter.parseSexagesimal("22:29:57");
        expectedAz = indiSexagesimalFormatter.parseSexagesimal("74:04:36");
        date.minutes = 12;
        jd = JulianDay.ln_get_julian_day(date);
        jdNow = JulianDay.ln_get_julian_from_sys();
        julianOffset = jd - jdNow;

        printSimpleAltAzValues(position, rightAscension, declination, julianOffset, expectedAlt, expectedAz);

        apparentTelescopeDirectionVector = new TelescopeDirectionVector();
        plugin.transformCelestialToTelescope(rightAscension, declination, julianOffset, apparentTelescopeDirectionVector);

        actualAltAz = new LnHrzPosn();
        apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);
        printAltAz(actualAltAz, expectedAlt, expectedAz);

        vector = new TelescopeDirectionVector();
        vector.copyFrom(apparentTelescopeDirectionVector);
        plugin.transformTelescopeToCelestial(vector, julianOffset, rightAscensionRef, declinationRef);
        Assert.assertEquals(rightAscensionRef.getValue(), rightAscension, 0.01d);
        Assert.assertEquals(declinationRef.getValue(), declination, 0.01d);

        // -----------------------------------------------------------
        e = new AlignmentDatabaseEntry();
        e.declination = declination;
        e.rightAscension = rightAscension;
        e.telescopeDirection = apparentTelescopeDirectionVector;
        e.observationJulianDate = jd;
        inMemoryDatabase.getAlignmentDatabase().add(e);
        plugin.initialise(inMemoryDatabase);
        // -----------------------------------------------------------

        rightAscension = indiSexagesimalFormatter.parseSexagesimal("15:34:41");
        declination = indiSexagesimalFormatter.parseSexagesimal("26:42:53");
        expectedAlt = indiSexagesimalFormatter.parseSexagesimal("14:38:02");
        expectedAz = indiSexagesimalFormatter.parseSexagesimal("66:10:19");
        date.minutes = 13;
        jd = JulianDay.ln_get_julian_day(date);
        jdNow = JulianDay.ln_get_julian_from_sys();
        julianOffset = jd - jdNow;

        printSimpleAltAzValues(position, rightAscension, declination, julianOffset, expectedAlt, expectedAz);

        apparentTelescopeDirectionVector = new TelescopeDirectionVector();
        plugin.transformCelestialToTelescope(rightAscension, declination, julianOffset, apparentTelescopeDirectionVector);

        actualAltAz = new LnHrzPosn();
        apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);
        printAltAz(actualAltAz, expectedAlt, expectedAz);

        vector = new TelescopeDirectionVector();
        vector.copyFrom(apparentTelescopeDirectionVector);
        plugin.transformTelescopeToCelestial(vector, julianOffset, rightAscensionRef, declinationRef);
        Assert.assertEquals(rightAscensionRef.getValue(), rightAscension, 0.01d);
        Assert.assertEquals(declinationRef.getValue(), declination, 0.01d);

        // -----------------------------------------------------------
        e = new AlignmentDatabaseEntry();
        e.declination = declination;
        e.rightAscension = rightAscension;
        e.telescopeDirection = apparentTelescopeDirectionVector;
        e.observationJulianDate = jd;
        inMemoryDatabase.getAlignmentDatabase().add(e);
        plugin.initialise(inMemoryDatabase);
        // -----------------------------------------------------------

        rightAscension = indiSexagesimalFormatter.parseSexagesimal("18:36:56");
        declination = indiSexagesimalFormatter.parseSexagesimal("38:46:58");
        expectedAlt = indiSexagesimalFormatter.parseSexagesimal("12:07:42");
        expectedAz = indiSexagesimalFormatter.parseSexagesimal("43:49:29");
        date.hours = 6;
        date.minutes = 47;
        jd = JulianDay.ln_get_julian_day(date);
        jdNow = JulianDay.ln_get_julian_from_sys();
        julianOffset = jd - jdNow;

        printSimpleAltAzValues(position, rightAscension, declination, julianOffset, expectedAlt, expectedAz);

        apparentTelescopeDirectionVector = new TelescopeDirectionVector();
        plugin.transformCelestialToTelescope(rightAscension, declination, julianOffset, apparentTelescopeDirectionVector);

        actualAltAz = new LnHrzPosn();
        apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);
        printAltAz(actualAltAz, expectedAlt, expectedAz);

        vector = new TelescopeDirectionVector();
        vector.copyFrom(apparentTelescopeDirectionVector);
        plugin.transformTelescopeToCelestial(vector, julianOffset, rightAscensionRef, declinationRef);
        Assert.assertEquals(rightAscensionRef.getValue(), rightAscension, 0.01d);
        Assert.assertEquals(declinationRef.getValue(), declination, 0.01d);
    }

    protected void printSimpleAltAzValues(LnLnlatPosn position, double rightAscension, double declination, double julianOffset, double expectedAlt, double expectedAz) {
        LnHrzPosn actualAltAz = new LnHrzPosn();
        LnEquPosn actualRaDec = new LnEquPosn();
        actualRaDec.ra = rightAscension * 15d;// hour to Degree
        actualRaDec.dec = declination;
        Transform.ln_get_hrz_from_equ(actualRaDec, position, JulianDay.ln_get_julian_from_sys() + julianOffset, actualAltAz);
        // 180 because nova does it the other way around
        printAltAz(actualAltAz, expectedAlt, expectedAz);
    }

    protected void printAltAz(LnHrzPosn actualAltAz, double expectedAlt, double expectedAz) {
        double az = actualAltAz.az - 180d;
        while (az < -180d) {
            az += 360d;
        }
        while (az > 180d) {
            az -= 360d;
        }
        // take a very big inaccuracy, because if something in the calculations
        // is wrong we will see a big difference somewhere. and small
        // differences are expected becaue that is the reason there is the math
        // plugin.
        Assert.assertEquals(expectedAz, az, 0.5d);
        LOG.info("Az = " + indiSexagesimalFormatter.format(az) + " Alt = " + indiSexagesimalFormatter.format(actualAltAz.alt));
    }
}
