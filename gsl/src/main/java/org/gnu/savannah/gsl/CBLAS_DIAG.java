package org.gnu.savannah.gsl;

public enum CBLAS_DIAG {
	CblasNonUnit(131), CblasUnit(132);

	final int nr;

	private CBLAS_DIAG(int nr) {
		this.nr = nr;
	}


	public static CBLAS_DIAG valueOf(int i) {
		for (CBLAS_DIAG order : values()) {
			if (order.nr == i) {
				return order;
			}
		}
		return null;
	}
}
