package org.gnu.savannah.gsl;

public enum CBLAS_TRANSPOSE {
	CblasNoTrans(111), CblasTrans(112), CblasConjTrans(113);

	final int nr;

	private CBLAS_TRANSPOSE(int nr) {
		this.nr = nr;
	}

	public int nr() {
		return nr;
	}

	public static CBLAS_TRANSPOSE valueOf(int i) {
		for (CBLAS_TRANSPOSE order : values()) {
			if (order.nr == i) {
				return order;
			}
		}
		return null;
	}

}
