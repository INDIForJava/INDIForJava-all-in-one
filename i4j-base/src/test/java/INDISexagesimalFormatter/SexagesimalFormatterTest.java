package INDISexagesimalFormatter;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.*;

import org.indilib.i4j.INDISexagesimalFormatter;
import org.junit.Assert;
import org.junit.Test;

public class SexagesimalFormatterTest {

    @Test
    public void testRaDec1() throws Exception {
        INDISexagesimalFormatter indiSexagesimalFormatter = new INDISexagesimalFormatter("%010.6m");
        String value = indiSexagesimalFormatter.format(1.999999d);
        Assert.assertEquals("the humanreadablke representation should be", "   2:00:00", value);
        double originalValue = indiSexagesimalFormatter.parseSexagesimal(value);
        Assert.assertEquals("converted back to number (with double rounding delta)", 1.999999d, originalValue, 0.00001);
    }

    @Test
    public void testRaDec2() throws Exception {
        INDISexagesimalFormatter indiSexagesimalFormatter = new INDISexagesimalFormatter("%010.6m");
        String value = indiSexagesimalFormatter.format(181.11111111111111111111111111111111111117d);
        Assert.assertEquals("the humanreadablke representation should be", " 181:06:40", value);
        double originalValue = indiSexagesimalFormatter.parseSexagesimal(value);
        Assert.assertEquals("converted back to number (with double rounding delta)", 181.111111d, originalValue, 0.00001);
    }
}
