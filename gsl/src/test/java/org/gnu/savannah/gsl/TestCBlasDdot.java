package org.gnu.savannah.gsl;

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
