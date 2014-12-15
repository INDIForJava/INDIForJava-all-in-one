package org.indilib.i4j.client.fx;

/*
 * #%L
 * INDI for Java Client UI Library
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javafx.application.Platform;

public class INDIFxPlatformThreadConnector implements InvocationHandler {

    private static final class PlatformInvoker implements Runnable {

        private final Object base;

        private final Method method;

        private final Object[] args;

        private Object result;

        private boolean ready = false;

        protected boolean isReady() {
            return ready;
        }

        protected Object getResult() {
            return result;
        }

        public PlatformInvoker(Object base, Method method, Object[] args) {
            this.base = base;
            this.method = method;
            this.args = args;
        }

        @Override
        public void run() {
            try {
                result = method.invoke(base, args);
            } catch (Exception e) {
                // TODO LOG it and re thow outside
                e.printStackTrace();
            }
            ready = true;
        }
    }

    private final Object base;

    public INDIFxPlatformThreadConnector(Object base) {
        this.base = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        PlatformInvoker invoker = new PlatformInvoker(base, method, args);
        if (Platform.isFxApplicationThread()) {
            invoker.run();
        } else {
            Platform.runLater(invoker);
            if (method.getReturnType() != null) {
                long start = System.currentTimeMillis();
                while (!invoker.isReady()) {
                    Thread.sleep(25L);
                    if (System.currentTimeMillis() - start > 1000) {
                        // log it and continue, we won't wait for more than a
                        // second.
                        break;
                    }
                }
            }
        }
        return invoker.getResult();
    }

    public static <T> T connect(Object implementor, Class<T> interfaceToImplement, Class<?>... extraInterfaces) {
        Class<?>[] interfaces = new Class<?>[extraInterfaces.length + 1];
        interfaces[0] = interfaceToImplement;
        System.arraycopy(extraInterfaces, 0, interfaces, 1, extraInterfaces.length);
        return interfaceToImplement.cast(//
                Proxy.newProxyInstance(interfaceToImplement.getClassLoader(), interfaces, new INDIFxPlatformThreadConnector(implementor)));
    }

}
