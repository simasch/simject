package org.simject.library.test;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.simject.SimFactory;
import org.simject.library.test.dummy.TestClass;
import org.simject.library.test.dummy.TestInterface;
import org.simject.library.test.dummy.TestInterfaceImpl;
import org.simject.library.test.dummy.TestNotFoundInterface;

public class SimFactoryTest {

	private static final String RESOURCES_XML = "resources.xml";

	private static final Logger logger = Logger.getLogger(SimFactoryTest.class);

	@Test
	public void testSimFactory() throws Exception {

		SimFactory factory = new SimFactory(RESOURCES_XML);
		TestClass testClass = factory.getResource(TestClass.class);

		logger.info(testClass.toString());

		assertNotNull(testClass);
	}

	@Test
	public void testSimFactoryWithInterface() throws Exception {

		SimFactory factory = new SimFactory(RESOURCES_XML);
		TestInterface testInterface = factory.getResource(TestInterface.class);

		logger.info(testInterface.toString());

		assertNotNull(testInterface);
	}

	@Test
	public void testSimFactoryNotFound() throws Exception {

		try {
			SimFactory factory = new SimFactory(RESOURCES_XML);
			TestNotFoundInterface testNotFoundInterface = factory
					.getResource(TestNotFoundInterface.class);

			fail();
		}
		catch (Exception e) {
			assertEquals(
					"Resource of type org.simject.library.test.dummy.TestNotFoundInterface not found",
					e.getMessage());
		}
	}

	@Test
	public void testDependencyInjection() throws Exception {

		SimFactory factory = new SimFactory(RESOURCES_XML);
		TestInterfaceImpl testInterfaceImpl = (TestInterfaceImpl) factory
				.getResource(TestInterface.class);

		assertNotNull(testInterfaceImpl);
	}
}
