package org.gnu.savannah.gsl;

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