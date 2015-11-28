package com.giraone.samples.common.entity;

/**
 * An exception to indicate, that a user transaction failed because of a constraint violation.
 * In REST calls, this should lead to HTTP STATUS CONFLICT (409).
 */
public class UserTransactionConstraintViolationException extends UserTransactionException
{
	private static final long serialVersionUID = 1L;
}
