package org.gnu.savannah.gsl;

public enum CBLAS_SIDE {
	CblasLeft(141), CblasRight(142);

	final int nr;

	private CBLAS_SIDE(int nr) {
		this.nr = nr;
	}


	public static CBLAS_SIDE valueOf(int i) {
		for (CBLAS_SIDE order : values()) {
			if (order.nr == i) {
				return order;
			}
		}
		return null;
	}
}
