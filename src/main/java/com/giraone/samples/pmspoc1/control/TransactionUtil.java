package com.giraone.samples.pmspoc1.control;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public class TransactionUtil
{
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
    			// TODO: Introduce special logger
    			System.err.println(e);
    		}
    	}
	}
}
