package org.simject.library.test.dummy;

import javax.annotation.Resource;

public class TestInterfaceImpl implements TestInterface {

	@Resource
	private TestClass testClass;

	public TestClass getTestClass() {
		return this.testClass;
	}
}