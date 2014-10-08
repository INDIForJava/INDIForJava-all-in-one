package org.indilib.i4j.androidui;

/*
 * #%L INDI for Java Android App %% Copyright (C) 2013 - 2014 indiforjava %%
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Lesser Public License for more details. You should have received a copy of
 * the GNU General Lesser Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>. #L%
 */

import android.widget.LinearLayout;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;

/**
 * An abstract class representing a View of a Element.
 * 
 * @version 1.3, April 9, 2012
 * @author S. Alonso (Zerjillo) [zerjio at zerjio.com]
 */
public abstract class INDIElementView extends LinearLayout implements INDIElementListener {

    private boolean changed;

    private boolean writable;

    private INDIPropertyView ipv;

    protected INDIElementView(PropertyPermissions perm) throws INDIException {
        super(I4JAndroidConfig.getContext());

        if (perm != PropertyPermissions.RO) {
            this.writable = true;
        } else {
            this.writable = false;
        }

        ipv = null;

        changed = false;
    }

    protected void setINDIPropertyView(INDIPropertyView ipv) {
        this.ipv = ipv;
    }

    protected void checkSetButton() {
        if (ipv != null) {
            ipv.checkSetButton();
        }
    }

    protected void setChanged(boolean changed) {
        this.changed = changed;
    }

    protected boolean isChanged() {
        return changed;
    }

    protected boolean isWritable() {
        return writable;
    }

    protected abstract Object getDesiredValue();

    protected abstract INDIElement getElement();

    protected abstract void setError(boolean erroneous, String errorMessage);

    protected abstract boolean isDesiredValueErroneous();

    protected abstract void cleanDesiredValue();
}
