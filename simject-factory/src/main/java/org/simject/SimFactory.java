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
package org.simject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.simject.exception.SimConfigException;
import org.simject.exception.SimResourceNotFoundException;
import org.simject.jaxb.Property;
import org.simject.jaxb.Resource;
import org.simject.jaxb.Resources;
import org.simject.remote.HttpClientProxy;
import org.simject.util.SimContants;

/**
 * SimFactory represents the IOC container. It parses the XML configuration
 * files, does the dependency injection and provides methods for retrieving
 * resources.
 * 
 * @author Simon Martinelli
 */
public class SimFactory {

	private static final Logger logger = Logger.getLogger(SimFactory.class);

	/**
	 * Holds the filenames
	 */
	private String[] fileNames;

	/**
	 * Container holding all configured resources
	 */
	@SuppressWarnings("unchecked")
	private Map<Class, Object> resourceMap = new HashMap<Class, Object>();

	/**
	 * Constructor that takes 0-n configuration files
	 * 
	 * @param fileNames
	 */
	public SimFactory(String... fileNames) {
		this.fileNames = new String[fileNames.length];

		int i = 0;
		for (String fileName : fileNames) {
			this.fileNames[i] = fileName;
			this.loadXmlConfig(fileName);
		}
		this.injectDependencies();
	}

	/**
	 * Generic method to retrieve a resource identified by <type> If <type> is
	 * an interface, <target> must be present. If <type> is
	 * javax.persistence.EntityManager a special treatment will occur
	 * 
	 * @param <T>
	 * @param clazz
	 *            must be the type configured in the config file
	 * @return an instance of the desired type
	 */
	public <T> T getResource(Class<T> clazz) {
		Object obj = this.resourceMap.get(clazz);
		if (obj == null) {
			String message = "Resource of type " + clazz.getName()
					+ " not found";
			logger.fatal(message);
			throw new SimResourceNotFoundException(message);
		}
		return (T) obj;
	}

	/**
	 * Uses JAXB to parse the config file
	 * 
	 * @param fileName
	 */
	private void loadXmlConfig(String fileName) {
		try {
			JAXBContext jc = JAXBContext.newInstance("org.simject.jaxb");
			Unmarshaller unmarshaller = jc.createUnmarshaller();

			InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(
							SimContants.DEFAULT_DIRECTORY + fileName);

			if (is != null) {
				Resources resources = (Resources) unmarshaller.unmarshal(is);
				for (Resource resource : resources.getResource()) {
					this.createResource(resource);
				}
			}
			else {
				throw new FileNotFoundException(SimContants.DEFAULT_DIRECTORY
						+ fileName + " not found");
			}
		}
		catch (Exception e) {
			logger.fatal(e);
			throw new SimConfigException(e.getMessage(), e);
		}
	}

	/**
	 * Creates an new Resource and stores it in the resource container
	 * 
	 * @param resource
	 *            the resource retrieved from config file
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws MalformedURLException
	 */
	@SuppressWarnings("unchecked")
	private void createResource(Resource resource)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, MalformedURLException {
		String className = resource.getType();
		Class clazz = Class.forName(className);

		Object obj = null;
		if (resource.getType().equals(EntityManager.class.getName())) {
			obj = this.createEntityManager(resource);
		}
		else if (resource.getTarget() != null
				&& 	resource.getTarget().startsWith("http")) {
			obj = this.createHttpClientProxy(clazz, resource.getTarget());
		}
		else {
			obj = this.createPojo(resource, clazz);
		}
		this.resourceMap.put(clazz, obj);

	}

	private Object createHttpClientProxy(Class clazz, String target)
			throws MalformedURLException {
		Object obj = null;

		URL url = new URL(target);
		obj = HttpClientProxy.newInstance(Thread.currentThread()
				.getContextClassLoader(), new Class[] { clazz }, url);

		return obj;
	}

	private Object createPojo(Resource resource, Class clazz)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Object obj;
		if (resource.getTarget() == null || resource.getTarget().equals("")) {
			obj = createInstance(clazz);
		}
		else {
			String realizedby = resource.getTarget();
			Class realizedbyClazz = Class.forName(realizedby);
			obj = createInstance(realizedbyClazz);
		}
		return obj;
	}

	private Object createEntityManager(Resource resource) {
		Object obj;
		// special treatment for EntityManager
		Map<String, String> props = new HashMap<String, String>();
		for (Property property : resource.getProperty()) {
			props.put(property.getName(), property.getValue());
		}
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(
				resource.getName(), props);
		obj = emf.createEntityManager();
		return obj;
	}

	/**
	 * Creates an instance of the provided Class. If Class is an Interface an
	 * Exception is thrown.
	 * 
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Object createInstance(Class clazz) throws InstantiationException,
			IllegalAccessException {

		if (clazz.isInterface()) {
			throw new InstantiationException(
					"Can not instantiate a interface. Please check configuration");
		}
		Object obj = clazz.newInstance();
		return obj;
	}

	/**
	 * Loops over the resource container and does the dependency injection
	 */
	private void injectDependencies() {
		try {
			for (Object obj : this.resourceMap.values()) {
				Field[] fields = obj.getClass().getDeclaredFields();
				for (Field field : fields) {
					Annotation[] annotations = field.getDeclaredAnnotations();
					for (Annotation annotation : annotations) {
						if (annotation.annotationType().equals(
								javax.annotation.Resource.class)) {
							Class clazz = field.getType();
							Object value = this.resourceMap.get(clazz);
							field.setAccessible(true);
							field.set(obj, value);
						}
					}
				}
			}
		}
		catch (Exception e) {
			logger.fatal(e);
			throw new SimConfigException(e.getMessage(), e);
		}
	}
}
