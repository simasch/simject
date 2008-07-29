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
package org.simject.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.simject.util.SimContants;

/**
 * Used to provide remote access over HTTP to a resource
 * 
 * @author Simon Martinelli
 */
public class HttpClientProxy implements InvocationHandler {

	private final static Logger logger = Logger
			.getLogger(HttpClientProxy.class);

	private URL url;

	public static Object newInstance(ClassLoader loader, Class<?>[] interfaces,
			URL url) {
		return java.lang.reflect.Proxy.newProxyInstance(loader, interfaces,
				new HttpClientProxy(url));
	}

	private HttpClientProxy(URL url) {
		this.url = url;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result;
		try {
			result = this.invokeUrl(method, args);
		}
		catch (Exception e) {
			logger.fatal(e);
			throw new RuntimeException("unexpected invocation exception: "
					+ e.getMessage());
		}
		return result;
	}

	private Object invokeUrl(Method method, Object[] args) throws IOException,
			ClassNotFoundException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(args);

		PostMethod post = new PostMethod(this.url.toString());
		post.setRequestEntity(new ByteArrayRequestEntity(baos.toByteArray()));

		post.addParameter(SimContants.PARAMETER_METHOD, method.getName());
		this.parseParamaters(method, post);

		HttpClient httpclient = new HttpClient();
		httpclient.executeMethod(post);
		byte[] response = post.getResponseBody();

		ByteArrayInputStream bais = new ByteArrayInputStream(response);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object result = ois.readObject();

		post.releaseConnection();

		return result;
	}

	private void parseParamaters(Method method, PostMethod post) {
		StringBuffer params = new StringBuffer();
		for (Class param : method.getParameterTypes()) {
			params.append(param + ",");
		}
		if (params.length() > 0) {
			String parameters = params.toString();
			post.addParameter(SimContants.PARAMETER_TYPES, parameters);
		}
	}
}
