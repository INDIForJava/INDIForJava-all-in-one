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

import org.junit.Test;

public class TestMatrix extends TestGsl {
	@Test
	public void main_test() {
		int M = 53;
		int N = 107;
		test_func(M, N);

		M = 4;
		N = 4;
		test_func(M, N);
		M = 2;
		N = 2;
		test_func(M, N);
	}

	void test_func(int M, int N) {
		int i, j;
		int k = 0;
		boolean isFailed;

		GslMatrix m = new GslMatrix(M, N);

		gsl_test(m.data == null, "gsl_matrix" + "_alloc returns valid pointer");
		gsl_test(m.size1 != M, "gsl_matrix" + "_alloc returns valid size1");
		gsl_test(m.size2 != N, "gsl_matrix" + "_alloc returns valid size2");
		gsl_test(m.tda != N, "gsl_matrix" + "_alloc returns valid tda");

		for (i = 0; i < M; i++) {
			for (j = 0; j < N; j++) {
				k++;
				m.set(i, j, (double) k);
			}
		}

		{
			isFailed = false;
			k = 0;
			for (i = 0; i < M; i++) {
				for (j = 0; j < N; j++) {
					k++;
					if (m.data.get(i * N + j) != (double) k)
						isFailed = true;
				}
				;
			}
			;

			gsl_test(isFailed, "gsl_matrix" + "_set writes into array");
		}

		{
			isFailed = false;
			k = 0;
			for (i = 0; i < M; i++) {
				for (j = 0; j < N; j++) {
					k++;
					if (m.get(i, j) != (double) k)
						isFailed = true;
				}
				;
			}
			;
			gsl_test(isFailed, "gsl_matrix" + "_get reads from array");
		}

		m = new GslMatrix(M, N);

		{
			isFailed = !m.isNull();
			gsl_test(isFailed, "_isnull" + " gsl_matrix " + " on calloc matrix");

			isFailed = m.isPos();
			gsl_test(isFailed, "_ispos" + " gsl_matrix " + " on calloc matrix");

			isFailed = m.isNeg();
			gsl_test(isFailed, "_isneg" + " gsl_matrix " + " on calloc matrix");

			isFailed = !m.isNonNeg();
			gsl_test(isFailed, "_isnonneg" + " gsl_matrix "
					+ " on calloc matrix");
		}

		for (i = 0; i < M; i++) {
			for (j = 0; j < N; j++) {
				m.set(i, j, (double) 0);
			}
		}

		{
			isFailed = !m.isNull();
			gsl_test(isFailed, "_isnull" + " gsl_matrix " + " on null matrix");

			isFailed = m.isPos();
			gsl_test(isFailed, "_ispos" + " gsl_matrix " + " on null matrix");

			isFailed = m.isNeg();
			gsl_test(isFailed, "_isneg" + " gsl_matrix " + " on null matrix");

			isFailed = !m.isNonNeg();
			gsl_test(isFailed, "_isnonneg" + " gsl_matrix " + " on null matrix");
		}

		k = (N * M < 10) ? 10 - N * M : 0;
		for (i = 0; i < M; i++) {
			for (j = 0; j < N; j++) {
				k++;
				m.set(i, j, (double) (k % 10));
			}
		}
		{
			isFailed = m.isNull();
			gsl_test(isFailed, "_isnull" + " gsl_matrix "
					+ " on non-negative matrix");

			isFailed = m.isPos();
			gsl_test(isFailed, "_ispos" + " gsl_matrix "
					+ " on non-negative matrix");

			isFailed = m.isNeg();
			gsl_test(isFailed, "_isneg" + " gsl_matrix "
					+ " on non-negative matrix");

			isFailed = !m.isNonNeg();
			gsl_test(isFailed, "_isnonneg" + " gsl_matrix "
					+ " on non-negative matrix");
		}

		k = (N * M < 10) ? 10 - N * M : 0;
		for (i = 0; i < M; i++) {
			for (j = 0; j < N; j++) {
				double mij = ((++k) % 10) - (double) 5;
				m.set(i, j, mij);
			}
		}

		{
			isFailed = m.isNull();
			gsl_test(isFailed, "_isnull" + " gsl_matrix " + " on mixed matrix");

			isFailed = m.isPos();
			gsl_test(isFailed, "_ispos" + " gsl_matrix " + " on mixed matrix");

			isFailed = m.isNeg();
			gsl_test(isFailed, "_isneg" + " gsl_matrix " + " on mixed matrix");

			isFailed = m.isNonNeg();
			gsl_test(isFailed, "_isnonneg" + " gsl_matrix "
					+ " on mixed matrix");
		}

		k = (N * M < 10) ? 10 - N * M : 0;
		for (i = 0; i < M; i++) {
			for (j = 0; j < N; j++) {
				k++;
				m.set(i, j, -(double) (k % 10));
			}
		}

		{
			isFailed = m.isNull();
			gsl_test(isFailed, "_isnull" + " gsl_matrix "
					+ " on non-positive matrix");

			isFailed = m.isPos();
			gsl_test(isFailed, "_ispos" + " gsl_matrix "
					+ " on non-positive matrix");

			isFailed = m.isNeg();
			gsl_test(isFailed, "_isneg" + " gsl_matrix "
					+ " on non-positive matrix");

			isFailed = m.isNonNeg();
			gsl_test(isFailed, "_isnonneg" + " gsl_matrix "
					+ " on non-positive matrix");
		}

		k = 0;
		for (i = 0; i < M; i++) {
			for (j = 0; j < N; j++) {
				k++;
				m.set(i, j, (double) (k % 10 + 1));
			}
		}

		{
			isFailed = m.isNull();
			gsl_test(isFailed, "_isnull" + " gsl_matrix "
					+ " on positive matrix");

			isFailed = !m.isPos();
			gsl_test(isFailed, "_ispos" + " gsl_matrix "
					+ " on positive matrix");

			isFailed = m.isNeg();
			gsl_test(isFailed, "_isneg" + " gsl_matrix "
					+ " on positive matrix");

			isFailed = !m.isNonNeg();
			gsl_test(isFailed, "_isnonneg" + " gsl_matrix "
					+ " on positive matrix");
		}

		k = 0;
		for (i = 0; i < M; i++) {
			for (j = 0; j < N; j++) {
				k++;
				m.set(i, j, -(double) (k % 10 + 1));
			}
		}

		{
			isFailed = m.isNull();
			gsl_test(isFailed, "_isnull" + " gsl_matrix "
					+ " on negative matrix");

			isFailed = m.isPos();
			gsl_test(isFailed, "_ispos" + " gsl_matrix "
					+ " on negative matrix");

			isFailed = !m.isNeg();
			gsl_test(isFailed, "_isneg" + " gsl_matrix "
					+ " on negative matrix");

			isFailed = m.isNonNeg();
			gsl_test(isFailed, "_isnonneg" + " gsl_matrix "
					+ " on negative matrix");
		}

	}

	@Test
	public void test_subMatrix() {
		GslMatrix m = new GslMatrix(5, 5);
		for (int index = 0; index < 25; index++) {
			m.data.set(index, index);
		}
		GslMatrixView sub = m.submatrix(1, 1, 2, 3);
		double[] expected = {
				//
				6.0, 7.0, 8.0,//
				11.0, 12.0, 13.0 };
		checkMatrix(expected, sub.matrix);
		sub.matrix.swapColumns(0, 2);
		expected = new double[] {
				//
				0.0, 1.0, 2.0, 3.0, 4.0,//
				5.0, 8.0, 7.0, 6.0, 9.0,//
				10.0, 13.0, 12.0, 11.0, 14.0,//
				15.0, 16.0, 17.0, 18.0, 19.0,//
				20.0, 21.0, 22.0, 23.0, 24.0 };
		checkMatrix(expected, m);
		sub.matrix.swapRows(0, 1);
		expected = new double[] {
				//
				0.0, 1.0, 2.0, 3.0, 4.0,//
				5.0, 13.0, 12.0, 11.0, 9.0,//
				10.0, 8.0, 7.0, 6.0, 14.0,//
				15.0, 16.0, 17.0, 18.0, 19.0,//
				20.0, 21.0, 22.0, 23.0, 24.0 };
		checkMatrix(expected, m);
		sub.matrix.toString();
	}

	@Test
	public void test_subVector_1() {
		GslMatrix m = new GslMatrix(5, 5);
		for (int index = 0; index < 25; index++) {
			m.data.set(index, index);
		}
		GslVector sub = m.column(2).vector;
		double[] expected = { 2.0, 7.0, 12.0, 17.0, 22.0 };
		checkVector(expected, sub);
		sub.setZero();
		expected = new double[] {
				//
				0.0, 1.0, 0.0, 3.0, 4.0,//
				5.0, 6.0, 0.0, 8.0, 9.0,//
				10.0, 11.0, 0.0, 13.0, 14.0,//
				15.0, 16.0, 0.0, 18.0, 19.0,//
				20.0, 21.0, 0.0, 23.0, 24.0 };
		checkMatrix(expected, m);
		sub.set(2, 99.0);
		expected = new double[] {
				//
				0.0, 1.0, 0.0, 3.0, 4.0,//
				5.0, 6.0, 0.0, 8.0, 9.0,//
				10.0, 11.0, 99.0, 13.0, 14.0,//
				15.0, 16.0, 0.0, 18.0, 19.0,//
				20.0, 21.0, 0.0, 23.0, 24.0 };
		checkMatrix(expected, m);
	}
	@Test
	public void test_subVector_2() {
		GslMatrix m = new GslMatrix(5, 5);
		for (int index = 0; index < 25; index++) {
			m.data.set(index, index);
		}
		GslVector sub = m.row(2).vector;
		double[] expected = {10.0,11.0,12.0,13.0,14.0 };
		checkVector(expected, sub);
		sub.setZero();
		expected = new double[] {
				//
				0.0,	1.0,	2.0,	3.0,	4.0,//
				5.0,	6.0,	7.0,	8.0,	9.0,//
				0.0,	0.0,	0.0,	0.0,	0.0,//
				15.0,	16.0,	17.0,	18.0,	19.0,//
				20.0,	21.0,	22.0,	23.0,	24.0 };
		checkMatrix(expected, m);
		sub.set(2, 99.0);
		expected = new double[] {
				//
				0.0,	1.0,	2.0,	3.0,	4.0,//
				5.0,	6.0,	7.0,	8.0,	9.0,//
				0.0,	0.0,	99.0,	0.0,	0.0,//
				15.0,	16.0,	17.0,	18.0,	19.0,//
				20.0,	21.0,	22.0,	23.0,	24.0 };
		checkMatrix(expected, m);
	}
}
