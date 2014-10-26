package org.indilib.i4j.driver.telescope.alignment;

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
import org.indilib.i4j.driver.telescope.alignment.ConvexHull.Face;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default math plugin for the sync calculation.
 * 
 * @author Richard van Nieuwenhoven
 */
public class BuiltInMathPlugin implements IMathPlugin {

    /**
     * It is the difference between the next representable number after 1 and 1.
     */
    public static final double DBL_EPSILON = 2.220446049250313E-16d;

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
            distance = distance;
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
    private ConvexHull actualConvexHull;

    /**
     * Apperent Convex hull for 4+ sync points case.
     */
    private ConvexHull apparentConvexHull;

    /**
     * Actual direction cosines for the 4+ case.
     */
    private List<TelescopeDirectionVector> actualDirectionCosines;

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

    /**
     * Calculate tranformation matrices from the supplied vectors
     * 
     * @param alpha1
     *            Alpha1 Pointer to the first coordinate in the alpha reference
     *            frame
     * @param alpha2
     *            Alpha2 Pointer to the second coordinate in the alpha reference
     *            frame
     * @param alpha3
     *            Alpha3 Pointer to the third coordinate in the alpha reference
     *            frame
     * @param beta1
     *            Beta1 Pointer to the first coordinate in the beta reference
     *            frame
     * @param beta2
     *            Beta2 Pointer to the second coordinate in the beta reference
     *            frame
     * @param beta3
     *            Beta3 Pointer to the third coordinate in the beta reference
     *            frame
     * @param alphaToBeta
     *            pAlphaToBeta Pointer to a matrix to receive the Alpha to Beta
     *            transformation matrix
     * @param betaToAlpha
     *            pBetaToAlpha Pointer to a matrix to receive the Beta to Alpha
     *            transformation matrix
     */
    private void calculateTransformMatrices(TelescopeDirectionVector alpha1, TelescopeDirectionVector alpha2, TelescopeDirectionVector alpha3, TelescopeDirectionVector beta1,
            TelescopeDirectionVector beta2, TelescopeDirectionVector beta3, GslMatrix alphaToBeta, GslMatrix betaToAlpha) {
        // Derive the Actual to Apparent transformation matrix
        GslMatrix alphaMatrix = new GslMatrix(3, 3);
        alphaMatrix.set(0, 0, alpha1.x);
        alphaMatrix.set(1, 0, alpha1.y);
        alphaMatrix.set(2, 0, alpha1.z);
        alphaMatrix.set(0, 1, alpha2.x);
        alphaMatrix.set(1, 1, alpha2.y);
        alphaMatrix.set(2, 1, alpha2.z);
        alphaMatrix.set(0, 2, alpha3.x);
        alphaMatrix.set(1, 2, alpha3.y);
        alphaMatrix.set(2, 2, alpha3.z);

        GslMatrix betaMatrix = new GslMatrix(3, 3);
        betaMatrix.set(0, 0, beta1.x);
        betaMatrix.set(1, 0, beta1.y);
        betaMatrix.set(2, 0, beta1.z);
        betaMatrix.set(0, 1, beta2.x);
        betaMatrix.set(1, 1, beta2.y);
        betaMatrix.set(2, 1, beta2.z);
        betaMatrix.set(0, 2, beta3.x);
        betaMatrix.set(1, 2, beta3.y);
        betaMatrix.set(2, 2, beta3.z);

        // Use the quick and dirty method
        // This can result in matrices which are not true transforms
        GslMatrix invertedAlphaMatrix = new GslMatrix(3, 3);

        if (!matrixInvert3x3(alphaMatrix, invertedAlphaMatrix)) {
            // pAlphaMatrix is singular and therefore is not a true transform
            // and cannot be inverted. This probably means it contains at least
            // one row or column that contains only zeroes
            invertedAlphaMatrix.setIdentity();
            LOG.error("CalculateTransformMatrices - Alpha matrix is singular! Alpha matrix is singular and cannot be inverted.");
        } else {
            MatrixMatrixMultiply(betaMatrix, invertedAlphaMatrix, alphaToBeta);

            if (!betaToAlpha.isNull()) {
                // Invert the matrix to get the Apparent to Actual transform
                if (!matrixInvert3x3(alphaToBeta, betaToAlpha)) {
                    // pAlphaToBeta is singular and therefore is not a true
                    // transform
                    // and cannot be inverted. This probably means it contains
                    // at least
                    // one row or column that contains only zeroes
                    betaToAlpha.setIdentity();
                    LOG.error("CalculateTransformMatrices - AlphaToBeta matrix is singular! Calculated Celestial to Telescope transformation matrix is singular (not a true transform).");
                }

            }
        }

    }

    /**
     * Use gsl to compute the inverse of a 3x3 matrix
     * 
     * @param input
     *            matrix to use
     * @param inversion
     *            result matrix
     * @return true if successful.
     */
    boolean matrixInvert3x3(GslMatrix input, GslMatrix inversion) {
        boolean retcode = true;
        GslPermutation permutation = new GslPermutation(3);
        GslMatrix decomp = new GslMatrix(3, 3);
        IntegerRef signum = new IntegerRef();

        decomp.copy(input);

        Gsl.gsl_linalg_LU_decomp(decomp, permutation, signum);

        // Test for singularity
        if (0 == Gsl.gsl_linalg_LU_det(decomp, signum.value)) {
            retcode = false;
        } else
            Gsl.gsl_linalg_LU_invert(decomp, permutation, inversion);

        return retcode;
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
    void MatrixMatrixMultiply(GslMatrix matrixA, GslMatrix matrixB, GslMatrix matrixC) {
        // Zeroise the output matrix
        matrixC.setZero();

        Gsl.gsl_blas_dgemm(CBLAS_TRANSPOSE.CblasNoTrans, CBLAS_TRANSPOSE.CblasNoTrans, 1.0, matrixA, matrixB, 0.0, matrixC);
    }

    @Override
    public void create() {
        actualToApparentTransform = new GslMatrix(3, 3);
        apparentToActualTransform = new GslMatrix(3, 3);

    }

    @Override
    public void destroy() {
        actualToApparentTransform = null;
        apparentToActualTransform = null;
        inMemoryDatabase = null;
    }

    @Override
    public MountAlignment getApproximateMountAlignment() {
        return approximateAlignment;
    }

    @Override
    public String id() {
        return INBUILT_MATH_PLUGIN_NAME;
    }

    @Override
    public boolean initialise(InMemoryDatabase inMemoryDatabase) {
        this.inMemoryDatabase = inMemoryDatabase;
        List<AlignmentDatabaseEntry> syncPoints = inMemoryDatabase.getAlignmentDatabase();

        // / See how many entries there are in the in memory database.
        // / - If just one use a hint to mounts approximate alignment, this can
        // either be ZENITH,
        // / NORTH_CELESTIAL_POLE or SOUTH_CELESTIAL_POLE. The hint is used to
        // make a dummy second
        // / entry. A dummy third entry is computed from the cross product of
        // the first two. A transform
        // / matrix is then computed.
        // / - If two make the dummy third entry and compute a transform matrix.
        // / - If three compute a transform matrix.
        // / - If four or more compute a convex hull, then matrices for each
        // / triangular facet of the hull.
        switch (syncPoints.size()) {
            case 0:
                // Not sure whether to return false or true here
                return true;

            case 1: {
                AlignmentDatabaseEntry entry1 = syncPoints.get(0);
                LnEquPosn raDec = new LnEquPosn();
                LnHrzPosn actualSyncPoint1 = new LnHrzPosn();
                LnLnlatPosn position = new LnLnlatPosn();
                if (inMemoryDatabase.getDatabaseReferencePosition(position))
                    return false;
                raDec.dec = entry1.declination;
                // libnova works in decimal degrees so conversion is needed here
                raDec.ra = entry1.rightAscension * 360.0 / 24.0;
                Transform.ln_get_hrz_from_equ(raDec, position, entry1.observationJulianDate, actualSyncPoint1);
                // Now express this coordinate as a normalised direction vector
                // (a.k.a direction cosines)
                TelescopeDirectionVector actualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint1);
                TelescopeDirectionVector dummyActualDirectionCosine2 = new TelescopeDirectionVector();
                TelescopeDirectionVector dummyApparentDirectionCosine2 = new TelescopeDirectionVector();
                TelescopeDirectionVector dummyActualDirectionCosine3 = new TelescopeDirectionVector();
                TelescopeDirectionVector dummyApparentDirectionCosine3 = new TelescopeDirectionVector();

                switch (getApproximateMountAlignment()) {
                    case ZENITH:
                        dummyActualDirectionCosine2.x = 0.0;
                        dummyActualDirectionCosine2.y = 0.0;
                        dummyActualDirectionCosine2.z = 1.0;
                        dummyApparentDirectionCosine2 = dummyActualDirectionCosine2;
                        break;

                    case NORTH_CELESTIAL_POLE: {
                        LnEquPosn DummyRaDec = new LnEquPosn();
                        LnHrzPosn DummyAltAz = new LnHrzPosn();
                        DummyRaDec.ra = 0.0;
                        DummyRaDec.dec = 90.0;

                        Transform.ln_get_hrz_from_equ(DummyRaDec, position, JulianDay.ln_get_julian_from_sys(), DummyAltAz);

                        dummyActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint1);
                        dummyApparentDirectionCosine2.x = 0;
                        dummyApparentDirectionCosine2.y = 0;
                        dummyApparentDirectionCosine2.z = 1;
                        break;
                    }
                    case SOUTH_CELESTIAL_POLE: {
                        LnEquPosn DummyRaDec = new LnEquPosn();
                        LnHrzPosn DummyAltAz = new LnHrzPosn();
                        DummyRaDec.ra = 0.0;
                        DummyRaDec.dec = -90.0;

                        Transform.ln_get_hrz_from_equ(DummyRaDec, position, JulianDay.ln_get_julian_from_sys(), DummyAltAz);
                        dummyActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint1);
                        dummyApparentDirectionCosine2.x = 0;
                        dummyApparentDirectionCosine2.y = 0;
                        dummyApparentDirectionCosine2.z = 1;
                        break;
                    }
                }

                dummyActualDirectionCosine3 = actualDirectionCosine1.multiply(dummyActualDirectionCosine2);
                dummyActualDirectionCosine3.normalise();
                dummyApparentDirectionCosine3 = entry1.telescopeDirection.multiply(dummyApparentDirectionCosine2);
                dummyApparentDirectionCosine3.normalise();
                calculateTransformMatrices(actualDirectionCosine1, dummyActualDirectionCosine2, dummyActualDirectionCosine3, entry1.telescopeDirection,
                        dummyApparentDirectionCosine2, dummyApparentDirectionCosine3, actualToApparentTransform, apparentToActualTransform);
                return true;
            }
            case 2: {
                // First compute local horizontal coordinates for the two sync
                // points
                AlignmentDatabaseEntry entry1 = syncPoints.get(0);
                AlignmentDatabaseEntry entry2 = syncPoints.get(1);
                LnHrzPosn actualSyncPoint1 = new LnHrzPosn();
                LnHrzPosn actualSyncPoint2 = new LnHrzPosn();
                LnEquPosn raDec1 = new LnEquPosn();
                LnEquPosn raDec2 = new LnEquPosn();
                raDec1.dec = entry1.declination;
                // libnova works in decimal degrees so conversion is needed here
                raDec1.ra = entry1.rightAscension * 360.0 / 24.0;
                raDec2.dec = entry2.declination;
                // libnova works in decimal degrees so conversion is needed here
                raDec2.ra = entry2.rightAscension * 360.0 / 24.0;
                LnLnlatPosn position = new LnLnlatPosn();
                if (!inMemoryDatabase.getDatabaseReferencePosition(position))
                    return false;
                Transform.ln_get_hrz_from_equ(raDec1, position, entry1.observationJulianDate, actualSyncPoint1);
                Transform.ln_get_hrz_from_equ(raDec2, position, entry2.observationJulianDate, actualSyncPoint2);

                // Now express these coordinates as normalised direction vectors
                // (a.k.a direction cosines)
                TelescopeDirectionVector actualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint1);
                TelescopeDirectionVector actualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint2);
                TelescopeDirectionVector dummyActualDirectionCosine3;
                TelescopeDirectionVector dummyApparentDirectionCosine3;
                dummyActualDirectionCosine3 = actualDirectionCosine1.multiply(actualDirectionCosine2);
                dummyActualDirectionCosine3.normalise();
                dummyApparentDirectionCosine3 = entry1.telescopeDirection.multiply(entry2.telescopeDirection);
                dummyApparentDirectionCosine3.normalise();

                // The third direction vectors is generated by taking the cross
                // product of the first two
                calculateTransformMatrices(actualDirectionCosine1, actualDirectionCosine2, dummyActualDirectionCosine3, entry1.telescopeDirection, entry2.telescopeDirection,
                        dummyApparentDirectionCosine3, actualToApparentTransform, apparentToActualTransform);
                return true;
            }

            case 3: {
                // First compute local horizontal coordinates for the three sync
                // points
                AlignmentDatabaseEntry entry1 = syncPoints.get(0);
                AlignmentDatabaseEntry entry2 = syncPoints.get(1);
                AlignmentDatabaseEntry entry3 = syncPoints.get(2);
                LnHrzPosn actualSyncPoint1 = new LnHrzPosn();
                LnHrzPosn actualSyncPoint2 = new LnHrzPosn();
                LnHrzPosn actualSyncPoint3 = new LnHrzPosn();
                LnEquPosn raDec1 = new LnEquPosn();
                LnEquPosn raDec2 = new LnEquPosn();
                LnEquPosn raDec3 = new LnEquPosn();
                raDec1.dec = entry1.declination;
                // libnova works in decimal degrees so conversion is needed here
                raDec1.ra = entry1.rightAscension * 360.0 / 24.0;
                raDec2.dec = entry2.declination;
                // libnova works in decimal degrees so conversion is needed here
                raDec2.ra = entry2.rightAscension * 360.0 / 24.0;
                raDec3.dec = entry3.declination;
                // libnova works in decimal degrees so conversion is needed here
                raDec3.ra = entry3.rightAscension * 360.0 / 24.0;
                LnLnlatPosn position = new LnLnlatPosn();
                if (!inMemoryDatabase.getDatabaseReferencePosition(position))
                    return false;
                Transform.ln_get_hrz_from_equ(raDec1, position, entry1.observationJulianDate, actualSyncPoint1);
                Transform.ln_get_hrz_from_equ(raDec2, position, entry2.observationJulianDate, actualSyncPoint2);
                Transform.ln_get_hrz_from_equ(raDec3, position, entry3.observationJulianDate, actualSyncPoint3);

                // Now express these coordinates as normalised direction vectors
                // (a.k.a direction cosines)
                TelescopeDirectionVector actualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint1);
                TelescopeDirectionVector actualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint2);
                TelescopeDirectionVector actualDirectionCosine3 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint3);

                calculateTransformMatrices(actualDirectionCosine1, actualDirectionCosine2, actualDirectionCosine3, entry1.telescopeDirection, entry2.telescopeDirection,
                        entry3.telescopeDirection, actualToApparentTransform, apparentToActualTransform);
                return true;
            }

            default: {
                LnLnlatPosn position = new LnLnlatPosn();
                if (!inMemoryDatabase.getDatabaseReferencePosition(position))
                    return false;

                // Compute Hulls etc.
                actualConvexHull.reset();
                apparentConvexHull.reset();
                actualDirectionCosines.clear();

                // Add a dummy point at the nadir
                actualConvexHull.makeNewVertex(0.0, 0.0, -1.0, 0);
                apparentConvexHull.makeNewVertex(0.0, 0.0, -1.0, 0);

                int vertexNumber = 1;
                // Add the rest of the vertices
                for (AlignmentDatabaseEntry alignmentDatabaseEntry : syncPoints) {
                    LnEquPosn raDec = new LnEquPosn();
                    LnHrzPosn actualSyncPoint = new LnHrzPosn();
                    raDec.dec = alignmentDatabaseEntry.declination;
                    // libnova works in decimal degrees so conversion is needed
                    // here
                    raDec.ra = alignmentDatabaseEntry.rightAscension * 360.0 / 24.0;
                    Transform.ln_get_hrz_from_equ(raDec, position, alignmentDatabaseEntry.observationJulianDate, actualSyncPoint);
                    // Now express this coordinate as normalised direction
                    // vectors (a.k.a direction cosines)
                    TelescopeDirectionVector actualDirectionCosine = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint);
                    actualDirectionCosines.add(actualDirectionCosine);
                    actualConvexHull.makeNewVertex(actualDirectionCosine.x, actualDirectionCosine.y, actualDirectionCosine.z, vertexNumber);
                    apparentConvexHull.makeNewVertex(alignmentDatabaseEntry.telescopeDirection.x, alignmentDatabaseEntry.telescopeDirection.y,
                            alignmentDatabaseEntry.telescopeDirection.z, vertexNumber);
                    vertexNumber++;
                }
                // I should only need to do this once but it is easier to do it
                // twice
                actualConvexHull.doubleTriangle();
                actualConvexHull.constructHull();
                actualConvexHull.edgeOrderOnFaces();
                apparentConvexHull.doubleTriangle();
                apparentConvexHull.constructHull();
                apparentConvexHull.edgeOrderOnFaces();

                // Make the matrices
                for (Face currentFace : actualConvexHull.faces) {
                    if ((0 == currentFace.vertex[0].vnum) || (0 == currentFace.vertex[1].vnum) || (0 == currentFace.vertex[2].vnum)) {
                    } else {
                        calculateTransformMatrices(actualDirectionCosines.get(currentFace.vertex[0].vnum - 1), actualDirectionCosines.get(currentFace.vertex[1].vnum - 1),
                                actualDirectionCosines.get(currentFace.vertex[2].vnum - 1), syncPoints.get(currentFace.vertex[0].vnum - 1).telescopeDirection,
                                syncPoints.get(currentFace.vertex[1].vnum - 1).telescopeDirection, syncPoints.get(currentFace.vertex[2].vnum - 1).telescopeDirection,
                                currentFace.pMatrix, null);
                    }
                }
                // One of these days I will optimise this
                for (Face currentFace : actualConvexHull.faces) {
                    if ((0 == currentFace.vertex[0].vnum) || (0 == currentFace.vertex[1].vnum) || (0 == currentFace.vertex[2].vnum)) {
                    } else {
                        calculateTransformMatrices(syncPoints.get(currentFace.vertex[0].vnum - 1).telescopeDirection,
                                syncPoints.get(currentFace.vertex[1].vnum - 1).telescopeDirection, syncPoints.get(currentFace.vertex[2].vnum - 1).telescopeDirection,
                                actualDirectionCosines.get(currentFace.vertex[0].vnum - 1), actualDirectionCosines.get(currentFace.vertex[1].vnum - 1),
                                actualDirectionCosines.get(currentFace.vertex[2].vnum - 1), currentFace.pMatrix, null);
                    }
                }
                return true;
            }
        }
    }

    @Override
    public String name() {
        return INBUILT_MATH_PLUGIN_LABEL;
    }

    @Override
    public void setApproximateMountAlignment(MountAlignment approximateAlignment) {
        this.approximateAlignment = approximateAlignment;
    }

    @Override
    public boolean transformCelestialToTelescope(final double rightAscension, final double declination, final double julianOffset,
            TelescopeDirectionVector apparentTelescopeDirectionVector) {

        LnEquPosn actualRaDec = new LnEquPosn();
        LnHrzPosn actualAltAz = new LnHrzPosn();
        // libnova works in decimal degrees so conversion is needed here
        actualRaDec.ra = rightAscension * 360.0 / 24.0;
        actualRaDec.dec = declination;
        LnLnlatPosn position = new LnLnlatPosn();

        // Should check that this the same as the current observing position
        if ((null == inMemoryDatabase) || !inMemoryDatabase.getDatabaseReferencePosition(position))
            return false;

        Transform.ln_get_hrz_from_equ(actualRaDec, position, JulianDay.ln_get_julian_from_sys() + julianOffset, actualAltAz);

        LOG.debug(String.format("Celestial to telescope - Actual Alt %lf Az %lf", actualAltAz.alt, actualAltAz.az));

        TelescopeDirectionVector actualVector = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualAltAz);

        List<AlignmentDatabaseEntry> syncPoints = inMemoryDatabase.getAlignmentDatabase();
        GslVector gslActualVector;
        GslVector gslApparentVector;
        switch (syncPoints.size()) {
            case 0: {
                // 0 sync points
                apparentTelescopeDirectionVector = actualVector;

                switch (approximateAlignment) {
                    case ZENITH:
                        break;

                    case NORTH_CELESTIAL_POLE:
                        // Rotate the TDV coordinate system clockwise (negative)
                        // around the y axis by 90 minus
                        // the (positive)observatory latitude. The vector itself
                        // is rotated anticlockwise
                        apparentTelescopeDirectionVector.RotateAroundY(position.lat - 90.0);
                        break;

                    case SOUTH_CELESTIAL_POLE:
                        // Rotate the TDV coordinate system anticlockwise
                        // (positive) around the y axis by 90 plus
                        // the (negative)observatory latitude. The vector itself
                        // is rotated clockwise
                        apparentTelescopeDirectionVector.RotateAroundY(position.lat + 90.0);
                        break;
                }
                break;
            }
            case 1:
            case 2:
            case 3:
                gslActualVector = new GslVector(3);
                gslActualVector.set(0, actualVector.x);
                gslActualVector.set(1, actualVector.y);
                gslActualVector.set(2, actualVector.z);
                gslApparentVector = new GslVector(3);
                MatrixVectorMultiply(actualToApparentTransform, gslActualVector, gslApparentVector);
                apparentTelescopeDirectionVector.x = gslApparentVector.get(0);
                apparentTelescopeDirectionVector.y = gslApparentVector.get(1);
                apparentTelescopeDirectionVector.z = gslApparentVector.get(2);
                apparentTelescopeDirectionVector.normalise();
                break;

            default:
                GslMatrix transform;
                GslMatrix computedTransform = null;
                // Scale the actual telescope direction vector to make sure it
                // traverses the unit sphere.
                TelescopeDirectionVector scaledActualVector = actualVector.multiply(2.0);
                // Shoot the scaled vector in the into the list of actual facets
                // and use the conversuion matrix from the one it intersects
                int actualFaces = 0;
                Face currentFace = null;
                for (Face currentFaceLoop : actualConvexHull.faces) {
                    currentFace = currentFaceLoop;
                    if (LOG.isDebugEnabled()) {
                        actualFaces++;
                    }
                    // Ignore faces containg vertex 0 (nadir).
                    if ((0 == currentFace.vertex[0].vnum) || (0 == currentFace.vertex[1].vnum) || (0 == currentFace.vertex[2].vnum)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Celestial to telescope - Ignoring actual face %d", actualFaces));
                        }
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Celestial to telescope - Processing actual face %d v1 %d v2 %d v3 %d", actualFaces, currentFace.vertex[0].vnum,
                                    currentFace.vertex[1].vnum, currentFace.vertex[2].vnum));
                        }
                        if (RayTriangleIntersection(scaledActualVector, actualDirectionCosines.get(currentFace.vertex[0].vnum - 1),
                                actualDirectionCosines.get(currentFace.vertex[1].vnum - 1), actualDirectionCosines.get(currentFace.vertex[2].vnum - 1))) {
                            break;
                        }
                    }
                    currentFace = null;
                }
                if (currentFace == null) {
                    List<AlignmentDatabaseEntryDistance> nearestMap = new ArrayList<AlignmentDatabaseEntryDistance>();
                    for (AlignmentDatabaseEntry entry : syncPoints) {
                        LnEquPosn raDec = new LnEquPosn();
                        LnHrzPosn ActualPoint = new LnHrzPosn();
                        raDec.ra = entry.rightAscension * 360.0 / 24.0;
                        raDec.dec = entry.declination;
                        Transform.ln_get_hrz_from_equ(raDec, position, entry.observationJulianDate, ActualPoint);
                        TelescopeDirectionVector ActualDirectionCosine = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualPoint);

                        nearestMap.add(new AlignmentDatabaseEntryDistance(ActualDirectionCosine.minus(actualVector).length(), entry));
                    }
                    Collections.sort(nearestMap);
                    // First compute local horizontal coordinates for the three
                    // sync points
                    AlignmentDatabaseEntry entry1 = nearestMap.get(0).entry;
                    AlignmentDatabaseEntry entry2 = nearestMap.get(1).entry;
                    AlignmentDatabaseEntry entry3 = nearestMap.get(2).entry;
                    LnHrzPosn actualSyncPoint1 = new LnHrzPosn();
                    LnHrzPosn actualSyncPoint2 = new LnHrzPosn();
                    LnHrzPosn actualSyncPoint3 = new LnHrzPosn();
                    LnEquPosn raDec1 = new LnEquPosn();
                    LnEquPosn raDec2 = new LnEquPosn();
                    LnEquPosn raDec3 = new LnEquPosn();
                    raDec1.dec = entry1.declination;
                    // libnova works in decimal degrees so conversion is needed
                    // here
                    raDec1.ra = entry1.rightAscension * 360.0 / 24.0;
                    raDec2.dec = entry2.declination;
                    // libnova works in decimal degrees so conversion is needed
                    // here
                    raDec2.ra = entry2.rightAscension * 360.0 / 24.0;
                    raDec3.dec = entry3.declination;
                    // libnova works in decimal degrees so conversion is needed
                    // here
                    raDec3.ra = entry3.rightAscension * 360.0 / 24.0;
                    Transform.ln_get_hrz_from_equ(raDec1, position, entry1.observationJulianDate, actualSyncPoint1);
                    Transform.ln_get_hrz_from_equ(raDec2, position, entry2.observationJulianDate, actualSyncPoint2);
                    Transform.ln_get_hrz_from_equ(raDec3, position, entry3.observationJulianDate, actualSyncPoint3);

                    // Now express these coordinates as normalised direction
                    // vectors (a.k.a direction cosines)
                    TelescopeDirectionVector ActualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint1);
                    TelescopeDirectionVector ActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint2);
                    TelescopeDirectionVector ActualDirectionCosine3 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(actualSyncPoint3);
                    computedTransform = new GslMatrix(3, 3);
                    calculateTransformMatrices(ActualDirectionCosine1, ActualDirectionCosine2, ActualDirectionCosine3, entry1.telescopeDirection, entry2.telescopeDirection,
                            entry3.telescopeDirection, computedTransform, null);
                    transform = computedTransform;
                } else
                    transform = currentFace.pMatrix;

                // OK - got an intersection - CurrentFace is pointing at the
                // face
                gslActualVector = new GslVector(3);
                gslActualVector.set(0, actualVector.x);
                gslActualVector.set(1, actualVector.y);
                gslActualVector.set(2, actualVector.z);
                gslApparentVector = new GslVector(3);
                MatrixVectorMultiply(transform, gslActualVector, gslApparentVector);
                apparentTelescopeDirectionVector.x = gslApparentVector.get(0);
                apparentTelescopeDirectionVector.y = gslApparentVector.get(1);
                apparentTelescopeDirectionVector.z = gslApparentVector.get(2);
                apparentTelescopeDirectionVector.normalise();
                break;

        }

        LnHrzPosn ApparentAltAz = new LnHrzPosn();
        apparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(ApparentAltAz);
        LOG.info(String.format("Celestial to telescope - Apparent Alt %lf Az %lf", ApparentAltAz.alt, ApparentAltAz.az));
        return true;
    }

    // / \brief Multiply matrix A by vector B and put the result in vector C
    void MatrixVectorMultiply(GslMatrix pA, GslVector pB, GslVector pC) {
        // Zeroise the output vector
        pC.setZero();
        Gsl.gsl_blas_dgemv(CBLAS_TRANSPOSE.CblasNoTrans, 1.0, pA, pB, 0.0, pC);
    }

    // / \brief Test if a ray intersects a triangle in 3d space
    // / \param[in] Ray The ray vector
    // / \param[in] TriangleVertex1 The first vertex of the triangle
    // / \param[in] TriangleVertex2 The second vertex of the triangle
    // / \param[in] TriangleVertex3 The third vertex of the triangle
    // / \note The order of the vertices determine whether the triangle is
    // facing away from or towards the origin.
    // / Intersection with triangles facing the origin will be ignored.
    boolean RayTriangleIntersection(TelescopeDirectionVector Ray, TelescopeDirectionVector TriangleVertex1, TelescopeDirectionVector TriangleVertex2,
            TelescopeDirectionVector TriangleVertex3) {
        // Use Möller-Trumbore

        // Find vectors for two edges sharing V1
        TelescopeDirectionVector Edge1 = TriangleVertex2.minus(TriangleVertex1);
        TelescopeDirectionVector Edge2 = TriangleVertex3.minus(TriangleVertex1);

        TelescopeDirectionVector P = Ray.multiply(Edge2); // cross product
        double Determinant = Edge1.dotProduct(P); // dot product
        double InverseDeterminant = 1.0 / Determinant;

        // If the determinant is negative the triangle is backfacing
        // If the determinant is close to 0, the ray misses the triangle

        if ((Determinant > -DBL_EPSILON) && (Determinant < DBL_EPSILON))
            return false;

        // I use zero as ray origin so
        TelescopeDirectionVector T = new TelescopeDirectionVector(-TriangleVertex1.x, -TriangleVertex1.y, -TriangleVertex1.z);

        // Calculate the u parameter
        double u = (T.dotProduct(P)) * InverseDeterminant;

        if (u < 0.0 || u > 1.0)
            // The intersection lies outside of the triangle
            return false;

        // Prepare to test v parameter
        TelescopeDirectionVector Q = T.multiply(Edge1);

        // Calculate v parameter and test bound
        double v = (Ray.dotProduct(Q)) * InverseDeterminant;

        if (v < 0.0 || u + v > 1.0)
            // The intersection lies outside of the triangle
            return false;

        double t = (Edge2.dotProduct(Q)) * InverseDeterminant;

        if (t > DBL_EPSILON) {
            // ray intersection
            return true;
        }

        // No hit, no win
        return false;
    }

    protected void Dump3(String Label, GslVector pVector) {
        LOG.info(String.format("Vector dump - %s", Label));
        LOG.info(String.format("%lf %lf %lf", pVector.get(0), pVector.get(1), pVector.get(2)));
    }

    protected void Dump3x3(String Label, GslMatrix pMatrix) {
        LOG.info(String.format("Matrix dump - %s", Label));
        LOG.info(String.format("Row 0 %lf %lf %lf", pMatrix.get(0, 0), pMatrix.get(0, 1), pMatrix.get(0, 2)));
        LOG.info(String.format("Row 1 %lf %lf %lf", pMatrix.get(1, 0), pMatrix.get(1, 1), pMatrix.get(1, 2)));
        LOG.info(String.format("Row 2 %lf %lf %lf", pMatrix.get(2, 0), pMatrix.get(2, 1), pMatrix.get(2, 2)));
    }

    @Override
    public boolean transformTelescopeToCelestial(TelescopeDirectionVector ApparentTelescopeDirectionVector, DoubleRef rightAscension, DoubleRef declination) {

        LnLnlatPosn Position = new LnLnlatPosn();

        LnHrzPosn ApparentAltAz = new LnHrzPosn();
        LnHrzPosn ActualAltAz = new LnHrzPosn();
        LnEquPosn ActualRaDec = new LnEquPosn();

        ApparentTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(ApparentAltAz);
        LOG.info(String.format("Telescope to celestial - Apparent Alt %lf Az %lf", ApparentAltAz.alt, ApparentAltAz.az));

        // Should check that this the same as the current observing position
        if ((null == inMemoryDatabase) || !inMemoryDatabase.getDatabaseReferencePosition(Position))
            return false;

        List<AlignmentDatabaseEntry> SyncPoints = inMemoryDatabase.getAlignmentDatabase();
        GslVector pGSLApparentVector;
        GslVector pGSLActualVector;
        TelescopeDirectionVector ActualTelescopeDirectionVector;
        switch (SyncPoints.size()) {
            case 0: {
                // 0 sync points

                TelescopeDirectionVector RotatedTDV =
                        new TelescopeDirectionVector(ApparentTelescopeDirectionVector.x, ApparentTelescopeDirectionVector.y, ApparentTelescopeDirectionVector.z);
                switch (approximateAlignment) {
                    case ZENITH:
                        break;

                    case NORTH_CELESTIAL_POLE:
                        // Rotate the TDV coordinate system anticlockwise
                        // (positive) around the y axis by 90 minus
                        // the (positive)observatory latitude. The vector itself
                        // is rotated clockwise
                        RotatedTDV.RotateAroundY(90.0 - Position.lat);
                        break;

                    case SOUTH_CELESTIAL_POLE:
                        // Rotate the TDV coordinate system clockwise (negative)
                        // around the y axis by 90 plus
                        // the (negative)observatory latitude. The vector itself
                        // is rotated anticlockwise
                        RotatedTDV.RotateAroundY(-90.0 - Position.lat);
                        break;
                }
                RotatedTDV.altitudeAzimuthFromTelescopeDirectionVector(ActualAltAz);

                Transform.ln_get_equ_from_hrz(ActualAltAz, Position, JulianDay.ln_get_julian_from_sys(), ActualRaDec);

                // libnova works in decimal degrees so conversion is needed here
                rightAscension.value = ActualRaDec.ra * 24.0 / 360.0;
                declination.value = ActualRaDec.dec;
                break;
            }
            case 1:
            case 2:
            case 3:
                pGSLApparentVector = new GslVector(3);
                pGSLApparentVector.set(0, ApparentTelescopeDirectionVector.x);
                pGSLApparentVector.set(1, ApparentTelescopeDirectionVector.y);
                pGSLApparentVector.set(2, ApparentTelescopeDirectionVector.z);
                pGSLActualVector = new GslVector(3);
                MatrixVectorMultiply(apparentToActualTransform, pGSLApparentVector, pGSLActualVector);

                Dump3("ApparentVector", pGSLApparentVector);
                Dump3("ActualVector", pGSLActualVector);

                ActualTelescopeDirectionVector = new TelescopeDirectionVector();
                ActualTelescopeDirectionVector.x = pGSLActualVector.get(0);
                ActualTelescopeDirectionVector.y = pGSLActualVector.get(1);
                ActualTelescopeDirectionVector.z = pGSLActualVector.get(2);
                ActualTelescopeDirectionVector.normalise();
                ActualTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(ActualAltAz);

                Transform.ln_get_equ_from_hrz(ActualAltAz, Position, JulianDay.ln_get_julian_from_sys(), ActualRaDec);

                // libnova works in decimal degrees so conversion is needed here
                rightAscension.value = ActualRaDec.ra * 24.0 / 360.0;
                declination.value = ActualRaDec.dec;
                break;

            default:
                GslMatrix pTransform;
                GslMatrix pComputedTransform = null;
                // Scale the apparent telescope direction vector to make sure it
                // traverses the unit sphere.
                TelescopeDirectionVector ScaledApparentVector = ApparentTelescopeDirectionVector.multiply(2.0);
                // Shoot the scaled vector in the into the list of apparent
                // facets
                // and use the conversuion matrix from the one it intersects
                Face CurrentFace = null;
                for (Face CurrentFaceLoop : actualConvexHull.faces) {
                    CurrentFace = CurrentFaceLoop;

                    int ApparentFaces = 0;

                    ApparentFaces++;

                    // Ignore faces containg vertex 0 (nadir).
                    if ((0 == CurrentFace.vertex[0].vnum) || (0 == CurrentFace.vertex[1].vnum) || (0 == CurrentFace.vertex[2].vnum)) {

                        LOG.debug(String.format("Celestial to telescope - Ignoring apparent face %d", ApparentFaces));

                    } else {
                        LOG.debug(String.format("TelescopeToCelestial - Processing apparent face %d v1 %d v2 %d v3 %d", ApparentFaces, CurrentFace.vertex[0].vnum,
                                CurrentFace.vertex[1].vnum, CurrentFace.vertex[2].vnum));

                        if (RayTriangleIntersection(ScaledApparentVector, SyncPoints.get(CurrentFace.vertex[0].vnum - 1).telescopeDirection,
                                SyncPoints.get(CurrentFace.vertex[1].vnum - 1).telescopeDirection, SyncPoints.get(CurrentFace.vertex[2].vnum - 1).telescopeDirection))
                            break;
                    }

                    CurrentFace = null;
                }
                if (CurrentFace == null) {

                    // Find the three nearest points and build a transform

                    List<AlignmentDatabaseEntryDistance> NearestMap = new ArrayList<AlignmentDatabaseEntryDistance>();
                    for (AlignmentDatabaseEntry entry : SyncPoints) {

                        NearestMap.add(new AlignmentDatabaseEntryDistance(entry.telescopeDirection.minus(ApparentTelescopeDirectionVector).length(), entry));
                    }
                    Collections.sort(NearestMap);
                    // First compute local horizontal coordinates for the three
                    // sync points
                    AlignmentDatabaseEntry pEntry1 = NearestMap.get(0).entry;
                    AlignmentDatabaseEntry pEntry2 = NearestMap.get(1).entry;
                    AlignmentDatabaseEntry pEntry3 = NearestMap.get(2).entry;

                    LnHrzPosn ActualSyncPoint1 = new LnHrzPosn();
                    LnHrzPosn ActualSyncPoint2 = new LnHrzPosn();
                    LnHrzPosn ActualSyncPoint3 = new LnHrzPosn();
                    LnEquPosn RaDec1 = new LnEquPosn();
                    LnEquPosn RaDec2 = new LnEquPosn();
                    LnEquPosn RaDec3 = new LnEquPosn();
                    RaDec1.dec = pEntry1.declination;
                    // libnova works in decimal degrees so conversion is needed
                    // here
                    RaDec1.ra = pEntry1.rightAscension * 360.0 / 24.0;
                    RaDec2.dec = pEntry2.declination;
                    // libnova works in decimal degrees so conversion is needed
                    // here
                    RaDec2.ra = pEntry2.rightAscension * 360.0 / 24.0;
                    RaDec3.dec = pEntry3.declination;
                    // libnova works in decimal degrees so conversion is needed
                    // here
                    RaDec3.ra = pEntry3.rightAscension * 360.0 / 24.0;
                    Transform.ln_get_hrz_from_equ(RaDec1, Position, pEntry1.observationJulianDate, ActualSyncPoint1);
                    Transform.ln_get_hrz_from_equ(RaDec2, Position, pEntry2.observationJulianDate, ActualSyncPoint2);
                    Transform.ln_get_hrz_from_equ(RaDec3, Position, pEntry3.observationJulianDate, ActualSyncPoint3);

                    // Now express these coordinates as normalised direction
                    // vectors (a.k.a direction cosines)
                    TelescopeDirectionVector ActualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint1);
                    TelescopeDirectionVector ActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint2);
                    TelescopeDirectionVector ActualDirectionCosine3 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint3);
                    pComputedTransform = new GslMatrix(3, 3);
                    calculateTransformMatrices(pEntry1.telescopeDirection, pEntry2.telescopeDirection, pEntry3.telescopeDirection, ActualDirectionCosine1,
                            ActualDirectionCosine2, ActualDirectionCosine3, pComputedTransform, null);
                    pTransform = pComputedTransform;
                } else
                    pTransform = CurrentFace.pMatrix;

                // OK - got an intersection - CurrentFace is pointing at the
                // face
                pGSLApparentVector = new GslVector(3);
                pGSLApparentVector.set(0, ApparentTelescopeDirectionVector.x);
                pGSLApparentVector.set(1, ApparentTelescopeDirectionVector.y);
                pGSLApparentVector.set(2, ApparentTelescopeDirectionVector.z);
                pGSLActualVector = new GslVector(3);
                MatrixVectorMultiply(pTransform, pGSLApparentVector, pGSLActualVector);
                ActualTelescopeDirectionVector = new TelescopeDirectionVector();
                ActualTelescopeDirectionVector.x = pGSLActualVector.get(0);
                ActualTelescopeDirectionVector.y = pGSLActualVector.get(1);
                ActualTelescopeDirectionVector.z = pGSLActualVector.get(2);
                ActualTelescopeDirectionVector.normalise();
                ActualTelescopeDirectionVector.altitudeAzimuthFromTelescopeDirectionVector(ActualAltAz);

                Transform.ln_get_equ_from_hrz(ActualAltAz, Position, JulianDay.ln_get_julian_from_sys(), ActualRaDec);

                // libnova works in decimal degrees so conversion is needed here
                rightAscension.value = ActualRaDec.ra * 24.0 / 360.0;
                declination.value = ActualRaDec.dec;
                break;
        }
        LOG.info(String.format("Telescope to Celestial - Actual Alt %lf Az %lf", ActualAltAz.alt, ActualAltAz.az));
        return true;
    }
}
