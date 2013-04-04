package util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author marcio
 * 
 */
//@SuppressWarnings("unchecked")
public class Transformer {

	Logger logger = Logger.getLogger(Transformer.class.getName());

	/**
	 * Complex properties may be handled by specific transformers
	 */
	private final Map<String, Class<? extends Transformer>> transformers;
	/**
	 * Class definition for target properties
	 */
	private final Map<String, Class<?>> targetInstances;
	/**
	 * Class definition for source properties
	 */
	private final Map<String, Class<?>> sourceInstances;
	/**
	 * If a property matches a specified value, the object will not be
	 * transformed
	 */
	private final Map<String, Object> invalidationConditions;
	/**
	 * Properties that will be copied or compared
	 */
	private String[] commonProperties;
	/**
	 * Properties that will be ignored in transformation or comparison
	 */
	private String[] excludedProperties;
	private Map<String, String[]> comparison;
	private String nodeName;
	
	private final Map<String, String> wirings;

	public Transformer() {
		this.transformers = new HashMap<String, Class<? extends Transformer>>();
		this.targetInstances = new HashMap<String, Class<?>>();
		this.sourceInstances = new HashMap<String, Class<?>>();
		this.wirings = new HashMap<String, String>();
		this.invalidationConditions = new HashMap<String, Object>();
		this.commonProperties = null;
		this.excludedProperties = null;
	}

	public Transformer(Class<?> commonInterface) {
		this.transformers = new HashMap<String, Class<? extends Transformer>>();
		this.targetInstances = new HashMap<String, Class<?>>();
		this.sourceInstances = new HashMap<String, Class<?>>();
		this.wirings = new HashMap<String, String>();
		this.invalidationConditions = new HashMap<String, Object>();
		this.commonProperties = getCommonProperties(commonInterface);
		this.excludedProperties = null;
	}

	/**
	 * Copies property from object "source" to object "target"
	 * 
	 * @param <T>
	 *            target type
	 * @param <S>
	 *            source type
	 * @param target
	 *            target object
	 * @param source
	 *            source object
	 * @param exclusions
	 *            properties that will be ignored
	 * @return
	 */
	public <T, S> T transform(S source, T target, String... exclusions) {

		if (source == null) {
			return null;
		}

		excludedProperties = exclusions;

		copyProperties(source, target);

		return target;
	}

	/**
	 * Transforms a collection with elements of type T2 into a collection with
	 * elements of type T1
	 * 
	 * @param <T>
	 *            target type
	 * @param <S>
	 *            source type
	 * @param targetClass
	 *            target class
	 * @param sourceCollection
	 *            source collection
	 * @param exclusions
	 *            properties that will be ignored
	 * @return collection resulting of the transformation of each element in
	 *         sourceCollection
	 */
	public <T, S> Collection<T> transformCollection(
			Collection<S> sourceCollection, Class<T> targetClass,
			String... exclusions) {

		try {

			Collection<T> targetCollection = getCollectionInstance(sourceCollection);

			T target = null;

			for (S source : sourceCollection) {

				target = transform(source, targetClass.newInstance(), exclusions);
				targetCollection.add(target);

			}

			return targetCollection;

		} catch (InstantiationException e) {
			logger.throwing(getClass().getName(), "transformCollection", e);
		} catch (IllegalAccessException e) {
			logger.throwing(getClass().getName(), "transformCollection", e);
		}

		return null;
	}

	/**
	 * Try to get the best Collection instance. Some classes in Collection
	 * framework are private (e.g EmptyList), so a workaround is needed to
	 * return a proper instance.
	 * 
	 * @param <T>
	 * @param source
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T,G> Collection<G> getCollectionInstance(Collection<T> source) {

		Collection<G> instance = null;

		try {
			
			instance = source.getClass().newInstance();

		} catch (IllegalAccessException e) {

			logger.fine("Cannot access collection instance. Trying to find the best alternative.");

			// workaround
			if (source instanceof Deque) {

				instance = new ArrayDeque<G>();

			} else if (source instanceof Queue) {

				instance = new ArrayBlockingQueue<G>(source.size());

			} else if (source instanceof Set) {

				instance = new HashSet<G>();

			} else {

				instance = new ArrayList<G>();
			}

		} catch (InstantiationException e) {
			logger.throwing(getClass().getName(), "getCollectionInstance", e);
		}

		return instance;
	}

	/**
	 * Compare two objects creating a map of <propertyName,
	 * {sourcePropertyValue, targetPropertyValue}>
	 * 
	 * @param <S>
	 *            source type
	 * @param <T>
	 *            target type
	 * @param source
	 * @param target
	 * @param exclusions
	 *            properties that will be ignored
	 * @return comparison map
	 */
	public <S, T> Map<String, String[]> compare(S source, T target,
			String... exclusions) {

		if (target == null && source == null) {
			return null;
		}

		excludedProperties = exclusions;
		if (comparison == null) {
			comparison = new HashMap<String, String[]>();
		}

		compareProperties(source, target);

		Map<String, String[]> result = comparison;
		comparison = null;

		return result;
	}

	private <T> T copyProperties(Object source, T target) {

		Map<String,?> sourceValues = describe(source, commonProperties);
		Map<String,?> targetValues = describe(target, commonProperties);

		processInvalidationConditions(source, sourceValues);

		Iterator<String> it = sourceValues.keySet().iterator();
		String key, targetKey;
		Class<?> targetPropertyClass = null;
		Class<?> sourcePropertyClass = null;
		Object targetPropertyValue;

		if ((sourceValues != null) && (targetValues != null)) {
			while (it.hasNext()) {

				sourcePropertyClass = null;
				targetPropertyClass = null;

				key = it.next();
				targetKey = key;

				if (sourceValues.get(key) != null) {

					try {

						if (!targetValues.containsKey(targetKey)) {
							
							if(wirings.containsKey(key)) {
								
								targetKey = wirings.get(key);
								
								if (!targetValues.containsKey(targetKey)) {
									continue;
								}
								
							} else {
							
								continue;
							}

						} else if (targetInstances.containsKey(targetKey)) {
							// override property classes	
							targetPropertyClass = targetInstances.get(targetKey);
							sourcePropertyClass = sourceInstances.get(key);

						} else { //if (targetValues.get(key) == null) {
								
							//targetPropertyClass = getPropertyClass(target, key);
							targetPropertyClass = PropertyUtils.getPropertyType(target, targetKey);
						}
						
						if (sourcePropertyClass == null) {
							sourcePropertyClass = PropertyUtils.getPropertyType(source, key);
						}

						if ((targetPropertyClass.equals(sourcePropertyClass))
								|| (targetPropertyClass.isPrimitive())
								|| (isSimpleClass(targetPropertyClass))
								|| (isSimpleClass(sourcePropertyClass))) {

							PropertyUtils.setProperty(target, targetKey,
									targetPropertyClass.cast(PropertyUtils
											.getProperty(source, key)));

						} else {

							targetPropertyValue = getTransformer(key).transform(
									PropertyUtils.getProperty(source, key),
									targetPropertyClass.newInstance());

							PropertyUtils.setProperty(target, targetKey,
									targetPropertyValue);

						}

					} catch (Exception e) {

						logger.throwing(getClass().getSimpleName(),
								"copyProperties", e);
						logger.info("Could not copy property " + key + " = "
								+ sourceValues.get(key) + " from class "
								+ source.getClass().getSimpleName() + " to property "
								+ targetKey + " in class "
								+ target.getClass().getSimpleName());
                                                e.printStackTrace();

					}
				}

			}
                }
		return target;
        }

	private void compareProperties(Object source, Object target) {

		Map<String, Object> sourceValues = null;
		Map<String, Object> targetValues = null;
		Set<String> keys = null;

		Class<?> targetPropertyClass = null;
		Class<?> sourcePropertyClass = null;
		Object targetPropertyValue = null;
		Object sourcePropertyValue = null;
		boolean complexTypeFlag;

		if (source != null && target != null) {

			sourceValues = describe(source, commonProperties);
			targetValues = describe(target, commonProperties);

			if (sourceValues.size() < targetValues.size()) {
				keys = sourceValues.keySet();
			
			} else {
				keys = targetValues.keySet();
			}

			processInvalidationConditions(target, targetValues);
			processInvalidationConditions(source, sourceValues);

		} else if (source != null) {

			sourceValues = describe(source, commonProperties);
			if (sourceValues != null) {
				keys = sourceValues.keySet();
				processInvalidationConditions(source, sourceValues);
			}

		} else if (target != null) {

			targetValues = describe(target, commonProperties);
			if (targetValues != null) {
				keys = targetValues.keySet();
				processInvalidationConditions(target, targetValues);
			}

		} else {
			throw new RuntimeException(
					"At least one argument must be not null!");
		}

		if ((sourceValues != null) || (targetValues != null))
			for (String key : keys) {

				try {

					sourcePropertyClass = null;
					targetPropertyClass = null;

					if (targetValues != null) {
						targetPropertyValue = targetValues.get(key);
					
					} else {
						targetPropertyValue = null;
					}

					if (sourceValues != null) {
						sourcePropertyValue = sourceValues.get(key);
					
					} else {
						sourcePropertyValue = null;
					}

					complexTypeFlag = false;

					if ((sourcePropertyValue == null && targetPropertyValue == null)) {
						continue;

					} else if (targetPropertyValue == null) {

						sourcePropertyClass = getPropertyClass(source, key);

					} else if (sourcePropertyValue == null) {

						targetPropertyClass = getPropertyClass(target, key);

					} else {

						sourcePropertyClass = getPropertyClass(source, key);
						targetPropertyClass = getPropertyClass(target, key);
					}

					if (sourcePropertyClass != null
							&& !sourcePropertyClass.isPrimitive()
							&& !isSimpleClass(sourcePropertyClass)) {

						complexTypeFlag = true;
					}

					if (targetPropertyClass != null
							&& !targetPropertyClass.isPrimitive()
							&& !isSimpleClass(targetPropertyClass)) {

						complexTypeFlag = true;
					}

					if (!complexTypeFlag) {

						String aux;

						if (source != null
								&& (nodeName == null || nodeName.equals("")))
							nodeName = source.getClass().getSimpleName();
						else if (target != null
								&& (nodeName == null || nodeName.equals("")))
							nodeName = target.getClass().getSimpleName();

						aux = nodeName + "." + key;

						String dValue = null;
						if (targetPropertyValue != null)
							dValue = targetValues.get(key).toString();

						String sValue = null;
						if (sourcePropertyValue != null)
							sValue = sourceValues.get(key).toString();

						if (dValue != null && sValue != null
								&& dValue.equals(sValue))
							continue;
						else if (dValue != null || sValue != null)
							comparison
									.put(aux, new String[] { dValue, sValue });

					} else {

						Transformer t = getTransformer(key);
						if (nodeName != null && !nodeName.equals(""))
							t.nodeName = nodeName;
						else if (source != null)
							t.nodeName = source.getClass().getSimpleName();
						else if (target != null)
							t.nodeName = target.getClass().getSimpleName();

						t.nodeName += "." + key;

						t.comparison = comparison;
						t.compare(targetPropertyValue, sourcePropertyValue,
								excludedProperties);

					}

				} catch (Exception e) {
					logger.throwing(getClass().getSimpleName(),
							"compareProperties", e);
					logger.info("Could not compare property: " + key + " in "
							+ source.getClass().getSimpleName() + " = "
							+ sourcePropertyValue + " with "
							+ target.getClass().getSimpleName() + " = "
							+ targetPropertyValue);
				}
			}

	}

	private Transformer getTransformer(String property) {

		if (transformers.get(property) != null) {

			try {

				return transformers.get(property).newInstance();

			} catch (Exception e) {
				logger
						.throwing(getClass().getSimpleName(), "getTransformer",
								e);
			}

		} else {

			return new Transformer();
		}

		return new Transformer();
	}

	/**
	 * Get the class of a property. It's necessary since the value returned by a
	 * getter method may be "null"
	 * 
	 * @param obj
	 * @param key
	 * @return property class
	 */
	private Class<?> getPropertyClass(Object obj, String key) {

		Method[] methods = obj.getClass().getMethods();

		for (Method m : methods) {

			if (m.getName().equalsIgnoreCase("get" + key))
				return m.getReturnType();
		}

		for (Method m : methods) {

			if (m.getName().equalsIgnoreCase("is" + key))
				return m.getReturnType();
		}

		return null;
	}

	/**
	 * Extracts values and property names from an object
	 * 
	 * @param obj
	 * @param desiredProperties
	 *            filter with the properties to be considered
	 * @return map of <propertyName, propertyValue>
	 */
	private Map<String, Object> describe(Object obj,
			String... desiredProperties) {

		Map<String, Object> properties = null;
		Object value = null;

		if (desiredProperties != null) {

			properties = new HashMap<String, Object>();

			for (String property : desiredProperties) {

				try {

					PropertyDescriptor pd = PropertyUtils
							.getPropertyDescriptor(obj, property);

					if (pd != null) {

						value = PropertyUtils.getProperty(obj, property);

						properties.put(property, value);
					}

				} catch (Exception e) {
					logger.throwing(getClass().getSimpleName(), "describe", e);
				}

			}

		} else {

			try {

				properties = new HashMap<String, Object>();

				PropertyDescriptor[] pDescriptors = PropertyUtils
						.getPropertyDescriptors(obj);

				if (pDescriptors != null)
					for (PropertyDescriptor pd : pDescriptors) {

						try {

							value = PropertyUtils.getProperty(obj, pd
									.getName());

						} catch (NoSuchMethodException e) {
							logger.fine("Property not found: " + pd.getName());
							logger.throwing(getClass().getSimpleName(),
									"describe", e);
						}

						if (!pd.getName().equals("class")
								&& !pd.getName().equals("DAOClass")
								&& !pd.getName().equals("repository")
								&& !pd.getName().equals("transformer")
								&& !pd.getName().equals("queryCacheable")
								&& !pd.getName().equals("queryCacheRegion")
								&& !pd.getName().equals("persistenceUnit"))
							properties.put(pd.getName(), value);
					}

			} catch (Exception e) {
				logger.throwing(getClass().getSimpleName(), "describe", e);
			}

		}

		// excluding undesired properties
		if (excludedProperties != null && properties != null)
			for (String excluded : excludedProperties) {

				properties.remove(excluded);
			}

		List<String> tempExcludedProperties = new ArrayList<String>();

		// handle more complex exclusions. Ex: "ClassName.property1.property2"
		if (properties != null && properties.keySet() != null) {
			for (String key : properties.keySet()) {

				String aux = null;

				if (nodeName != null && !nodeName.equals(""))
					aux = nodeName + "." + key;
				else
					aux = obj.getClass().getSimpleName() + "." + key;

				for (String excluded : excludedProperties) {

					if (excluded.equals(aux)) {
						// Can't do it (would raise exception):
						// properties.remove(key);

						// Alternative
						tempExcludedProperties.add(key);
					}
				}
			}
		}

		if (tempExcludedProperties != null && properties != null)
			for (String excluded : tempExcludedProperties) {

				properties.remove(excluded);
			}

		return properties;

	}

	/**
	 * Excludes the properties that match an invalidation condition.
	 * 
	 * @param obj
	 * @param properties
	 * @return
	 */
	private Map<String, ?> processInvalidationConditions(Object obj,
			Map<String, ?> properties) {

		Object invalidationValue;
		Object value;

		Iterator<String> it = invalidationConditions.keySet().iterator();
		String key;

		while (it.hasNext()) {

			key = it.next();

			invalidationValue = invalidationConditions.get(key);

			try {

				value = PropertyUtils.getProperty(obj, key);

				// if property value matches invalidation condition 
				// it will be excluded from transformation
				if (invalidationValue.equals(value))
					properties.remove(key.split("\\.")[0]);

			} catch (NestedNullException e) {

				logger.finer("Invalidation condition \"" + key
						+ "\" has no effect");

			} catch (Exception e) {
				logger.throwing(getClass().getSimpleName(),
						"processInvalidationConditions", e);
			}

		}

		return properties;
	}

	/**
	 * Extract the properties of a common interface. If set, only these
	 * properties will be considered for transformation
	 * 
	 * @param commonInterface
	 * @return
	 */
	private String[] getCommonProperties(Class<?> commonInterface) {

		if (commonInterface != null) {

			PropertyDescriptor[] pd = PropertyUtils
					.getPropertyDescriptors(commonInterface);

			if ((pd == null) || (pd.length == 0))
				return null;

			String[] properties = new String[pd.length];

			for (int i = 0; i < pd.length; i++) {

				properties[i] = pd[i].getName();
			}

			return properties;

		} else {

			return null;
		}

	}

	/**
	 * Identifies classes that don't need to be transformed (wrapper classes,
	 * String, etc), that is, can be directly copied
	 * 
	 * @param clazz
	 * @return
	 */
	private boolean isSimpleClass(Class<?> clazz) {

		Class<?>[] classes = { Short.class, Byte.class, Long.class, Integer.class,
				Double.class, Character.class, Boolean.class, Float.class,
				String.class };

		for (Class<?> c : classes)
			if (c.equals(clazz))
				return true;

		return false;
	}

	protected final void addEntityProperty(String propertyName,
			Class<?> sourceClass, Class<?> targetClass, Class<? extends Transformer> transformerClass) {

		this.targetInstances.put(propertyName, targetClass);
		this.sourceInstances.put(propertyName, sourceClass);

		transformers.put(propertyName, transformerClass);

	}

	protected final void addInvalidationCondition(String propertyName,
			Object value) {

		this.invalidationConditions.put(propertyName, value);

	}

	public void setCommonInterface(Class<?> commonInterface) {

		this.commonProperties = getCommonProperties(commonInterface);
	}

	/**
	 * @param excludedProperties
	 *            the excludedProperties to set
	 */
	public final void setExcludedProperties(String[] excludedProperties) {
		this.excludedProperties = excludedProperties;
	}
	
	public final void addWiring(String sourceProperty, String targetProperty) {
		this.wirings.put(sourceProperty, targetProperty);
	}

}