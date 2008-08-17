/*
 * Copyright 2008 Simon Martinelli, Rebenweg 32, 3236 Gampelen, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.simject.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.simject.SimFactory;
import org.simject.remoting.client.HttpClientProxy;
import org.simject.test.dummy.TestClass;
import org.simject.test.dummy.TestImpl;
import org.simject.test.dummy.TestInterface;
import org.simject.test.dummy.TestNotFoundInterface;
import org.simject.test.dummy.TestRemoteInterface;

/**
 * JUnit tests
 * 
 * @author Simon Martinelli
 */
public class SimFactoryTest {

	/**
	 * Name of the resources xml file
	 */
	private static final String RESOURCES_XML = "test_resources.xml";

	private static final Logger logger = Logger.getLogger(SimFactoryTest.class);

	/**
	 * Positive test using a class
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSimFactory() {

		final SimFactory factory = new SimFactory(RESOURCES_XML);
		final TestClass testClass = factory.getResource(TestClass.class);

		logger.info(testClass.toString());

		assertNotNull("Class not found", testClass);
	}

	/**
	 * Positiv test using an interface
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSimFactoryWithInterface() {

		final SimFactory factory = new SimFactory(RESOURCES_XML);
		final TestInterface testInterface = factory
				.getResource(TestInterface.class);

		logger.info(testInterface.toString());

		assertNotNull("Interface not found", testInterface);
	}

	/**
	 * Negativ test with wrong class
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSimFactoryClassNotFound() {

		try {
			final SimFactory factory = new SimFactory(RESOURCES_XML);
			final TestNotFoundInterface tnfi = factory
					.getResource(TestNotFoundInterface.class);
			tnfi.getClass();

			fail();
		} catch (Exception e) {
			assertEquals(
					"Exception not as expected",
					"Resource of type org.simject.test.dummy.TestNotFoundInterface not found",
					e.getMessage());
		}
	}

	/**
	 * Negativ test with wrong config file name
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSimFactoryConfigFileNotFound() {

		try {
			final SimFactory factory = new SimFactory("abc");
			final TestNotFoundInterface tnfi = factory
					.getResource(TestNotFoundInterface.class);
			tnfi.getClass();

			fail();
		} catch (Exception e) {
			assertEquals("Exception not as expected", "META-INF/abc not found",
					e.getMessage());
		}
	}

	/**
	 * General DI test
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDependencyInjection() {

		final SimFactory factory = new SimFactory(RESOURCES_XML);
		final TestImpl testInterfaceImpl = (TestImpl) factory
				.getResource(TestInterface.class);

		assertNotNull("Implementation not found", testInterfaceImpl);
	}

	/**
	 * Test instantiation of HttpClientProxy
	 */
	@Test
	public void testHttpClientProxy() {
		final SimFactory factory = new SimFactory(RESOURCES_XML);
		final Object testRemoteInterface = factory
				.getResource(TestRemoteInterface.class);

		assertNotNull("Implementation not found", testRemoteInterface);
		if (testRemoteInterface instanceof Proxy) {
			// ok
		} else {
			fail("Proxy expected");
		}
	}

	/**
	 * Test instantiation of EntityManager
	 */
	@Test
	public void testEntityManager() {
		final SimFactory factory = new SimFactory(RESOURCES_XML);
		final EntityManager entityManager = factory
				.getResource(EntityManager.class);

		assertNotNull("EntityManager not found", entityManager);
	}
}
