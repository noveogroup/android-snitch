package com.taskadapter.redmineapi.internal.comm;

import org.apache.http.HttpRequest;
import com.taskadapter.redmineapi.RedMineException;

public interface Communicator<K> {

	/**
	 * Performs a request.
	 * 
	 * @return the response body.
	 */
	public abstract <R> R sendRequest(HttpRequest request,
                                      ContentHandler<K, R> contentHandler) throws RedMineException;

}