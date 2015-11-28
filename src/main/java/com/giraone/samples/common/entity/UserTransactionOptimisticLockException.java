package com.giraone.samples.common.entity;

import javax.persistence.OptimisticLockException;

/**
 * An exception to indicate, that a user transaction failed because of an optimistic lock problem.
 * See @link {@link OptimisticLockException} og JPA.
 * In REST calls, this should lead to HTTP STATUS CONFLICT (409).
 */
public class UserTransactionOptimisticLockException extends UserTransactionException
{
	private static final long serialVersionUID = 1L;
}
