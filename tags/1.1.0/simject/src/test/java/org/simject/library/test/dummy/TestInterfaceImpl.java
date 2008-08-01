package org.simject.library.test.dummy;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

public class TestInterfaceImpl implements TestInterface {

	private final static Logger logger = Logger
			.getLogger(TestInterfaceImpl.class);

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
