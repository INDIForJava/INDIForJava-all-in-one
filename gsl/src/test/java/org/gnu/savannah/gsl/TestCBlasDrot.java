package org.gnu.savannah.gsl;

import org.gnu.savannah.gsl.util.DoubleArray;
import org.junit.Test;

public class TestCBlasDrot extends TestGsl {
	final double flteps = 1e-4, dbleps = 1e-6;

	@Test
	public void test_rot1()

	{
		int N = 1;
		double c = 0;
		double s = 0;
		DoubleArray X = new DoubleArray(-0.493);
		int incX = 1;
		DoubleArray Y = new DoubleArray(-0.014);
		int incY = -1;
		DoubleArray x_expected = new DoubleArray(0.0);
		DoubleArray y_expected = new DoubleArray(0.0);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 566)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 567)");
			}
		}
	}

	@Test
	public void test_rot2() {
		int N = 1;
		double c = 0.866025403784;
		double s = 0.5;
		DoubleArray X = new DoubleArray(-0.493);
		int incX = 1;
		DoubleArray Y = new DoubleArray(-0.014);
		int incY = -1;
		DoubleArray x_expected = new DoubleArray(-0.433950524066);
		DoubleArray y_expected = new DoubleArray(0.234375644347);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 568)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 569)");
			}
		}
	}

	@Test
	public void test_rot3() {
		int N = 1;
		double c = 0;
		double s = -1;
		DoubleArray X = new DoubleArray(-0.493);
		int incX = 1;
		DoubleArray Y = new DoubleArray(-0.014);
		int incY = -1;
		DoubleArray x_expected = new DoubleArray(0.014);
		DoubleArray y_expected = new DoubleArray(-0.493);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 570)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 571)");
			}
		}
	}

	@Test
	public void test_rot4() {
		int N = 1;
		double c = -1;
		double s = 0;
		DoubleArray X = new DoubleArray(-0.493);
		int incX = 1;
		DoubleArray Y = new DoubleArray(-0.014);
		int incY = -1;
		DoubleArray x_expected = new DoubleArray(0.493);
		DoubleArray y_expected = new DoubleArray(0.014);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 572)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 573)");
			}
		}
	}

	@Test
	public void test_rot5() {
		int N = 1;
		double c = 0;
		double s = 0;
		DoubleArray X = new DoubleArray(-0.176);
		int incX = -1;
		DoubleArray Y = new DoubleArray(-0.165);
		int incY = 1;
		DoubleArray x_expected = new DoubleArray(0.0);
		DoubleArray y_expected = new DoubleArray(0.0);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 582)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 583)");
			}
		}
	}

	@Test
	public void test_rot6() {
		int N = 1;
		double c = 0.866025403784;
		double s = 0.5;
		DoubleArray X = new DoubleArray(-0.176);
		int incX = -1;
		DoubleArray Y = new DoubleArray(-0.165);
		int incY = 1;
		DoubleArray x_expected = new DoubleArray(-0.234920471066);
		DoubleArray y_expected = new DoubleArray(-0.0548941916244);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 584)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 585)");
			}
		}
	}

	@Test
	public void test_rot7() {
		int N = 1;
		double c = 0;
		double s = -1;
		DoubleArray X = new DoubleArray(-0.176);
		int incX = -1;
		DoubleArray Y = new DoubleArray(-0.165);
		int incY = 1;
		DoubleArray x_expected = new DoubleArray(0.165);
		DoubleArray y_expected = new DoubleArray(-0.176);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 586)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 587)");
			}
		}
	}

	@Test
	public void test_rot8() {
		int N = 1;
		double c = -1;
		double s = 0;
		DoubleArray X = new DoubleArray(-0.176);
		int incX = -1;
		DoubleArray Y = new DoubleArray(-0.165);
		int incY = 1;
		DoubleArray x_expected = new DoubleArray(0.176);
		DoubleArray y_expected = new DoubleArray(0.165);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 588)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 589)");
			}
		}
	}

	@Test
	public void test_rot9() {
		int N = 1;
		double c = 0;
		double s = 0;
		DoubleArray X = new DoubleArray(-0.464);
		int incX = -1;
		DoubleArray Y = new DoubleArray(0.7);
		int incY = -1;
		DoubleArray x_expected = new DoubleArray(0.0);
		DoubleArray y_expected = new DoubleArray(0.0);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 598)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 599)");
			}
		}
	}

	@Test
	public void test_rot10() {
		int N = 1;
		double c = 0.866025403784;
		double s = 0.5;
		DoubleArray X = new DoubleArray(-0.464);
		int incX = -1;
		DoubleArray Y = new DoubleArray(0.7);
		int incY = -1;
		DoubleArray x_expected = new DoubleArray(-0.051835787356);
		DoubleArray y_expected = new DoubleArray(0.838217782649);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 600)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 601)");
			}
		}
	}

	@Test
	public void test_rot11() {
		int N = 1;
		double c = 0;
		double s = -1;
		DoubleArray X = new DoubleArray(-0.464);
		int incX = -1;
		DoubleArray Y = new DoubleArray(0.7);
		int incY = -1;
		DoubleArray x_expected = new DoubleArray(-0.7);
		DoubleArray y_expected = new DoubleArray(-0.464);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 602)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 603)");
			}
		}
	}

	@Test
	public void test_rot12() {
		int N = 1;
		double c = -1;
		double s = 0;
		DoubleArray X = new DoubleArray(-0.464);
		int incX = -1;
		DoubleArray Y = new DoubleArray(0.7);
		int incY = -1;
		DoubleArray x_expected = new DoubleArray(0.464);
		DoubleArray y_expected = new DoubleArray(-0.7);
		Gsl.cblas_drot(N, X, incX, Y, incY, c, s);
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(X.get(i), x_expected.get(i), dbleps,
						"drot(case 604)");
			}
		}
		{
			int i;
			for (i = 0; i < 1; i++) {
				gsl_test_rel(Y.get(i), y_expected.get(i), dbleps,
						"drot(case 605)");
			}
		}
	}

}