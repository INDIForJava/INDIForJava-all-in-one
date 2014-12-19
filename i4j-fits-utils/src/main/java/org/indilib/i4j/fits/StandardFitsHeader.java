package org.indilib.i4j.fits;

/*
 * #%L
 * INDI for Java Utilities for the fits image format
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

/**
 * Here all the 'standard' fits headers are defined with a detailed description,
 * captiored from differents places around the internet.
 * 
 * @author Richard van Nieuwenhoven
 */
public class StandardFitsHeader {

    /**
     * indicates the processing history of the image. This keyword may be
     * repeated as many times as necessary.
     */
    public static String HISTORY = "HISTORY";

    /**
     * The value field shall contain a logical constant with the value T if the
     * file conforms to this standard. This keyword is mandatory for the primary
     * header and must not appear in extension headers. A value of F signifies
     * that the file does not conform to this standard.
     */
    public static String SIMPLE = "SIMPLE";

    /**
     * The value field shall contain an integer. The absolute value is used in
     * computing the sizes of data structures. It shall specify the number of
     * bits that represent a data value in the associated data array. The only
     * valid values of BITPIX are given in Table 8. Writers of FITS arrays
     * should select a BITPIX data type appropriate to the form, range of
     * values, and accuracy of the data in the array. Interpretation of valid
     * BITPIX value. 8 = Character or unsigned binary integer 16 = 16-bit two’s
     * complement binary integer 32 = 32-bit two’s complement binary integer 64
     * = 64-bit two’s complement binary integer -32 = IEEE single precision
     * floating point -64 = IEEE double precision floating point
     */
    public static String BITPIX = "BITPIX";

    /**
     * The value field shall contain a non-negative integer no greater than 999
     * representing the number of axes in the associated data array. A value of
     * zero signifies that no data follow the header in the HDU.
     */
    public static String NAXIS = "NAXIS";

    /**
     * The NAXISn keywords must be present for all values n = 1, ..., NAXIS, in
     * increasing order of n, and for no other values of n. The value field of
     * this indexed keyword shall contain a non-negative integer representing
     * the number of elements along axis n of a data array. A value of zero for
     * any of the NAXISn signifies that no data follow the header in the HDU
     * (however, the random groups structure described in Sect. 6 has NAXIS1 =
     * 0, but will have data following the header if the other NAXISn keywords
     * are non-zero). If NAXIS is equal to 0, there shall not be any NAXISn
     * keywords.
     */
    public static String NAXIS1 = "NAXIS1";

    /**
     * 2 axis {@link #NAXIS1} .
     */
    public static String NAXIS2 = "NAXIS2";

    /**
     * 3 axis. resent only for color images; value is always 3 (red, green, blue
     * color planes are present in that order). {@link #NAXIS1}
     */
    public static String NAXIS3 = "NAXIS3";

    /**
     * The value field shall always contain a floating-point number, regardless
     * of the value of BITPIX . This number shall give the maximum valid
     * physical value represented by the array, exclusive of any IEEE special
     * values.
     */
    public static String DATAMAX = "DATAMAX";

    /**
     * The value field shall always contain a floating-point number, regardless
     * of the value of BITPIX. This number shall give the minimum valid physical
     * value represented by the array (from Eq. 3), exclusive of any IEEE
     * special values.
     */
    public static String DATAMIN = "DATAMIN";

    /**
     * if present the image has a valid Bayer color pattern.
     */
    public static String BAYERPAT = "BAYERPAT";

    /**
     * The value of DATE-OBS shall be assumed to refer to the start of an
     * observation, unless another interpretation is clearly explained in the
     * comment field. Explicit specification of the time scale is recommended.
     * By default, times for TAI and times that run simultaneously with TAI,
     * e.,g., UTC and TT, will be assumed to be as measured at the detector (or,
     * in practical cases, at the observatory). For coordinate times such as
     * TCG, TCB and TDB, the default shall be to include lighttime corrections
     * to the associated spatial origin, namely the geocenter for TCG and the
     * solar-system barycenter for the other two.
     */
    public static String DATE_OBS = "DATE-OBS";

    /**
     * The value field shall contain a character string identifying the
     * telescope used to acquire the data associated with the header.
     */
    public static String TELESCOP = "TELESCOP";

    /**
     * The value field shall contain a character string identifying the
     * instrument used to acquire the data associated with the header.
     */
    public static String INSTRUME = "INSTRUME";

    /**
     * The value field shall contain a character string identifying who acquired
     * the data associated with the header.
     */
    public static String OBSERVER = "OBSERVER";

    /**
     * The value field shall contain a character string giving a name for the
     * object observed.
     */
    public static String OBJECT = "OBJECT";

    /**
     * duration of exposure in seconds.
     */
    public static String EXPTIME = "EXPTIME";

    /**
     * dark current integration time, if recorded. May be longer than exposure
     * time.
     */
    public static String DARKTIME = "DARKTIME";

    /**
     * binning factor used on X axis.
     */
    public static String XBINNING = "XBINNING";

    /**
     * binning factor used on Y axis.
     */
    public static String YBINNING = "YBINNING";

    /**
     * physical X dimension of the sensor's pixels in microns (present only if
     * the information is provided by the camera driver). Includes binning.
     */
    public static String XPIXSZ = "XPIXSZ";

    /**
     * physical Y dimension of the sensor's pixels in microns (present only if
     * the information is provided by the camera driver). Includes binning.
     */
    public static String YPIXSZ = "YPIXSZ";

    /**
     * synonym for {@link #XPIXSZ}.
     */
    public static String PIXSIZE1 = "PIXSIZE1";

    /**
     * synonym for {@link #YPIXSZ}.
     */
    public static String PIXSIZE2 = "PIXSIZE2";

    /**
     * Time system default: 'UTC approximate'.
     */
    public static String TIMESYS = "TIMESYS";

    /**
     * Time of observation start. format '23:57:44.0'
     */
    public static String TIME_OBS = "TIME-OBS";

    /**
     * MJD of observation start.(float)
     */
    public static String MJD_OBS = "MJD-OBS";

    /**
     * type of image: Light Frame, Bias Frame, Dark Frame, Flat Frame, or
     * Tricolor Image.
     */
    public static String IMAGETYP = "IMAGETYP";

    /**
     * subframe origin on X axis
     */
    public static String XORGSUBF = "XORGSUBF";

    /**
     * subframe origin on Y axis
     */
    public static String YORGSUBF = "YORGSUBF";

    /**
     * X offset of Bayer array on imaging sensor.
     */
    public static String XBAYROFF = "XBAYROFF";

    /**
     * Y offset of Bayer array on imaging sensor.
     */
    public static String YBAYROFF = "YBAYROFF";

}
