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
import org.simject.exception.SimException;
import org.simject.util.SimContants;

import com.thoughtworks.xstream.XStream;

/**
 * Used to provide remote access over HTTP to a resource. HttpClientProxy uses
 * Commons HTTPClient for communication and XStream for XML serialization
 * 
 * @author Simon Martinelli
 */
public final class HttpClientProxy implements InvocationHandler {

	private final static Logger logger = Logger
			.getLogger(HttpClientProxy.class);

	/**
	 * Holds the requested URL
	 */
	final private URL url;

	/**
	 * Factory method for creation
	 * 
	 * @param loader
	 * @param interfaces
	 * @param url
	 * @return an instance of HttpClientProxy
	 */
	public static Object newInstance(final ClassLoader loader,
			final Class<?>[] interfaces, final URL url) {
		return java.lang.reflect.Proxy.newProxyInstance(loader, interfaces,
				new HttpClientProxy(url));
	}

	/**
	 * Private Constructor
	 * 
	 * @param url
	 */
	private HttpClientProxy(final URL url) {
		this.url = url;
	}

	@Override
	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		Object result;
		try {
			result = this.invokeUrl(method, args);
		}
		catch (Exception e) {
			logger.fatal(e);
			e.printStackTrace();
			throw new SimException("unexpected invocation exception: "
					+ e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Synchronous call over HTTP
	 * 
	 * 1. Serializes the arguments to XML using XStream and make a remote call
	 * over HTTP with Commons HttpClient to the desired method. 2. Deserializes
	 * the XML response from the server.
	 * 
	 * @param method
	 * @param args
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Object invokeUrl(final Method method, final Object[] args)
			throws IOException, ClassNotFoundException {

		final XStream xstream = new XStream();
		final String xml = xstream.toXML(args);

		final PostMethod post = new PostMethod(this.url.toString());
		final RequestEntity req = new ByteArrayRequestEntity(xml.getBytes(),
				SimContants.CONTENT_TYPE_XML);
		post.setRequestEntity(req);

		// The method name and the parameter types are set to the header
		final Header headerMethod = new Header(SimContants.PARAM_METHOD, method
				.getName());
		post.addRequestHeader(headerMethod);

		// Get all parameter types and add them to a string delimited by ,
		final StringBuffer params = new StringBuffer();
		for (Class<?> param : method.getParameterTypes()) {
			final String paramString = param.getName()
					+ SimContants.PARAM_TYPE_DELIM;
			params.append(paramString);
		}
		if (params.length() > 0) {
			final String parameters = params.toString();
			final Header headerParamTypes = new Header(SimContants.PARAM_TYPES,
					parameters);
			post.addRequestHeader(headerParamTypes);
		}

		final HttpClient httpclient = new HttpClient();
		httpclient.executeMethod(post);

		logger.debug(post.getResponseBodyAsString());

		// Get response if the content is > 0
		Object result = null;
		if (post.getResponseContentLength() > 0) {
			result = xstream.fromXML(post.getResponseBodyAsString());
		}

		post.releaseConnection();

		return result;
	}
}
