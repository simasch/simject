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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.simject.SimFactory;
import org.simject.util.SimContants;

/**
 * Used to provide remote access over HTTP to a resource
 * 
 * @author Simon Martinelli
 */
public class SimServerServlet extends HttpServlet {

	private final static Logger logger = Logger
			.getLogger(SimServerServlet.class);

	private final static String SIMJECT_CONFIG = "simjectConfig";

	private SimFactory simFactory;

	@Override
	public void init() throws ServletException {
		String configFile = (String) this.getServletContext().getInitParameter(
				SIMJECT_CONFIG);
		simFactory = new SimFactory(configFile);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		try {
			String className = this.getClassName(req);
			Class clazz = Class.forName(className);
			Object obj = this.simFactory.getResource(clazz);

			Object args = this.getArguments(req);
			this.invokeMethod(req, resp, obj, args);
		}
		catch (Exception e) {
			logger.fatal(e);
			e.printStackTrace();
		}
	}

	/**
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
			Object obj, Object args) throws IllegalAccessException,
			InvocationTargetException, IOException, ClassNotFoundException,
			SecurityException, NoSuchMethodException {
		String methodString = req.getParameter(SimContants.PARAMETER_METHOD);

		String parameterString = req.getParameter(SimContants.PARAMETER_TYPES);

		Class[] parameters = this.getParameterTypes(parameterString);
		Method method = obj.getClass().getMethod(methodString, parameters);
		Object result = null;
		if (args != null) {
			result = method.invoke(obj, args);
		}
		else {
			result = method.invoke(obj);
		}
		if (result != null) {
			OutputStream os = resp.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(result);
		}
	}

	/**
	 * @param parameterString
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class[] getParameterTypes(String parameterString)
			throws ClassNotFoundException {
		Class[] parameters = new Class[0];
		if (parameterString != null) {
			StringTokenizer st = new StringTokenizer(parameterString);
			parameters = new Class[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				Class clazz = Class.forName(st.nextToken());
				parameters[i] = clazz;
				i++;
			}
		}
		return parameters;
	}

	/**
	 * @param req
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Object getArguments(HttpServletRequest req) throws IOException,
			ClassNotFoundException {
		InputStream is = req.getInputStream();
		Object args = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(is);
			args = ois.readObject();
		}
		catch (EOFException e) {
			// nothing to read so no parameters present!
		}
		return args;
	}

	/**
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
