package org.indilib.i4j.driver.telescope.alignment;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.novaforjava.JulianDay;
import net.sourceforge.novaforjava.Transform;
import net.sourceforge.novaforjava.api.LnEquPosn;
import net.sourceforge.novaforjava.api.LnHrzPosn;
import net.sourceforge.novaforjava.api.LnLnlatPosn;

import org.gnu.savannah.gsl.CBLAS_TRANSPOSE;
import org.gnu.savannah.gsl.Gsl;
import org.gnu.savannah.gsl.GslMatrix;
import org.gnu.savannah.gsl.GslPermutation;
import org.gnu.savannah.gsl.util.IntegerRef;

public class BuiltInMathPlugin implements IMathPlugin {

    private static Logger LOG = Logger.getLogger(InMemoryDatabase.class.getName());

    public static final String INBUILT_MATH_PLUGIN_LABEL = "Inbuilt Math Plugin";

    public static final String INBUILT_MATH_PLUGIN_NAME = "INBUILT_MATH_PLUGIN";

    private GslMatrix pActualToApparentTransform;

    private GslMatrix pApparentToActualTransform;

    ConvexHull ActualConvexHull;

    ConvexHull ApparentConvexHull;

    // Actual direction cosines for the 4+ case
    List<TelescopeDirectionVector> ActualDirectionCosines;

    private InMemoryDatabase inMemoryDatabase;

    private void CalculateTransformMatrices(TelescopeDirectionVector Alpha1, TelescopeDirectionVector Alpha2, TelescopeDirectionVector Alpha3, TelescopeDirectionVector Beta1,
            TelescopeDirectionVector Beta2, TelescopeDirectionVector Beta3, GslMatrix pAlphaToBeta, GslMatrix pBetaToAlpha) {
        // Derive the Actual to Apparent transformation matrix
        GslMatrix pAlphaMatrix = new GslMatrix(3, 3);
        pAlphaMatrix.set(0, 0, Alpha1.x);
        pAlphaMatrix.set(1, 0, Alpha1.y);
        pAlphaMatrix.set(2, 0, Alpha1.z);
        pAlphaMatrix.set(0, 1, Alpha2.x);
        pAlphaMatrix.set(1, 1, Alpha2.y);
        pAlphaMatrix.set(2, 1, Alpha2.z);
        pAlphaMatrix.set(0, 2, Alpha3.x);
        pAlphaMatrix.set(1, 2, Alpha3.y);
        pAlphaMatrix.set(2, 2, Alpha3.z);

        GslMatrix pBetaMatrix = new GslMatrix(3, 3);
        pBetaMatrix.set(0, 0, Beta1.x);
        pBetaMatrix.set(1, 0, Beta1.y);
        pBetaMatrix.set(2, 0, Beta1.z);
        pBetaMatrix.set(0, 1, Beta2.x);
        pBetaMatrix.set(1, 1, Beta2.y);
        pBetaMatrix.set(2, 1, Beta2.z);
        pBetaMatrix.set(0, 2, Beta3.x);
        pBetaMatrix.set(1, 2, Beta3.y);
        pBetaMatrix.set(2, 2, Beta3.z);

        // Use the quick and dirty method
        // This can result in matrices which are not true transforms
        GslMatrix pInvertedAlphaMatrix = new GslMatrix(3, 3);

        if (!MatrixInvert3x3(pAlphaMatrix, pInvertedAlphaMatrix)) {
            // pAlphaMatrix is singular and therefore is not a true transform
            // and cannot be inverted. This probably means it contains at least
            // one row or column that contains only zeroes
            pInvertedAlphaMatrix.setIdentity();
            LOG.log(Level.SEVERE, "CalculateTransformMatrices - Alpha matrix is singular! Alpha matrix is singular and cannot be inverted.");
        } else {
            MatrixMatrixMultiply(pBetaMatrix, pInvertedAlphaMatrix, pAlphaToBeta);

            if (!pBetaToAlpha.isNull()) {
                // Invert the matrix to get the Apparent to Actual transform
                if (!MatrixInvert3x3(pAlphaToBeta, pBetaToAlpha)) {
                    // pAlphaToBeta is singular and therefore is not a true
                    // transform
                    // and cannot be inverted. This probably means it contains
                    // at least
                    // one row or column that contains only zeroes
                    pBetaToAlpha.setIdentity();
                    LOG.log(Level.SEVERE,
                            "CalculateTransformMatrices - AlphaToBeta matrix is singular! Calculated Celestial to Telescope transformation matrix is singular (not a true transform).");
                }

            }
        }

    }

    // / Use gsl to compute the inverse of a 3x3 matrix
    boolean MatrixInvert3x3(GslMatrix pInput, GslMatrix pInversion) {
        boolean Retcode = true;
        GslPermutation pPermutation = new GslPermutation(3);
        GslMatrix pDecomp = new GslMatrix(3, 3);
        IntegerRef Signum = new IntegerRef();

        pDecomp.copy(pInput);

        Gsl.gsl_linalg_LU_decomp(pDecomp, pPermutation, Signum);

        // Test for singularity
        if (0 == Gsl.gsl_linalg_LU_det(pDecomp, Signum.value)) {
            Retcode = false;
        } else
            Gsl.gsl_linalg_LU_invert(pDecomp, pPermutation, pInversion);

        return Retcode;
    }

    // / Use gsl blas support to multiply two matrices together and put the
    // result in a third.
    // / For our purposes all the matrices should be 3 by 3.
    void MatrixMatrixMultiply(GslMatrix pA, GslMatrix pB, GslMatrix pC) {
        // Zeroise the output matrix
        pC.setZero();

        Gsl.gsl_blas_dgemm(CBLAS_TRANSPOSE.CblasNoTrans, CBLAS_TRANSPOSE.CblasNoTrans, 1.0, pA, pB, 0.0, pC);
    }

    @Override
    public void create() {
        pActualToApparentTransform = new GslMatrix(3, 3);
        pApparentToActualTransform = new GslMatrix(3, 3);

    }

    @Override
    public void destroy() {
        pActualToApparentTransform = null;
        pApparentToActualTransform = null;
        inMemoryDatabase = null;
    }

    @Override
    public MountAlignment getApproximateMountAlignment() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String id() {
        return INBUILT_MATH_PLUGIN_NAME;
    }

    @Override
    public boolean initialise(InMemoryDatabase inMemoryDatabase) {
        this.inMemoryDatabase = inMemoryDatabase;
        List<AlignmentDatabaseEntry> SyncPoints = inMemoryDatabase.GetAlignmentDatabase();

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
        switch (SyncPoints.size()) {
            case 0:
                // Not sure whether to return false or true here
                return true;

            case 1: {
                AlignmentDatabaseEntry Entry1 = SyncPoints.get(0);
                LnEquPosn RaDec = new LnEquPosn();
                LnHrzPosn ActualSyncPoint1 = new LnHrzPosn();
                LnLnlatPosn Position = new LnLnlatPosn();
                if (inMemoryDatabase.GetDatabaseReferencePosition(Position))
                    return false;
                RaDec.dec = Entry1.declination;
                // libnova works in decimal degrees so conversion is needed here
                RaDec.ra = Entry1.rightAscension * 360.0 / 24.0;
                Transform.ln_get_hrz_from_equ(RaDec, Position, Entry1.observationJulianDate, ActualSyncPoint1);
                // Now express this coordinate as a normalised direction vector
                // (a.k.a direction cosines)
                TelescopeDirectionVector ActualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint1);
                TelescopeDirectionVector DummyActualDirectionCosine2 = new TelescopeDirectionVector();
                TelescopeDirectionVector DummyApparentDirectionCosine2 = new TelescopeDirectionVector();
                TelescopeDirectionVector DummyActualDirectionCosine3 = new TelescopeDirectionVector();
                TelescopeDirectionVector DummyApparentDirectionCosine3 = new TelescopeDirectionVector();

                switch (getApproximateMountAlignment()) {
                    case ZENITH:
                        DummyActualDirectionCosine2.x = 0.0;
                        DummyActualDirectionCosine2.y = 0.0;
                        DummyActualDirectionCosine2.z = 1.0;
                        DummyApparentDirectionCosine2 = DummyActualDirectionCosine2;
                        break;

                    case NORTH_CELESTIAL_POLE: {
                        LnEquPosn DummyRaDec = new LnEquPosn();
                        LnHrzPosn DummyAltAz = new LnHrzPosn();
                        DummyRaDec.ra = 0.0;
                        DummyRaDec.dec = 90.0;

                        Transform.ln_get_hrz_from_equ(DummyRaDec, Position, JulianDay.ln_get_julian_from_sys(), DummyAltAz);

                        DummyActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint1);
                        DummyApparentDirectionCosine2.x = 0;
                        DummyApparentDirectionCosine2.y = 0;
                        DummyApparentDirectionCosine2.z = 1;
                        break;
                    }
                    case SOUTH_CELESTIAL_POLE: {
                        LnEquPosn DummyRaDec = new LnEquPosn();
                        LnHrzPosn DummyAltAz = new LnHrzPosn();
                        DummyRaDec.ra = 0.0;
                        DummyRaDec.dec = -90.0;

                        Transform.ln_get_hrz_from_equ(DummyRaDec, Position, JulianDay.ln_get_julian_from_sys(), DummyAltAz);
                        DummyActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint1);
                        DummyApparentDirectionCosine2.x = 0;
                        DummyApparentDirectionCosine2.y = 0;
                        DummyApparentDirectionCosine2.z = 1;
                        break;
                    }
                }

                DummyActualDirectionCosine3 = ActualDirectionCosine1.multiply(DummyActualDirectionCosine2);
                DummyActualDirectionCosine3.normalise();
                DummyApparentDirectionCosine3 = Entry1.telescopeDirection.multiply(DummyApparentDirectionCosine2);
                DummyApparentDirectionCosine3.normalise();
                CalculateTransformMatrices(ActualDirectionCosine1, DummyActualDirectionCosine2, DummyActualDirectionCosine3, Entry1.telescopeDirection,
                        DummyApparentDirectionCosine2, DummyApparentDirectionCosine3, pActualToApparentTransform, pApparentToActualTransform);
                return true;
            }
            case 2: {
                // First compute local horizontal coordinates for the two sync
                // points
                AlignmentDatabaseEntry Entry1 = SyncPoints.get(0);
                AlignmentDatabaseEntry Entry2 = SyncPoints.get(1);
                LnHrzPosn ActualSyncPoint1 = new LnHrzPosn();
                LnHrzPosn ActualSyncPoint2 = new LnHrzPosn();
                LnEquPosn RaDec1 = new LnEquPosn();
                LnEquPosn RaDec2 = new LnEquPosn();
                RaDec1.dec = Entry1.declination;
                // libnova works in decimal degrees so conversion is needed here
                RaDec1.ra = Entry1.rightAscension * 360.0 / 24.0;
                RaDec2.dec = Entry2.declination;
                // libnova works in decimal degrees so conversion is needed here
                RaDec2.ra = Entry2.rightAscension * 360.0 / 24.0;
                LnLnlatPosn Position = new LnLnlatPosn();
                if (!inMemoryDatabase.GetDatabaseReferencePosition(Position))
                    return false;
                Transform.ln_get_hrz_from_equ(RaDec1, Position, Entry1.observationJulianDate, ActualSyncPoint1);
                Transform.ln_get_hrz_from_equ(RaDec2, Position, Entry2.observationJulianDate, ActualSyncPoint2);

                // Now express these coordinates as normalised direction vectors
                // (a.k.a direction cosines)
                TelescopeDirectionVector ActualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint1);
                TelescopeDirectionVector ActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint2);
                TelescopeDirectionVector DummyActualDirectionCosine3;
                TelescopeDirectionVector DummyApparentDirectionCosine3;
                DummyActualDirectionCosine3 = ActualDirectionCosine1.multiply(ActualDirectionCosine2);
                DummyActualDirectionCosine3.normalise();
                DummyApparentDirectionCosine3 = Entry1.telescopeDirection.multiply(Entry2.telescopeDirection);
                DummyApparentDirectionCosine3.normalise();

                // The third direction vectors is generated by taking the cross
                // product of the first two
                CalculateTransformMatrices(ActualDirectionCosine1, ActualDirectionCosine2, DummyActualDirectionCosine3, Entry1.telescopeDirection, Entry2.telescopeDirection,
                        DummyApparentDirectionCosine3, pActualToApparentTransform, pApparentToActualTransform);
                return true;
            }

            case 3: {
                // First compute local horizontal coordinates for the three sync
                // points
                AlignmentDatabaseEntry Entry1 = SyncPoints.get(0);
                AlignmentDatabaseEntry Entry2 = SyncPoints.get(1);
                AlignmentDatabaseEntry Entry3 = SyncPoints.get(2);
                LnHrzPosn ActualSyncPoint1 = new LnHrzPosn();
                LnHrzPosn ActualSyncPoint2 = new LnHrzPosn();
                LnHrzPosn ActualSyncPoint3 = new LnHrzPosn();
                LnEquPosn RaDec1 = new LnEquPosn();
                LnEquPosn RaDec2 = new LnEquPosn();
                LnEquPosn RaDec3 = new LnEquPosn();
                RaDec1.dec = Entry1.declination;
                // libnova works in decimal degrees so conversion is needed here
                RaDec1.ra = Entry1.rightAscension * 360.0 / 24.0;
                RaDec2.dec = Entry2.declination;
                // libnova works in decimal degrees so conversion is needed here
                RaDec2.ra = Entry2.rightAscension * 360.0 / 24.0;
                RaDec3.dec = Entry3.declination;
                // libnova works in decimal degrees so conversion is needed here
                RaDec3.ra = Entry3.rightAscension * 360.0 / 24.0;
                LnLnlatPosn Position = new LnLnlatPosn();
                if (!inMemoryDatabase.GetDatabaseReferencePosition(Position))
                    return false;
                Transform.ln_get_hrz_from_equ(RaDec1, Position, Entry1.observationJulianDate, ActualSyncPoint1);
                Transform.ln_get_hrz_from_equ(RaDec2, Position, Entry2.observationJulianDate, ActualSyncPoint2);
                Transform.ln_get_hrz_from_equ(RaDec3, Position, Entry3.observationJulianDate, ActualSyncPoint3);

                // Now express these coordinates as normalised direction vectors
                // (a.k.a direction cosines)
                TelescopeDirectionVector ActualDirectionCosine1 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint1);
                TelescopeDirectionVector ActualDirectionCosine2 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint2);
                TelescopeDirectionVector ActualDirectionCosine3 = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint3);

                CalculateTransformMatrices(ActualDirectionCosine1, ActualDirectionCosine2, ActualDirectionCosine3, Entry1.telescopeDirection, Entry2.telescopeDirection,
                        Entry3.telescopeDirection, pActualToApparentTransform, pApparentToActualTransform);
                return true;
            }

            default: {
                LnLnlatPosn Position = new LnLnlatPosn();
                if (!inMemoryDatabase.GetDatabaseReferencePosition(Position))
                    return false;

                // Compute Hulls etc.
                ActualConvexHull.reset();
                ApparentConvexHull.reset();
                ActualDirectionCosines.clear();

                // Add a dummy point at the nadir
                ActualConvexHull.makeNewVertex(0.0, 0.0, -1.0, 0);
                ApparentConvexHull.makeNewVertex(0.0, 0.0, -1.0, 0);

                int VertexNumber = 1;
                // Add the rest of the vertices
                for (AlignmentDatabaseEntry alignmentDatabaseEntry : SyncPoints) {
                    LnEquPosn RaDec = new LnEquPosn();
                    LnHrzPosn ActualSyncPoint = new LnHrzPosn();
                    RaDec.dec = alignmentDatabaseEntry.declination;
                    // libnova works in decimal degrees so conversion is needed
                    // here
                    RaDec.ra = alignmentDatabaseEntry.rightAscension * 360.0 / 24.0;
                    Transform.ln_get_hrz_from_equ(RaDec, Position, alignmentDatabaseEntry.observationJulianDate, ActualSyncPoint);
                    // Now express this coordinate as normalised direction
                    // vectors (a.k.a direction cosines)
                    TelescopeDirectionVector ActualDirectionCosine = TelescopeDirectionVector.telescopeDirectionVectorFromAltitudeAzimuth(ActualSyncPoint);
                    ActualDirectionCosines.add(ActualDirectionCosine);
                    ActualConvexHull.makeNewVertex(ActualDirectionCosine.x, ActualDirectionCosine.y, ActualDirectionCosine.z, VertexNumber);
                    ApparentConvexHull.makeNewVertex(alignmentDatabaseEntry.telescopeDirection.x, alignmentDatabaseEntry.telescopeDirection.y,
                            alignmentDatabaseEntry.telescopeDirection.z, VertexNumber);
                    VertexNumber++;
                }
                // I should only need to do this once but it is easier to do it
                // twice
                ActualConvexHull.doubleTriangle();
                ActualConvexHull.constructHull();
                ActualConvexHull.edgeOrderOnFaces();
                ApparentConvexHull.doubleTriangle();
                ApparentConvexHull.constructHull();
                ApparentConvexHull.edgeOrderOnFaces();

                // Make the matrices
                ConvexHull.Face CurrentFace = ActualConvexHull.faces;

                if (CurrentFace != null) {
                    do {

                        if ((0 == CurrentFace.vertex[0].vnum) || (0 == CurrentFace.vertex[1].vnum) || (0 == CurrentFace.vertex[2].vnum)) {

                        } else {

                            CalculateTransformMatrices(ActualDirectionCosines.get(CurrentFace.vertex[0].vnum - 1), ActualDirectionCosines.get(CurrentFace.vertex[1].vnum - 1),
                                    ActualDirectionCosines.get(CurrentFace.vertex[2].vnum - 1), SyncPoints.get(CurrentFace.vertex[0].vnum - 1).telescopeDirection,
                                    SyncPoints.get(CurrentFace.vertex[1].vnum - 1).telescopeDirection, SyncPoints.get(CurrentFace.vertex[2].vnum - 1).telescopeDirection,
                                    CurrentFace.pMatrix, null);
                        }
                        CurrentFace = CurrentFace.next;
                    } while (CurrentFace != ActualConvexHull.faces);
                }

                // One of these days I will optimise this
                CurrentFace = ApparentConvexHull.faces;

                if (CurrentFace != null) {
                    do {

                        if ((0 == CurrentFace.vertex[0].vnum) || (0 == CurrentFace.vertex[1].vnum) || (0 == CurrentFace.vertex[2].vnum)) {

                        } else {

                            CalculateTransformMatrices(SyncPoints.get(CurrentFace.vertex[0].vnum - 1).telescopeDirection,
                                    SyncPoints.get(CurrentFace.vertex[1].vnum - 1).telescopeDirection, SyncPoints.get(CurrentFace.vertex[2].vnum - 1).telescopeDirection,
                                    ActualDirectionCosines.get(CurrentFace.vertex[0].vnum - 1), ActualDirectionCosines.get(CurrentFace.vertex[1].vnum - 1),
                                    ActualDirectionCosines.get(CurrentFace.vertex[2].vnum - 1), CurrentFace.pMatrix, null);
                        }
                        CurrentFace = CurrentFace.next;
                    } while (CurrentFace != ApparentConvexHull.faces);
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
    public void setApproximateMountAlignment(InMemoryDatabase inMemoryDatabase) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean transformCelestialToTelescope(double d, double e, double julianOffset, TelescopeDirectionVector apparentTelescopeDirectionVector) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean transformTelescopeToCelestial(TelescopeDirectionVector telescopeDirectionVector, DoubleRef rightAscension, DoubleRef declination) {
        // TODO Auto-generated method stub
        return false;
    }
}
