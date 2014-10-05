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

public class TestCBlasDnrm2 extends TestGsl {
	final double flteps = 1e-4, dbleps = 1e-6;

	@Test
	public void test_nrm2_1() {
		int N = 1;
		DoubleArray X = new DoubleArray(0.071);
		int incX = -1;
		double expected = 0;
		double f;
		f = Gsl.cblas_dnrm2(N, X, incX);
		gsl_test_rel(f, expected, dbleps, "dnrm2(case 29)");
	}

	@Test
	public void test_nrm2_2() {
		int N = 2;
		DoubleArray X = new DoubleArray(0.696, -0.804);
		int incX = 1;
		double expected = 1.06340584915;
		double f;
		f = Gsl.cblas_dnrm2(N, X, incX);
		gsl_test_rel(f, expected, dbleps, "dnrm2(case 33)");
	}

	@Test
	public void test_nrm2_3() {
		int N = 2;
		DoubleArray X = new DoubleArray(0.217, -0.588);
		int incX = -1;
		double expected = 0;
		double f;
		f = Gsl.cblas_dnrm2(N, X, incX);
		gsl_test_rel(f, expected, dbleps, "dnrm2(case 37)");
	}

}