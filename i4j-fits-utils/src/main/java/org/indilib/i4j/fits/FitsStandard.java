package org.indilib.i4j.fits;

/*
 * #%L
 * INDI for Java Utilities for the fits image format
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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

import static org.indilib.i4j.fits.IFitsHeader.*;

/**
 * This data dictionary lists the 53 keywords currently defined in the FITS
 * Standard.
 * 
 * @see http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html
 * @author Richard van Nieuwenhoven
 */
public enum FitsStandard implements IFitsHeader {
    /**
     * Columns 1-8 contain ASCII blanks. This keyword has no associated value.
     * Columns 9-80 may contain any ASCII text. Any number of card images with
     * blank keyword fields may appear in a header.
     */
    BLANKS("        ", STATUS.RESERVED, HDU.ANY, VALUE.NONE, "descriptive comment"),
    /**
     * The value field shall contain a character string identifying who compiled
     * the information in the data associated with the header. This keyword is
     * appropriate when the data originate in a published paper or are compiled
     * from many sources.
     */
    AUTHOR(STATUS.RESERVED, HDU.ANY, VALUE.STRING, "author of the data"),
    /**
     * The value field shall contain an integer. The absolute value is used in
     * computing the sizes of data structures. It shall specify the number of
     * bits that represent a data value. RANGE: -64,-32,8,16,32
     */
    BITPIX(STATUS.MANDATORY, HDU.ANY, VALUE.INTEGER, "bits per data value"),

    /**
     * This keyword shall be used only in primary array headers or IMAGE
     * extension headers with positive values of BITPIX (i.e., in arrays with
     * integer data). Columns 1-8 contain the string, `BLANK ' (ASCII blanks in
     * columns 6-8). The value field shall contain an integer that specifies the
     * representation of array values whose physical values are undefined.
     */
    BLANK(STATUS.RESERVED, HDU.IMAGE, VALUE.INTEGER, "value used for undefined array elements"),

    /**
     * This keyword may be used only in the primary header. It shall appear
     * within the first 36 card images of the FITS file. (Note: This keyword
     * thus cannot appear if NAXIS is greater than 31, or if NAXIS is greater
     * than 30 and the EXTEND keyword is present.) Its presence with the
     * required logical value of T advises that the physical block size of the
     * FITS file on which it appears may be an integral multiple of the logical
     * record length, and not necessarily equal to it. Physical block size and
     * logical record length may be equal even if this keyword is present or
     * unequal if it is absent. It is reserved primarily to prevent its use with
     * other meanings. Since the issuance of version 1 of the standard, the
     * BLOCKED keyword has been deprecated.
     */
    BLOCKED(STATUS.RESERVED, HDU.PRIMARY, VALUE.LOGICAL, "is physical blocksize a multiple of 2880?");

    private final String headerName;

    private final STATUS status;

    private final HDU hdu;

    private final VALUE valueType;

    private final String comment;

    private FitsStandard(STATUS status, HDU hdu, VALUE valueType, String comment) {
        this(null, status, hdu, valueType, comment);
    }

    private FitsStandard(String headerName, STATUS status, HDU hdu, VALUE valueType, String comment) {
        this.headerName = headerName;
        this.status = status;
        this.hdu = hdu;
        this.valueType = valueType;
        this.comment = comment;
    }

    @Override
    public String headerName() {
        return headerName == null ? name() : headerName;
    }

    @Override
    public STATUS status() {
        return status;
    }

    @Override
    public HDU hdu() {
        return hdu;
    }

    @Override
    public VALUE valueType() {
        return valueType;
    }

    @Override
    public String comment() {
        return comment;
    }
}
