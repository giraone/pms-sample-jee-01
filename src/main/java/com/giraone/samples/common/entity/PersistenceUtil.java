package com.giraone.samples.common.entity;

import java.util.List;

/**
 * Static utilities for JPA.
 */
public class PersistenceUtil
{	
	/**
	 * Check a result list returned from a JPA query, whether it returns a single result object.
	 * @param resultList	The result list to be checked.
	 * @param context		A context for the exception, typically "ClassName.nameOfUniqueColumn".
	 * @return The first item in the list, if there is exactly one. Null, if the result list is empty.
	 * @throws IllegalStateException, when the result list has more than one row.
	 */
	public static <T> T sanityCheckForSingleResultList(List<T> resultList, String context)
	{
		if (resultList != null && resultList.size() > 0)
		{
			if (resultList.size() > 1)
			{
				throw new IllegalStateException("Database schema corrupt: " + context + " is not unique!");
			}
			return resultList.get(0);
		}
		else
		{
			return null;
		}
	}
}