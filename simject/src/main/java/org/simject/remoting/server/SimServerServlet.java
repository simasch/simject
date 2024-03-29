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
package org.simject.remoting.server;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.simject.SimFactory;
import org.simject.util.Protocol;
import org.simject.util.SimConstants;

/**
 * This servlet can be used as endpoint for remote access over HTTP to a
 * resource
 * 
 * @author Simon Martinelli
 */
@SuppressWarnings("serial")
public class SimServerServlet extends HttpServlet {

	private final static Logger logger = Logger
			.getLogger(SimServerServlet.class.getName());

	/**
	 * Context parameter that contains the configuration file location
	 */
	private final static String SIMJECT_CONFIG = "simjectConfig";

	/**
	 * Reference to the SimFactory
	 */
	private SimFactory simFactory;

	@Override
	public void init() throws ServletException {
		if (this.simFactory == null) {
			// Get the context parameter with the config file
			final String configFile = this.getServletContext()
					.getInitParameter(SIMJECT_CONFIG);
			// create a SimFactory based on the config file
			this.simFactory = new SimFactory(configFile);
		}
	}

	@Override
	protected void doPost(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		this.invokeMethod(req, resp);
	}

	/**
	 * Invokes a method based on the parameters passed from the client
	 * 
	 * @param req
	 * @param resp
	 * @param obj
	 * @param args
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private void invokeMethod(final HttpServletRequest req,
			final HttpServletResponse resp) throws IOException {

		Object result = null;
		try {
			// get the classname
			final String className = this.getClassName(req);
			// create a Class instance
			final Class<?> clazz = Class.forName(className);
			// and get the resource from the SimFactory
			final Object obj = this.simFactory.getResource(clazz);
			// get passed arguments and invoke the method
			final Object[] args = this.getArguments(req);
			// get the method name from the HTTP header
			final String methodString = req
					.getHeader(SimConstants.PARAM_METHOD);

			// get the parameter types from the HTTP header
			final String paramTypesString = req
					.getHeader(SimConstants.PARAM_TYPES);

			logger.info("Request for class <" + clazz.getName() + "> method <"
					+ methodString + "> params <" + paramTypesString + ">");

			if (paramTypesString == null) {
				// method without parameters to invoke
				final Method method = obj.getClass().getMethod(methodString);
				result = method.invoke(obj);
			} else {
				// method with parameters should be called. Converts the string
				// from the header to Class array
				final Class<?>[] parameterTypes = this
						.getParameterTypes(paramTypesString);
				final Method method = obj.getClass().getMethod(methodString,
						parameterTypes);

				result = method.invoke(obj, args);
			}
		} catch (Exception e) {
			// If an exception occurs during invocation put it in the result to
			// have it serialized
			logger.log(Level.INFO, "Exception occured during invocation", e);
			result = e;
		}
		if (result != null) {
			if (req.getContentType().equals(Protocol.Xml.getContentType())) {
				this.sendXmlResponse(resp, result);
			} else {
				this.sendBinaryResponse(resp, result);
			}
		}
	}

	/**
	 * Sends the result as binary stream
	 * 
	 * @param resp
	 * @param result
	 * @throws IOException
	 */
	private void sendBinaryResponse(final HttpServletResponse resp,
			final Object result) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(result);
		oos.close();

		resp.setContentType(Protocol.Binary.getContentType());
		resp.getOutputStream().write(baos.toByteArray());
	}

	/**
	 * Sends the result as XML stream
	 * 
	 * @param resp
	 * @param result
	 * @throws IOException
	 */
	private void sendXmlResponse(final HttpServletResponse resp,
			final Object result) throws IOException {
		resp.setContentType(Protocol.Xml.getContentType());
		final XMLEncoder encoder = new XMLEncoder(resp.getOutputStream());
		encoder.writeObject(result);
	}

	/**
	 * Returns an array of Class containing the types of the parameters
	 * 
	 * @param parameterString
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?>[] getParameterTypes(final String parameterString)
			throws ClassNotFoundException {
		Class<?>[] parameters = new Class[0];
		if (parameterString != null) {
			// The parameter types are seperated by ","
			final StringTokenizer stokenizer = new StringTokenizer(
					parameterString, SimConstants.PARAM_TYPE_DELIM);
			parameters = new Class[stokenizer.countTokens()];
			int index = 0;
			while (stokenizer.hasMoreTokens()) {
				final String className = stokenizer.nextToken();

				final Class<?> clazz = Class.forName(className);
				parameters[index] = clazz;
				index++;
			}
		}
		return parameters;
	}

	/**
	 * Get Arguments
	 * 
	 * @param req
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Object[] getArguments(final HttpServletRequest req)
			throws IOException, ClassNotFoundException {

		Object args = null;
		if (req.getContentType().equals(Protocol.Xml.getContentType())) {
			final XMLDecoder decoder = new XMLDecoder(req.getInputStream());
			args = decoder.readObject();
		} else {
			final ObjectInputStream ois = new ObjectInputStream(req
					.getInputStream());
			args = ois.readObject();
		}

		return (Object[]) args;
	}

	/**
	 * Parses the class name out of the path info
	 * 
	 * @param req
	 * @return
	 */
	private String getClassName(final HttpServletRequest req) {
		final StringTokenizer stokenzier = new StringTokenizer(req
				.getPathInfo(), "/");
		String[] tokens = new String[stokenzier.countTokens()];
		int index = 0;
		while (stokenzier.hasMoreTokens()) {
			tokens[index] = stokenzier.nextToken();
			index++;
		}
		return tokens[0];
	}

	@Override
	protected void doGet(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		this.doPost(req, resp);
	}
}
