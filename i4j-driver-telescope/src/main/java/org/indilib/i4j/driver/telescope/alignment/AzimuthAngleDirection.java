package org.indilib.i4j.driver.telescope.alignment;

/**
 * * The direction of measurement of an azimuth angle. The following are the
 * conventions for some coordinate systems. - Right Ascension is measured
 * ANTI_CLOCKWISE from the vernal equinox. - Local Hour Angle is measured
 * CLOCKWISE from the observer's meridian. - Greenwich Hour Angle is measured
 * CLOCKWISE from the Greenwich meridian. - Azimuth (as in Altitude Azimuth
 * coordinate systems ) is often measured CLOCKWISE\n from north. But ESO FITS
 * (Clockwise from South) and SDSS FITS(Anticlockwise from South)\n have
 * different conventions. Horizontal coordinates in libnova are measured
 * clockwise from south. alignment subsystem.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum AzimuthAngleDirection {
    /**
     * Angle is measured clockwise
     */
    CLOCKWISE,
    /**
     * Angle is measured anti clockwise
     */
    ANTI_CLOCKWISE
}
