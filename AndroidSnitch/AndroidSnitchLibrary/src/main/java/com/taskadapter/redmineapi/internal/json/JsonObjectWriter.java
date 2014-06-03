package com.taskadapter.redmineapi.internal.json;

import org.json.JSONException;
import org.json.JSONStringer;

/**
 * Json object writer.
 * 
 * @author maxkar
 * 
 */
public interface JsonObjectWriter<T> {
	public void write(JSONStringer writer, T object) throws JSONException;
}
