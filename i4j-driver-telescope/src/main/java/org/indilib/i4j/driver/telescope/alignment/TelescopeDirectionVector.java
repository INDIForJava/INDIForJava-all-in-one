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

import java.io.Serializable;

import net.sourceforge.novaforjava.Utility;
import net.sourceforge.novaforjava.api.LnEquPosn;
import net.sourceforge.novaforjava.api.LnHrzPosn;
import net.sourceforge.novaforjava.api.LnhEquPosn;
import net.sourceforge.novaforjava.api.LnhHrzPosn;

import org.gnu.savannah.gsl.CBLAS_TRANSPOSE;
import org.gnu.savannah.gsl.Gsl;
import org.gnu.savannah.gsl.GslMatrix;
import org.gnu.savannah.gsl.GslVector;

/**
 * These functions are used to convert different coordinate systems to and from
 * the telescope direction vectors (normalised vector/direction cosines) used
 * for telescope coordinates in the alignment subsystem.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TelescopeDirectionVector implements Cloneable, Serializable {

    /**
     * the number of degrees in a half circle.
     */
    private static final double HALF_CIRCLE_IN_DEGREES = 180.0;

    /**
     * 3 dimentions.
     */
    private static final int DIM_3D = 3;

    /**
     * Servial version id.
     */
    private static final long serialVersionUID = 7085347593351496492L;

    /**
     * Calculates a normalised direction vector from the supplied altitude and
     * azimuth.This assumes a right handed coordinate system for the telescope
     * direction vector with XY being the azimuthal plane, and azimuth being
     * measured in a clockwise direction.
     * 
     * @param horizontalCoordinates
     *            Altitude and Azimuth in degrees minutes seconds
     * @return A TelescopeDirectionVector
     */
    public static TelescopeDirectionVector telescopeDirectionVectorFromAltitudeAzimuth(LnhHrzPosn horizontalCoordinates) {
        return telescopeDirectionVectorFromSphericalCoordinate(Utility.ln_dms_to_rad(horizontalCoordinates.az), AzimuthAngleDirection.CLOCKWISE,
                Utility.ln_dms_to_rad(horizontalCoordinates.alt), PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
    }

    /**
     * Calculates a normalised direction vector from the supplied altitude and
     * azimuth.This assumes a right handed coordinate system for the telescope
     * direction vector with XY being the azimuthal plane, and azimuth being
     * measured in a clockwise direction.
     * 
     * @param horizontalCoordinates
     *            Altitude and Azimuth in decimal degrees
     * @return A TelescopeDirectionVector
     */
    public static TelescopeDirectionVector telescopeDirectionVectorFromAltitudeAzimuth(LnHrzPosn horizontalCoordinates) {
        return telescopeDirectionVectorFromSphericalCoordinate(Utility.ln_deg_to_rad(horizontalCoordinates.az), AzimuthAngleDirection.CLOCKWISE,
                Utility.ln_deg_to_rad(horizontalCoordinates.alt), PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
    }

    /**
     * Calculates a telescope direction vector from the supplied equatorial
     * coordinates.This assumes a right handed coordinate system for the
     * direction vector with the right ascension being in the XY plane.
     * 
     * @param equatorialCoordinates
     *            The equatorial coordinates in decimal degrees
     * @return A TelescopeDirectionVector
     */
    public static TelescopeDirectionVector telescopeDirectionVectorFromEquatorialCoordinates(LnEquPosn equatorialCoordinates) {
        return telescopeDirectionVectorFromSphericalCoordinate(Utility.ln_deg_to_rad(equatorialCoordinates.ra), AzimuthAngleDirection.ANTI_CLOCKWISE,
                Utility.ln_deg_to_rad(equatorialCoordinates.dec), PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
    }

    /**
     * Calculates a telescope direction vector from the supplied equatorial
     * coordinates.This assumes a right handed coordinate system for the
     * direction vector with the right ascension being in the XY plane.
     * 
     * @param equatorialCoordinates
     *            The equatorial coordinates in hours minutes seconds and
     *            degrees minutes seconds
     * @return A TelescopeDirectionVector
     */
    public static TelescopeDirectionVector telescopeDirectionVectorFromEquatorialCoordinates(LnhEquPosn equatorialCoordinates) {
        return telescopeDirectionVectorFromSphericalCoordinate(Utility.ln_hms_to_rad(equatorialCoordinates.ra), AzimuthAngleDirection.ANTI_CLOCKWISE,
                Utility.ln_dms_to_rad(equatorialCoordinates.dec), PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
    }

    /**
     * Calculates a telescope direction vector from the supplied local hour
     * angle and declination.This assumes a right handed coordinate system for
     * the direction vector with the hour angle being in the XY plane.
     * 
     * @param equatorialCoordinates
     *            The local hour angle and declination in decimal degrees
     * @return A TelescopeDirectionVector
     */
    public static TelescopeDirectionVector telescopeDirectionVectorFromLocalHourAngleDeclination(LnEquPosn equatorialCoordinates) {
        return telescopeDirectionVectorFromSphericalCoordinate(Utility.ln_deg_to_rad(equatorialCoordinates.ra), AzimuthAngleDirection.CLOCKWISE,
                Utility.ln_deg_to_rad(equatorialCoordinates.dec), PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
    }

    /**
     * Calculates a telescope direction vector from the supplied spherical
     * coordinate information. TelescopeDirectionVectors are always assumed to
     * be normalised and right handed.
     * 
     * @param azimuthAngle
     *            The azimuth angle in radians
     * @param azimuthAngleDirection
     *            The direction the azimuth angle has been measured either
     *            CLOCKWISE or ANTI_CLOCKWISE
     * @param polarAngle
     *            The polar angle in radians
     * @param polarAngleDirection
     *            The direction the polar angle has been measured either
     *            FROM_POLAR_AXIS or FROM_AZIMUTHAL_PLANE
     * @return A TelescopeDirectionVector
     */
    public static TelescopeDirectionVector telescopeDirectionVectorFromSphericalCoordinate(double azimuthAngle, AzimuthAngleDirection azimuthAngleDirection,
            double polarAngle, PolarAngleDirection polarAngleDirection) {
        TelescopeDirectionVector vector = new TelescopeDirectionVector();

        if (AzimuthAngleDirection.ANTI_CLOCKWISE == azimuthAngleDirection) {
            if (PolarAngleDirection.FROM_AZIMUTHAL_PLANE == polarAngleDirection) {
                vector.x = Math.cos(polarAngle) * Math.cos(azimuthAngle);
                vector.y = Math.cos(polarAngle) * Math.sin(azimuthAngle);
                vector.z = Math.sin(polarAngle);
            } else {
                vector.x = Math.sin(polarAngle) * Math.sin(azimuthAngle);
                vector.y = Math.sin(polarAngle) * Math.cos(azimuthAngle);
                vector.z = Math.cos(polarAngle);
            }
        } else {
            if (PolarAngleDirection.FROM_AZIMUTHAL_PLANE == polarAngleDirection) {
                vector.x = Math.cos(polarAngle) * Math.cos(-azimuthAngle);
                vector.y = Math.cos(polarAngle) * Math.sin(-azimuthAngle);
                vector.z = Math.sin(polarAngle);
            } else {
                vector.x = Math.sin(polarAngle) * Math.sin(-azimuthAngle);
                vector.y = Math.sin(polarAngle) * Math.cos(-azimuthAngle);
                vector.z = Math.cos(polarAngle);
            }
        }

        return vector;
    }

    /**
     * x part of the vector.
     */
    protected double x;

    /**
     * y part of the vector.
     */
    protected double y;

    /**
     * z part of the vector.
     */
    protected double z;

    /**
     * Default constructor.
     */
    public TelescopeDirectionVector() {
        x = 0;
        y = 0;
        z = 0;
    }

    /**
     * Copy constructor .
     * 
     * @param x
     *            the x part of the vector.
     * @param y
     *            the y part of the vector.
     * @param z
     *            the z part of the vector.
     */
    public TelescopeDirectionVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Rotate the reference frame around the Y axis. This has the affect of
     * rotating the vector itself in the opposite direction
     * 
     * @param angle
     *            The angle to rotate the reference frame by. Positive values
     *            give an anti-clockwise rotation from the perspective of
     *            looking down the positive axis towards the origin.
     **/
    void rotateAroundY(double angle) {
        angle = angle * Math.PI / HALF_CIRCLE_IN_DEGREES;
        GslVector pGSLInputVector = new GslVector(DIM_3D);
        pGSLInputVector.set(0, x);
        pGSLInputVector.set(1, y);
        pGSLInputVector.set(2, z);
        GslMatrix pRotationMatrix = new GslMatrix(DIM_3D, DIM_3D);
        pRotationMatrix.set(0, 0, Math.cos(angle));
        pRotationMatrix.set(0, 1, 0.0);
        pRotationMatrix.set(0, 2, Math.sin(angle));
        pRotationMatrix.set(1, 0, 0.0);
        pRotationMatrix.set(1, 1, 1.0);
        pRotationMatrix.set(1, 2, 0.0);
        pRotationMatrix.set(2, 0, -Math.sin(angle));
        pRotationMatrix.set(2, 1, 0.0);
        pRotationMatrix.set(2, 2, Math.cos(angle));
        GslVector pGSLOutputVector = new GslVector(DIM_3D);
        pGSLOutputVector.setZero();
        Gsl.gsl_blas_dgemv(CBLAS_TRANSPOSE.CblasNoTrans, 1.0, pRotationMatrix, pGSLInputVector, 0.0, pGSLOutputVector);
        x = pGSLOutputVector.get(0);
        y = pGSLOutputVector.get(1);
        z = pGSLOutputVector.get(2);
    }

    /**
     * Calculates an altitude and azimuth from the supplied normalised direction
     * vector and declination.This assumes a right handed coordinate system for
     * the telescope direction vector with XY being the azimuthal plane, and
     * azimuth being measured in a clockwise direction.
     * 
     * @param horizontalCoordinates
     *            Altitude and Azimuth in degrees minutes seconds
     */
    public void altitudeAzimuthFromTelescopeDirectionVector(LnhHrzPosn horizontalCoordinates) {
        DoubleRef azimuthAngle = new DoubleRef();
        DoubleRef altitudeAngle = new DoubleRef();
        sphericalCoordinateFromTelescopeDirectionVector(azimuthAngle, AzimuthAngleDirection.CLOCKWISE, altitudeAngle, PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
        Utility.ln_rad_to_dms(azimuthAngle.getValue(), horizontalCoordinates.az);
        Utility.ln_rad_to_dms(altitudeAngle.getValue(), horizontalCoordinates.alt);
    }

    /**
     * Calculates an altitude and azimuth from the supplied normalised direction
     * vector and declination.This assumes a right handed coordinate system for
     * the telescope direction vector with XY being the azimuthal plane, and
     * azimuth being measured in a clockwise direction.
     * 
     * @param horizontalCoordinates
     *            Altitude and Azimuth in decimal degrees
     */
    public void altitudeAzimuthFromTelescopeDirectionVector(LnHrzPosn horizontalCoordinates) {
        DoubleRef azimuthAngle = new DoubleRef();
        DoubleRef altitudeAngle = new DoubleRef();
        sphericalCoordinateFromTelescopeDirectionVector(azimuthAngle, AzimuthAngleDirection.CLOCKWISE, altitudeAngle, PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
        horizontalCoordinates.az = Utility.ln_rad_to_deg(azimuthAngle.getValue());
        horizontalCoordinates.alt = Utility.ln_rad_to_deg(altitudeAngle.getValue());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    };

    /**
     * do a dot product of this vector.
     * 
     * @param rhs
     *            the richt hand side of the operation.
     * @return the result of the dor product.
     */
    public double dotProduct(TelescopeDirectionVector rhs) {
        return x * rhs.x + y * rhs.y + z * rhs.z;

    };

    /**
     * Calculates equatorial coordinates from the supplied telescope direction
     * vector and declination.This assumes a right handed coordinate system for
     * the direction vector with the right ascension being in the XY plane.
     * 
     * @param equatorialCoordinates
     *            The equatorial coordinates in decimal degrees
     */
    public void equatorialCoordinatesFromTelescopeDirectionVector(LnEquPosn equatorialCoordinates) {
        DoubleRef azimuthAngle = new DoubleRef();
        DoubleRef polarAngle = new DoubleRef();
        sphericalCoordinateFromTelescopeDirectionVector(azimuthAngle, AzimuthAngleDirection.ANTI_CLOCKWISE, polarAngle, PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
        equatorialCoordinates.ra = Utility.ln_rad_to_deg(azimuthAngle.getValue());
        equatorialCoordinates.dec = Utility.ln_rad_to_deg(polarAngle.getValue());
    };

    /**
     * Calculates equatorial coordinates from the supplied telescope direction
     * vector and declination.This assumes a right handed coordinate system for
     * the direction vector with the right ascension being in the XY plane.
     * 
     * @param equatorialCoordinates
     *            The equatorial coordinates in hours minutes seconds and
     *            degrees minutes seconds
     */
    public void equatorialCoordinatesFromTelescopeDirectionVector(LnhEquPosn equatorialCoordinates) {
        DoubleRef azimuthAngle = new DoubleRef();
        DoubleRef polarAngle = new DoubleRef();
        sphericalCoordinateFromTelescopeDirectionVector(azimuthAngle, AzimuthAngleDirection.ANTI_CLOCKWISE, polarAngle, PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
        Utility.ln_rad_to_hms(azimuthAngle.getValue(), equatorialCoordinates.ra);
        Utility.ln_rad_to_dms(polarAngle.getValue(), equatorialCoordinates.dec);
    };

    /**
     * @return the length of the vecor.
     */
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);

    };

    /**
     * Calculates a local hour angle and declination from the supplied telescope
     * direction vector and declination.This assumes a right handed coordinate
     * system for the direction vector with the hour angle being in the XY
     * plane.
     * 
     * @param equatorialCoordinates
     *            The local hour angle and declination in decimal degrees
     */
    public void localHourAngleDeclinationFromTelescopeDirectionVector(LnEquPosn equatorialCoordinates) {
        DoubleRef azimuthAngle = new DoubleRef();
        DoubleRef polarAngle = new DoubleRef();
        sphericalCoordinateFromTelescopeDirectionVector(azimuthAngle, AzimuthAngleDirection.CLOCKWISE, polarAngle, PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
        equatorialCoordinates.ra = Utility.ln_rad_to_deg(azimuthAngle.getValue());
        equatorialCoordinates.dec = Utility.ln_rad_to_deg(polarAngle.getValue());
    };

    /**
     * Calculates a local hour angle and declination from the supplied telescope
     * direction vector and declination.This assumes a right handed coordinate
     * system for the direction vector with the hour angle being in the XY
     * plane.
     * 
     * @param equatorialCoordinates
     *            The local hour angle and declination in hours minutes seconds
     *            and degrees minutes seconds
     */
    public void localHourAngleDeclinationFromTelescopeDirectionVector(LnhEquPosn equatorialCoordinates) {
        DoubleRef azimuthAngle = new DoubleRef();
        DoubleRef polarAngle = new DoubleRef();
        sphericalCoordinateFromTelescopeDirectionVector(azimuthAngle, AzimuthAngleDirection.CLOCKWISE, polarAngle, PolarAngleDirection.FROM_AZIMUTHAL_PLANE);
        Utility.ln_rad_to_hms(azimuthAngle.getValue(), equatorialCoordinates.ra);
        Utility.ln_rad_to_dms(polarAngle.getValue(), equatorialCoordinates.dec);
    }

    /**
     * minus operation.
     * 
     * @param rhs
     *            the right hand side of the operation.
     * @return the resulting new vector.
     */
    public TelescopeDirectionVector minus(TelescopeDirectionVector rhs) {
        return new TelescopeDirectionVector(x - rhs.x, y - rhs.y, z - rhs.z);

    };

    /**
     * multiply operation.
     * 
     * @param rhs
     *            the right hand side of the operation.
     * @return the resulting new vector.
     */
    public TelescopeDirectionVector multiply(double rhs) {
        TelescopeDirectionVector result = new TelescopeDirectionVector();

        result.x = x * rhs;
        result.y = y * rhs;
        result.z = z * rhs;
        return result;

    };

    /**
     * multiply operation.
     * 
     * @param rhs
     *            the right hand side of the operation.
     * @return the resulting new vector.
     */
    public TelescopeDirectionVector multiply(TelescopeDirectionVector rhs) {
        TelescopeDirectionVector result = new TelescopeDirectionVector();

        result.x = y * rhs.z - z * rhs.y;
        result.y = z * rhs.x - x * rhs.z;
        result.z = x * rhs.y - y * rhs.x;
        return result;

    };

    /**
     * multiply operation and set the results to this vector.
     * 
     * @param rhs
     *            the right hand side of the operation.
     * @return the resulting new vector.
     */
    public TelescopeDirectionVector multiplyAsign(double rhs) {
        x = x * rhs;
        y = y * rhs;
        z = z * rhs;
        return this;

    };

    /**
     * normalize the vector.
     */
    public void normalise() {
        double length = Math.sqrt(x * x + y * y + z * z);
        x /= length;
        y /= length;
        z /= length;

    };

    /**
     * Calculates a spherical coordinate from the supplied telescope direction
     * vectorTelescope.DirectionVectors are always normalised and right handed.
     * 
     * @param azimuthAngle
     *            The azimuth angle in radians
     * @param azimuthAngleDirection
     *            The direction the azimuth angle has been measured either
     *            CLOCKWISE or ANTI_CLOCKWISE
     * @param polarAngle
     *            The polar angle in radians
     * @param polarAngleDirection
     *            The direction the polar angle has been measured either
     *            FROM_POLAR_AXIS or FROM_AZIMUTHAL_PLANE
     */
    public void sphericalCoordinateFromTelescopeDirectionVector(DoubleRef azimuthAngle, AzimuthAngleDirection azimuthAngleDirection, DoubleRef polarAngle,
            PolarAngleDirection polarAngleDirection) {
        if (AzimuthAngleDirection.ANTI_CLOCKWISE == azimuthAngleDirection) {
            if (PolarAngleDirection.FROM_AZIMUTHAL_PLANE == polarAngleDirection) {
                azimuthAngle.setValue(Math.atan2(y, x));
                polarAngle.setValue(Math.asin(z));
            } else {
                azimuthAngle.setValue(Math.atan2(y, x));
                polarAngle.setValue(Math.acos(z));
            }
        } else {
            if (PolarAngleDirection.FROM_AZIMUTHAL_PLANE == polarAngleDirection) {
                azimuthAngle.setValue(Math.atan2(-y, x));
                polarAngle.setValue(Math.asin(z));
            } else {
                azimuthAngle.setValue(Math.atan2(-y, x));
                polarAngle.setValue(Math.acos(z));
            }
        }
    }

    /**
     * copy the direction from the other vector.
     * 
     * @param otherVector
     *            the vector to copy the data from.
     */
    public void copyFrom(TelescopeDirectionVector otherVector) {
        x = otherVector.x;
        y = otherVector.y;
        z = otherVector.z;
    }

}
