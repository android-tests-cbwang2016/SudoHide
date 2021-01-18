package com.sudocode.sudohide;

import java.util.HashMap;

@SuppressWarnings("ConstantConditions")
public class PrefMap<K, V> extends HashMap<K, V> {

	public boolean getBoolean(String key) {
		return get(key) != null && (Boolean)get(key);
	}

}
