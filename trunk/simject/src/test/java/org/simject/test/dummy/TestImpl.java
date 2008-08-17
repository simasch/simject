package org.simject.test.dummy;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

public class TestImpl implements TestInterface {

	private final static Logger logger = Logger
			.getLogger(TestImpl.class);

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
