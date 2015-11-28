package com.giraone.samples.common.entity;

/**
 * An exception to indicate, that a user transaction failed because of notifiable problem.
 */
public abstract class UserTransactionException extends IllegalArgumentException
{
	private static final long serialVersionUID = 1L;
}
