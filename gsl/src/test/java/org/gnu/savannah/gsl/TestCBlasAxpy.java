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

public class TestCBlasAxpy extends TestGsl {
	double flteps = 1e-4, dbleps = 1e-6;

	@Test
	public void test_axpy2() {

		int N = 1;
		double alpha = 0;
		DoubleArray X = new DoubleArray(0.071);
		int incX = 1;
		DoubleArray Y = new DoubleArray(-0.888);
		int incY = -1;
		double expected[] = { -0.888 };
		Gsl.cblas_daxpy(N, alpha, X, incX, Y, incY);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), expected[i], dbleps, "daxpy(case 65)");
			}
		}

	}

	@Test
	public void test_axpy6() {

		int N = 1;
		double alpha = -0.3;
		DoubleArray X = new DoubleArray(0.029);
		int incX = -1;
		DoubleArray Y = new DoubleArray(-0.992);
		int incY = 1;
		double expected[] = { -1.0007 };
		Gsl.cblas_daxpy(N, alpha, X, incX, Y, incY);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), expected[i], dbleps, "daxpy(case 69)");
			}
		}
	}

	@Test
	public void test_axpy10() {

		int N = 1;
		double alpha = -1;
		DoubleArray X = new DoubleArray(-0.558);
		int incX = -1;
		DoubleArray Y = new DoubleArray(0.308);
		int incY = -1;
		double expected[] = { 0.866 };
		Gsl.cblas_daxpy(N, alpha, X, incX, Y, incY);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), expected[i], dbleps, "daxpy(case 73)");
			}
		}
	}

}
