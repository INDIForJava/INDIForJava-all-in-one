package org.gnu.savannah.gsl;

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
