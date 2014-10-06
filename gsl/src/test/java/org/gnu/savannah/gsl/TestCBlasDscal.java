package org.gnu.savannah.gsl;

/*
 * #%L
 * GNU Scientific Library port
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

import org.gnu.savannah.gsl.util.DoubleArray;
import org.junit.Test;

public class TestCBlasDscal extends TestGsl {

    final double flteps = 1e-4, dbleps = 1e-6;

    @Test
    public void test_scal_1() {
        int N = 1;
        double alpha = 0;
        DoubleArray X = new DoubleArray(0.686);
        int incX = -1;
        DoubleArray expected = new DoubleArray(0.686);
        Gsl.cblas_dscal(N, alpha, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), expected.get(i), dbleps, "dscal(case 115)");
            }
        }
    }

    @Test
    public void test_scal_2() {
        int N = 1;
        double alpha = 0.1;
        DoubleArray X = new DoubleArray(0.686);
        int incX = -1;
        DoubleArray expected = new DoubleArray(0.686);
        Gsl.cblas_dscal(N, alpha, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), expected.get(i), dbleps, "dscal(case 116)");
            }
        }
    }

    @Test
    public void test_scal_3() {
        int N = 1;
        double alpha = 1;
        DoubleArray X = new DoubleArray(0.686);
        int incX = -1;
        DoubleArray expected = new DoubleArray(0.686);
        Gsl.cblas_dscal(N, alpha, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), expected.get(i), dbleps, "dscal(case 117)");
            }
        }
    }

    @Test
    public void test_scal_4()

    {
        int N = 2;
        double alpha = 0;
        DoubleArray X = new DoubleArray(-0.429, -0.183);
        int incX = 1;
        DoubleArray expected = new DoubleArray(-0.0, -0.0);
        Gsl.cblas_dscal(N, alpha, X, incX);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(X.get(i), expected.get(i), dbleps, "dscal(case 133)");
            }
        }
    }

    @Test
    public void test_scal_5() {
        int N = 2;
        double alpha = 0.1;
        DoubleArray X = new DoubleArray(-0.429, -0.183);
        int incX = 1;
        DoubleArray expected = new DoubleArray(-0.0429, -0.0183);
        Gsl.cblas_dscal(N, alpha, X, incX);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(X.get(i), expected.get(i), dbleps, "dscal(case 134)");
            }
        }
    }

    @Test
    public void test_scal_6()

    {
        int N = 2;
        double alpha = 1;
        DoubleArray X = new DoubleArray(-0.429, -0.183);
        int incX = 1;
        DoubleArray expected = new DoubleArray(-0.429, -0.183);
        Gsl.cblas_dscal(N, alpha, X, incX);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(X.get(i), expected.get(i), dbleps, "dscal(case 135)");
            }
        }
    }

    @Test
    public void test_scal_7() {
        int N = 2;
        double alpha = 0;
        DoubleArray X = new DoubleArray(0.398, -0.656);
        int incX = -1;
        DoubleArray expected = new DoubleArray(0.398, -0.656);
        Gsl.cblas_dscal(N, alpha, X, incX);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(X.get(i), expected.get(i), dbleps, "dscal(case 151)");
            }
        }
    }

    @Test
    public void test_scal_8()

    {
        int N = 2;
        double alpha = 0.1;
        DoubleArray X = new DoubleArray(0.398, -0.656);
        int incX = -1;
        DoubleArray expected = new DoubleArray(0.398, -0.656);
        Gsl.cblas_dscal(N, alpha, X, incX);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(X.get(i), expected.get(i), dbleps, "dscal(case 152)");
            }
        }
    }

    @Test
    public void test_scal_9() {
        int N = 2;
        double alpha = 1;
        DoubleArray X = new DoubleArray(0.398, -0.656);
        int incX = -1;
        DoubleArray expected = new DoubleArray(0.398, -0.656);
        Gsl.cblas_dscal(N, alpha, X, incX);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(X.get(i), expected.get(i), dbleps, "dscal(case 153)");
            }
        }
    }

}
