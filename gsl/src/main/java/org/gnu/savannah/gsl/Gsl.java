package org.gnu.savannah.gsl;

import org.gnu.savannah.gsl.util.DoubleArray;
import org.gnu.savannah.gsl.util.DoubleRef;
import org.gnu.savannah.gsl.util.IntegerRef;

public class Gsl {

	public static void cblas_daxpy(int N, double alpha, DoubleArray X,
			int incX, DoubleArray Y, int incY) {

		int i;

		if (alpha == 0.0) {
			return;
		}

		if (incX == 1 && incY == 1) {
			int m = N % 4;

			for (i = 0; i < m; i++) {
				Y.set(i, Y.get(i) + alpha * X.get(i));
			}

			for (i = m; i + 3 < N; i += 4) {
				Y.set(i, Y.get(i) + alpha * X.get(i));
				Y.set(i + 1, Y.get(i + 1) + alpha * X.get(i + 1));
				Y.set(i + 2, Y.get(i + 2) + alpha * X.get(i + 2));
				Y.set(i + 3, Y.get(i + 3) + alpha * X.get(i + 3));
			}
		} else {
			int ix = offset(N, incX);
			int iy = offset(N, incY);

			for (i = 0; i < N; i++) {
				Y.set(iy, Y.get(iy) + alpha * X.get(ix));
				ix += incX;
				iy += incY;
			}
		}
	}

	public static double cblas_ddot(int N, DoubleArray X, int incX,
			DoubleArray Y, int incY) {

		double r = 0.0;
		int i;
		int ix = offset(N, incX);
		int iy = offset(N, incY);

		for (i = 0; i < N; i++) {
			r += X.get(ix) * Y.get(iy);
			ix += incX;
			iy += incY;
		}

		return r;
	}
	public static void cblas_dgemm(CBLAS_ORDER Order, CBLAS_TRANSPOSE TransA,
			CBLAS_TRANSPOSE TransB, int M, int N, int K, double alpha,
			DoubleArray A, int lda, DoubleArray B, int ldb, double beta,
			DoubleArray C, int ldc) {

		int i, j, k;
		int n1, n2;
		int ldf, ldg;
		CBLAS_TRANSPOSE TransF, TransG;
		DoubleArray F, G;

		// CHECK_ARGS14(GEMM,Order,TransA,TransB,M,N,K,alpha,A,lda,B,ldb,beta,C,ldc);

		if (alpha == 0.0 && beta == 1.0)
			return;

		if (Order == CBLAS_ORDER.CblasRowMajor) {
			n1 = M;
			n2 = N;
			F = A;
			ldf = lda;
			TransF = (TransA == CBLAS_TRANSPOSE.CblasConjTrans) ? CBLAS_TRANSPOSE.CblasTrans
					: TransA;
			G = B;
			ldg = ldb;
			TransG = (TransB == CBLAS_TRANSPOSE.CblasConjTrans) ? CBLAS_TRANSPOSE.CblasTrans
					: TransB;
		} else {
			n1 = N;
			n2 = M;
			F = B;
			ldf = ldb;
			TransF = (TransB == CBLAS_TRANSPOSE.CblasConjTrans) ? CBLAS_TRANSPOSE.CblasTrans
					: TransB;
			G = A;
			ldg = lda;
			TransG = (TransA == CBLAS_TRANSPOSE.CblasConjTrans) ? CBLAS_TRANSPOSE.CblasTrans
					: TransA;
		}

		/* form y := beta*y */
		if (beta == 0.0) {
			for (i = 0; i < n1; i++) {
				for (j = 0; j < n2; j++) {
					C.set(ldc * i + j, 0.0);
				}
			}
		} else if (beta != 1.0) {
			for (i = 0; i < n1; i++) {
				for (j = 0; j < n2; j++) {
					C.set(ldc * i + j, C.get(ldc * i + j) * beta);
				}
			}
		}

		if (alpha == 0.0)
			return;

		if (TransF == CBLAS_TRANSPOSE.CblasNoTrans
				&& TransG == CBLAS_TRANSPOSE.CblasNoTrans) {

			/* form C := alpha*A*B + C */

			for (k = 0; k < K; k++) {
				for (i = 0; i < n1; i++) {
					double temp = alpha * F.get(ldf * i + k);
					if (temp != 0.0) {
						for (j = 0; j < n2; j++) {
							C.set(ldc * i + j,
									C.get(ldc * i + j) + temp
											* G.get(ldg * k + j));
						}
					}
				}
			}

		} else if (TransF == CBLAS_TRANSPOSE.CblasNoTrans
				&& TransG == CBLAS_TRANSPOSE.CblasTrans) {

			/* form C := alpha*A*B' + C */

			for (i = 0; i < n1; i++) {
				for (j = 0; j < n2; j++) {
					double temp = 0.0;
					for (k = 0; k < K; k++) {
						temp += F.get(ldf * i + k) * G.get(ldg * j + k);
					}
					C.set(ldc * i + j, C.get(ldc * i + j) + alpha * temp);
				}
			}

		} else if (TransF == CBLAS_TRANSPOSE.CblasTrans
				&& TransG == CBLAS_TRANSPOSE.CblasNoTrans) {

			for (k = 0; k < K; k++) {
				for (i = 0; i < n1; i++) {
					double temp = alpha * F.get(ldf * k + i);
					if (temp != 0.0) {
						for (j = 0; j < n2; j++) {
							C.set(ldc * i + j,
									C.get(ldc * i + j) + temp
											* G.get(ldg * k + j));
						}
					}
				}
			}

		} else if (TransF == CBLAS_TRANSPOSE.CblasTrans
				&& TransG == CBLAS_TRANSPOSE.CblasTrans) {

			for (i = 0; i < n1; i++) {
				for (j = 0; j < n2; j++) {
					double temp = 0.0;
					for (k = 0; k < K; k++) {
						temp += F.get(ldf * k + i) * G.get(ldg * j + k);
					}
					C.set(ldc * i + j, C.get(ldc * i + j) + alpha * temp);
				}
			}

		} else {

			throw new IllegalStateException("unrecognized operation");
		}
	}
	public static void cblas_dgemv(CBLAS_ORDER order, CBLAS_TRANSPOSE TransA,
			int M, int N, double alpha, DoubleArray A, int lda, DoubleArray X,
			int incX, double beta, DoubleArray Y, int incY) {
		// #define double double
		{
			int i, j;
			int lenX, lenY;

			CBLAS_TRANSPOSE Trans = (TransA != CBLAS_TRANSPOSE.CblasConjTrans) ? TransA
					: CBLAS_TRANSPOSE.CblasTrans;

			// deactivate check
			// CHECK_ARGS12(GEMV, order, TransA, M, N, alpha, A, lda, X, incX,
			// beta, Y, incY);

			if (M == 0 || N == 0)
				return;

			if (alpha == 0.0 && beta == 1.0)
				return;

			if (Trans == CBLAS_TRANSPOSE.CblasNoTrans) {
				lenX = N;
				lenY = M;
			} else {
				lenX = M;
				lenY = N;
			}

			/* form y := beta*y */
			if (beta == 0.0) {
				int iy = offset(lenY, incY);
				for (i = 0; i < lenY; i++) {
					Y.set(iy, 0.0);
					iy += incY;
				}
			} else if (beta != 1.0) {
				int iy = offset(lenY, incY);
				for (i = 0; i < lenY; i++) {
					Y.set(iy, Y.get(iy) * beta);
					iy += incY;
				}
			}

			if (alpha == 0.0)
				return;

			if ((order == CBLAS_ORDER.CblasRowMajor && Trans == CBLAS_TRANSPOSE.CblasNoTrans)
					|| (order == CBLAS_ORDER.CblasColMajor && Trans == CBLAS_TRANSPOSE.CblasTrans)) {
				/* form y := alpha*A*x + y */
				int iy = offset(lenY, incY);
				for (i = 0; i < lenY; i++) {
					double temp = 0.0;
					int ix = offset(lenX, incX);
					for (j = 0; j < lenX; j++) {
						temp += X.get(ix) * A.get(lda * i + j);
						ix += incX;
					}
					Y.set(iy, Y.get(iy) + (alpha * temp));
					iy += incY;
				}
			} else if ((order == CBLAS_ORDER.CblasRowMajor && Trans == CBLAS_TRANSPOSE.CblasTrans)
					|| (order == CBLAS_ORDER.CblasColMajor && Trans == CBLAS_TRANSPOSE.CblasNoTrans)) {
				/* form y := alpha*A'*x + y */
				int ix = offset(lenX, incX);
				for (j = 0; j < lenX; j++) {
					double temp = alpha * X.get(ix);
					if (temp != 0.0) {
						int iy = offset(lenY, incY);
						for (i = 0; i < lenY; i++) {
							Y.set(iy, Y.get(iy) + (temp * A.get(lda * j + i)));
							iy += incY;
						}
					}
					ix += incX;
				}
			} else {
				throw new UnsupportedOperationException(
						"unrecognized operation");
			}
		}
		// #undef double
	}
	public static double cblas_dnrm2(int N, DoubleArray X, int incX) {
		double scale = 0.0;
		double ssq = 1.0;
		int i;
		int ix = 0;
		if (N <= 0 || incX <= 0) {
			return 0;
		} else if (N == 1) {
			return Math.abs(X.get(0));
		}
		for (i = 0; i < N; i++) {
			double x = X.get(ix);
			if (x != 0.0) {
				double ax = Math.abs(x);

				if (scale < ax) {
					ssq = 1.0 + ssq * (scale / ax) * (scale / ax);
					scale = ax;
				} else {
					ssq += (ax / scale) * (ax / scale);
				}
			}
			ix += incX;
		}
		return scale * Math.sqrt(ssq);
	}

	public static void cblas_drot(int N, DoubleArray X, int incX,
			DoubleArray Y, int incY, double c, double s) {

		{
			int i;
			int ix = offset(N, incX);
			int iy = offset(N, incY);
			for (i = 0; i < N; i++) {
				double x = X.get(ix);
				double y = Y.get(iy);
				X.set(ix, c * x + s * y);
				Y.set(iy, -s * x + c * y);
				ix += incX;
				iy += incY;
			}
		}
	}

	public static void cblas_dscal(int N, double alpha, DoubleArray X, int incX) {

		int i;
		int ix = 0;

		if (incX <= 0) {
			return;
		}

		for (i = 0; i < N; i++) {
			X.set(ix, X.get(ix) * alpha);
			ix += incX;
		}
	}

	public static void cblas_dtrsv(CBLAS_ORDER order, CBLAS_UPLO Uplo,
			CBLAS_TRANSPOSE TransA, CBLAS_DIAG Diag, int N, DoubleArray A,
			int lda, DoubleArray X, int incX) {

		{
			boolean nonunit = (Diag == CBLAS_DIAG.CblasNonUnit);
			int ix, jx;
			int i, j;
			CBLAS_TRANSPOSE Trans = (TransA != CBLAS_TRANSPOSE.CblasConjTrans) ? TransA
					: CBLAS_TRANSPOSE.CblasTrans;

			// CHECK_ARGS9(TRSV,order,Uplo,TransA,Diag,N,A,lda,X,incX);

			if (N == 0)
				return;

			/* form x := inv( A )*x */

			if ((order == CBLAS_ORDER.CblasRowMajor
					&& Trans == CBLAS_TRANSPOSE.CblasNoTrans && Uplo == CBLAS_UPLO.CblasUpper)
					|| (order == CBLAS_ORDER.CblasColMajor
							&& Trans == CBLAS_TRANSPOSE.CblasTrans && Uplo == CBLAS_UPLO.CblasLower)) {
				/* backsubstitution */
				ix = offset(N, incX) + incX * (N - 1);
				if (nonunit) {
					X.set(ix, X.get(ix) / A.get(lda * (N - 1) + (N - 1)));
				}
				ix -= incX;
				for (i = N - 1; i > 0 && i-- != 0;) {
					double tmp = X.get(ix);
					jx = ix + incX;
					for (j = i + 1; j < N; j++) {
						double Aij = A.get(lda * i + j);
						tmp -= Aij * X.get(jx);
						jx += incX;
					}
					if (nonunit) {
						X.set(ix, tmp / A.get(lda * i + i));
					} else {
						X.set(ix, tmp);
					}
					ix -= incX;
				}
			} else if ((order == CBLAS_ORDER.CblasRowMajor
					&& Trans == CBLAS_TRANSPOSE.CblasNoTrans && Uplo == CBLAS_UPLO.CblasLower)
					|| (order == CBLAS_ORDER.CblasColMajor
							&& Trans == CBLAS_TRANSPOSE.CblasTrans && Uplo == CBLAS_UPLO.CblasUpper)) {

				/* forward substitution */
				ix = offset(N, incX);
				if (nonunit) {
					X.set(ix, X.get(ix) / A.get(lda * 0 + 0));
				}
				ix += incX;
				for (i = 1; i < N; i++) {
					double tmp = X.get(ix);
					jx = offset(N, incX);
					for (j = 0; j < i; j++) {
						double Aij = A.get(lda * i + j);
						tmp -= Aij * X.get(jx);
						jx += incX;
					}
					if (nonunit) {
						X.set(ix, tmp / A.get(lda * i + i));
					} else {
						X.set(ix, tmp);
					}
					ix += incX;
				}
			} else if ((order == CBLAS_ORDER.CblasRowMajor
					&& Trans == CBLAS_TRANSPOSE.CblasTrans && Uplo == CBLAS_UPLO.CblasUpper)
					|| (order == CBLAS_ORDER.CblasColMajor
							&& Trans == CBLAS_TRANSPOSE.CblasNoTrans && Uplo == CBLAS_UPLO.CblasLower)) {

				/* form x := inv( A' )*x */

				/* forward substitution */
				ix = offset(N, incX);
				if (nonunit) {
					X.set(ix, X.get(ix) / A.get(lda * 0 + 0));
				}
				ix += incX;
				for (i = 1; i < N; i++) {
					double tmp = X.get(ix);
					jx = offset(N, incX);
					for (j = 0; j < i; j++) {
						double Aji = A.get(lda * j + i);
						tmp -= Aji * X.get(jx);
						jx += incX;
					}
					if (nonunit) {
						X.set(ix, tmp / A.get(lda * i + i));
					} else {
						X.set(ix, tmp);
					}
					ix += incX;
				}
			} else if ((order == CBLAS_ORDER.CblasRowMajor
					&& Trans == CBLAS_TRANSPOSE.CblasTrans && Uplo == CBLAS_UPLO.CblasLower)
					|| (order == CBLAS_ORDER.CblasColMajor
							&& Trans == CBLAS_TRANSPOSE.CblasNoTrans && Uplo == CBLAS_UPLO.CblasUpper)) {

				/* backsubstitution */
				ix = offset(N, incX) + (N - 1) * incX;
				if (nonunit) {
					X.set(ix, X.get(ix) / A.get(lda * (N - 1) + (N - 1)));
				}
				ix -= incX;
				for (i = N - 1; i > 0 && i-- != 0;) {
					double tmp = X.get(ix);
					jx = ix + incX;
					for (j = i + 1; j < N; j++) {
						double Aji = A.get(lda * j + i);
						tmp -= Aji * X.get(jx);
						jx += incX;
					}
					if (nonunit) {
						X.set(ix, tmp / A.get(lda * i + i));
					} else {
						X.set(ix, tmp);
					}
					ix -= incX;
				}
			} else {
				throw new IllegalStateException("unrecognized operation");
			}

		}
	}

	private static void chase_out_intermediate_zero(GslVector d, GslVector f,
			GslMatrix U, int k0) {

		int n = d.size;
		DoubleRef c = new DoubleRef(), s = new DoubleRef();
		double x, y;
		int k;

		x = f.get(k0);
		y = d.get(k0 + 1);

		for (k = k0; k < n - 1; k++) {
			gsl_linalg_givens(y, -x, c, s);

			/* Compute U <= U G */

			{
				GslVectorView Uk0 = U.column(k0);
				GslVectorView Ukp1 = U.column(k + 1);
				gsl_blas_drot(Uk0.vector, Ukp1.vector, c.value, -s.value);
			}

			/* compute B <= G^T B */

			d.set(k + 1, s.value * x + c.value * y);

			if (k == k0)
				f.set(k, c.value * x - s.value * y);

			if (k < n - 2) {
				double z = f.get(k + 1);
				f.set(k + 1, c.value * z);

				x = -s.value * z;
				y = d.get(k + 2);
			}
		}
	}

	private static void chase_out_trailing_zero(GslVector d, GslVector f,
			GslMatrix V) {

		int n = d.size;
		DoubleRef c = new DoubleRef(), s = new DoubleRef();
		double x, y;
		int k;

		x = d.get(n - 2);
		y = f.get(n - 2);

		for (k = n - 1; k-- > 0;) {
			gsl_linalg_givens(x, y, c, s);

			/* Compute V <= V G where G = [c, s ; -s, c] */

			{
				GslVectorView Vp = V.column(k);
				GslVectorView Vq = V.column(n - 1);
				gsl_blas_drot(Vp.vector, Vq.vector, c.value, -s.value);
			}

			/* compute B <= B G */

			d.set(k, c.value * x - s.value * y);

			if (k == n - 2)
				f.set(k, s.value * x + c.value * y);

			if (k > 0) {
				double z = f.get(k - 1);
				f.set(k - 1, c.value * z);

				x = d.get(k - 1);
				y = s.value * z;
			}
		}
	}

	private static void chop_small_elements(GslVector d, GslVector f) {
		int N = d.size;
		double d_i = d.get(0);

		int i;

		for (i = 0; i < N - 1; i++) {
			double f_i = f.get(i);
			double d_ip1 = d.get(i + 1);

			if (Math.abs(f_i) < GSL_DBL_EPSILON
					* (Math.abs(d_i) + Math.abs(d_ip1))) {
				f.set(i, 0.0);
			}

			d_i = d_ip1;
		}

	}

	private static void create_schur(double d0, double f0, double d1,
			DoubleRef c, DoubleRef s) {
		double apq = 2.0 * d0 * f0;

		if (d0 == 0 || f0 == 0) {
			c.value = 1.0;
			s.value = 0.0;
			return;
		}

		/* Check if we need to rescale to avoid underflow/overflow */
		if (Math.abs(d0) < GSL_SQRT_DBL_MIN || Math.abs(d0) > GSL_SQRT_DBL_MAX
				|| Math.abs(f0) < GSL_SQRT_DBL_MIN
				|| Math.abs(f0) > GSL_SQRT_DBL_MAX
				|| Math.abs(d1) < GSL_SQRT_DBL_MIN
				|| Math.abs(d1) > GSL_SQRT_DBL_MAX) {
			double scale;
			IntegerRef d0_exp = new IntegerRef(), f0_exp = new IntegerRef();
			frexp(d0, d0_exp);
			frexp(f0, f0_exp);
			/* Bring |d0*f0| into the range GSL_DBL_MIN to GSL_DBL_MAX */
			scale = ldexp(1.0, -(d0_exp.value + f0_exp.value) / 4);
			d0 *= scale;
			f0 *= scale;
			d1 *= scale;
			apq = 2.0 * d0 * f0;
		}

		if (apq != 0.0) {
			double t;
			double tau = (f0 * f0 + (d1 + d0) * (d1 - d0)) / apq;

			if (tau >= 0.0) {
				t = 1.0 / (tau + Math.hypot(1.0, tau));
			} else {
				t = -1.0 / (-tau + Math.hypot(1.0, tau));
			}

			c.value = 1.0 / Math.hypot(1.0, t);
			s.value = t * (c.value);
		} else {
			c.value = 1.0;
			s.value = 0.0;
		}
	}

	private static double frexp(double x, IntegerRef e) {
		return gsl_frexp(x, e);
	}

	public static GslErrno gsl_blas_daxpy(double alpha, GslVector X,
			GslVector Y) {
		if (X.size == Y.size) {
			cblas_daxpy(X.size, alpha, X.data, X.stride, Y.data, Y.stride);
			return GslErrno.GSL_SUCCESS;
		} else {
			throw new IllegalStateException("invalid length");
		}
	}

	/*
	 * Factorise a general M x N matrix A into,
	 * 
	 * A = U D V^T
	 * 
	 * where U is a column-orthogonal M x N matrix (U^T U = I), D is a diagonal
	 * N x N matrix, and V is an N x N orthogonal matrix (V^T V = V V^T = I)
	 * 
	 * U is stored in the original matrix A, which has the same size
	 * 
	 * V is stored as a separate matrix (not V^T). You must take the transpose
	 * to form the product above.
	 * 
	 * The diagonal matrix D is stored in the vector S, D_ii = S_i
	 */

	public static GslErrno gsl_blas_ddot(GslVector X, GslVector Y,
			DoubleRef result) {
		if (X.size == Y.size) {
			result.value = cblas_ddot(X.size, X.data, X.stride, Y.data,
					Y.stride);
			return GslErrno.GSL_SUCCESS;
		} else {
			throw new IllegalStateException("invalid length");
		}
	}

	public static GslErrno gsl_blas_dgemm(CBLAS_TRANSPOSE TransA,
			CBLAS_TRANSPOSE TransB, double alpha, GslMatrix A, GslMatrix B,
			double beta, GslMatrix C) {
		int M = C.size1;
		int N = C.size2;
		int MA = (TransA == CBLAS_TRANSPOSE.CblasNoTrans) ? A.size1 : A.size2;
		int NA = (TransA == CBLAS_TRANSPOSE.CblasNoTrans) ? A.size2 : A.size1;
		int MB = (TransB == CBLAS_TRANSPOSE.CblasNoTrans) ? B.size1 : B.size2;
		int NB = (TransB == CBLAS_TRANSPOSE.CblasNoTrans) ? B.size2 : B.size1;

		if (M == MA && N == NB && NA == MB) /* [MxN] = [MAxNA][MBxNB] */
		{
			cblas_dgemm(CBLAS_ORDER.CblasRowMajor, TransA, TransB, M, N, NA,
					alpha, A.data, A.tda, B.data, B.tda, beta, C.data, C.tda);
			return GslErrno.GSL_SUCCESS;
		} else {
			throw new IllegalStateException("invalid length");
		}
	}

	public static GslErrno gsl_blas_dgemv(CBLAS_TRANSPOSE TransA,
			double alpha, GslMatrix A, GslVector X, double beta, GslVector Y) {
		int M = A.size1;
		int N = A.size2;

		if ((TransA == CBLAS_TRANSPOSE.CblasNoTrans && N == X.size && M == Y.size)
				|| (TransA == CBLAS_TRANSPOSE.CblasTrans && M == X.size && N == Y.size)) {
			cblas_dgemv(CBLAS_ORDER.CblasRowMajor, TransA, M, N,
					alpha, A.data, A.tda, X.data, X.stride, beta,
					Y.data, Y.stride);
			return GslErrno.GSL_SUCCESS;
		} else {
			throw new IllegalStateException("invalid length");
		}
	}

	public static double gsl_blas_dnrm2(GslVector X) {
		return cblas_dnrm2(X.size, X.data, X.stride);
	}

	public static GslErrno gsl_blas_drot(GslVector X, GslVector Y, double c,
			double s) {
		if (X.size == Y.size) {
			cblas_drot(X.size, X.data, X.stride, Y.data, Y.stride, c, s);
			return GslErrno.GSL_SUCCESS;
		} else {
			throw new IllegalStateException("invalid length");
		}
	}

	public static void gsl_blas_dscal(double alpha, GslVector X) {
		cblas_dscal(X.size, alpha, X.data, X.stride);
	}

	public static GslErrno gsl_blas_dtrsv(CBLAS_UPLO Uplo,
			CBLAS_TRANSPOSE TransA, CBLAS_DIAG Diag, GslMatrix A, GslVector X) {
		int M = A.size1;
		int N = A.size2;

		if (M != N) {
			throw new IllegalStateException("matrix must be square");
		} else if (N != X.size) {
			throw new IllegalStateException("invalid length");
		}

		cblas_dtrsv(CBLAS_ORDER.CblasRowMajor, Uplo, TransA, Diag, N, A.data,
				A.tda, X.data, X.stride);
		return GslErrno.GSL_SUCCESS;
	}

	/*
	 * remove off-diagonal elements which are neglegible compared with the
	 * neighboring diagonal elements
	 */

	private static double gsl_frexp(double x, IntegerRef e) {
		if (x == 0.0) {
			e.value = 0;
			return 0.0;
		} else if (!Double.isInfinite(x)) {
			e.value = 0;
			return x;
		} else if (Math.abs(x) >= 0.5 && Math.abs(x) < 1) /*
														 * Handle the common
														 * case
														 */
		{
			e.value = 0;
			return x;
		} else {
			double ex = Math.ceil(Math.log(Math.abs(x)) / M_LN2);
			int ei = (int) ex;
			double f;

			/* Prevent underflow and overflow of 2**(-ei) */
			if (ei < Double.MIN_EXPONENT)
				ei = Double.MIN_EXPONENT;

			if (ei > -Double.MIN_EXPONENT)
				ei = -Double.MIN_EXPONENT;

			f = x * Math.pow(2.0, -ei);

			if (!Double.isInfinite(f)) {
				/* This should not happen */
				e.value = 0;
				return f;
			}

			while (Math.abs(f) >= 1.0) {
				ei++;
				f /= 2.0;
			}

			while (Math.abs(f) > 0 && Math.abs(f) < 0.5) {
				ei--;
				f *= 2.0;
			}

			e.value = ei;
			return f;
		}
	}

	private static double gsl_ldexp(double x, int e) {
		IntegerRef ex = new IntegerRef();

		if (x == 0.0) {
			return x;
		}

		{
			double y = gsl_frexp(x, ex);
			double e2 = e + ex.value, p2;

			if (e2 >= Double.MAX_EXPONENT) {
				y *= Math.pow(2.0, e2 - Double.MAX_EXPONENT + 1);
				e2 = Double.MAX_EXPONENT - 1;
			} else if (e2 <= Double.MIN_EXPONENT) {
				y *= Math.pow(2.0, e2 - Double.MIN_EXPONENT - 1);
				e2 = Double.MIN_EXPONENT + 1;
			}

			p2 = Math.pow(2.0, e2);
			return y * p2;
		}
	}

	public static GslErrno gsl_linalg_bidiag_decomp(GslMatrix A,
			GslVector tau_U, GslVector tau_V) {
		if (A.size1 < A.size2) {
			throw new IllegalStateException(
					"bidiagonal decomposition requires M>=N");
		} else if (tau_U.size != A.size2) {
			throw new IllegalStateException("size of tau_U must be N");
		} else if (tau_V.size + 1 != A.size2) {
			throw new IllegalStateException("size of tau_V must be (N - 1)");
		} else {
			int M = A.size1;
			int N = A.size2;
			int i;

			for (i = 0; i < N; i++) {
				/* Apply Householder transformation to current column */

				{
					GslVectorView c = A.column(i);
					GslVectorView v = c.vector.subvector(i, M - i);
					double tau_i = gsl_linalg_householder_transform(v.vector);

					/* Apply the transformation to the remaining columns */

					if (i + 1 < N) {
						GslMatrixView m = A.submatrix(i, i + 1, M
								- i, N - (i + 1));
						gsl_linalg_householder_hm(tau_i, v.vector, m.matrix);
					}

					tau_U.set(i, tau_i);

				}

				/* Apply Householder transformation to current row */

				if (i + 1 < N) {
					GslVectorView r = A.row(i);
					GslVectorView v = r.vector.subvector(i + 1, N
							- (i + 1));
					double tau_i = gsl_linalg_householder_transform(v.vector);

					/* Apply the transformation to the remaining rows */

					if (i + 1 < M) {
						GslMatrixView m = A.submatrix(i + 1,
								i + 1, M - (i + 1), N - (i + 1));
						gsl_linalg_householder_mh(tau_i, v.vector, m.matrix);
					}

					tau_V.set(i, tau_i);
				}
			}
		}

		return GslErrno.GSL_SUCCESS;
	}

	public static GslErrno gsl_linalg_bidiag_unpack2(GslMatrix A,
			GslVector tau_U, GslVector tau_V, GslMatrix V) {
		int M = A.size1;
		int N = A.size2;

		int K = Math.min(M, N);

		if (M < N) {
			throw new IllegalStateException("matrix A must have M >= N");
		} else if (tau_U.size != K) {
			throw new IllegalStateException("size of tau must be MIN(M,N)");
		} else if (tau_V.size + 1 != K) {
			throw new IllegalStateException("size of tau must be MIN(M,N) - 1");
		} else if (V.size1 != N || V.size2 != N) {
			throw new IllegalStateException("size of V must be N x N");
		} else {
			int i, j;

			/* Initialize V to the identity */

			V.setIdentity();

			for (i = N - 1; i-- > 0;) {
				/* Householder row transformation to accumulate V */
				GslVectorView r = A.row(i);
				GslVectorView h = r.vector.subvector(i + 1, N
						- (i + 1));

				double ti = tau_V.get(i);

				GslMatrixView m = V.submatrix(i + 1, i + 1, N
						- (i + 1), N - (i + 1));

				gsl_linalg_householder_hm(ti, h.vector, m.matrix);
			}

			/* Copy superdiagonal into tau_v */

			for (i = 0; i < N - 1; i++) {
				double Aij = A.get(i, i + 1);
				tau_V.set(i, Aij);
			}

			/*
			 * Allow U to be unpacked into the same memory as A, copy diagonal
			 * into tau_U
			 */

			for (j = N; j-- > 0;) {
				/* Householder column transformation to accumulate U */
				double tj = tau_U.get(j);
				double Ajj = A.get(j, j);
				GslMatrixView m = A.submatrix(j, j, M - j, N - j);

				tau_U.set(j, Ajj);
				gsl_linalg_householder_hm1(tj, m.matrix);
			}

			return GslErrno.GSL_SUCCESS;
		}
	}

	/*
	 * Generate a Givens rotation (cos,sin) which takes v=(x,y) to (|v|,0) From
	 * Golub and Van Loan, "Matrix Computations", Section 5.1.8
	 */
	private static void gsl_linalg_givens(double a, double b, DoubleRef c,
			DoubleRef s) {
		if (b == 0) {
			c.value = 1;
			s.value = 0;
		} else if (Math.abs(b) > Math.abs(a)) {
			double t = -a / b;
			double s1 = 1.0 / Math.sqrt(1 + t * t);
			s.value = s1;
			c.value = s1 * t;
		} else {
			double t = -b / a;
			double c1 = 1.0 / Math.sqrt(1 + t * t);
			c.value = c1;
			s.value = c1 * t;
		}
	} /* gsl_linalg_givens() */

	public static GslErrno gsl_linalg_householder_hm(double tau, GslVector v,
			GslMatrix A) {
		/* applies a householder transformation v,tau to matrix m */

		if (tau == 0.0) {
			return GslErrno.GSL_SUCCESS;
		}

		{
			GslVectorView v1 = v.subvector(1, v.size - 1);
			GslMatrixView A1 = A.submatrix(1, 0, A.size1 - 1,
					A.size2);
			int j;

			for (j = 0; j < A.size2; j++) {
				DoubleRef wj = new DoubleRef(0.0);
				GslVectorView A1j = A1.matrix.column(j);
				gsl_blas_ddot(A1j.vector, v1.vector, wj);
				wj.value += A.get(0, j);

				{
					double A0j = A.get(0, j);
					A.set(0, j, A0j - tau * wj.value);
				}

				gsl_blas_daxpy(-tau * wj.value, v1.vector, A1j.vector);
			}
		}

		return GslErrno.GSL_SUCCESS;
	}

	public static GslErrno gsl_linalg_householder_hm1(double tau, GslMatrix A) {
		/*
		 * applies a householder transformation v,tau to a matrix being build up
		 * from the identity matrix, using the first column of A as a
		 * householder vector
		 */

		if (tau == 0) {
			int i, j;

			A.set(0, 0, 1.0);

			for (j = 1; j < A.size2; j++) {
				A.set(0, j, 0.0);
			}

			for (i = 1; i < A.size1; i++) {
				A.set(i, 0, 0.0);
			}

			return GslErrno.GSL_SUCCESS;
		}

		/* w = A' v */

		{
			GslMatrixView A1 = A.submatrix(1, 0, A.size1 - 1,
					A.size2);
			GslVectorView v1 = A1.matrix.column(0);
			int j;

			for (j = 1; j < A.size2; j++) {
				DoubleRef wj = new DoubleRef(0.0); /* A0j * v0 */

				GslVectorView A1j = A1.matrix.column(j);
				gsl_blas_ddot(A1j.vector, v1.vector, wj);

				/* A = A - tau v w' */

				A.set(0, j, -tau * wj.value);

				gsl_blas_daxpy(-tau * wj.value, v1.vector, A1j.vector);
			}

			gsl_blas_dscal(-tau, v1.vector);

			A.set(0, 0, 1.0 - tau);
		}

		return GslErrno.GSL_SUCCESS;
	}

	public static GslErrno gsl_linalg_householder_mh(double tau, GslVector v,
			GslMatrix A) {
		/*
		 * applies a householder transformation v,tau to matrix m from the right
		 * hand side in order to zero out rows
		 */

		if (tau == 0)
			return GslErrno.GSL_SUCCESS;

		/* A = A - tau w v' */

		{
			GslVectorView v1 = v.subvector(1, v.size - 1);
			GslMatrixView A1 = A.submatrix(0, 1, A.size1,
					A.size2 - 1);
			int i;

			for (i = 0; i < A.size1; i++) {
				DoubleRef wi = new DoubleRef(0.0);
				GslVectorView A1i = A1.matrix.row(i);
				gsl_blas_ddot(A1i.vector, v1.vector, wi);
				wi.value += A.get(i, 0);

				{
					double Ai0 = A.get(i, 0);
					A.set(i, 0, Ai0 - tau * wi.value);
				}

				gsl_blas_daxpy(-tau * wi.value, v1.vector, A1i.vector);
			}
		}

		return GslErrno.GSL_SUCCESS;
	}

	public static double gsl_linalg_householder_transform(GslVector v) {
		/*
		 * replace v[0:n-1] with a householder vector (v[0:n-1]) and coefficient
		 * tau that annihilate v[1:n-1]
		 */

		int n = v.size;

		if (n == 1) {
			return 0.0; /* tau = 0 */
		} else {
			double alpha, beta, tau;

			GslVectorView x = v.subvector(1, n - 1);

			double xnorm = gsl_blas_dnrm2(x.vector);

			if (xnorm == 0) {
				return 0.0; /* tau = 0 */
			}

			alpha = v.get(0);
			beta = -(alpha >= 0.0 ? +1.0 : -1.0) * Math.hypot(alpha, xnorm);
			tau = (beta - alpha) / beta;

			{
				double s = (alpha - beta);

				if (Math.abs(s) > GSL_DBL_MIN) {
					gsl_blas_dscal(1.0 / s, x.vector);
					v.set(0, beta);
				} else {
					gsl_blas_dscal(GSL_DBL_EPSILON / s, x.vector);
					gsl_blas_dscal(1.0 / GSL_DBL_EPSILON, x.vector);
					v.set(0, beta);
				}
			}

			return tau;
		}
	}

	/**
	 * Factorise a general N x N matrix A into,
	 *
	 * P A = L U
	 *
	 * where P is a permutation matrix, L is unit lower triangular and U is
	 * upper triangular.
	 *
	 * L is stored in the strict lower triangular part of the input matrix. The
	 * diagonal elements of L are unity and are not stored.
	 *
	 * U is stored in the diagonal and upper triangular part of the input
	 * matrix.
	 * 
	 * P is stored in the permutation p. Column j of P is column k of the
	 * identity matrix, where k = permutation.data[j]
	 *
	 * signum gives the sign of the permutation, (-1)^n, where n is the number
	 * of interchanges in the permutation.
	 *
	 * See Golub & Van Loan, Matrix Computations, Algorithm 3.4.1 (Gauss
	 * Elimination with Partial Pivoting).
	 */

	public static GslErrno gsl_linalg_LU_decomp(GslMatrix A,
			GslPermutation p, IntegerRef signum) {
		if (A.size1 != A.size2) {
			throw new IllegalStateException(
					"LU decomposition requires square matrix");
		} else if (p.size != A.size1) {
			throw new IllegalStateException(
					"permutation length must match matrix size");
		}
		int N = A.size1;
		int i, j, k;
		signum.value = 1;
		p.init();
		for (j = 0; j < N - 1; j++) {
			/* Find maximum in the j-th column */
			double ajj, max = Math.abs(A.get(j, j));
			int i_pivot = j;
			for (i = j + 1; i < N; i++) {
				double aij = Math.abs(A.get(i, j));
				if (aij > max) {
					max = aij;
					i_pivot = i;
				}
			}
			if (i_pivot != j) {
				A.swapRows(j, i_pivot);
				p.swap(j, i_pivot);
				signum.value = -(signum.value);
			}
			ajj = A.get(j, j);
			if (ajj != 0.0) {
				for (i = j + 1; i < N; i++) {
					double aij = A.get(i, j) / ajj;
					A.set(i, j, aij);

					for (k = j + 1; k < N; k++) {
						double aik = A.get(i, k);
						double ajk = A.get(j, k);
						A.set(i, k, aik - aij * ajk);
					}
				}
			}
		}
		return GslErrno.GSL_SUCCESS;
	}

	public static double gsl_linalg_LU_det(GslMatrix LU, int signum) {
		int i, n = LU.size1;
		double det = signum;
		for (i = 0; i < n; i++) {
			det *= LU.get(i, i);
		}
		return det;
	}

	public static GslErrno gsl_linalg_LU_invert(GslMatrix LU,
			GslPermutation p, GslMatrix inverse) {
		int i, n = LU.size1;

		GslErrno status = GslErrno.GSL_SUCCESS;

		if (singular(LU) != 0) {
			throw new IllegalStateException("matrix is singular");
		}

		inverse.setIdentity();

		for (i = 0; i < n; i++) {
			GslVectorView c = inverse.column(i);
			GslErrno status_i = gsl_linalg_LU_svx(LU, p, c.vector);

			if (status_i != GslErrno.GSL_SUCCESS)
				status = status_i;
		}

		return status;
	}

	public static GslErrno gsl_linalg_LU_svx(GslMatrix LU, GslPermutation p,
			GslVector x) {
		if (LU.size1 != LU.size2) {
			throw new IllegalStateException("LU matrix must be square");
		} else if (LU.size1 != p.size) {
			throw new IllegalStateException(
					"permutation length must match matrix size");
		} else if (LU.size1 != x.size) {
			throw new IllegalStateException(
					"matrix size must match solution/rhs size");
		} else if (singular(LU) != 0) {
			throw new IllegalStateException("matrix is singular");
		} else {
			/* Apply permutation to RHS */

			p.permuteVector(x);

			/* Solve for c using forward-substitution, L c = P b */

			gsl_blas_dtrsv(CBLAS_UPLO.CblasLower, CBLAS_TRANSPOSE.CblasNoTrans,
					CBLAS_DIAG.CblasUnit, LU, x);

			/* Perform back-substitution, U x = c */

			gsl_blas_dtrsv(CBLAS_UPLO.CblasUpper, CBLAS_TRANSPOSE.CblasNoTrans,
					CBLAS_DIAG.CblasNonUnit, LU, x);

			return GslErrno.GSL_SUCCESS;
		}
	}

	/*
	 * gsl_linalg_householder_transform() Compute a householder transformation
	 * (tau,v) of a vector x so that P x = [ I - tau*v*v' ] x annihilates
	 * x(1:n-1)
	 * 
	 * Inputs: v - on input, x vector on output, householder vector v
	 * 
	 * Notes: 1) on output, v is normalized so that v[0] = 1. The 1 is not
	 * actually stored; instead v[0] = -sign(x[0])*||x|| so that:
	 * 
	 * P x = v[0] * e_1
	 * 
	 * Therefore external routines should take care when applying the projection
	 * matrix P to vectors, taking into account that v[0] should be 1 when doing
	 * so.
	 */

	public static GslErrno gsl_linalg_SV_decomp(GslMatrix A, GslMatrix V,
			GslVector S, GslVector work) {
		int a, b, i, j, iter;

		int M = A.size1;
		int N = A.size2;
		int K = Math.min(M, N);

		if (M < N) {
			throw new IllegalStateException(
					"svd of MxN matrix, M<N, is not implemented");
		} else if (V.size1 != N) {
			throw new IllegalStateException(
					"square matrix V must match second dimension of matrix A");
		} else if (V.size1 != V.size2) {
			throw new IllegalStateException("matrix V must be square");
		} else if (S.size != N) {
			throw new IllegalStateException(
					"length of vector S must match second dimension of matrix A");
		} else if (work.size != N) {
			throw new IllegalStateException(
					"length of workspace must match second dimension of matrix A");
		}

		/* Handle the case of N = 1 (SVD of a column vector) */

		if (N == 1) {
			GslVectorView column = A.column(0);
			double norm = gsl_blas_dnrm2(column.vector);

			S.set(0, norm);
			V.set(0, 0, 1.0);

			if (norm != 0.0) {
				gsl_blas_dscal(1.0 / norm, column.vector);
			}

			return GslErrno.GSL_SUCCESS;
		}

		{
			GslVectorView f = work.subvector(0, K - 1);

			/* bidiagonalize matrix A, unpack A into U S V */

			gsl_linalg_bidiag_decomp(A, S, f.vector);
			gsl_linalg_bidiag_unpack2(A, S, f.vector, V);

			/* apply reduction steps to B=(S,Sd) */

			chop_small_elements(S, f.vector);

			/* Progressively reduce the matrix until it is diagonal */

			b = N - 1;
			iter = 0;

			while (b > 0) {
				double fbm1 = f.vector.get(b - 1);

				if (fbm1 == 0.0 || Double.isNaN(fbm1)) {
					b--;
					continue;
				}

				/*
				 * Find the largest unreduced block (a,b) starting from b and
				 * working backwards
				 */

				a = b - 1;

				while (a > 0) {
					double fam1 = f.vector.get(a - 1);

					if (fam1 == 0.0 || Double.isNaN(fam1)) {
						break;
					}

					a--;
				}

				iter++;

				if (iter > 100 * N) {
					throw new IllegalStateException(
							"SVD decomposition failed to converge");
				}

				{
					int n_block = b - a + 1;
					GslVectorView S_block = S
							.subvector(a, n_block);
					GslVectorView f_block = f.vector.subvector(a,
							n_block - 1);

					GslMatrixView U_block = A.submatrix(0, a,
							A.size1, n_block);
					GslMatrixView V_block = V.submatrix(0, a,
							V.size1, n_block);

					int rescale = 0;
					double scale = 1;
					double norm = 0;

					/*
					 * Find the maximum absolute values of the diagonal and
					 * subdiagonal
					 */

					for (i = 0; i < n_block; i++) {
						double s_i = S_block.vector.get(i);
						double a1 = Math.abs(s_i);
						if (a1 > norm)
							norm = a1;
					}

					for (i = 0; i < n_block - 1; i++) {
						double f_i = f_block.vector.get(i);
						double a1 = Math.abs(f_i);
						if (a1 > norm)
							norm = a1;
					}

					/* Temporarily scale the submatrix if necessary */

					if (norm > GSL_SQRT_DBL_MAX) {
						scale = (norm / GSL_SQRT_DBL_MAX);
						rescale = 1;
					} else if (norm < GSL_SQRT_DBL_MIN && norm > 0) {
						scale = (norm / GSL_SQRT_DBL_MIN);
						rescale = 1;
					}

					if (rescale != 0) {
						gsl_blas_dscal(1.0 / scale, S_block.vector);
						gsl_blas_dscal(1.0 / scale, f_block.vector);
					}

					/* Perform the implicit QR step */

					qrstep(S_block.vector, f_block.vector, U_block.matrix,
							V_block.matrix);
					/* remove any small off-diagonal elements */

					chop_small_elements(S_block.vector, f_block.vector);

					/* Undo the scaling if needed */

					if (rescale != 0) {
						gsl_blas_dscal(scale, S_block.vector);
						gsl_blas_dscal(scale, f_block.vector);
					}
				}

			}
		}

		/* Make singular values positive by reflections if necessary */

		for (j = 0; j < K; j++) {
			double Sj = S.get(j);

			if (Sj < 0.0) {
				for (i = 0; i < N; i++) {
					double Vij = V.get(i, j);
					V.set(i, j, -Vij);
				}

				S.set(j, -Sj);
			}
		}

		/* Sort singular values into decreasing order */

		for (i = 0; i < K; i++) {
			double S_max = S.get(i);
			int i_max = i;

			for (j = i + 1; j < K; j++) {
				double Sj = S.get(j);

				if (Sj > S_max) {
					S_max = Sj;
					i_max = j;
				}
			}

			if (i_max != i) {
				/* swap eigenvalues */
				S.swapElements(i, i_max);

				/* swap eigenvectors */
				A.swapColumns(i, i_max);
				V.swapColumns(i, i_max);
			}
		}

		return GslErrno.GSL_SUCCESS;
	}

	private static double ldexp(double x, int e) {
		return gsl_ldexp(x, e);
	}

	private static int offset(int N, int incX) {
		return ((incX) > 0 ? 0 : ((N) - 1) * (-(incX)));

	}

	private static void qrstep(GslVector d, GslVector f, GslMatrix U,
			GslMatrix V) {
		int n = d.size;
		double y, z;
		@SuppressWarnings("unused")
		double ak, bk, zk, ap, bp, aq, bq;
		int i, k;

		if (n == 1)
			return; /* shouldn't happen */

		/* Compute 2x2 svd directly */

		if (n == 2) {
			svd2(d, f, U, V);
			return;
		}

		/* Chase out any zeroes on the diagonal */

		for (i = 0; i < n - 1; i++) {
			double d_i = d.get(i);

			if (d_i == 0.0) {
				chase_out_intermediate_zero(d, f, U, i);
				return;
			}
		}

		/* Chase out any zero at the end of the diagonal */

		{
			double d_nm1 = d.get(n - 1);

			if (d_nm1 == 0.0) {
				chase_out_trailing_zero(d, f, V);
				return;
			}
		}

		/* Apply QR reduction steps to the diagonal and offdiagonal */

		{
			double d0 = d.get(0);
			double f0 = f.get(0);

			double d1 = d.get(1);
			double f1 = f.get(1);

			{
				double mu = trailing_eigenvalue(d, f);

				y = d0 * d0 - mu;
				z = d0 * f0;
			}

			/* Set up the recurrence for Givens rotations on a bidiagonal matrix */

			ak = 0;
			bk = 0;

			ap = d0;
			bp = f0;

			aq = d1;
			bq = f1;
		}

		for (k = 0; k < n - 1; k++) {
			DoubleRef c = new DoubleRef(), s = new DoubleRef();
			gsl_linalg_givens(y, z, c, s);

			/* Compute V <= V G */

			{
				GslVectorView Vk = V.column(k);
				GslVectorView Vkp1 = V.column(k + 1);
				gsl_blas_drot(Vk.vector, Vkp1.vector, c.value, -s.value);
			}

			/* compute B <= B G */

			{
				double bk1 = c.value * bk - s.value * z;

				double ap1 = c.value * ap - s.value * bp;
				double bp1 = s.value * ap + c.value * bp;
				double zp1 = -s.value * aq;

				double aq1 = c.value * aq;

				if (k > 0) {
					f.set(k - 1, bk1);
				}

				ak = ap1;
				bk = bp1;
				zk = zp1;

				ap = aq1;

				if (k < n - 2) {
					bp = f.get(k + 1);
				} else {
					bp = 0.0;
				}

				y = ak;
				z = zk;
			}

			gsl_linalg_givens(y, z, c, s);

			/* Compute U <= U G */

			{
				GslVectorView Uk = U.column(k);
				GslVectorView Ukp1 = U.column(k + 1);
				gsl_blas_drot(Uk.vector, Ukp1.vector, c.value, -s.value);
			}

			/* compute B <= G^T B */

			{
				double ak1 = c.value * ak - s.value * zk;
				double bk1 = c.value * bk - s.value * ap;
				double zk1 = -s.value * bp;

				double ap1 = s.value * bk + c.value * ap;
				double bp1 = c.value * bp;

				d.set(k, ak1);

				ak = ak1;
				bk = bk1;
				zk = zk1;

				ap = ap1;
				bp = bp1;

				if (k < n - 2) {
					aq = d.get(k + 2);
				} else {
					aq = 0.0;
				}

				y = bk;
				z = zk;
			}
		}

		f.set(n - 2, bk);
		d.set(n - 1, ap);
	}

	public static int singular(GslMatrix LU) {
		int i, n = LU.size1;

		for (i = 0; i < n; i++) {
			double u = LU.get(i, i);
			if (u == 0)
				return 1;
		}

		return 0;
	}

	private static void svd2(GslVector d, GslVector f, GslMatrix U,
			GslMatrix V) {
		int i;
		DoubleRef c = new DoubleRef(), s = new DoubleRef();
		double a11, a12, a21, a22;

		int M = U.size1;
		int N = V.size1;

		double d0 = d.get(0);
		double f0 = f.get(0);

		double d1 = d.get(1);

		if (d0 == 0.0) {
			/* Eliminate off-diagonal element in [0,f0;0,d1] to make [d,0;0,0] */

			gsl_linalg_givens(f0, d1, c, s);

			/* compute B <= G^T B X, where X = [0,1;1,0] */

			d.set(0, c.value * f0 - s.value * d1);
			f.set(0, s.value * f0 + c.value * d1);
			d.set(1, 0.0);

			/* Compute U <= U G */

			for (i = 0; i < M; i++) {
				double Uip = U.get(i, 0);
				double Uiq = U.get(i, 1);
				U.set(i, 0, c.value * Uip - s.value * Uiq);
				U.set(i, 1, s.value * Uip + c.value * Uiq);
			}

			/* Compute V <= V X */

			V.swapColumns(0, 1);

			return;
		} else if (d1 == 0.0) {
			/* Eliminate off-diagonal element in [d0,f0;0,0] */

			gsl_linalg_givens(d0, f0, c, s);

			/* compute B <= B G */

			d.set(0, d0 * c.value - f0 * s.value);
			f.set(0, 0.0);

			/* Compute V <= V G */

			for (i = 0; i < N; i++) {
				double Vip = V.get(i, 0);
				double Viq = V.get(i, 1);
				V.set(i, 0, c.value * Vip - s.value * Viq);
				V.set(i, 1, s.value * Vip + c.value * Viq);
			}

			return;
		} else {
			/* Make columns orthogonal, A = [d0, f0; 0, d1] * G */

			create_schur(d0, f0, d1, c, s);

			/* compute B <= B G */

			a11 = c.value * d0 - s.value * f0;
			a21 = -s.value * d1;

			a12 = s.value * d0 + c.value * f0;
			a22 = c.value * d1;

			/* Compute V <= V G */

			for (i = 0; i < N; i++) {
				double Vip = V.get(i, 0);
				double Viq = V.get(i, 1);
				V.set(i, 0, c.value * Vip - s.value * Viq);
				V.set(i, 1, s.value * Vip + c.value * Viq);
			}

			/*
			 * Eliminate off-diagonal elements, bring column with largest norm
			 * to first column
			 */

			if (Math.hypot(a11, a21) < Math.hypot(a12, a22)) {
				double t1, t2;

				/* B <= B X */

				t1 = a11;
				a11 = a12;
				a12 = t1;
				t2 = a21;
				a21 = a22;
				a22 = t2;

				/* V <= V X */

				V.swapColumns(0, 1);
			}

			gsl_linalg_givens(a11, a21, c, s);

			/* compute B <= G^T B */

			d.set(0, c.value * a11 - s.value * a21);
			f.set(0, c.value * a12 - s.value * a22);
			d.set(1, s.value * a12 + c.value * a22);

			/* Compute U <= U G */

			for (i = 0; i < M; i++) {
				double Uip = U.get(i, 0);
				double Uiq = U.get(i, 1);
				U.set(i, 0, c.value * Uip - s.value * Uiq);
				U.set(i, 1, s.value * Uip + c.value * Uiq);
			}

			return;
		}
	}

	private static double trailing_eigenvalue(GslVector d, GslVector f) {
		int n = d.size;

		double da = d.get(n - 2);
		double db = d.get(n - 1);
		double fa = (n > 2) ? f.get(n - 3) : 0.0;
		double fb = f.get(n - 2);

		double mu;

		{
			/*
			 * We can compute mu more accurately than using the formula above
			 * since we know the roots cannot be negative. This also avoids the
			 * possibility of NaNs in the formula above.
			 * 
			 * The matrix is [ da^2 + fa^2, da fb ; da fb , db^2 + fb^2 ] and mu
			 * is the eigenvalue closest to the bottom right element.
			 */

			double ta = da * da + fa * fa;
			double tb = db * db + fb * fb;
			double tab = da * fb;

			double dt = (ta - tb) / 2.0;

			double S = ta + tb;
			double da2 = da * da, db2 = db * db;
			double fa2 = fa * fa, fb2 = fb * fb;
			double P = (da2 * db2) + (fa2 * db2) + (fa2 * fb2);
			double D = Math.hypot(dt, tab);
			double r1 = S / 2 + D;

			if (dt >= 0) {
				/* tb < ta, choose smaller root */
				mu = (r1 > 0) ? P / r1 : 0.0;
			} else {
				/* tb > ta, choose larger root */
				mu = r1;
			}
		}

		return mu;
	}

	public static double GSL_SQRT_DBL_MAX = 1.3407807929942596e+154d;

	public static double GSL_SQRT_DBL_MIN = 1.4916681462400413e-154d;

	public static double GSL_DBL_EPSILON = 2.2204460492503131e-16d;

	public static double M_LN2 = 0.69314718055994530941723212146d; /* ln(2) */

	public static double GSL_DBL_MIN = 2.2250738585072014e-308d;
}
