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
package org.simject.remote.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;

import org.apache.log4j.Logger;
import org.simject.SimFactory;
import org.simject.util.SimContants;

import com.thoughtworks.xstream.XStream;

/**
 * Used to provide remote access over HTTP to a resource
 * 
 * @author Simon Martinelli
 */
public class SimServerServlet extends HttpServlet {

	private final static Logger logger = Logger
			.getLogger(SimServerServlet.class);

	/**
	 * Context parameter that contains the configuration file location
	 */
	private final static String SIMJECT_CONFIG = "simjectConfig";

	/**
	 * SimFactory
	 */
	private SimFactory simFactory;

	@Override
	public void init() throws ServletException {
		// Get the context parameter with the config file
		String configFile = (String) this.getServletContext().getInitParameter(
				SIMJECT_CONFIG);
		// create a SimFactory based on the config file
		simFactory = new SimFactory(configFile);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		try {
			// get the classname, create a Class instance and get the resource
			// from the SimFactory
			String className = this.getClassName(req);
			Class clazz = Class.forName(className);
			Object obj = this.simFactory.getResource(clazz);
			// get the arguments passed by the client and invoke the desired
			// method
			Object[] args = this.getArguments(req);
			this.invokeMethod(req, resp, obj, args);
		}
		catch (Exception e) {
			// if exception occurs log it
			logger.fatal(e);
			e.printStackTrace();
		}
	}

	/**
	 * Invokes a method based on the parameters passed from the client
	 * 
	 * @param req
	 * @param resp
	 * @param obj
	 * @param args
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private void invokeMethod(HttpServletRequest req, HttpServletResponse resp,
			Object obj, Object[] args) throws IllegalAccessException,
			InvocationTargetException, IOException, ClassNotFoundException,
			SecurityException, NoSuchMethodException {

		// get the method name from the HTTP header
		String methodString = req.getHeader(SimContants.PARAMETER_METHOD);
		logger.debug("methodString: " + methodString);
		// get the parameter types from the HTTP header
		String parameterTypesString = req
				.getHeader(SimContants.PARAMETER_TYPES);

		Object result = null;
		if (parameterTypesString == null) {
			// method without parameters to invoke
			Method method = obj.getClass().getMethod(methodString);
			result = method.invoke(obj);
		}
		else {
			// method with parameters should be called. convert the string from
			// the header to Class array
			Class[] parameterTypes = this
					.getParameterTypes(parameterTypesString);
			Method method = obj.getClass().getMethod(methodString,
					parameterTypes);

			logger.debug("args: " + args);
			result = method.invoke(obj, args);
		}
		if (result != null) {
			// if there is a result use XStream to serialize it to XML
			XStream xstream = new XStream();
			String xml = xstream.toXML(result);
			resp.getWriter().write(xml);
		}
	}

	/**
	 * Returns an array of Class containing the types of the parameters
	 * 
	 * @param parameterString
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class[] getParameterTypes(String parameterString)
			throws ClassNotFoundException {
		Class[] parameters = new Class[0];
		if (parameterString != null) {
			// The parameter types are seperated by ","
			StringTokenizer st = new StringTokenizer(parameterString,
					SimContants.PARAMETER_TYPE_DELIMITER);
			parameters = new Class[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				String className = st.nextToken();
				logger.debug("parameterTyp: " + className);

				Class clazz = Class.forName(className);
				parameters[i] = clazz;
				i++;
			}
		}
		return parameters;
	}

	/**
	 * Uses XStream to convert XML to Object
	 * 
	 * @param req
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Object[] getArguments(HttpServletRequest req) throws IOException,
			ClassNotFoundException {

		String xml = this.inputStreamToString(req.getInputStream());
		logger.debug(xml);

		XStream xstream = new XStream();
		Object args = xstream.fromXML(xml);

		return (Object[]) args;
	}

	/**
	 * Helper Method to convert InputStream to String
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private String inputStreamToString(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * Parses the class name out of the path info
	 * 
	 * @param req
	 * @return
	 */
	private String getClassName(HttpServletRequest req) {
		StringTokenizer st = new StringTokenizer(req.getPathInfo(), "/");
		String[] tokens = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			tokens[i] = st.nextToken();
			logger.debug("path token: " + tokens[i]);
			i++;
		}
		String className = tokens[0];
		return className;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doPost(req, resp);
	}
}
