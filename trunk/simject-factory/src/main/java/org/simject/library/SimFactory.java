package org.simject.library;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.simject.exception.SimConfigException;
import org.simject.exception.SimResourceNotFoundException;
import org.simject.jaxb.Resource;
import org.simject.jaxb.Resources;

public class SimFactory {

	private final static Logger logger = Logger.getLogger(SimFactory.class
			.getName());

	private final static String DEFAULT_FILENAME = "META-INF/resources.xml";

	private String name;

	@SuppressWarnings("unchecked")
	private Map<Class, Object> resourceMap = new HashMap<Class, Object>();

	public SimFactory(String name) {
		this.name = name;

		this.loadXmlConfig();
	}

	public <T> T getResource(Class<T> clazz) {
		Object obj = this.resourceMap.get(clazz);
		if (obj == null) {
			String message = "Resource of type " + clazz.getName()
					+ " not found";
			logger.severe(message);
			throw new SimResourceNotFoundException(message);
		}
		return (T) obj;
	}

	private void loadXmlConfig() {
		try {
			JAXBContext jc = JAXBContext.newInstance("org.simject.jaxb");
			Unmarshaller unmarshaller = jc.createUnmarshaller();

			InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(DEFAULT_FILENAME);

			if (is != null) {
				Resources resources = (Resources) unmarshaller.unmarshal(is);
				for (Resource resource : resources.getResource()) {
					this.createResource(resource);
				}
			}
			else {
				throw new FileNotFoundException(DEFAULT_FILENAME + " not found");
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new SimConfigException(e.getMessage(), e);
		}

	}

	@SuppressWarnings("unchecked")
	private void createResource(Resource resource)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		String className = resource.getType();
		Class clazz = Class.forName(className);

		Object obj = null;
		if (resource.getRealizedby() == null
				|| resource.getRealizedby().equals("")) {
			obj = createInstance(clazz);
		}
		else {
			String realizedby = resource.getRealizedby();
			Class realizedbyClazz = Class.forName(realizedby);
			obj = createInstance(realizedbyClazz);
		}
		this.resourceMap.put(clazz, obj);

	}

	private Object createInstance(Class clazz) throws InstantiationException,
			IllegalAccessException {

		if (clazz.isInterface()) {
			throw new InstantiationException(
					"Can not instantiate a interface. Please check configuration");
		}
		Object obj = clazz.newInstance();
		return obj;
	}
}
