package org.indilib.i4j.driver.junit;

/*
 * #%L
 * INDI for Java Driver JUNIT Test Utilities
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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

import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * This JUnit runner can be used to write utit tests for drivers. it will
 * require you to have 2 flield in your test class. First a field for the driver
 * class and second a field for the test client. You may use test subclasses of
 * your driver to mock some of the methods. Attention: all tests are singular so
 * very test method will get "fresh" instances.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIDriverRunner extends BlockJUnit4ClassRunner {

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}.
     * 
     * @param klass
     *            the test class
     * @throws InitializationError
     *             if the test class is malformed.
     */
    public INDIDriverRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        Object newInstance = getTestClass().getOnlyConstructor().newInstance();
        return newInstance;
    }

    @Override
    protected List<TestRule> getTestRules(final Object target) {
        List<TestRule> testRules = super.getTestRules(target);
        testRules.add(new TestRule() {

            @Override
            public Statement apply(final Statement base, final Description description) {
                return new INDIStatement(base, target);
            }
        });
        return testRules;
    }
}
