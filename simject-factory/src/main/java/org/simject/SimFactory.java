package org.simject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

public class SimFactory {

	private static final Logger logger = Logger.getLogger(SimFactory.class);

	private final static String DEFAULT_DIRECTORY = "META-INF/";

	private String fileName;

	@SuppressWarnings("unchecked")
	private Map<Class, Object> resourceMap = new HashMap<Class, Object>();

	public SimFactory(String fileName) {
		this.fileName = fileName;

		this.loadXmlConfig();

		this.injectDependencies();
	}

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

	private void loadXmlConfig() {
		try {
			JAXBContext jc = JAXBContext.newInstance("org.simject.jaxb");
			Unmarshaller unmarshaller = jc.createUnmarshaller();

			InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(DEFAULT_DIRECTORY + this.fileName);

			if (is != null) {
				Resources resources = (Resources) unmarshaller.unmarshal(is);
				for (Resource resource : resources.getResource()) {
					this.createResource(resource);
				}
			}
			else {
				throw new FileNotFoundException(DEFAULT_DIRECTORY
						+ this.fileName + " not found");
			}
		}
		catch (Exception e) {
			logger.fatal(e);
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
		if (resource.getType().equals(EntityManager.class.getName())) {
			// special treatment for EntityManager
			Map<String, String> props = new HashMap<String, String>();
			for (Property property : resource.getProperty()) {
				props.put(property.getName(), property.getValue());
			}
			EntityManagerFactory emf = Persistence.createEntityManagerFactory(
					resource.getName(), props);
			obj = emf.createEntityManager();
		}
		else {
			if (resource.getTarget() == null || resource.getTarget().equals("")) {
				obj = createInstance(clazz);
			}
			else {
				String realizedby = resource.getTarget();
				Class realizedbyClazz = Class.forName(realizedby);
				obj = createInstance(realizedbyClazz);
			}
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
