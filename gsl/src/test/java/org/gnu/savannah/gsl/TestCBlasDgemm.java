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
import org.junit.Test;

public class TestCBlasDgemm extends TestGsl {

    double flteps = 1e-4, dbleps = 1e-6;

    @Test
    public void test_dgemm1()

    {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE transA = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_TRANSPOSE transB = CBLAS_TRANSPOSE.valueOf(111);
        int M = 1;
        int N = 2;
        int K = 4;
        double alpha = 0;
        double beta = 0;
        DoubleArray A = new DoubleArray(0.017, 0.191, 0.863, -0.97);
        int lda = 4;
        DoubleArray B = new DoubleArray(-0.207, -0.916, -0.278, 0.403, 0.885, 0.409, -0.772, -0.27);
        int ldb = 2;
        DoubleArray C = new DoubleArray(-0.274, -0.858);
        int ldc = 2;
        DoubleArray C_expected = new DoubleArray(0.0, 0.0);
        Gsl.cblas_dgemm(order, transA, transB, M, N, K, alpha, A, lda, B, ldb, beta, C, ldc);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(C.get(i), C_expected.get(i), dbleps, "dgemm(case 1474)");
            }
        }
    }

    @Test
    public void test_dgemm8() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE transA = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_TRANSPOSE transB = CBLAS_TRANSPOSE.valueOf(111);
        int M = 1;
        int N = 2;
        int K = 4;
        double alpha = 0;
        double beta = 0;
        DoubleArray A = new DoubleArray(0.571, 0.081, 0.109, 0.988);
        int lda = 1;
        DoubleArray B = new DoubleArray(-0.048, -0.753, -0.8, -0.89, -0.535, -0.017, -0.018, -0.544);
        int ldb = 4;
        DoubleArray C = new DoubleArray(-0.876, -0.792);
        int ldc = 1;
        DoubleArray C_expected = new DoubleArray(0.0, 0.0);
        Gsl.cblas_dgemm(order, transA, transB, M, N, K, alpha, A, lda, B, ldb, beta, C, ldc);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(C.get(i), C_expected.get(i), dbleps, "dgemm(case 1475)");
            }
        }
        ;
    }

    @Test
    public void test_dgemm2() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE transA = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_TRANSPOSE transB = CBLAS_TRANSPOSE.valueOf(112);
        int M = 1;
        int N = 2;
        int K = 4;
        double alpha = -0.3;
        double beta = 1;
        DoubleArray A = new DoubleArray(0.939, 0.705, 0.977, 0.4);
        int lda = 4;
        DoubleArray B = new DoubleArray(-0.089, -0.822, 0.937, 0.159, 0.789, -0.413, -0.172, 0.88);
        int ldb = 4;
        DoubleArray C = new DoubleArray(-0.619, 0.063);
        int ldc = 2;
        DoubleArray C_expected = new DoubleArray(-0.7137904, -0.1270986);
        Gsl.cblas_dgemm(order, transA, transB, M, N, K, alpha, A, lda, B, ldb, beta, C, ldc);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(C.get(i), C_expected.get(i), dbleps, "dgemm(case 1476)");
            }
        }
        ;
    }

    @Test
    public void test_dgemm3() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE transA = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_TRANSPOSE transB = CBLAS_TRANSPOSE.valueOf(112);
        int M = 1;
        int N = 2;
        int K = 4;
        double alpha = -0.3;
        double beta = 1;
        DoubleArray A = new DoubleArray(-0.795, 0.81, 0.388, 0.09);
        int lda = 1;
        DoubleArray B = new DoubleArray(-0.847, 0.031, -0.938, 0.09, -0.286, -0.478, -0.981, 0.881);
        int ldb = 2;
        DoubleArray C = new DoubleArray(-0.242, -0.02);
        int ldc = 1;
        DoubleArray C_expected = new DoubleArray(-0.1562981, -0.0026243);
        Gsl.cblas_dgemm(order, transA, transB, M, N, K, alpha, A, lda, B, ldb, beta, C, ldc);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(C.get(i), C_expected.get(i), dbleps, "dgemm(case 1477)");
            }
        }
        ;
    }

    @Test
    public void test_dgemm4() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE transA = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_TRANSPOSE transB = CBLAS_TRANSPOSE.valueOf(111);
        int M = 1;
        int N = 2;
        int K = 4;
        double alpha = -1;
        double beta = 0;
        DoubleArray A = new DoubleArray(-0.556, 0.532, 0.746, 0.673);
        int lda = 1;
        DoubleArray B = new DoubleArray(-0.525, 0.967, 0.687, -0.024, 0.527, 0.485, 0.109, -0.46);
        int ldb = 2;
        DoubleArray C = new DoubleArray(-0.495, 0.859);
        int ldc = 2;
        DoubleArray C_expected = new DoubleArray(-1.123883, 0.49819);
        Gsl.cblas_dgemm(order, transA, transB, M, N, K, alpha, A, lda, B, ldb, beta, C, ldc);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(C.get(i), C_expected.get(i), dbleps, "dgemm(case 1478)");
            }
        }
        ;
    }

    @Test
    public void test_dgemm5() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE transA = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_TRANSPOSE transB = CBLAS_TRANSPOSE.valueOf(111);
        int M = 1;
        int N = 2;
        int K = 4;
        double alpha = -1;
        double beta = 0;
        DoubleArray A = new DoubleArray(-0.358, 0.224, -0.941, 0.513);
        int lda = 4;
        DoubleArray B = new DoubleArray(-0.201, -0.159, -0.586, -0.016, -0.324, 0.411, 0.115, -0.229);
        int ldb = 4;
        DoubleArray C = new DoubleArray(0.558, 0.596);
        int ldc = 1;
        DoubleArray C_expected = new DoubleArray(-0.57956, 0.017636);
        Gsl.cblas_dgemm(order, transA, transB, M, N, K, alpha, A, lda, B, ldb, beta, C, ldc);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(C.get(i), C_expected.get(i), dbleps, "dgemm(case 1479)");
            }
        }
        ;
    }

    @Test
    public void test_dgemm6() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE transA = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_TRANSPOSE transB = CBLAS_TRANSPOSE.valueOf(112);
        int M = 1;
        int N = 2;
        int K = 4;
        double alpha = -0.3;
        double beta = 1;
        DoubleArray A = new DoubleArray(-0.586, 0.809, 0.709, -0.524);
        int lda = 1;
        DoubleArray B = new DoubleArray(0.768, 0.7, 0.619, -0.478, -0.129, -0.778, -0.432, 0.454);
        int ldb = 4;
        DoubleArray C = new DoubleArray(0.042, 0.252);
        int ldc = 2;
        DoubleArray C_expected = new DoubleArray(-0.1996785, 0.5813976);
        Gsl.cblas_dgemm(order, transA, transB, M, N, K, alpha, A, lda, B, ldb, beta, C, ldc);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(C.get(i), C_expected.get(i), dbleps, "dgemm(case 1480)");
            }
        }
        ;
    }

    @Test
    public void test_dgemm7() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE transA = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_TRANSPOSE transB = CBLAS_TRANSPOSE.valueOf(112);
        int M = 1;
        int N = 2;
        int K = 4;
        double alpha = -0.3;
        double beta = 1;
        DoubleArray A = new DoubleArray(-0.164, 0.522, 0.948, -0.624);
        int lda = 4;
        DoubleArray B = new DoubleArray(-0.142, 0.778, 0.359, 0.622, -0.637, -0.757, -0.282, -0.805);
        int ldb = 2;
        DoubleArray C = new DoubleArray(-0.09, 0.183);
        int ldc = 1;
        DoubleArray C_expected = new DoubleArray(-0.0248334, 0.1884672);
        Gsl.cblas_dgemm(order, transA, transB, M, N, K, alpha, A, lda, B, ldb, beta, C, ldc);
        {
            int i;
            for (i = 0; i < 2; i++) {
                gsl_test_rel(C.get(i), C_expected.get(i), dbleps, "dgemm(case 1481)");
            }
        }
        ;
    }

}
