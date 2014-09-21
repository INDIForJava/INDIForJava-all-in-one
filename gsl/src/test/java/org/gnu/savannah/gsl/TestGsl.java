package org.gnu.savannah.gsl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class TestGsl {

	boolean verbose;
	int tests;
	int passed;
	int failed;

	@Before
	public void initialise() {
		String p = System.getProperty("GSL_TEST_VERBOSE", "true");

		verbose = Boolean.valueOf(p);

		return;
	}

	private void update(boolean isFailed) {
		tests++;

		if (!isFailed) {
			passed++;
		} else {
			failed++;
		}
	}

	protected void gsl_test(boolean isFailed, String test_description,
			String... args) {

		update(isFailed);

		if (isFailed || verbose) {
			printf(isFailed ? "FAIL: " : "PASS: ");
			printf(test_description);
			if (isFailed) {
				Assert.fail("see console");
			}

			if (isFailed && !verbose)
				printf(" [%u]", tests);

			printf("\n");

		}
	}

	private void printf(String format, Object... args) {
		System.out.printf(format, args);
	}

	protected void gsl_test_rel(double result, double expected,
			double relative_error, String test_description, Object... args) {
		boolean status;

		/* Check for NaN vs inf vs number */

		if (Double.isNaN(result) || Double.isNaN(expected)) {
			status = Double.isNaN(result) != Double.isNaN(expected);
		} else if (Double.isInfinite(result) || Double.isInfinite(expected)) {
			status = Double.isInfinite(result) != Double.isInfinite(expected);
		} else if ((expected > 0 && expected < Gsl.GSL_DBL_MIN)
				|| (expected < 0 && expected > -(Gsl.GSL_DBL_MIN))) {
			status = false;
		} else if (expected != 0) {
			status = (Math.abs(result - expected) / Math.abs(expected) > relative_error);
		} else {
			status = (Math.abs(result) > relative_error);
		}

		update(status);

		if (status || verbose) {
			printf(status ? "FAIL: " : "PASS: ");

			if (status) {
				if (strlen(test_description) < 45) {
					printf(" (%g observed vs %g expected)", result, expected);
				} else {
					printf(" (%g obs vs %g exp)", result, expected);
				}
				Assert.fail("see console");
			} else {
				printf(" (%.18g observed vs %.18g expected)", result, expected);

			}

			if (status && !verbose)
				printf(" [%u]", tests);

			printf("\n");

		}
	}

	private int strlen(String test_description) {
		return test_description.length();
	}

	protected void gsl_test_abs(double result, double expected,
			double absolute_error, String test_description, Object... args) {
		boolean status;

		/* Check for NaN vs inf vs number */

		if (Double.isNaN(result) || Double.isNaN(expected)) {
			status = Double.isNaN(result) != Double.isNaN(expected);
		} else if (Double.isInfinite(result) || Double.isInfinite(expected)) {
			status = Double.isInfinite(result) != Double.isInfinite(expected);
		} else if ((expected > 0 && expected < Gsl.GSL_DBL_MIN)
				|| (expected < 0 && expected > -(Gsl.GSL_DBL_MIN))) {
			status = false;
		} else {
			status = Math.abs(result - expected) > absolute_error;
		}

		update(status);

		if (status || verbose) {
			printf(status ? "FAIL: " : "PASS: ");

			if (status) {
				if (strlen(test_description) < 45) {
					printf(" (%g observed vs %g expected)", result, expected);
				} else {
					printf(" (%g obs vs %g exp)", result, expected);
				}
				Assert.fail("see console");
			} else {
				printf(" (%.18g observed vs %.18g expected)", result, expected);
			}

			if (status && !verbose)
				printf(" [%u]", tests);

			printf("\n");

		}
	}

	protected void gsl_test_factor(double result, double expected,
			double factor, String test_description, Object... args) {
		boolean status;

		if ((expected > 0 && expected < Gsl.GSL_DBL_MIN)
				|| (expected < 0 && expected > -(Gsl.GSL_DBL_MIN))) {
			status = false;
		} else if (result == expected) {
			status = true;
		} else if (expected == 0.0) {
			status = (result > expected || result < expected);
		} else {
			double u = result / expected;
			status = (u > factor || u < 1.0 / factor);
		}

		update(status);

		if (status || verbose) {
			printf(status ? "FAIL: " : "PASS: ");

			if (status) {
				if (strlen(test_description) < 45) {
					printf(" (%g observed vs %g expected)", result, expected);
				} else {
					printf(" (%g obs vs %g exp)", result, expected);
				}
				Assert.fail("see console");
			} else {
				printf(" (%.18g observed vs %.18g expected)", result, expected);

			}

			if (status && !verbose)
				printf(" [%u]", tests);

			printf("\n");

		}
	}

	protected void gsl_test_int(int result, int expected,
			String test_description, Object... args) {
		boolean status = (result != expected);

		update(status);

		if (status || verbose) {
			printf(status ? "FAIL: " : "PASS: ");

			if (status) {
				printf(" (%d observed vs %d expected)", result, expected);
				Assert.fail("see console");
			} else {
				printf(" (%d observed vs %d expected)", result, expected);

			}

			if (status && !verbose)
				printf(" [%u]", tests);

			printf("\n");

		}
	}

	protected void gsl_test_str(String result, String expected,
			String test_description, Object... args) {
		boolean status = strcmp(result, expected) == 0;

		update(status);

		if (status || verbose) {
			printf(status ? "FAIL: " : "PASS: ");

			if (status) {
				printf(" (%s observed vs %s expected)", result, expected);
				Assert.fail("see console");
			}
			if (status && !verbose)
				printf(" [%u]", tests);

			printf("\n");

		}
	}

	private int strcmp(String result, String expected) {

		return expected.compareTo(result);
	}

	@After
	public void gsl_test_summary() {
		if (verbose)
			printf("%d tests, passed %d, failed %d.\n", tests, passed, failed);

		if (failed != 0) {
			Assert.fail();
		} else if (tests != passed + failed) {
			if (verbose)
				printf("TEST RESULTS DO NOT ADD UP %d != %d + %d\n", tests,
						passed, failed);
			Assert.fail();
		} else if (passed == tests) {
			if (!verbose) /* display a summary of passed tests */
				printf("Completed [%d/%d]\n", passed, tests);
		} else {
			Assert.fail();
		}

	}

	protected void checkMatrix(double[] expected, GslMatrix sub) {
		int i = 0;
		for (int y = 0; y < sub.size1; y++) {
			for (int x = 0; x < sub.size2; x++) {
				if (sub.get(y, x) != expected[i++]) {
					Assert.fail("matrix not as expected");
				}
			}
		}
	}

	protected void checkVector(double[] expected, GslVector sub) {
		int i = 0;
		for (int y = 0; y < sub.size; y++) {
			if (sub.get(y) != expected[i++]) {
				Assert.fail("vector not as expected");
			}
		}
	}
}
