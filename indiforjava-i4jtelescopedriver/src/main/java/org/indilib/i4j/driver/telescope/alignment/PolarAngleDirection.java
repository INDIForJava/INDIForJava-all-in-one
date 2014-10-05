package org.indilib.i4j.driver.telescope.alignment;

/**
 * * The direction of measurement of a polar angle. The following are
 * conventions for some coordinate systems - Declination is measured
 * FROM_AZIMUTHAL_PLANE. - Altitude is measured FROM_AZIMUTHAL_PLANE. - Altitude
 * in libnova horizontal coordinates is measured FROM_AZIMUTHAL_PLANE.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum PolarAngleDirection {
    /**
     * Angle is measured down from the polar axis
     */
    FROM_POLAR_AXIS,
    /**
     * Angle is measured upwards from the azimuthal plane
     */
    FROM_AZIMUTHAL_PLANE
}
