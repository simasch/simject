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
package org.simject.remote.client;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.log4j.Logger;
import org.simject.util.SimContants;

import com.thoughtworks.xstream.XStream;

/**
 * Used to provide remote access over HTTP to a resource. HttpClientProxy uses
 * Commons HTTPClient for communication and XStream for XML serialization
 * 
 * @author Simon Martinelli
 */
public class HttpClientProxy implements InvocationHandler {

	private final static Logger logger = Logger
			.getLogger(HttpClientProxy.class);

	/**
	 * Holds the URL
	 */
	private URL url;

	/**
	 * Factory Method for creation
	 * 
	 * @param loader
	 * @param interfaces
	 * @param url
	 * @return an instance of HttpClientProxy
	 */
	public static Object newInstance(ClassLoader loader, Class<?>[] interfaces,
			URL url) {
		return java.lang.reflect.Proxy.newProxyInstance(loader, interfaces,
				new HttpClientProxy(url));
	}

	/**
	 * Private Constructor
	 * 
	 * @param url
	 */
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
			e.printStackTrace();
			throw new RuntimeException("unexpected invocation exception: "
					+ e.getMessage());
		}
		return result;
	}

	/**
	 * Synchronous call over HTTP
	 * 
	 * 1. Serializes the argumens and make a remot call to the desired method.
	 * 2. Deserializes the response from the server.
	 * 
	 * @param method
	 * @param args
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Object invokeUrl(Method method, Object[] args) throws IOException,
			ClassNotFoundException {

		XStream xstream = new XStream();
		String xml = xstream.toXML(args);

		PostMethod post = new PostMethod(this.url.toString());
		RequestEntity req = new ByteArrayRequestEntity(xml.getBytes(),
				SimContants.CONTENT_TYPE_XML);
		post.setRequestEntity(req);

		Header headerMethod = new Header(SimContants.PARAMETER_METHOD, method
				.getName());
		post.addRequestHeader(headerMethod);

		StringBuffer params = new StringBuffer();
		for (Class param : method.getParameterTypes()) {
			params.append(param.getName()
					+ SimContants.PARAMETER_TYPE_DELIMITER);
		}
		if (params.length() > 0) {
			String parameters = params.toString();
			Header headerParamTypes = new Header(SimContants.PARAMETER_TYPES,
					parameters);
			post.addRequestHeader(headerParamTypes);
		}

		HttpClient httpclient = new HttpClient();
		httpclient.executeMethod(post);

		logger.debug(post.getResponseBodyAsString());

		Object result = xstream.fromXML(post.getResponseBodyAsString());

		post.releaseConnection();

		return result;
	}
}
