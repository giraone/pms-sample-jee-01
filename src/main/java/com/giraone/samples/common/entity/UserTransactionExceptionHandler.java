package com.giraone.samples.common.entity;

public interface UserTransactionExceptionHandler
{
	/**
	 * Handle a user transaction exception and return an appropriate result.
	 * See {@link UserTransactionInterceptor} for details.
	 * They handler code may not throw any exceptions.
	 * @param userTransactionException the exception (duplicate key, optimistic lock, ...) which was thrown and catched. 
	 * @return the object to be returned by the annotated method. 
	 */
	public Object handleUserTransactionException(UserTransactionException userTransactionException);
}
