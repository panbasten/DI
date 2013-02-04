package com.yonyou.bq8.di.el;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * <p>
 * This is a Map implementation driven by a data source that only provides an
 * enumeration of keys and a getValue(key) method. This class must be subclassed
 * to implement those methods.
 * 
 * <p>
 * Some of the methods may incur a performance penalty that involves enumerating
 * the entire data source. In these cases, the Map will try to save the results
 * of that enumeration, but only if the underlying data source is immutable.
 * 
 * @author PeterPan
 * 
 **/
public abstract class EnumeratedMap implements Map {
	// -------------------------------------
	// Member variables
	// -------------------------------------

	Map mMap;

	// -------------------------------------
	public void clear() {
		throw new UnsupportedOperationException();
	}

	// -------------------------------------
	public boolean containsKey(Object pKey) {
		return getValue(pKey) != null;
	}

	// -------------------------------------
	public boolean containsValue(Object pValue) {
		return getAsMap().containsValue(pValue);
	}

	// -------------------------------------
	public Set entrySet() {
		return getAsMap().entrySet();
	}

	// -------------------------------------
	public Object get(Object pKey) {
		return getValue(pKey);
	}

	// -------------------------------------
	public boolean isEmpty() {
		return !enumerateKeys().hasMoreElements();
	}

	// -------------------------------------
	public Set keySet() {
		return getAsMap().keySet();
	}

	// -------------------------------------
	public Object put(Object pKey, Object pValue) {
		throw new UnsupportedOperationException();
	}

	// -------------------------------------
	public void putAll(Map pMap) {
		throw new UnsupportedOperationException();
	}

	// -------------------------------------
	public Object remove(Object pKey) {
		throw new UnsupportedOperationException();
	}

	// -------------------------------------
	public int size() {
		return getAsMap().size();
	}

	// -------------------------------------
	public Collection values() {
		return getAsMap().values();
	}

	// -------------------------------------
	// Abstract methods
	// -------------------------------------
	/**
	 * 
	 * Returns an enumeration of the keys
	 **/
	public abstract Enumeration enumerateKeys();

	// -------------------------------------
	/**
	 * 
	 * Returns true if it is possible for this data source to change
	 **/
	public abstract boolean isMutable();

	// -------------------------------------
	/**
	 * 
	 * Returns the value associated with the given key, or null if not found.
	 **/
	public abstract Object getValue(Object pKey);

	// -------------------------------------
	/**
	 * 
	 * Converts the MapSource to a Map. If the map is not mutable, this is
	 * cached
	 **/
	public Map getAsMap() {
		if (mMap != null) {
			return mMap;
		} else {
			Map m = convertToMap();
			if (!isMutable()) {
				mMap = m;
			}
			return m;
		}
	}

	// -------------------------------------
	/**
	 * 
	 * Converts to a Map
	 **/
	Map convertToMap() {
		Map ret = new HashMap();
		for (Enumeration e = enumerateKeys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object value = getValue(key);
			ret.put(key, value);
		}
		return ret;
	}

	// -------------------------------------
}
