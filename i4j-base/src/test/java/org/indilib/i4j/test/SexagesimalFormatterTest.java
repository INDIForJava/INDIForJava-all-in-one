package org.indilib.i4j.test;

/*
 * #%L
 * INDI for Java Base Library
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

import org.indilib.i4j.INDISexagesimalFormatter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the SexagesimalFormatter.
 * 
 * @author Richard van Nieuwenhoven
 */
public class SexagesimalFormatterTest {

    /**
     * presision for the tests.
     */
    private static final double PRESISION = 0.00001;

    /**
     * test value 1.
     */
    private static final double TEST_VALUE_1_999999D = 1.999999d;

    /**
     * test value 2.
     */
    private static final double TEST_VALUE_181_111111D = 181.11111111111111111111111111111111111117d;

    /**
     * test Ra Dec 1.
     * 
     * @throws Exception
     *             on failure.
     */
    @Test
    public void testRaDec1() throws Exception {
        INDISexagesimalFormatter indiSexagesimalFormatter = new INDISexagesimalFormatter("%010.6m");
        String value = indiSexagesimalFormatter.format(TEST_VALUE_1_999999D);
        Assert.assertEquals("the humanreadablke representation should be", "   2:00:00", value);
        double originalValue = indiSexagesimalFormatter.parseSexagesimal(value);
        Assert.assertEquals("converted back to number (with double rounding delta)", TEST_VALUE_1_999999D, originalValue, PRESISION);
    }

    /**
     * test Ra Dec 2.
     * 
     * @throws Exception
     *             on failure.
     */
    @Test
    public void testRaDec2() throws Exception {
        INDISexagesimalFormatter indiSexagesimalFormatter = new INDISexagesimalFormatter("%010.6m");
        String value = indiSexagesimalFormatter.format(TEST_VALUE_181_111111D);
        Assert.assertEquals("the humanreadablke representation should be", " 181:06:40", value);
        double originalValue = indiSexagesimalFormatter.parseSexagesimal(value);
        Assert.assertEquals("converted back to number (with double rounding delta)", TEST_VALUE_181_111111D, originalValue, PRESISION);
    }
}
