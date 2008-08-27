package org.simject.test.dummy;

import java.util.logging.Logger;

import javax.annotation.Resource;

public class TestImpl implements TestInterface {

	private final static Logger logger = Logger.getLogger(TestImpl.class
			.getName());

	@Resource
	private TestClass testClass;

	public TestClass getTestClass() {
		return this.testClass;
	}

	@Override
	public void sayHello() {
		logger.info("Hello World");
	}
}
