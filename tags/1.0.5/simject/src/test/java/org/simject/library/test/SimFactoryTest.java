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

		final SimFactory factory = new SimFactory(RESOURCES_XML);
		final TestClass testClass = factory.getResource(TestClass.class);

		logger.info(testClass.toString());

		assertNotNull("Class not found", testClass);
	}

	@Test
	public void testSimFactoryWithInterface() throws Exception {

		final SimFactory factory = new SimFactory(RESOURCES_XML);
		final TestInterface testInterface = factory
				.getResource(TestInterface.class);

		logger.info(testInterface.toString());

		assertNotNull("Interface not found", testInterface);
	}

	@Test
	public void testSimFactoryNotFound() throws Exception {

		try {
			final SimFactory factory = new SimFactory(RESOURCES_XML);
			final TestNotFoundInterface tnfi = factory
					.getResource(TestNotFoundInterface.class);
			tnfi.getClass();

			fail();
		}
		catch (Exception e) {
			assertEquals(
					"Exception not as expected",
					"Resource of type org.simject.library.test.dummy.TestNotFoundInterface not found",
					e.getMessage());
		}
	}

	@Test
	public void testDependencyInjection() throws Exception {

		final SimFactory factory = new SimFactory(RESOURCES_XML);
		final TestInterfaceImpl testInterfaceImpl = (TestInterfaceImpl) factory
				.getResource(TestInterface.class);

		assertNotNull("Implementation not found", testInterfaceImpl);
	}
}