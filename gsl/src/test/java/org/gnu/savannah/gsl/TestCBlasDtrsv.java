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

public class TestCBlasDtrsv extends TestGsl {

    final double flteps = 1e-4, dbleps = 1e-6;

    @Test
    public void test_trsv_1() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(121);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(131);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(-0.21);
        DoubleArray X = new DoubleArray(0.473);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(-2.25238095238);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1166)");
            }
        }
    }

    @Test
    public void test_trsv_2() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(121);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(132);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(-0.21);
        DoubleArray X = new DoubleArray(0.473);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(0.473);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1167)");
            }
        }
    }

    @Test
    public void test_trsv_3() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(122);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(131);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(-0.21);
        DoubleArray X = new DoubleArray(0.473);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(-2.25238095238);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1168)");
            }
        }
    }

    @Test
    public void test_trsv_16() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(122);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(132);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(-0.21);
        DoubleArray X = new DoubleArray(0.473);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(0.473);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1169)");
            }
        }
    }

    @Test
    public void test_trsv_4() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(121);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(131);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(-0.21);
        DoubleArray X = new DoubleArray(0.473);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(-2.25238095238);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1170)");
            }
        }
    }

    @Test
    public void test_trsv_5() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(121);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(132);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(-0.21);
        DoubleArray X = new DoubleArray(0.473);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(0.473);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1171)");
            }
        }
    }

    @Test
    public void test_trsv_6() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(122);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(131);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(-0.21);
        DoubleArray X = new DoubleArray(0.473);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(-2.25238095238);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1172)");
            }
        }
    }

    @Test
    public void test_trsv_7() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(111);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(122);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(132);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(-0.21);
        DoubleArray X = new DoubleArray(0.473);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(0.473);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1173)");
            }
        }
    }

    @Test
    public void test_trsv_8() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(121);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(131);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(0.748);
        DoubleArray X = new DoubleArray(0.979);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(1.30882352941);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1174)");
            }
        }
    }

    @Test
    public void test_trsv_9() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(121);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(132);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(0.748);
        DoubleArray X = new DoubleArray(0.979);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(0.979);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1175)");
            }
        }
    }

    @Test
    public void test_trsv_10() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(122);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(131);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(0.748);
        DoubleArray X = new DoubleArray(0.979);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(1.30882352941);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1176)");
            }
        }
    }

    @Test
    public void test_trsv_11() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(101);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(122);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(132);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(0.748);
        DoubleArray X = new DoubleArray(0.979);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(0.979);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1177)");
            }
        }
    }

    @Test
    public void test_trsv_12() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(121);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(131);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(0.748);
        DoubleArray X = new DoubleArray(0.979);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(1.30882352941);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1178)");
            }
        }
    }

    @Test
    public void test_trsv_13() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(121);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(132);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(0.748);
        DoubleArray X = new DoubleArray(0.979);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(0.979);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1179)");
            }
        }
    }

    @Test
    public void test_trsv_14() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(122);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(131);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(0.748);
        DoubleArray X = new DoubleArray(0.979);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(1.30882352941);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1180)");
            }
        }
    }

    @Test
    public void test_trsv_15() {
        CBLAS_ORDER order = CBLAS_ORDER.valueOf(102);
        CBLAS_TRANSPOSE trans = CBLAS_TRANSPOSE.valueOf(112);
        CBLAS_UPLO uplo = CBLAS_UPLO.valueOf(122);
        CBLAS_DIAG diag = CBLAS_DIAG.valueOf(132);
        int N = 1;
        int lda = 1;
        DoubleArray A = new DoubleArray(0.748);
        DoubleArray X = new DoubleArray(0.979);
        int incX = -1;
        DoubleArray x_expected = new DoubleArray(0.979);
        Gsl.cblas_dtrsv(order, uplo, trans, diag, N, A, lda, X, incX);
        {
            int i;
            for (i = 0; i < 1; i++) {
                gsl_test_rel(X.get(i), x_expected.get(i), dbleps, "dtrsv(case 1181)");
            }
        }
    }

}
