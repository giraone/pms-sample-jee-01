package com.giraone.samples.common.entity;

import java.lang.reflect.Method;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.annotation.Resource;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.OptimisticLockException;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Interceptor to use user transactions (see {@link TransactionManagementType.BEAN}) instead of container managed
 * transactions (see {@link TransactionManagementType.CONTAINER} without having boiler plate code in the business
 * classes.
 * <p>
 * The need for user transactions is due to the requirement of catching certain persistence exceptions like
 * <ul>
 * <li>"SQLIntegrityConstraintViolationException" for handling e.g. duplicate keys properly. In REST call this should
 * result in returning a HTTP CONFLICT (409) status code.</li>
 * <li>"OptimisticLockException" for handling concurrent modifications. In REST call this should also result in
 * returning a HTTP CONFLICT (409) status code.</li>
 * </ul>
 * Doing this would not be possible with CMT. To use this feature, the service method has to be annotated
 * with @UserTransactional and this interceptor class must be registered in the beans.xml file.
 * </p>
 * <p>
 * For technical background information see
 * <a href="https://blogs.oracle.com/arungupta/entry/totd_151_transactional_interceptors_using">Transactional
 * interceptors (by Arun Gupta)</a>.
 * </p>
 */
@UserTransactional
@Interceptor
public class UserTransactionInterceptor
{
	private static final Marker LOG_TAG = MarkerManager.getMarker("JTA");

	private static final String NAME_OF_HANDLER_METHOD = "handleUserTransactionException";
	//private static final Pattern DUPLICATE_KEY_IN_MESSAGE = Pattern.compile(".*duplicate.*key.*");
	private static final String DUPLICATE_KEY_IN_MESSAGE = "duplicate key";

	@Inject
	private Logger logger;

	@Resource
	private UserTransaction userTransaction;

	/**
	 * Interceptor code.
	 * 
	 * @param invocationContext Invocation context
	 * @return Result of payload method
	 * @throws UserTransactionConstraintViolationException If a constraint violation was detected.
	 * @throws Exception Exceptions thrown by the payload method
	 */
	@AroundInvoke
	public Object aroundInvoke(final InvocationContext invocationContext) throws Exception
	{
		Object result = null;

		try
		{
			userTransaction.begin();
			if (logger != null && logger.isDebugEnabled())
			{
				logger.debug(LOG_TAG,
					"UserTransaction.begin    - " + invocationContext.getTarget().getClass().getSimpleName() + "."
						+ invocationContext.getMethod().getName());
			}

			result = invocationContext.proceed();

			userTransaction.commit();
			if (logger != null && logger.isDebugEnabled())
			{
				logger.debug(LOG_TAG,
					"UserTransaction.commited - " + invocationContext.getTarget().getClass().getSimpleName() + "."
						+ invocationContext.getMethod().getName());
			}
		}
		catch (Exception e)
		{
			if (!(e instanceof RollbackException))
			{
				this.safeRollback(userTransaction, invocationContext);
			}

			UserTransactionException notifiableException = this.isNotifiableException(e);
			if (notifiableException != null)
			{
				// UserTransactional ut = invocationContext.getMethod().getAnnotation(UserTransactional.class);
				Method handlerMethod = invocationContext.getTarget().getClass().getMethod(NAME_OF_HANDLER_METHOD,
					new Class[] { UserTransactionException.class });
				result = handlerMethod.invoke(invocationContext.getTarget(), notifiableException);
			}
			else
			{
				throw e;
			}
		}

		return result;
	}

	private void safeRollback(final UserTransaction userTransaction, final InvocationContext invocationContext)
	{
		if (userTransaction != null)
		{
			try
			{
				userTransaction.rollback();
				if (logger != null)
				{
					logger.warn(LOG_TAG,
						"UserTransaction.rollback - " + invocationContext.getTarget().getClass().getSimpleName() + "."
							+ invocationContext.getMethod().getName());
				}
				else
				{
					System.err.println(
						"UserTransaction.rollback - " + invocationContext.getTarget().getClass().getSimpleName() + "."

							+ invocationContext.getMethod().getName());
				}
			}
			catch (Throwable e)
			{
				if (logger != null)
				{
					logger.error(LOG_TAG,
						"UserTransaction.rollback FAILED - " + invocationContext.getTarget().getClass().getSimpleName()
							+ "." + invocationContext.getMethod().getName());
				}
				else
				{
					System.err.println(
						"UserTransaction.rollback FAILED - " + invocationContext.getTarget().getClass().getSimpleName()
							+ "." + invocationContext.getMethod().getName());
				}
			}
		}
	}

	private UserTransactionException isNotifiableException(Throwable e)
	{
		if (logger != null && logger.isDebugEnabled())
		{
			logger.debug(LOG_TAG, "UserTransactionInterceptor.isNotifiableException1 " + e.getClass());
		}

		/**
		 * Hibernate and Apache Derby: SQLIntegrityConstraintViolationException works. EclipseLink and PostgreSQL:
		 * PersistenceException org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint
		 * "XXX_key".
		 */

		boolean isSQLIntegrityConstraintViolationException = e instanceof SQLIntegrityConstraintViolationException;
		boolean isOptimisticLockException = e instanceof OptimisticLockException;
		boolean isDuplicateKeyException = e.getMessage() != null && matchesDuplicateKey(e.getMessage());
		while (e.getCause() != null)
		{
			/*
			if (logger != null && logger.isDebugEnabled())
			{
				logger.debug(LOG_TAG, "UserTransactionInterceptor.isNotifiableException# " + e.getClass());
			}
			*/
			e = e.getCause();
			isSQLIntegrityConstraintViolationException = isSQLIntegrityConstraintViolationException
				|| e instanceof SQLIntegrityConstraintViolationException;
			isOptimisticLockException = isOptimisticLockException || e instanceof OptimisticLockException;
			isDuplicateKeyException = isDuplicateKeyException
				|| e.getMessage() != null && matchesDuplicateKey(e.getMessage());
		}

		if (isSQLIntegrityConstraintViolationException)
		{
			return new UserTransactionConstraintViolationException();
		}
		else if (isOptimisticLockException)
		{
			return new UserTransactionOptimisticLockException();
		}
		else if (isDuplicateKeyException)
		{
			return new UserTransactionConstraintViolationException();
		}

		return null;
	}

	private boolean matchesDuplicateKey(String message)
	{
		boolean ret = message.contains(DUPLICATE_KEY_IN_MESSAGE);
		//System.err.println("++++++++++++++++++ " + message + " ================= " + ret);
		return ret;
	}
}
