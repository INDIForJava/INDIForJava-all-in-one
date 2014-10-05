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

public class GslMatrix {
	public GslMatrix(int n1, int n2) {

		if (n1 == 0) {
			throw new IllegalStateException(
					"matrix dimension n1 must be positive integer");
		} else if (n2 == 0) {
			throw new IllegalStateException(
					"matrix dimension n2 must be positive integer");
		}

		/* FIXME: n1*n2 could overflow for large dimensions */

		block = new GslBlock(n1 * n2);

		data = block.data;
		size1 = n1;
		size2 = n2;
		tda = n2;
		owner = 1;

	}

	public int size1;
	public int size2;
	public int tda;
	public DoubleArray data;
	public GslBlock block;
	public int owner;

	public double get(int i, int j) {
		return data.get(i * tda + j);
	}

	public void set(int i, int j, double x) {
		data.set(i * tda + j, x);
	}

	public void copy(GslMatrix matrix) {
		DoubleArray.arraycopy(matrix.data, 0, data, 0, size1 * size2);
	}

	public GslErrno swapRows(int i, int j) {

		if (i >= size1) {
			throw new IllegalStateException("first row index is out of range");
		}

		if (j >= size1) {
			throw new IllegalStateException("second row index is out of range");
		}

		if (i != j) {
			int row1Offset = 1 * i * tda;
			int row2Offset = 1 * j * tda;

			int k;

			for (k = 0; k < 1 * size2; k++) {
				double tmp = data.get(row1Offset + k);
				data.set(row1Offset + k, data.get(row2Offset + k));
				data.set(row2Offset + k, tmp);
			}
		}

		return GslErrno.GSL_SUCCESS;
	}

	public void setIdentity() {
		int p = size1;
		int q = size2;

		for (int i = 0; i < p; i++) {
			for (int j = 0; j < q; j++) {
				data.set((i * tda + j), ((i == j) ? 1d : 0d));
			}
		}
	}

	public GslVectorView column(int j) {
		GslVectorView view = new GslVectorView();

		if (j >= size2) {
			throw new IllegalStateException("column index is out of range");
		}

		{
			GslVector v = new GslVector(j);

			// should referense same data
			v.data = new DoubleArray(this.data, j * 1);
			v.size = this.size1;
			v.stride = this.tda;
			v.block = this.block;
			v.owner = 0;

			view.vector = v;
			return view;
		}
	}

	public void setZero() {
		int i, j;
		int p = this.size1;
		int q = this.size2;
		int tda = this.tda;
		for (i = 0; i < p; i++) {
			for (j = 0; j < q; j++) {
				data.set(1 * (i * tda + j), 0d);
			}
		}
	}

	public GslErrno transpose() {
		int size1 = this.size1;
		int size2 = this.size2;
		int i, j, k;

		if (size1 != size2) {
			throw new IllegalStateException(
					"matrix must be square to take transpose");
		}

		for (i = 0; i < size1; i++) {
			for (j = i + 1; j < size2; j++) {
				for (k = 0; k < 1; k++) {
					int e1 = (i * this.tda + j) * 1 + k;
					int e2 = (j * this.tda + i) * 1 + k;
					{
						double tmp = this.data.get(e1);
						this.data.set(e1, this.data.get(e2));
						this.data.set(e2, tmp);
					}
				}
			}
		}

		return GslErrno.GSL_SUCCESS;
	}

	public GslErrno swapColumns(int i, int j) {
		int size1 = this.size1;
		int size2 = this.size2;

		if (i >= size2) {
			throw new IllegalStateException(
					"first column index is out of range");
		}

		if (j >= size2) {
			throw new IllegalStateException(
					"second column index is out of range");
		}

		if (i != j) {
			int col1Offset = 1 * i;
			int col2Offset = 1 * j;

			int p;

			for (p = 0; p < size1; p++) {
				int k;
				int n = p * 1 * this.tda;

				for (k = 0; k < 1; k++) {
					double tmp = data.get(col1Offset + n + k);
					data.set(col1Offset + n + k, data.get(col2Offset + n + k));
					data.set(col2Offset + n + k, tmp);
				}
			}
		}

		return GslErrno.GSL_SUCCESS;
	}

	public GslMatrixView submatrix(int i, int j, int n1, int n2) {
		GslMatrixView view = new GslMatrixView();

		if (i >= this.size1) {
			throw new IllegalStateException("row index is out of range");
		} else if (j >= this.size2) {
			throw new IllegalStateException("column index is out of range");
		} else if (n1 == 0) {
			throw new IllegalStateException("first dimension must be non-zero");
		} else if (n2 == 0) {
			throw new IllegalStateException("second dimension must be non-zero");
		} else if (i + n1 > this.size1) {
			throw new IllegalStateException("first dimension overflows matrix");
		} else if (j + n2 > this.size2) {
			throw new IllegalStateException("second dimension overflows matrix");
		}

		{
			GslMatrix s = new GslMatrix(1, 1);

			s.data = new DoubleArray(this.data, 1 * (i * this.tda + j));
			s.size1 = n1;
			s.size2 = n2;
			s.tda = this.tda;
			s.block = this.block;
			s.owner = 0;

			view.matrix = s;
			return view;
		}
	}

	public GslVectorView row(int i) {
		GslVectorView view = new GslVectorView();

		if (i >= this.size1) {
			throw new IllegalStateException("row index is out of range");
		}

		{
			GslVector v = new GslVector(1);

			v.data = new DoubleArray(this.data, i * 1 * this.tda);
			v.size = this.size2;
			v.stride = 1;
			v.block = this.block;
			v.owner = 0;

			view.vector = v;
			return view;
		}
	}

	public boolean isNull() {
		int size1 = this.size1;
		int size2 = this.size2;
		int tda = this.tda;

		int i, j, k;

		for (i = 0; i < size1; i++) {
			for (j = 0; j < size2; j++) {
				for (k = 0; k < 1; k++) {
					if (this.data.get((i * tda + j) * 1 + k) != 0.0) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public boolean isPos() {
		int size1 = this.size1;
		int size2 = this.size2;
		int tda = this.tda;

		int i, j, k;

		for (i = 0; i < size1; i++) {
			for (j = 0; j < size2; j++) {
				for (k = 0; k < 1; k++) {
					if (this.data.get((i * tda + j) * 1 + k) <= 0.0) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public boolean isNeg() {
		int size1 = this.size1;
		int size2 = this.size2;
		int tda = this.tda;

		int i, j, k;

		for (i = 0; i < size1; i++) {
			for (j = 0; j < size2; j++) {
				for (k = 0; k < 1; k++) {
					if (this.data.get((i * tda + j) * 1 + k) >= 0.0) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public boolean isNonNeg() {
		int size1 = this.size1;
		int size2 = this.size2;
		int tda = this.tda;

		int i, j, k;

		for (i = 0; i < size1; i++) {
			for (j = 0; j < size2; j++) {
				for (k = 0; k < 1; k++) {
					if (this.data.get((i * tda + j) * 1 + k) < 0.0) {
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer("gsl_matrix(\n");
		for (int i = 0; i < size1; i++) {
			if (i != 0) {
				result.append('\n');
			}
			for (int j = 0; j < size2; j++) {
				if (j != 0) {
					result.append(",\t");
				}
				double value = this.data.get((i * tda + j) * 1);
				result.append(value);
			}
		}
		result.append(")");
		return result.toString();
	}
}
