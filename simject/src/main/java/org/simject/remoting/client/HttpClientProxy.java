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
package org.simject.remoting.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.simject.util.Protocol;
import org.simject.util.SimConstants;

import com.thoughtworks.xstream.XStream;

/**
 * Used to provide remote access over HTTP to a resource. HttpClientProxy uses
 * XStream for XML serialization or normal Java serialization for binary
 * protocol
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
	 * Holds the protocol
	 */
	final private Protocol protocol;

	/**
	 * Creates a new instance of a proxy
	 * 
	 * @param loader
	 * @param interfaces
	 * @param url
	 * @return an instance of HttpClientProxy
	 * @throws MalformedURLException
	 */
	public static Object newInstance(final ClassLoader loader,
			final Class<?>[] interfaces, final String target)
			throws MalformedURLException {

		// Extract the protocol
		final String protcolString = target.substring(0, 3);
		Protocol protocol = Protocol.Xml;
		if (protcolString.equals(Protocol.Binary.getString())) {
			protocol = Protocol.Binary;
		}
		// Extract the URL
		final String urlString = target.substring(4);
		final URL url = new URL(urlString);

		if (logger.isInfoEnabled()) {
			logger.info("Creating proxy for URL <" + url + "> using <"
					+ protocol + "> protocol");
		}
		// Create an instance of the proxy
		return java.lang.reflect.Proxy.newProxyInstance(loader, interfaces,
				new HttpClientProxy(url, protocol));
	}

	/**
	 * Private Constructor
	 * 
	 * @param url
	 */
	private HttpClientProxy(final URL url, final Protocol protocol) {
		this.url = url;
		this.protocol = protocol;
	}

	@Override
	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		if (logger.isInfoEnabled()) {
			logger.info("Invoking method <" + method.getName()
					+ "> with arguments <" + args + ">");
		}
		Object result;
		try {
			if (protocol == Protocol.Binary) {
				result = this.invokeUrlBinary2(method, args);
			} else {
				result = this.invokeUrlXml2(method, args);
			}
		} catch (Exception e) {
			logger.fatal(e);
			throw e;
		}
		return result;
	}

	/**
	 * Synchronous call over HTTP using binary protocol
	 * 
	 * 1. Serializes the arguments to Binary and make a remote call over HTTP
	 * with Commons HttpClient to the desired method. 2. Deserializes the binary
	 * response from the server.
	 * 
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	private Object invokeUrlBinary2(final Method method, final Object[] args)
			throws Throwable {

		HttpURLConnection con = (HttpURLConnection) this.url.openConnection();
		con.setDoOutput(true);

		// The method name and the parameter types are set to the header
		this.createHeader2(method, con);

		final ObjectOutputStream oos = new ObjectOutputStream(con
				.getOutputStream());
		oos.writeObject(args);
		oos.close();

		// Get response if the content is > 0
		Object result = null;
		if (con.getContentLength() > 0) {
			final ObjectInputStream ois = new ObjectInputStream(con
					.getInputStream());
			result = ois.readObject();

			if (result instanceof Throwable) {
				throw ((Throwable) result);
			}
		}

		con.disconnect();

		return result;
	}

	/**
	 * Synchronous call over HTTP using XML protocol
	 * 
	 * 1. Serializes the arguments to XML using XStream and make a remote call
	 * over HTTP with Commons HttpClient to the desired method. 2. Deserializes
	 * the XML response from the server.
	 * 
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	private Object invokeUrlXml2(final Method method, final Object[] args)
			throws Throwable {

		final XStream xstream = new XStream();
		final String xml = xstream.toXML(args);

		final HttpURLConnection con = (HttpURLConnection) this.url
				.openConnection();
		con.setDoOutput(true);

		// The method name and the parameter types are set to the header
		this.createHeader2(method, con);

		final OutputStreamWriter osw = new OutputStreamWriter(con
				.getOutputStream());
		osw.write(xml);

		// Get response if the content is > 0
		Object result = null;
		if (con.getContentLength() > 0) {
			final String response = con.getInputStream().toString();
			result = xstream.fromXML(response);
			if (result instanceof Throwable) {
				throw ((Throwable) result);
			}
		}

		con.disconnect();

		return result;
	}

	/**
	 * Create the necessairy Header entries
	 * 
	 * @param method
	 * @param post
	 */
	private void createHeader2(final Method method, final HttpURLConnection con) {
		con.setRequestProperty(SimConstants.PARAM_METHOD, method.getName());

		// Get all parameter types and add them to a string delimited by ,
		final StringBuffer params = new StringBuffer();
		for (Class<?> param : method.getParameterTypes()) {
			final String paramString = param.getName()
					+ SimConstants.PARAM_TYPE_DELIM;
			params.append(paramString);
		}
		if (params.length() > 0) {
			final String parameters = params.toString();
			con.setRequestProperty(SimConstants.PARAM_TYPES, parameters);
		}
	}
}
