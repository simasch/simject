package org.simject.library.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.simject.SimFactory;
import org.simject.library.test.dummy.TestClass;
import org.simject.library.test.dummy.TestInterface;
import org.simject.library.test.dummy.TestInterfaceImpl;
import org.simject.library.test.dummy.TestNotFoundInterface;

public class SimFactoryTest {

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
        }
        catch (Exception e) {
            assertEquals(
                    "Exception not as expected",
                    "Resource of type org.simject.library.test.dummy.TestNotFoundInterface not found",
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
        }
        catch (Exception e) {
            assertEquals("Exception not as expected", "META-INF/abc not found",
                    e.getMessage());
        }
    }

    /**
     * Generall DI test
     * 
     * @throws Exception
     */
    @Test
    public void testDependencyInjection() {

        final SimFactory factory = new SimFactory(RESOURCES_XML);
        final TestInterfaceImpl testInterfaceImpl = (TestInterfaceImpl) factory
                .getResource(TestInterface.class);

        assertNotNull("Implementation not found", testInterfaceImpl);
    }
}
