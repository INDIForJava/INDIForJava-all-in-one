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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class redirects all calls to an object to the fx gui thread, this way
 * the normal gui does not have to think about if it can call a method or should
 * it ge send to the gui thread first.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIFxPlatformThreadConnector implements InvocationHandler {

    /**
     * we will wait a maximum of 1 second for a method to end.
     */
    private static final int MAX_WAIT_TIME = 1000;

    /**
     * the logger to use.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIFxPlatformThreadConnector.class);

    /**
     * runnable to use for calling a method in the gui platform.
     */
    private static final class PlatformInvoker implements Runnable {

        /**
         * the object on with to call the method.
         */
        private final Object base;

        /**
         * the method to call.
         */
        private final Method method;

        /**
         * the arguments of the method call.
         */
        private final Object[] args;

        /**
         * the return value of the method.
         */
        private Object result;

        /**
         * has the method been called?
         */
        private boolean hasBeenCalled = false;

        /**
         * @return the return value of the method.
         */
        protected Object getResult() {
            return result;
        }

        /**
         * constructor for the method call.
         * 
         * @param base
         *            the object on with to call the method.
         * @param method
         *            the method to call.
         * @param args
         *            the arguments of the method call.
         */
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
                LOG.error("could not invoke the method in the fx platform.", e);
            } finally {
                synchronized (this) {
                    hasBeenCalled = true;
                    this.notify();
                }
            }
        }
    }

    /**
     * the object on with to call the methods.
     */
    private final Object base;

    /**
     * the contructor for the thread connector.
     * 
     * @param base
     *            the object on with to call the method.
     */
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
                try {
                    synchronized (invoker) {
                        if (!invoker.hasBeenCalled) {
                            invoker.wait(MAX_WAIT_TIME);
                        }
                    }
                } catch (InterruptedException e) {
                    LOG.warn("wait interrupted", e);
                }
                if (System.currentTimeMillis() - start > MAX_WAIT_TIME) {
                    LOG.warn("We will not wait any longer, continueing!");
                }
            }
        }
        return invoker.getResult();
    }

    /**
     * create the proxy that redirects the method calls to the gui thread.
     * 
     * @param implementor
     *            the object on with to call the methods.
     * @param interfaceToImplement
     *            the interfaces the proxy should implement
     * @param extraInterfaces
     *            the other interfaces the proxy should implement
     * @param <T>
     *            the type of the main interface
     * @return the proxy casted to the base interface
     */
    public static <T> T connect(Object implementor, Class<T> interfaceToImplement, Class<?>... extraInterfaces) {
        Class<?>[] interfaces = new Class<?>[extraInterfaces.length + 1];
        interfaces[0] = interfaceToImplement;
        System.arraycopy(extraInterfaces, 0, interfaces, 1, extraInterfaces.length);
        return interfaceToImplement.cast(//
                Proxy.newProxyInstance(interfaceToImplement.getClassLoader(), interfaces, new INDIFxPlatformThreadConnector(implementor)));
    }

}
