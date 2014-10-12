package org.indilib.i4j;

/*
 * #%L INDI for Java Base Library %% Copyright (C) 2013 - 2014 indiforjava %%
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Lesser Public License for more details. You should have received a copy of
 * the GNU General Lesser Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>. #L%
 */

import static org.junit.Assert.*;

import java.net.URL;

import org.indilib.i4j.url.INDIURLStreamHandlerFactory;
import org.junit.Assert;
import org.junit.Test;

public class TestURLStreamHandler {

    static {
        INDIURLStreamHandlerFactory.init();
    }

    @Test
    public void testName1() throws Exception {

        Assert.assertEquals(7624, new URL("indi://localhost").getDefaultPort());
    }

    @Test
    public void testSexa() throws Exception {
        System.out.println(new INDISexagesimalFormatter("%2.6m").format(0.66));
    }
}
