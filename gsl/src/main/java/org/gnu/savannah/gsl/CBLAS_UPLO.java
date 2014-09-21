package org.gnu.savannah.gsl;

public enum CBLAS_UPLO {
	CblasUpper(121), CblasLower(122);

	final int nr;

	private CBLAS_UPLO(int nr) {
		this.nr = nr;
	}

	public static CBLAS_UPLO valueOf(int i) {
		for (CBLAS_UPLO order : values()) {
			if (order.nr == i) {
				return order;
			}
		}
		return null;
	}

}
