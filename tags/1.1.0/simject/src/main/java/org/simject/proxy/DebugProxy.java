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
package org.simject.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.simject.exception.SimException;

/**
 * Example Proxy to do some debugging
 * 
 * @author Simon Martinelli
 */
public final class DebugProxy implements InvocationHandler {

	private final static Logger logger = Logger.getLogger(DebugProxy.class);

	private final Object obj;

	public static Object newInstance(final Object obj) {
		return java.lang.reflect.Proxy.newProxyInstance(obj.getClass()
				.getClassLoader(), obj.getClass().getInterfaces(),
				new DebugProxy(obj));
	}

	private DebugProxy(final Object obj) {
		this.obj = obj;
	}

	@Override
	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		Object result;
		try {
			logger.debug("before method " + method.getName());
			result = method.invoke(obj, args);
		}
		catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
		catch (Exception e) {
			throw new SimException("unexpected invocation exception: "
					+ e.getMessage(), e);
		}
		finally {
			logger.debug("after method " + method.getName());
		}
		return result;
	}

}
