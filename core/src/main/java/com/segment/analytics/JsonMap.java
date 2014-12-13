package com.segment.analytics;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/**
 * A {@link Map} wrapper to expose Json functionality. All methods will be forwarded to a delegate
 * map.
 * <p>
 * The purpose of this class is to not limit clients to a custom implementation of a Json type,
 * they can use existing {@link Map} and {@link java.util.List} implementations as they see fit. It
 * adds some utility methods, such as methods to coerce numeric types from Strings, and a {@link
 * #putValue(String, Object)} to be able to chain method calls.
 * <p>
 * Although it lets you use custom objects for values, note that type information is lost during
 * serialization. You should use one of the coercion methods instead to get objects of a concrete
 * type.
 */
class JsonMap implements Map<String, Object> {
  private final Map<String, Object> delegate;

  JsonMap() {
    delegate = new LinkedHashMap<String, Object>();
  }

  JsonMap(Map<String, Object> map) {
    if (map == null) {
      throw new IllegalArgumentException("Map must not be null.");
    }
    this.delegate = map;
  }

  @Override public void clear() {
    delegate.clear();
  }

  @Override public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  @Override public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  @Override public Set<Entry<String, Object>> entrySet() {
    return delegate.entrySet();
  }

  @Override public Object get(Object key) {
    return delegate.get(key);
  }

  @Override public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override public Set<String> keySet() {
    return delegate.keySet();
  }

  @Override public Object put(String key, Object value) {
    return delegate.put(key, value);
  }

  @Override public void putAll(Map<? extends String, ?> map) {
    delegate.putAll(map);
  }

  @Override public Object remove(Object key) {
    return delegate.remove(key);
  }

  @Override public int size() {
    return delegate.size();
  }

  @Override public Collection<Object> values() {
    return delegate.values();
  }

  @Override public boolean equals(Object object) {
    return object == this || delegate.equals(object);
  }

  @Override public int hashCode() {
    return delegate.hashCode();
  }

  @Override public String toString() {
    return delegate.toString();
  }

  /** Helper method to be able to chain put methods. */
  JsonMap putValue(String key, Object value) {
    delegate.put(key, value);
    return this;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a integer or can be coerced to a
   * integer. Returns defaultValue otherwise.
   */
  int getInt(String key, int defaultValue) {
    Object value = get(key);
    if (value instanceof Integer) {
      return (int) value;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof String) {
      try {
        return Integer.valueOf((String) value);
      } catch (NumberFormatException ignored) {
        // ignore
      }
    }
    return defaultValue;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a long or can be coerced to a
   * long. Returns defaultValue otherwise.
   */
  long getLong(String key, long defaultValue) {
    Object value = get(key);
    if (value instanceof Long) {
      return (long) value;
    }
    if (value instanceof Number) {
      return ((Number) value).longValue();
    } else if (value instanceof String) {
      try {
        return Long.valueOf((String) value);
      } catch (NumberFormatException ignored) {
        // ignore
      }
    }
    return defaultValue;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a double or can be coerced to a
   * double. Returns defaultValue otherwise.
   */
  double getDouble(String key, double defaultValue) {
    Object value = get(key);
    if (value instanceof Double) {
      return (double) value;
    }
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    } else if (value instanceof String) {
      try {
        return Double.valueOf((String) value);
      } catch (NumberFormatException ignored) {
        // ignore
      }
    }
    return defaultValue;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a char or can be coerced to a
   * char. Returns defaultValue otherwise.
   */
  char getChar(String key, char defaultValue) {
    Object value = get(key);
    if (value instanceof Character) {
      return (Character) value;
    }
    if (value != null && value instanceof String) {
      if (((String) value).length() == 1) {
        return ((String) value).charAt(0);
      }
    }
    return defaultValue;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a string or can be coerced to a
   * string. Returns null otherwise.
   * <p>
   * This will return null only if the value does not exist, since all types can have a String
   * representation.
   */
  String getString(String key) {
    Object value = get(key);
    if (value instanceof String) {
      return (String) value;
    } else if (value != null) {
      return String.valueOf(value);
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a boolean or can be coerced to a
   * boolean. Returns defaultValue otherwise.
   */
  boolean getBoolean(String key, boolean defaultValue) {
    Object value = get(key);
    if (value instanceof Boolean) {
      return (boolean) value;
    } else if (value instanceof String) {
      return Boolean.valueOf((String) value);
    }
    return defaultValue;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a enum or can be coerced to a
   * enum.
   * Returns null otherwise.
   */
  <T extends Enum<T>> T getEnum(Class<T> enumType, String key) {
    if (enumType == null) {
      throw new IllegalArgumentException("enumType may not be null");
    }
    Object value = get(key);
    if (enumType.isInstance(value)) {
      return (T) value;
    } else if (value instanceof String) {
      String stringValue = (String) value;
      return Enum.valueOf(enumType, stringValue);
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a JsonMap. Returns null otherwise.
   */
  JsonMap getJsonMap(Object key) {
    Object value = get(key);
    if (value instanceof JsonMap) {
      return (JsonMap) value;
    } else if (value instanceof Map) {
      return new JsonMap((Map<String, Object>) value);
    } else {
      return null;
    }
  }

  /**
   * Returns the value mapped by {@code key} if it exists and if it can be coerced to the given
   * type. The JsonMap subclass MUST have a map constructor.
   */
  <T extends JsonMap> T getJsonMap(String key, Class<T> clazz) {
    Object value = get(key);
    T typedValue = castToJsonMap(value, clazz);
    if (typedValue != null) put(key, typedValue);
    return typedValue;
  }

  /** Coerce an object to a JsonMap. */
  private <T extends JsonMap> T castToJsonMap(Object object, Class<T> clazz) {
    if (clazz.isInstance(object)) {
      //noinspection unchecked
      return (T) object;
    } else if (object instanceof Map) {
      // Try the map constructor, it's more efficient since we've already parsed the json tree
      try {
        Constructor<T> constructor = clazz.getDeclaredConstructor(Map.class);
        constructor.setAccessible(true);
        return constructor.newInstance(object);
      } catch (NoSuchMethodException ignored) {
      } catch (InvocationTargetException ignored) {
      } catch (InstantiationException ignored) {
      } catch (IllegalAccessException ignored) {
      }
      throw new AssertionError("Could not find map constructor for " + clazz.getCanonicalName());
    }
    return null;
  }

  /**
   * Returns the value mapped by {@code key} if it exists and is a List of {@code T}. Returns null
   * otherwise.
   */
  <T extends JsonMap> List<T> getJsonList(Object key, Class<T> clazz) {
    Object value = get(key);
    if (value instanceof List) {
      List list = (List) value;
      try {
        ArrayList<T> real = new ArrayList<T>();
        for (Object item : list) {
          T typedValue = castToJsonMap(item, clazz);
          if (typedValue != null) {
            real.add(typedValue);
          }
        }
        return real;
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  JSONObject toJsonObject() {
    return new JSONObject(delegate);
  }

  Map<String, String> toStringMap() {
    Map<String, String> map = new HashMap<String, String>();
    for (Map.Entry<String, Object> entry : entrySet()) {
      map.put(entry.getKey(), String.valueOf(entry.getValue()));
    }
    return map;
  }
}
