package org.simject.library.test;

import org.junit.Test;
import org.simject.library.test.dummy.TestInterface;
import org.simject.library.test.dummy.TestInterfaceImpl;
import org.simject.proxy.DebugProxy;

import static org.junit.Assert.*;

public class DebugProxyTest {

	@Test
	public void testProxy() throws Exception {

		try {
			final TestInterface testInterface = (TestInterface) DebugProxy
					.newInstance(new TestInterfaceImpl());
			testInterface.sayHello();
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
