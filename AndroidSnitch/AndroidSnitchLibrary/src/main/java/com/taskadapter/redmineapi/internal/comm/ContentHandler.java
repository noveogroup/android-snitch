package com.taskadapter.redmineapi.internal.comm;

import com.taskadapter.redmineapi.RedMineException;

/**
 * Redmine content handler.
 * 
 * @author maxkar
 * 
 */
public interface ContentHandler<K, R> {
	/**
	 * Consumes content of a specified type and returns a specified result.
	 * 
	 * @param content
	 *            content to process.
	 * @return processed content.
	 * @throws com.taskadapter.redmineapi.RedMineException
	 *             if something goes wrong.
	 */
	R processContent(K content) throws RedMineException;
}
