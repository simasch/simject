package org.simject.library.test;

import org.junit.Test;
import org.simject.library.test.dummy.TestInterface;
import org.simject.library.test.dummy.TestInterfaceImpl;
import org.simject.proxy.DebugProxy;
import org.simject.remote.HttpClientProxy;

public class DebugProxyTest {

	@Test
	public void testProxy() throws Exception {

		TestInterface testInterface = (TestInterface) DebugProxy.newInstance(new TestInterfaceImpl());
		testInterface.sayHello();
	}
}
