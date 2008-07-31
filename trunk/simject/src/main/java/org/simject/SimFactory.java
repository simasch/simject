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
import org.simject.remote.client.HttpClientProxy;
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
	 * Container holding all configured resources
	 */
	@SuppressWarnings("unchecked")
	final private Map<Class, Object> resourceMap = new HashMap<Class, Object>();

	/**
	 * Constructor that takes 0-n configuration files
	 * 
	 * @param fileNames
	 */
	public SimFactory(final String... fileNames) {

		for (String fileName : fileNames) {
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
	@SuppressWarnings("unchecked")
	public <T> T getResource(final Class<T> clazz) {
		final Object obj = this.resourceMap.get(clazz);
		if (obj == null) {
			final String message = "Resource of type " + clazz.getName()
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
	private void loadXmlConfig(final String fileName) {
		try {
			final JAXBContext jcontext = JAXBContext
					.newInstance("org.simject.jaxb");
			final Unmarshaller unmarshaller = jcontext.createUnmarshaller();

			final InputStream istream = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(
							SimContants.DEFAULT_DIRECTORY + fileName);

			if (istream == null) {
				throw new FileNotFoundException(SimContants.DEFAULT_DIRECTORY
						+ fileName + " not found");
			}
			else {
				final Resources resources = (Resources) unmarshaller
						.unmarshal(istream);
				for (Resource resource : resources.getResource()) {
					this.createResource(resource);
				}
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
	private void createResource(final Resource resource)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, MalformedURLException {

		final String className = resource.getType();
		final Class clazz = Class.forName(className);

		Object obj = null;
		if (resource.getType().equals(EntityManager.class.getName())) {
			obj = this.createEntityManager(resource);
		}
		else if (resource.getTarget() != null
				&& resource.getTarget().startsWith("http")) {
			obj = this.createHttpClientProxy(clazz, resource.getTarget());
		}
		else {
			obj = this.createPojo(resource, clazz);
		}
		this.resourceMap.put(clazz, obj);

	}

	/**
	 * Creates a HttpClientProxy
	 * 
	 * @param clazz
	 * @param target
	 * @return
	 * @throws MalformedURLException
	 */
	private Object createHttpClientProxy(final Class<?> clazz, final String target)
			throws MalformedURLException {
		Object obj = null;

		final URL url = new URL(target);
		obj = HttpClientProxy.newInstance(Thread.currentThread()
				.getContextClassLoader(), new Class[] { clazz }, url);

		return obj;
	}

	/**
	 * Creates an simple POJO
	 * 
	 * @param resource
	 * @param clazz
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private Object createPojo(final Resource resource, final Class<?> clazz)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Object obj;
		if (resource.getTarget() == null || resource.getTarget().equals("")) {
			obj = createInstance(clazz);
		}
		else {
			final String realizedby = resource.getTarget();
			final Class<?> realizedbyClazz = Class.forName(realizedby);
			obj = createInstance(realizedbyClazz);
		}
		return obj;
	}

	/**
	 * Creates a JPA EntityManager
	 * 
	 * @param resource
	 * @return
	 */
	private Object createEntityManager(final Resource resource) {

		final Map<String, String> props = new HashMap<String, String>();
		for (Property property : resource.getProperty()) {
			props.put(property.getName(), property.getValue());
		}
		final EntityManagerFactory emf = Persistence.createEntityManagerFactory(
				resource.getName(), props);
		return emf.createEntityManager();
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
	private Object createInstance(final Class<?> clazz) throws InstantiationException,
			IllegalAccessException {

		if (clazz.isInterface()) {
			throw new InstantiationException(
					"Can not instantiate a interface. Please check configuration");
		}

		return clazz.newInstance();
	}

	/**
	 * Loops over the resource container and does the dependency injection
	 */
	private void injectDependencies() {
		try {
			for (Object obj : this.resourceMap.values()) {
				final Field[] fields = obj.getClass().getDeclaredFields();
				for (Field field : fields) {
					final Annotation[] annotations = field.getDeclaredAnnotations();
					for (Annotation annotation : annotations) {
						if (annotation.annotationType().equals(
								javax.annotation.Resource.class)) {
							final Class<?> clazz = field.getType();
							final Object value = this.resourceMap.get(clazz);
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
