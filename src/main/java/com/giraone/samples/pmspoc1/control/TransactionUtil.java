package com.giraone.samples.pmspoc1.control;

import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class TransactionUtil
{
	private static final Marker LOG_TAG = MarkerManager.getMarker("JTA");
	
	@Inject
	private static Logger logger;
	
	public static void begin(UserTransaction tx) throws NotSupportedException, SystemException
	{
		tx.begin();
	}
	
	public static UserTransaction commit(UserTransaction tx) throws SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException
	{
		tx.commit();
		return tx;
	}
	
	public static void rollback(UserTransaction tx)
	{
		if (tx != null)
    	{
    		try
    		{
    			tx.rollback();
    		}
    		catch (Exception e)
    		{
    			if (logger != null)
    				logger.warn(LOG_TAG, "TransactionUtil.rollback failed", e);
    			else
    				System.err.println("TransactionUtil.rollback failed: " + e);
    		}
    	}
	}
}
