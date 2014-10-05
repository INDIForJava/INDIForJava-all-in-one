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

public class TestCBlasDgemv extends TestGsl {
	final double flteps = 1e-4, dbleps = 1e-6;

	@Test
	public void test_gemv1() {
		CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
		CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
		int M = 1;
		int N = 1;
		int lda = 1;
		double alpha = -0.3;
		double beta = -1;
		DoubleArray A = new DoubleArray(-0.047);
		DoubleArray X = new DoubleArray(0.672);
		int incX = -1;
		DoubleArray Y = new DoubleArray(0.554);
		int incY = -1;
		DoubleArray y_expected = new DoubleArray(-0.5445248);
		Gsl.cblas_dgemv(order, trans, M, N, alpha, A, lda, X, incX, beta, Y,
				incY);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"dgemv(case 778)");
			}
		}
	}

	@Test
	public void test_gemv2() {
		CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
		CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
		int M = 1;
		int N = 1;
		int lda = 1;
		double alpha = -0.3;
		double beta = -1;
		DoubleArray A = new DoubleArray(-0.047);
		DoubleArray X = new DoubleArray(0.672);
		int incX = -1;
		DoubleArray Y = new DoubleArray(0.554);
		int incY = -1;
		DoubleArray y_expected = new DoubleArray(-0.5445248);
		Gsl.cblas_dgemv(order, trans, M, N, alpha, A, lda, X, incX, beta, Y,
				incY);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"dgemv(case 779)");
			}
		}
	}

	@Test
	public void test_gemv3() {
		CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
		CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
		int M = 1;
		int N = 1;
		int lda = 1;
		double alpha = -1;
		double beta = 1;
		DoubleArray A = new DoubleArray(-0.047);
		DoubleArray X = new DoubleArray(0.672);
		int incX = -1;
		DoubleArray Y = new DoubleArray(0.554);
		int incY = -1;
		DoubleArray y_expected = new DoubleArray(0.585584);
		Gsl.cblas_dgemv(order, trans, M, N, alpha, A, lda, X, incX, beta, Y,
				incY);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"dgemv(case 780)");
			}
		}
	}

	@Test
	public void test_gemv4() {
		CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
		CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
		int M = 1;
		int N = 1;
		int lda = 1;
		double alpha = -1;
		double beta = 1;
		DoubleArray A = new DoubleArray(-0.047);
		DoubleArray X = new DoubleArray(0.672);
		int incX = -1;
		DoubleArray Y = new DoubleArray(0.554);
		int incY = -1;
		DoubleArray y_expected = new DoubleArray(0.585584);
		Gsl.cblas_dgemv(order, trans, M, N, alpha, A, lda, X, incX, beta, Y,
				incY);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"dgemv(case 781)");
			}
		}
	}
}