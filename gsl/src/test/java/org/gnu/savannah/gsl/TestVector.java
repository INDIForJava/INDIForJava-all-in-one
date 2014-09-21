package org.gnu.savannah.gsl;

import org.gnu.savannah.gsl.util.DoubleArray;
import org.junit.Test;

public class TestVector extends TestGsl {

	@Test
	public void main_test() {
		int stride, N;

		for (N = 10; N < 1024; N = 2 * N + 1) {
			for (stride = 1; stride < 5; stride++) {

				test_func(stride, N);
			}
		}

	}

	void test_func(int stride, int N) {
		GslVector v0;
		GslVector v;
		GslVectorView view;

		int i;

		if (stride == 1) {
			v = new GslVector(N);

			gsl_test(v.data == null, "_alloc pointer");
			gsl_test(v.size != N, "_alloc size");
			gsl_test(v.stride != 1, "_alloc stride");

			/* free whatever is in v */
		}

		if (stride == 1) {
			v0 = new GslVector(N);
			view = v0.subvector(0, N);
			v = view.vector;
		} else {
			v0 = new GslVector(N * stride);

			for (i = 0; i < N * stride; i++) {
				v0.data.set(i, i);
			}

			view = v0.subvectorWithStride(0, stride, N);
			v = view.vector;
		}

		{
			boolean status = false;

			for (i = 0; i < N; i++) {
				v.set(i, (double) i);
			}

			for (i = 0; i < N; i++) {
				if (v.data.get(i * stride) != (double) (i))
					status = true;
			}

			gsl_test(status, "_set" + "gsl_vector" + " writes into array");
		}

		{
			boolean status = true;

			for (i = 0; i < N; i++) {
				if (v.get(i) != (double) (i))
					status = false;
			}

			gsl_test(!status, "_get" + "gsl_vector" + " reads from array");
		}

		{
			boolean status = true;

			for (i = 0; i < N; i++) {
				if (v.ptr(i).equals(
						new DoubleArray(v.data, i * stride)))
					status = false;
			}

			gsl_test(status, "_ptr" + "gsl_vector" + " access to array");
		}

		{
			boolean status = true;

			for (i = 0; i < N; i++) {
				v.set(i, (double) 0);
			}

			status = (!v.isNull());
			gsl_test(status, "_isnull" + "gsl_vector" + " on null vector");

			status = (v.isPos());
			gsl_test(status, "_ispos" + "gsl_vector" + " on null vector");

			status = (v.isNeg());
			gsl_test(status, "_isneg" + "gsl_vector" + " on null vector");

			status = (!v.isNonNeg());
			gsl_test(status, "_isnonneg" + "gsl_vector" + " on null vector");

		}

		{
			boolean status = true;

			for (i = 0; i < N; i++) {
				v.set(i, (double) (i % 10));
			}

			status = (v.isNull());
			gsl_test(status, "_isnull" + "gsl_vector"
					+ " on non-negative vector");

			status = (v.isPos());
			gsl_test(status, "_ispos" + "gsl_vector"
					+ " on non-negative vector");

			status = (v.isNeg());
			gsl_test(status, "_isneg" + "gsl_vector"
					+ " on non-negative vector");

			status = (!v.isNonNeg());
			gsl_test(status, "_isnonneg" + "gsl_vector"
					+ " on non-negative vector");
		}

		{
			boolean status = false;

			for (i = 0; i < N; i++) {
				double vi = (i % 10) - (double) 5;
				v.set(i, vi);
			}

			status = (v.isNull());
			gsl_test(status, "_isnull" + "gsl_vector" + " on mixed vector");

			status = (v.isPos());
			gsl_test(status, "_ispos" + "gsl_vector" + " on mixed vector");

			status = (v.isNeg());
			gsl_test(status, "_isneg" + "gsl_vector" + " on mixed vector");

			status = (v.isNonNeg());
			gsl_test(status, "_isnonneg" + "gsl_vector" + " on mixed vector");
		}

		{
			boolean status = false;

			for (i = 0; i < N; i++) {
				v.set(i, -(double) (i % 10));
			}

			status = (v.isNull());
			gsl_test(status, "_isnull" + "gsl_vector"
					+ " on non-positive vector");

			status = (v.isPos());
			gsl_test(status, "_ispos" + "gsl_vector"
					+ " on non-positive vector");

			status = (v.isNeg());
			gsl_test(status, "_isneg" + "gsl_vector"
					+ " on non-positive non-null vector");

			status = (v.isNonNeg());
			gsl_test(status, "_isnonneg" + "gsl_vector"
					+ " on non-positive non-null vector");
		}

		{
			boolean status = false;

			for (i = 0; i < N; i++) {
				v.set(i, (double) (i % 10 + 1));
			}

			status = (v.isNull());
			gsl_test(status, "_isnull" + "gsl_vector" + " on positive vector");

			status = (!v.isPos());
			gsl_test(status, "_ispos" + "gsl_vector" + " on positive vector");

			status = (v.isNeg());
			gsl_test(status, "_isneg" + "gsl_vector" + " on positive vector");

			status = (!v.isNonNeg());
			gsl_test(status, "_isnonneg" + "gsl_vector" + " on positive vector");
		}

		{
			boolean status = false;

			v.setZero();

			for (i = 0; i < N; i++) {
				if (v.get(i) != (double) 0)
					status = true;
			}

			gsl_test(status, "_setzero" + "gsl_vector" + " on non-null vector");
		}

		{
			boolean status = false;

			for (i = 0; i < N; i++) {
				v.set(i, (double) i);
			}

			v.swapElements(2, 5);

			status = (v.get(2) != 5);
			status |= (v.get(5) != 2);

			v.swapElements(2, 5);

			status |= (v.get(2) != 2);
			status |= (v.get(5) != 5);

			gsl_test(status, "_swap_elements" + "gsl_vector" + " (2,5)");
		}

		{
			boolean status = false;

			GslVectorView v1 = v.subvector(N / 3, N / 2);

			for (i = 0; i < N / 2; i++) {
				if (v1.vector.get(i) != v
						.get((N / 3) + i))
					status = true;
			}

			gsl_test(status, "_view_subvector" + "gsl_vector");
		}
	}
}
