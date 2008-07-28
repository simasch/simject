package org.simject.library.test;

import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.simject.SimFactory;
import org.simject.library.test.dummy.TestClass;
import org.simject.library.test.dummy.TestInterface;
import org.simject.library.test.dummy.TestNotFoundInterface;

public class SimFactoryTest {

	private final static Logger logger = Logger.getLogger(SimFactoryTest.class
			.getName());

	@Test
	public void testSimFactory() throws Exception {

		SimFactory factory = new SimFactory("test");
		TestClass testClass = factory.getResource(TestClass.class);

		logger.info(testClass.toString());

		Assert.assertNotNull(testClass);
	}

	@Test
	public void testSimFactoryWithInterface() throws Exception {

		SimFactory factory = new SimFactory("test");
		TestInterface testInterface = factory.getResource(TestInterface.class);

		logger.info(testInterface.toString());

		Assert.assertNotNull(testInterface);
	}

	@Test
	public void testSimFactoryNotFound() throws Exception {

		try {
			SimFactory factory = new SimFactory("test");
			TestNotFoundInterface testNotFoundInterface = factory
					.getResource(TestNotFoundInterface.class);

			Assert.fail();
		}
		catch (Exception e) {
			Assert
					.assertEquals(
							"Resource of type org.simject.library.test.dummy.TestNotFoundInterface not found",
							e.getMessage());
		}
	}
}
