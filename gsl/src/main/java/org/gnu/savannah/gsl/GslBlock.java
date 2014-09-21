package org.gnu.savannah.gsl;

import org.gnu.savannah.gsl.util.DoubleArray;

public class GslBlock {
	public GslBlock(int n) {
		if (n == 0) {
			throw new IllegalStateException(
					"block length n must be positive integer");
		}
		data = new DoubleArray(n);
		size = n;
	}

	int size;
	DoubleArray data;
}
