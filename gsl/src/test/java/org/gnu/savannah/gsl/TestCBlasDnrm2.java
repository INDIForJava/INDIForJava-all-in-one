package org.gnu.savannah.gsl;

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