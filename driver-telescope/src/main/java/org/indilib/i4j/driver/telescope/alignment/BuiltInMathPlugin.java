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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.novaforjava.JulianDay;
import net.sourceforge.novaforjava.Transform;
import net.sourceforge.novaforjava.api.LnEquPosn;
import net.sourceforge.novaforjava.api.LnHrzPosn;
import net.sourceforge.novaforjava.api.LnLnlatPosn;

import org.gnu.savannah.gsl.CBLAS_TRANSPOSE;
import org.gnu.savannah.gsl.Gsl;
import org.gnu.savannah.gsl.GslMatrix;
import org.gnu.savannah.gsl.GslPermutation;
import org.gnu.savannah.gsl.GslVector;
import org.gnu.savannah.gsl.util.IntegerRef;
import org.indilib.i4j.driver.telescope.alignment.WrappedQuickHull3D.Face;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default math plugin for the sync calculation.
 * 
 * @author Richard van Nieuwenhoven
 */
public class BuiltInMathPlugin implements IMathPlugin {

    /**
     * The point database has no entries at all.
     */
    private static final int POINT_DB_EMPTY = 0;

    /**
     * The point database has one single entry.
     */
    private static final int POINT_DB_ONE_ENTRY = 1;

    /**
     * The point database has two entries.
     */
    private static final int POINT_DB_TWO_ENTRIES = 2;

    /**
     * The point database has three entries.
     */
    private static final int POINT_DB_THREE_ENTRIES = 3;

    /**
     * ra coordinates of the north celestrial pole.
     */
    private static final double NORTH_CELESTIAL_POLE_RA = 0.0;

    /**
     * dec coordinates of the north celestrial pole.
     */
    private static final double NORTH_CELESTIAL_POLE_DEC = 90.0;

    /**
     * ra coordinates of the south celestrial pole.
     */
    private static final double SOUTH_CELESTIAL_POLE_RA = 0.0;

    /**
     * dec coordinates of the south celestrial pole.
     */
    private static final double SOUTH_CELESTIAL_POLE_DEC = -90.0;

    /**
     * 3 dimentions.
     */
    private static final int DIM_3D = 3;

    /**
     * number of hours per day.
     */
    private static final double HOURS_PER_DAY = 24d;

    /**
     * number of degrees ind circle.
     */
    private static final double DEGREES_IN_CIRCLE = 360d;

    /**
     * It is the difference between the next representable number after 1 and 1.
     */
    public static final double DBL_EPSILON = 2.220446049250313E-16d;

    /**
     * multiplier to make degrees from hours.
     */
    private static final double DEGREES_TO_HOUR = DEGREES_IN_CIRCLE / HOURS_PER_DAY;

    /**
     * multiplier to make hours from degrees .
     */
    private static final double HOUR_TO_DEGREES = HOURS_PER_DAY / DEGREES_IN_CIRCLE;

    /**
     * Helper class to sort the database entries.
     */
    static class AlignmentDatabaseEntryDistance implements Comparable<AlignmentDatabaseEntryDistance> {

        /**
         * The datebase entry.
         */
        private AlignmentDatabaseEntry entry;

        /**
         * the distance to the current point.
         */
        private double distance;

        /**
         * Constructor for the entry distance.
         * 
         * @param distance
         *            the distance.
         * @param entry
         *            the alignment entry
         */
        public AlignmentDatabaseEntryDistance(double distance, AlignmentDatabaseEntry entry) {
            this.distance = distance;
            this.entry = entry;
        }

        @Override
        public int compareTo(AlignmentDatabaseEntryDistance o) {
            double d = distance - o.distance;
            if (d > 0) {
                return 1;
            } else if (d < 0) {
                return -1;
            }
            return 0;
        }

        @Override
        public int hashCode() {
            return Double.valueOf(distance).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AlignmentDatabaseEntryDistance) {
                return compareTo((AlignmentDatabaseEntryDistance) obj) == 0;
            }
            return false;
        }

    }

    /**
     * The logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BuiltInMathPlugin.class);

    /**
     * The label of the Default math plugin.
     */
    public static final String INBUILT_MATH_PLUGIN_LABEL = "Inbuilt Math Plugin";

    /**
     * the internal name of the default math plugin.
     */
    public static final String INBUILT_MATH_PLUGIN_NAME = "INBUILT_MATH_PLUGIN";

    /**
     * actual to appenent transformation.
     */
    private GslMatrix actualToApparentTransform;

    /**
     * appenrent to aktual transformation.
     */
    private GslMatrix apparentToActualTransform;

    /**
     * Actual Convex hull for 4+ sync points case.
     */
    private WrappedQuickHull3D actualConvexHull = new WrappedQuickHull3D();

    /**
     * Apperent Convex hull for 4+ sync points case.
     */
    private WrappedQuickHull3D apparentConvexHull = new WrappedQuickHull3D();

    /**
     * Actual direction cosines for the 4+ case.
     */
    private List<TelescopeDirectionVector> actualDirectionCosines = new ArrayList<TelescopeDirectionVector>();

    /**
     * the in memory alignment database.
     */
    private InMemoryDatabase inMemoryDatabase;

    /**
     * Describe the alignment of a telescope axis. This is normally used to
     * differentiate between equatorial mounts in differnet hemispheres and
     * altaz or dobsonian mounts.
     */
    private MountAlignment approximateAlignment;

    @Override
    public void create() {
        actualToApparentTransform = new GslMatrix(DIM_3D, DIM_3D);
        apparentToActualTransform = new GslMatrix(DIM_3D, DIM_3D);

    }

    @Override
    public void destroy() {
        actualToApparentTransform = null;
        apparentToActualTransform = null;
        inMemoryDatabase = null;
    }

    @Override
    public MountAlignment getApproximateAlignment() {
        return approximateAlignment;
    }

    @Override
    public String id() {
        return INBUILT_MATH_PLUGIN_NAME;
    }

    /**
     * See how many entries there are in the in memory database. - If just one
     * use a hint to mounts approximate alignment, this can either be ZENITH,
     * NORTH_CELESTIAL_POLE or SOUTH_CELESTIAL_POLE. The hint is used to make a
     * dummy second entry. A dummy third entry is computed from the cross
     * product of the first two. A transform matrix is then computed. - If two
     * make the dummy third entry and compute a transform matrix. - If three
     * compute a transform matrix. - If four or more compute a convex hull, then
     * matrices for each triangular facet of the hull.
     * 
     * @param anInMemoryDatabase
     *            the database to use
     * @return true if successful
     */
    @Override
    public boolean initialise(InMemoryDatabase anInMemoryDatabase) {
        inMemoryDatabase = anInMemoryDatabase;

        switch (inMemoryDatabase.getAlignmentDatabase().size()) {
            case POINT_DB_EMPTY:
                // Not sure whether to return false or true here
                return true;

            case POINT_DB_ONE_ENTRY:
                return initialiseOnePoint();

            case POINT_DB_TWO_ENTRIES:
                return initialiseTwoPoints();

            case POINT_DB_THREE_ENTRIES:
                return initialiseThreePoints();

            default:
                return initialiseMoreThanTreePoints();

        }
    }

    /**
     * Initialize the plugin with more than three points in the database .
     * 
     * @return true is successfull.
     */
    private boolean initialiseMoreThanTreePoints() {
        List<AlignmentDatabaseEntry> syncPoints = inMemoryDatabase.getAlignmentDatabase();
        LnLnlatPosn position = new LnLnlatPosn();
        if (!inMemoryDatabase.getDatabaseReferencePosition(position)) {
            return false;
        }
        // Compute Hulls etc.
        actualConvexHull.clear();
        apparentConvexHull.clear();
        actualDirectionCosines.clear();

        // Add a dummy point at the nadir
        actualConvexHull.add(0.0, 0.0, -1.0);
        apparentConvexHull.add(0.0, 0.0, -1.0);

        // Add the rest of the vertices
        for (AlignmentDatabaseEntry alignmentDatabaseEntry : syncPoints) {
            LnEquPosn raDec = new LnEquPosn();
            LnHrzPosn actualSyncPoint = new LnHrzPosn();
            raDec.dec = alignmentDatabaseEntry.declination;
            // libnova works in decimal degrees so conversion is needed
            // here
            raDec.ra = alignmentDatabaseEntry.rightAscension * DEGREES_TO_HOUR;
            Transform.ln_get_hrz_from_equ(raDec, position, alignmentDatabaseEntry.observationJulianDate, actualSyncPoint);
            // Now express this coordinate as normalised direction
            // vectors (a.k.a direction cosines)
            TelescopeDirectionVector actualDirectionCosine = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint);
            actualDirectionCosines.add(actualDirectionCosine);
            actualConvexHull.add(actualDirectionCosine.x, actualDirectionCosine.y, actualDirectionCosine.z);
            apparentConvexHull.add(alignmentDatabaseEntry.telescopeDirection.x, alignmentDatabaseEntry.telescopeDirection.y, alignmentDatabaseEntry.telescopeDirection.z);
        }
        // I should only need to do this once but it is easier to do it
        // twice
        actualConvexHull.build();
        apparentConvexHull.build();

        // Make the matrices
        for (Face currentFace : actualConvexHull.getFaces()) {
            int vnum0 = currentFace.vnum(0);
            int vnum1 = currentFace.vnum(1);
            int vnum2 = currentFace.vnum(2);

            if (vnum0 != 0 && vnum1 != 0 && vnum2 != 0) {
                calculateTransformMatrices(//
                        createActualToApparentMatrix(//
                                actualDirectionCosines.get(vnum0 - 1), //
                                actualDirectionCosines.get(vnum1 - 1), //
                                actualDirectionCosines.get(vnum2 - 1)), //
                        createActualToApparentMatrix(//
                                syncPoints.get(vnum0 - 1).telescopeDirection, //
                                syncPoints.get(vnum1 - 1).telescopeDirection, //
                                syncPoints.get(vnum2 - 1).telescopeDirection), //
                        currentFace.matrix, null);
            }
        }
        // One of these days I will optimise this
        for (Face currentFace : apparentConvexHull.getFaces()) {
            int vnum0 = currentFace.vnum(0);
            int vnum1 = currentFace.vnum(1);
            int vnum2 = currentFace.vnum(2);
            if (vnum0 != 0 && vnum1 != 0 && vnum2 != 0) {
                calculateTransformMatrices(//
                        createActualToApparentMatrix(//
                                syncPoints.get(vnum0 - 1).telescopeDirection, //
                                syncPoints.get(vnum1 - 1).telescopeDirection, //
                                syncPoints.get(vnum2 - 1).telescopeDirection), //
                        createActualToApparentMatrix(//
                                actualDirectionCosines.get(vnum0 - 1), //
                                actualDirectionCosines.get(vnum1 - 1), //
                                actualDirectionCosines.get(vnum2 - 1)), //
                        currentFace.matrix, null);
            }
        }
        return true;
    }

    /**
     * Initialize the plugin with three points in the database .
     * 
     * @return true is successfull.
     */
    protected boolean initialiseThreePoints() {
        List<AlignmentDatabaseEntry> syncPoints = inMemoryDatabase.getAlignmentDatabase();
        LnLnlatPosn position = new LnLnlatPosn();
        if (!inMemoryDatabase.getDatabaseReferencePosition(position)) {
            return false;
        }
        // First compute local horizontal coordinates for the three sync
        // points
        AlignmentDatabaseEntry entry1 = syncPoints.get(0);
        AlignmentDatabaseEntry entry2 = syncPoints.get(1);
        AlignmentDatabaseEntry entry3 = syncPoints.get(2);
        calculateTransformMatrices(//
                createActualToApparentMatrix(//
                        calculateHorizontalCoordsNormalisedDirectionVector(position, entry1), //
                        calculateHorizontalCoordsNormalisedDirectionVector(position, entry2), //
                        calculateHorizontalCoordsNormalisedDirectionVector(position, entry3)), //
                createActualToApparentMatrix(//
                        entry1.telescopeDirection, //
                        entry2.telescopeDirection, //
                        entry3.telescopeDirection), //
                actualToApparentTransform, apparentToActualTransform);
        return true;
    }

    /**
     * Initialize the plugin with two points in the database .
     * 
     * @return true is successfull.
     */
    protected boolean initialiseTwoPoints() {
        List<AlignmentDatabaseEntry> syncPoints = inMemoryDatabase.getAlignmentDatabase();
        LnLnlatPosn position = new LnLnlatPosn();
        if (!inMemoryDatabase.getDatabaseReferencePosition(position)) {
            return false;
        }

        // First compute local horizontal coordinates for the two sync
        // points
        AlignmentDatabaseEntry entry1 = syncPoints.get(0);
        TelescopeDirectionVector actualDirectionCosine1 = calculateHorizontalCoordsNormalisedDirectionVector(position, entry1);

        AlignmentDatabaseEntry entry2 = syncPoints.get(1);
        TelescopeDirectionVector actualDirectionCosine2 = calculateHorizontalCoordsNormalisedDirectionVector(position, entry2);

        TelescopeDirectionVector dummyActualDirectionCosine3 = actualDirectionCosine1.multiply(actualDirectionCosine2);
        dummyActualDirectionCosine3.normalise();
        TelescopeDirectionVector dummyApparentDirectionCosine3 = entry1.telescopeDirection.multiply(entry2.telescopeDirection);
        dummyApparentDirectionCosine3.normalise();

        // The third direction vectors is generated by taking the cross
        // product of the first two
        calculateTransformMatrices(//
                createActualToApparentMatrix(//
                        actualDirectionCosine1, //
                        actualDirectionCosine2, //
                        dummyActualDirectionCosine3), //
                createActualToApparentMatrix(//
                        entry1.telescopeDirection, //
                        entry2.telescopeDirection, //
                        dummyApparentDirectionCosine3), //
                actualToApparentTransform, apparentToActualTransform);
        return true;
    }

    /**
     * Initialize the plugin with one point in the database .
     * 
     * @return true is successfull.
     */
    protected boolean initialiseOnePoint() {
        List<AlignmentDatabaseEntry> syncPoints = inMemoryDatabase.getAlignmentDatabase();
        AlignmentDatabaseEntry entry1 = syncPoints.get(0);
        LnEquPosn raDec = new LnEquPosn();
        LnHrzPosn actualSyncPoint1 = new LnHrzPosn();
        LnLnlatPosn position = new LnLnlatPosn();
        if (!inMemoryDatabase.getDatabaseReferencePosition(position)) {
            return false;
        }
        raDec.dec = entry1.declination;
        // libnova works in decimal degrees so conversion is needed here
        raDec.ra = entry1.rightAscension * DEGREES_TO_HOUR;
        Transform.ln_get_hrz_from_equ(raDec, position, entry1.observationJulianDate, actualSyncPoint1);
        // Now express this coordinate as a normalised direction vector
        // (a.k.a direction cosines)
        TelescopeDirectionVector actualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint1);
        TelescopeDirectionVector dummyActualDirectionCosine2 = new TelescopeDirectionVector();
        TelescopeDirectionVector dummyApparentDirectionCosine2 = new TelescopeDirectionVector();

        LnEquPosn dummyRaDec = new LnEquPosn();
        LnHrzPosn dummyAltAz = new LnHrzPosn();
        switch (getApproximateAlignment()) {
            case ZENITH:
                dummyActualDirectionCosine2.x = 0.0;
                dummyActualDirectionCosine2.y = 0.0;
                dummyActualDirectionCosine2.z = 1.0;
                dummyApparentDirectionCosine2 = dummyActualDirectionCosine2;
                break;

            case NORTH_CELESTIAL_POLE:
                dummyRaDec.ra = NORTH_CELESTIAL_POLE_RA;
                dummyRaDec.dec = NORTH_CELESTIAL_POLE_DEC;

                Transform.ln_get_hrz_from_equ(dummyRaDec, position, JulianDay.ln_get_julian_from_sys(), dummyAltAz);

                dummyActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint1);
                dummyApparentDirectionCosine2.x = 0;
                dummyApparentDirectionCosine2.y = 0;
                dummyApparentDirectionCosine2.z = 1;
                break;

            case SOUTH_CELESTIAL_POLE:
                dummyRaDec.ra = SOUTH_CELESTIAL_POLE_RA;
                dummyRaDec.dec = SOUTH_CELESTIAL_POLE_DEC;

                Transform.ln_get_hrz_from_equ(dummyRaDec, position, JulianDay.ln_get_julian_from_sys(), dummyAltAz);
                dummyActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint1);
                dummyApparentDirectionCosine2.x = 0;
                dummyApparentDirectionCosine2.y = 0;
                dummyApparentDirectionCosine2.z = 1;
                break;

            default:
                throw new IllegalArgumentException("illegal mount allignment");
        }

        TelescopeDirectionVector dummyActualDirectionCosine3 = actualDirectionCosine1.multiply(dummyActualDirectionCosine2);
        dummyActualDirectionCosine3.normalise();
        TelescopeDirectionVector dummyApparentDirectionCosine3 = entry1.telescopeDirection.multiply(dummyApparentDirectionCosine2);
        dummyApparentDirectionCosine3.normalise();
        calculateTransformMatrices(//
                createActualToApparentMatrix(//
                        actualDirectionCosine1, //
                        dummyActualDirectionCosine2, //
                        dummyActualDirectionCosine3), //
                createActualToApparentMatrix(//
                        entry1.telescopeDirection, //
                        dummyApparentDirectionCosine2, //
                        dummyApparentDirectionCosine3), //
                actualToApparentTransform, //
                apparentToActualTransform);
        return true;
    }

    @Override
    public String name() {
        return INBUILT_MATH_PLUGIN_LABEL;
    }

    @Override
    public void setApproximateAlignment(MountAlignment approximateAlignment) {
        this.approximateAlignment = approximateAlignment;
    }

    @Override
    public boolean transformCelestialToTelescope(final double rightAscension, final double declination, final double julianOffset,
            TelescopeDirectionVector apparentTelescopeDirectionVector) {
        LnLnlatPosn position = new LnLnlatPosn();

        // Should check that this the same as the current observing position
        if (!inMemoryDatabase.getDatabaseReferencePosition(position)) {
            return false;
        }

        LnEquPosn actualRaDec = new LnEquPosn();
        // libnova works in decimal degrees so conversion is needed here
        actualRaDec.ra = rightAscension * DEGREES_TO_HOUR;
        actualRaDec.dec = declination;
        LnHrzPosn actualAltAz = new LnHrzPosn();
        Transform.ln_get_hrz_from_equ(actualRaDec, position, JulianDay.ln_get_julian_from_sys() + julianOffset, actualAltAz);

        LOG.debug(String.format("Celestial to telescope - Actual Alt %f Az %f", actualAltAz.alt, actualAltAz.az));

        TelescopeDirectionVector actualVector = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualAltAz);
        switch (inMemoryDatabase.getAlignmentDatabase().size()) {
            case POINT_DB_EMPTY:
                transformCelestialToTelescopeWithEmptyDb(apparentTelescopeDirectionVector, position, actualVector);
                break;

            case POINT_DB_ONE_ENTRY:
            case POINT_DB_TWO_ENTRIES:
            case POINT_DB_THREE_ENTRIES:
                transformCelestialToTelescopeWithSmallDb(apparentTelescopeDirectionVector, actualVector);
                break;

            default:
                transformCelestialToTelescopeWithBigDb(apparentTelescopeDirectionVector, position, actualVector);
                break;

        }

        LnHrzPosn apparentAltAz = new LnHrzPosn();
        apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(apparentAltAz);
        LOG.info(String.format("Celestial to telescope - Apparent Alt %f Az %f", apparentAltAz.alt, apparentAltAz.az));
        return true;
    }

    /**
     * transform the Celestial coordinates to telescope coordinates with more
     * than 3 points in the database.
     * 
     * @param apparentTelescopeDirectionVector
     *            corrected telescope direction vector
     * @param position
     *            the current position
     * @param actualVector
     *            telescope direction vector
     */
    protected void transformCelestialToTelescopeWithBigDb(TelescopeDirectionVector apparentTelescopeDirectionVector, LnLnlatPosn position,
            TelescopeDirectionVector actualVector) {

        List<AlignmentDatabaseEntry> syncPoints = inMemoryDatabase.getAlignmentDatabase();
        // Scale the actual telescope direction vector to make sure it
        // traverses the unit sphere.
        TelescopeDirectionVector scaledActualVector = actualVector.multiply(2.0);
        // Shoot the scaled vector in the into the list of actual facets
        // and use the conversuion matrix from the one it intersects
        int actualFaces = 0;
        Face currentFace = null;
        for (Face currentFaceLoop : actualConvexHull.getFaces()) {
            currentFace = currentFaceLoop;

            actualFaces++;

            // Ignore faces containg vertex 0 (nadir).
            int vnum0 = currentFace.vnum(0);
            int vnum1 = currentFace.vnum(1);
            int vnum2 = currentFace.vnum(2);
            if (0 == vnum0 || 0 == vnum1 || 0 == vnum2) {
                LOG.debug("Celestial to telescope - Ignoring actual face %d", actualFaces);
            } else {
                LOG.debug("Celestial to telescope - Processing actual face %d v1 %d v2 %d v3 %d", //
                        actualFaces, //
                        vnum0, //
                        vnum1, //
                        vnum2);
                if (rayTriangleIntersection(scaledActualVector, //
                        actualDirectionCosines.get(vnum0 - 1), //
                        actualDirectionCosines.get(vnum1 - 1), //
                        actualDirectionCosines.get(vnum2 - 1))) {
                    break;
                }
            }
            currentFace = null;
        }
        GslMatrix transform;
        if (currentFace == null) {
            List<AlignmentDatabaseEntryDistance> nearestMap = new ArrayList<AlignmentDatabaseEntryDistance>();
            for (AlignmentDatabaseEntry entry : syncPoints) {
                LnEquPosn raDec = new LnEquPosn();
                LnHrzPosn actualPoint = new LnHrzPosn();
                raDec.ra = entry.rightAscension * DEGREES_TO_HOUR;
                raDec.dec = entry.declination;
                Transform.ln_get_hrz_from_equ(raDec, position, entry.observationJulianDate, actualPoint);
                TelescopeDirectionVector actualDirectionCosine = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualPoint);

                nearestMap.add(new AlignmentDatabaseEntryDistance(actualDirectionCosine.minus(actualVector).length(), entry));
            }
            Collections.sort(nearestMap);
            // First compute local horizontal coordinates for the three
            // sync points
            AlignmentDatabaseEntry entry1 = nearestMap.get(0).entry;
            AlignmentDatabaseEntry entry2 = nearestMap.get(1).entry;
            AlignmentDatabaseEntry entry3 = nearestMap.get(2).entry;

            GslMatrix computedTransform = new GslMatrix(DIM_3D, DIM_3D);
            calculateTransformMatrices(//
                    createActualToApparentMatrix(//
                            calculateHorizontalCoordsNormalisedDirectionVector(position, entry1), //
                            calculateHorizontalCoordsNormalisedDirectionVector(position, entry2), //
                            calculateHorizontalCoordsNormalisedDirectionVector(position, entry3)), //
                    createActualToApparentMatrix(//
                            entry1.telescopeDirection, //
                            entry2.telescopeDirection, //
                            entry3.telescopeDirection), //
                    computedTransform, null);
            transform = computedTransform;
        } else {
            transform = currentFace.matrix;
        }
        multiplyVectorWithMatrix(apparentTelescopeDirectionVector, actualVector, transform);
    }

    /**
     * transform the Celestial coordinates to telescope coordinates with 1 to 3
     * points in the database.
     * 
     * @param apparentTelescopeDirectionVector
     *            corrected telescope direction vector
     * @param actualVector
     *            telescope direction vector
     */
    protected void transformCelestialToTelescopeWithSmallDb(TelescopeDirectionVector apparentTelescopeDirectionVector, TelescopeDirectionVector actualVector) {
        GslMatrix multiplyMatrix = actualToApparentTransform;
        multiplyVectorWithMatrix(apparentTelescopeDirectionVector, actualVector, multiplyMatrix);
    }

    /**
     * Multyply the vector with the matrix and set the normalized result in the
     * resulting vector.
     * 
     * @param resultingVector
     *            the resulting vector
     * @param vector
     *            the vector to multiply
     * @param multiplyMatrix
     *            the multiplication matrix
     */
    protected void multiplyVectorWithMatrix(TelescopeDirectionVector resultingVector, TelescopeDirectionVector vector, GslMatrix multiplyMatrix) {
        GslVector gslVector = new GslVector(DIM_3D);
        gslVector.set(0, vector.x);
        gslVector.set(1, vector.y);
        gslVector.set(2, vector.z);
        GslVector gslResult = new GslVector(DIM_3D);
        matrixVectorMultiply(multiplyMatrix, gslVector, gslResult);
        resultingVector.x = gslResult.get(0);
        resultingVector.y = gslResult.get(1);
        resultingVector.z = gslResult.get(2);
        resultingVector.normalise();
        dump3("ApparentVector", gslResult);
        dump3("ActualVector", gslVector);
    }

    /**
     * calculate the horizontal coordinates and convert it to a direction
     * vector.
     * 
     * @param position
     *            the current position of the scope.
     * @param entry
     *            the syncpoint
     * @return the telescope direction vector.
     */
    protected TelescopeDirectionVector calculateHorizontalCoordsNormalisedDirectionVector(LnLnlatPosn position, AlignmentDatabaseEntry entry) {
        LnHrzPosn actualSyncPoint = new LnHrzPosn();
        LnEquPosn raDec = new LnEquPosn();
        raDec.dec = entry.declination;
        // libnova works in decimal degrees so conversion is needed
        // here
        raDec.ra = entry.rightAscension * DEGREES_TO_HOUR;
        Transform.ln_get_hrz_from_equ(raDec, position, entry.observationJulianDate, actualSyncPoint);
        // Now express these coordinates as normalised direction
        // vectors (a.k.a direction cosines)
        TelescopeDirectionVector actualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint);
        return actualDirectionCosine1;
    }

    /**
     * transform the Celestial coordinates to telescope coordinates with no
     * entries in the database.
     * 
     * @param apparentTelescopeDirectionVector
     *            corrected telescope direction vector
     * @param position
     *            the current position
     * @param actualVector
     *            telescope direction vector
     */
    protected void transformCelestialToTelescopeWithEmptyDb(TelescopeDirectionVector apparentTelescopeDirectionVector, LnLnlatPosn position,
            TelescopeDirectionVector actualVector) {
        // 0 sync points
        apparentTelescopeDirectionVector.copyFrom(actualVector);

        switch (approximateAlignment) {
            case ZENITH:
                break;

            case NORTH_CELESTIAL_POLE:
                // Rotate the TDV coordinate system clockwise (negative)
                // around the y axis by 90 minus
                // the (positive)observatory latitude. The vector itself
                // is rotated anticlockwise
                apparentTelescopeDirectionVector.rotateAroundY(position.lat + SOUTH_CELESTIAL_POLE_DEC);
                break;

            case SOUTH_CELESTIAL_POLE:
                // Rotate the TDV coordinate system anticlockwise
                // (positive) around the y axis by 90 plus
                // the (negative)observatory latitude. The vector itself
                // is rotated clockwise
                apparentTelescopeDirectionVector.rotateAroundY(position.lat + NORTH_CELESTIAL_POLE_DEC);
                break;

            default:
                throw new IllegalArgumentException("illegal mount allignment");
        }
    }

    @Override
    public boolean transformTelescopeToCelestial(TelescopeDirectionVector apparentTelescopeDirectionVector, double julianOffset, DoubleRef rightAscension,
            DoubleRef declination) {

        LnLnlatPosn position = new LnLnlatPosn();

        LnHrzPosn apparentAltAz = new LnHrzPosn();

        apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(apparentAltAz);
        LOG.info(String.format("Telescope to celestial - Apparent Alt %f Az %f", apparentAltAz.alt, apparentAltAz.az));

        // Should check that this the same as the current observing position
        if (!inMemoryDatabase.getDatabaseReferencePosition(position)) {
            return false;
        }

        switch (inMemoryDatabase.getAlignmentDatabase().size()) {
            case POINT_DB_EMPTY:
                transformTelescopeToCelestialWithEmptyDb(apparentTelescopeDirectionVector, julianOffset, rightAscension, declination, position);
                break;
            case POINT_DB_ONE_ENTRY:
            case POINT_DB_TWO_ENTRIES:
            case POINT_DB_THREE_ENTRIES:
                transformTelescopeToCelestialWithSmallDb(apparentTelescopeDirectionVector, julianOffset, rightAscension, declination, position);
                break;
            default:
                transformTelescopeToCelestialWithBigDb(apparentTelescopeDirectionVector, julianOffset, rightAscension, declination, position);
                break;
        }
        return true;
    }

    /**
     * Transform the Telescope coordinates to celestrial coordinates using a
     * sync point db with more that 3 points.
     * 
     * @param apparentTelescopeDirectionVector
     *            the vector the scope is pointing.
     * @param julianOffset
     *            to be applied to the current julian date.
     * @param rightAscension
     *            the resulting right ascension
     * @param declination
     *            the resulting declination
     * @param position
     *            the current scope position.
     */
    protected void transformTelescopeToCelestialWithBigDb(TelescopeDirectionVector apparentTelescopeDirectionVector, double julianOffset, DoubleRef rightAscension,
            DoubleRef declination, LnLnlatPosn position) {

        List<AlignmentDatabaseEntry> syncPoints = inMemoryDatabase.getAlignmentDatabase();

        // Scale the apparent telescope direction vector to make sure it
        // traverses the unit sphere.
        TelescopeDirectionVector scaledApparentVector = apparentTelescopeDirectionVector.multiply(2.0);
        // Shoot the scaled vector in the into the list of apparent
        // facets
        // and use the conversuion matrix from the one it intersects
        int apparentFaces = 0;
        Face currentFace = null;
        for (Face currentFaceLoop : actualConvexHull.getFaces()) {
            currentFace = currentFaceLoop;

            apparentFaces++;

            // Ignore faces containg vertex 0 (nadir).
            int vnum0 = currentFace.vnum(0);
            int vnum1 = currentFace.vnum(1);
            int vnum2 = currentFace.vnum(2);
            if (0 == vnum0 || 0 == vnum1 || 0 == vnum2) {
                LOG.debug("Telescope to celestial - Ignoring apparent face %d", apparentFaces);
            } else {
                LOG.debug("Telescope to celestial - Processing apparent face %d v1 %d v2 %d v3 %d", //
                        apparentFaces, //
                        vnum0, //
                        vnum1, //
                        vnum2);
                if (rayTriangleIntersection(scaledApparentVector, //
                        syncPoints.get(vnum0 - 1).telescopeDirection, //
                        syncPoints.get(vnum1 - 1).telescopeDirection, //
                        syncPoints.get(vnum2 - 1).telescopeDirection)) {
                    break;
                }
            }

            currentFace = null;
        }
        GslMatrix transform;
        if (currentFace == null) {

            // Find the three nearest points and build a transform

            List<AlignmentDatabaseEntryDistance> nearestMap = new ArrayList<AlignmentDatabaseEntryDistance>();
            for (AlignmentDatabaseEntry entry : syncPoints) {

                nearestMap.add(new AlignmentDatabaseEntryDistance(entry.telescopeDirection.minus(apparentTelescopeDirectionVector).length(), entry));
            }
            Collections.sort(nearestMap);
            // First compute local horizontal coordinates for the three
            // sync points
            AlignmentDatabaseEntry entry1 = nearestMap.get(0).entry;
            AlignmentDatabaseEntry entry2 = nearestMap.get(1).entry;
            AlignmentDatabaseEntry entry3 = nearestMap.get(2).entry;

            transform = new GslMatrix(DIM_3D, DIM_3D);
            calculateTransformMatrices(//
                    createActualToApparentMatrix(//
                            entry1.telescopeDirection, //
                            entry2.telescopeDirection, //
                            entry3.telescopeDirection), //
                    createActualToApparentMatrix(//
                            calculateHorizontalCoordsNormalisedDirectionVector(position, entry1), //
                            calculateHorizontalCoordsNormalisedDirectionVector(position, entry2), //
                            calculateHorizontalCoordsNormalisedDirectionVector(position, entry3)), //
                    transform, null);
        } else {
            transform = currentFace.matrix;
        }
        // OK - got an intersection - CurrentFace is pointing at the
        // face
        TelescopeDirectionVector actualTelescopeDirectionVector = new TelescopeDirectionVector();

        multiplyVectorWithMatrix(actualTelescopeDirectionVector, apparentTelescopeDirectionVector, transform);

        LnHrzPosn actualAltAz = new LnHrzPosn();
        actualTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);

        LnEquPosn actualRaDec = new LnEquPosn();
        Transform.ln_get_equ_from_hrz(actualAltAz, position, JulianDay.ln_get_julian_from_sys() + julianOffset, actualRaDec);

        // libnova works in decimal degrees so conversion is needed here
        rightAscension.setValue(actualRaDec.ra * HOUR_TO_DEGREES);
        declination.setValue(actualRaDec.dec);

        LOG.info(String.format("Telescope to Celestial - Actual Alt %f Az %f", actualAltAz.alt, actualAltAz.az));
    }

    /**
     * Transform the Telescope coordinates to celestrial coordinates using a
     * sync point db with 1 to 3 points.
     * 
     * @param apparentTelescopeDirectionVector
     *            the vector the scope is pointing.
     * @param julianOffset
     *            to be applied to the current julian date.
     * @param rightAscension
     *            the resulting right ascension
     * @param declination
     *            the resulting declination
     * @param position
     *            the current scope position.
     */
    protected void transformTelescopeToCelestialWithSmallDb(TelescopeDirectionVector apparentTelescopeDirectionVector, double julianOffset, DoubleRef rightAscension,
            DoubleRef declination, LnLnlatPosn position) {

        TelescopeDirectionVector actualTelescopeDirectionVector = new TelescopeDirectionVector();

        multiplyVectorWithMatrix(actualTelescopeDirectionVector, apparentTelescopeDirectionVector, apparentToActualTransform);

        LnHrzPosn actualAltAz = new LnHrzPosn();
        actualTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);

        LnEquPosn actualRaDec = new LnEquPosn();
        Transform.ln_get_equ_from_hrz(actualAltAz, position, JulianDay.ln_get_julian_from_sys() + julianOffset, actualRaDec);

        // libnova works in decimal degrees so conversion is needed here
        rightAscension.setValue(actualRaDec.ra * HOUR_TO_DEGREES);
        declination.setValue(actualRaDec.dec);

        LOG.info(String.format("Telescope to Celestial - Actual Alt %f Az %f", actualAltAz.alt, actualAltAz.az));
    }

    /**
     * Transform the Telescope coordinates to celestrial coordinates using a
     * empty sync point db.
     * 
     * @param apparentTelescopeDirectionVector
     *            the vector the scope is pointing.
     * @param julianOffset
     *            to be applied to the current julian date.
     * @param rightAscension
     *            the resulting right ascension
     * @param declination
     *            the resulting declination
     * @param position
     *            the current scope position.
     */
    protected void transformTelescopeToCelestialWithEmptyDb(TelescopeDirectionVector apparentTelescopeDirectionVector, double julianOffset, DoubleRef rightAscension,
            DoubleRef declination, LnLnlatPosn position) {

        LnHrzPosn actualAltAz = new LnHrzPosn();
        LnEquPosn actualRaDec = new LnEquPosn();
        // 0 sync points

        TelescopeDirectionVector rotatedTDV =
                new TelescopeDirectionVector(apparentTelescopeDirectionVector.x, apparentTelescopeDirectionVector.y, apparentTelescopeDirectionVector.z);
        switch (approximateAlignment) {
            case ZENITH:
                break;

            case NORTH_CELESTIAL_POLE:
                // Rotate the TDV coordinate system anticlockwise
                // (positive) around the y axis by 90 minus
                // the (positive)observatory latitude. The vector itself
                // is rotated clockwise
                rotatedTDV.rotateAroundY(NORTH_CELESTIAL_POLE_DEC - position.lat);
                break;

            case SOUTH_CELESTIAL_POLE:
                // Rotate the TDV coordinate system clockwise (negative)
                // around the y axis by 90 plus
                // the (negative)observatory latitude. The vector itself
                // is rotated anticlockwise
                rotatedTDV.rotateAroundY(SOUTH_CELESTIAL_POLE_DEC - position.lat);
                break;
            default:
                throw new IllegalArgumentException("illegal mount allignment");
        }
        rotatedTDV.altitudeAzimuthFromTelescopeDirectionVector(actualAltAz);

        Transform.ln_get_equ_from_hrz(actualAltAz, position, JulianDay.ln_get_julian_from_sys() + julianOffset, actualRaDec);

        // libnova works in decimal degrees so conversion is needed here
        rightAscension.setValue(actualRaDec.ra * HOUR_TO_DEGREES);
        declination.setValue(actualRaDec.dec);

        LOG.info(String.format("Telescope to Celestial - Actual Alt %f Az %f", actualAltAz.alt, actualAltAz.az));
    }

    /**
     * Log the vector.
     * 
     * @param label
     *            the label to use.
     * @param vector
     *            the vector to print
     */
    protected void dump3(String label, GslVector vector) {
        LOG.info(String.format("Vector dump - %s", label));
        LOG.info(String.format("%f %f %f", vector.get(0), vector.get(1), vector.get(2)));
    }

    /**
     * Log the matrix.
     * 
     * @param label
     *            the label to use.
     * @param matrix
     *            the matrix to print
     */
    protected void dump3x3(String label, GslMatrix matrix) {
        LOG.info(String.format("Matrix dump - %s", label));
        LOG.info(String.format("Row 0 %f %f %f", matrix.get(0, 0), matrix.get(0, 1), matrix.get(0, 2)));
        LOG.info(String.format("Row 1 %f %f %f", matrix.get(1, 0), matrix.get(1, 1), matrix.get(1, 2)));
        LOG.info(String.format("Row 2 %f %f %f", matrix.get(2, 0), matrix.get(2, 1), matrix.get(2, 2)));
    }

    /**
     * Calculate tranformation matrices from the supplied vectors.
     * 
     * @param alphaMatrix
     *            Alpha matrix to the first coordinate in the alpha reference
     *            frame
     * @param betaMatrix
     *            Beta matrix Pointer to the first coordinate in the beta
     *            reference frame
     * @param alphaToBeta
     *            pAlphaToBeta Pointer to a matrix to receive the Alpha to Beta
     *            transformation matrix
     * @param betaToAlpha
     *            pBetaToAlpha Pointer to a matrix to receive the Beta to Alpha
     *            transformation matrix
     */
    private void calculateTransformMatrices(GslMatrix alphaMatrix, GslMatrix betaMatrix, GslMatrix alphaToBeta, GslMatrix betaToAlpha) {

        // Use the quick and dirty method
        // This can result in matrices which are not true transforms
        GslMatrix invertedAlphaMatrix = new GslMatrix(DIM_3D, DIM_3D);

        if (!matrixInvert3x3(alphaMatrix, invertedAlphaMatrix)) {
            // pAlphaMatrix is singular and therefore is not a true transform
            // and cannot be inverted. This probably means it contains at least
            // one row or column that contains only zeroes
            invertedAlphaMatrix.setIdentity();
            LOG.error("CalculateTransformMatrices - Alpha matrix is singular! Alpha matrix is singular and cannot be inverted.");
        } else {
            matrixMatrixMultiply(betaMatrix, invertedAlphaMatrix, alphaToBeta);

            if (betaToAlpha != null) {
                // Invert the matrix to get the Apparent to Actual transform
                if (!matrixInvert3x3(alphaToBeta, betaToAlpha)) {
                    // pAlphaToBeta is singular and therefore is not a true
                    // transform
                    // and cannot be inverted. This probably means it contains
                    // at least
                    // one row or column that contains only zeroes
                    betaToAlpha.setIdentity();
                    LOG.error("CalculateTransformMatrices - AlphaToBeta matrix is singular! " //
                            + "Calculated Celestial to Telescope transformation matrix is singular (not a true transform).");
                }

            }
        }

    }

    /**
     * Derive the Actual to Apparent transformation matrix.
     * 
     * @param alpha1
     *            first point of the matrix
     * @param alpha2
     *            second point of the matrix
     * @param alpha3
     *            thrird point of the matrix
     * @return the created matrix.
     */
    protected GslMatrix createActualToApparentMatrix(TelescopeDirectionVector alpha1, TelescopeDirectionVector alpha2, TelescopeDirectionVector alpha3) {
        GslMatrix alphaMatrix = new GslMatrix(DIM_3D, DIM_3D);
        alphaMatrix.set(0, 0, alpha1.x);
        alphaMatrix.set(1, 0, alpha1.y);
        alphaMatrix.set(2, 0, alpha1.z);
        alphaMatrix.set(0, 1, alpha2.x);
        alphaMatrix.set(1, 1, alpha2.y);
        alphaMatrix.set(2, 1, alpha2.z);
        alphaMatrix.set(0, 2, alpha3.x);
        alphaMatrix.set(1, 2, alpha3.y);
        alphaMatrix.set(2, 2, alpha3.z);
        return alphaMatrix;
    }

    /**
     * Test if a ray intersects a triangle in 3d space The order of the vertices
     * determine whether the triangle is facing away from or towards the origin.
     * Intersection with triangles facing the origin will be ignored.
     * 
     * @param ray
     *            Ray The ray vector
     * @param triangleVertex1
     *            TriangleVertex1 The first vertex of the triangle
     * @param triangleVertex2
     *            TriangleVertex2 The second vertex of the triangle
     * @param triangleVertex3
     *            TriangleVertex3 The third vertex of the triangle
     * @return if the rays intersect.
     */
    private boolean rayTriangleIntersection(TelescopeDirectionVector ray, TelescopeDirectionVector triangleVertex1, TelescopeDirectionVector triangleVertex2,
            TelescopeDirectionVector triangleVertex3) {
        // Use Möller-Trumbore

        // Find vectors for two edges sharing V1
        TelescopeDirectionVector edge1 = triangleVertex2.minus(triangleVertex1);
        TelescopeDirectionVector edge2 = triangleVertex3.minus(triangleVertex1);

        TelescopeDirectionVector p = ray.multiply(edge2); // cross product
        double determinant = edge1.dotProduct(p); // dot product
        double inverseDeterminant = 1.0 / determinant;

        // If the determinant is negative the triangle is backfacing
        // If the determinant is close to 0, the ray misses the triangle

        if (determinant > -DBL_EPSILON && determinant < DBL_EPSILON) {
            return false;
        }

        // I use zero as ray origin so
        TelescopeDirectionVector tVector = new TelescopeDirectionVector(-triangleVertex1.x, -triangleVertex1.y, -triangleVertex1.z);

        // Calculate the u parameter
        double u = tVector.dotProduct(p) * inverseDeterminant;

        if (u < 0.0 || u > 1.0) {
            // The intersection lies outside of the triangle
            return false;
        }

        // Prepare to test v parameter
        TelescopeDirectionVector q = tVector.multiply(edge1);

        // Calculate v parameter and test bound
        double v = ray.dotProduct(q) * inverseDeterminant;

        if (v < 0.0 || u + v > 1.0) {
            // The intersection lies outside of the triangle
            return false;
        }
        double t = edge2.dotProduct(q) * inverseDeterminant;

        if (t > DBL_EPSILON) {
            // ray intersection
            return true;
        }

        // No hit, no win
        return false;
    }

    /**
     * Multiply matrix A by vector B and put the result in vector C.
     * 
     * @param matrixA
     *            matrix A
     * @param matrixB
     *            matrix B
     * @param vectorC
     *            the resulting vector
     */
    private void matrixVectorMultiply(GslMatrix matrixA, GslVector matrixB, GslVector vectorC) {
        // Zeroise the output vector
        vectorC.setZero();
        Gsl.gsl_blas_dgemv(CBLAS_TRANSPOSE.CblasNoTrans, 1.0, matrixA, matrixB, 0.0, vectorC);
    }

    /**
     * Use gsl blas support to multiply two matrices together and put the result
     * in a third. For our purposes all the matrices should be 3 by 3.
     * 
     * @param matrixA
     *            matrix A
     * @param matrixB
     *            matrix B
     * @param matrixC
     *            matrix C
     */
    private void matrixMatrixMultiply(GslMatrix matrixA, GslMatrix matrixB, GslMatrix matrixC) {
        // Zeroise the output matrix
        matrixC.setZero();

        Gsl.gsl_blas_dgemm(CBLAS_TRANSPOSE.CblasNoTrans, CBLAS_TRANSPOSE.CblasNoTrans, 1.0, matrixA, matrixB, 0.0, matrixC);
    }

    /**
     * Use gsl to compute the inverse of a 3x3 matrix.
     * 
     * @param input
     *            matrix to use
     * @param inversion
     *            result matrix
     * @return true if successful.
     */
    private boolean matrixInvert3x3(GslMatrix input, GslMatrix inversion) {
        boolean retcode = true;
        GslPermutation permutation = new GslPermutation(DIM_3D);
        GslMatrix decomp = new GslMatrix(DIM_3D, DIM_3D);
        IntegerRef signum = new IntegerRef();

        decomp.copy(input);

        Gsl.gsl_linalg_LU_decomp(decomp, permutation, signum);

        // Test for singularity
        if (0 == Gsl.gsl_linalg_LU_det(decomp, signum.value)) {
            retcode = false;
        } else {
            Gsl.gsl_linalg_LU_invert(decomp, permutation, inversion);
        }
        return retcode;
    }
}
