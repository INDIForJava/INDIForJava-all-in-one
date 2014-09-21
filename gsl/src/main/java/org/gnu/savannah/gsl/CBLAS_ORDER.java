package org.gnu.savannah.gsl;

public enum CBLAS_ORDER {
	CblasRowMajor(101), CblasColMajor(102);

	final int nr;

	private CBLAS_ORDER(int nr) {
		this.nr = nr;
	}

	public static CBLAS_ORDER valueOf(int i) {
		for (CBLAS_ORDER order : values()) {
			if (order.nr == i) {
				return order;
			}
		}
		return null;
	}
}
