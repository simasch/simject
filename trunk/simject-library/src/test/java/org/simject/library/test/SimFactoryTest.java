package org.simject.library.test;

import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.simject.library.SimFactory;

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

		TestNotFoundInterface testNotFoundInterface;
		try {
			SimFactory factory = new SimFactory("test");
			testNotFoundInterface = factory
					.getResource(TestNotFoundInterface.class);

			Assert.fail();
		}
		catch (Exception e) {
			Assert
					.assertEquals(
							"Resource of type org.simject.library.test.TestNotFoundInterface not found",
							e.getMessage());
		}
	}
}
