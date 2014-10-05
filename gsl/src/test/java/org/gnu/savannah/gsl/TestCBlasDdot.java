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

public class TestCBlasDdot extends TestGsl {
	double flteps = 1e-4, dbleps = 1e-6;

	@Test
	public void test_dot1()

	{
		int N = 1;
		DoubleArray X = new DoubleArray(0.79);
		DoubleArray Y = new DoubleArray(-0.679);
		int incX = 1;
		int incY = -1;
		double expected = -0.53641;
		double f;
		f = Gsl.cblas_ddot(N, X, incX, Y, incY);
		gsl_test_rel(f, expected, dbleps, "ddot(case 11)");
	};

	@Test
	public void test_dot2()

	{
		int N = 1;
		DoubleArray X = new DoubleArray(0.949);
		DoubleArray Y = new DoubleArray(-0.873);
		int incX = -1;
		int incY = 1;
		double expected = -0.828477;
		double f;
		f = Gsl.cblas_ddot(N, X, incX, Y, incY);
		gsl_test_rel(f, expected, dbleps, "ddot(case 17)");
	};

	@Test
	public void test_dot3() {
		int N = 1;
		DoubleArray X = new DoubleArray(-0.434);
		DoubleArray Y = new DoubleArray(-0.402);
		int incX = -1;
		int incY = -1;
		double expected = 0.174468;
		double f;
		f = Gsl.cblas_ddot(N, X, incX, Y, incY);
		gsl_test_rel(f, expected, dbleps, "ddot(case 23)");
	};

}
