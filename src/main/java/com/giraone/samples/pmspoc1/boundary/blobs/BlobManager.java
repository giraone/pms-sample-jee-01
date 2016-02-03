package com.giraone.samples.pmspoc1.boundary.blobs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Store and load BLOBs using streaming via JDBC.
 */
public class BlobManager
{
	private static final int BUFSIZE = 4096;
	
	private static final Marker LOG_TAG = MarkerManager.getMarker("BLOB");
	
    // @Inject // does not work here!
	private Logger logger;

	public BlobManager()
	{
		logger = LogManager.getLogger(BlobManager.class);
	}
			
	public boolean streamBlobToDatabase(Connection connection, InputStream in, long inLength, BlobModelConfig blobModelConfig, Object id)
		throws SQLException, IOException
	{
		return streamBlobToDatabase(connection, in, new Long(inLength), blobModelConfig, id);
	}
	
	public boolean streamBlobToDatabase(Connection connection, InputStream in, BlobModelConfig blobModelConfig, Object id)
		throws SQLException, IOException
	{
		return streamBlobToDatabase(connection, in, null, blobModelConfig, id);
	}
	
	public boolean streamBlobToDatabase(EntityManager entityManager, InputStream in, long inLength, BlobModelConfig blobModelConfig, Object id)
		throws SQLException, IOException
	{
		return streamBlobToDatabase(entityManager.unwrap(Connection.class), in, new Long(inLength), blobModelConfig, id);
	}
	
	public boolean streamBlobToDatabase(EntityManager entityManager, InputStream in, BlobModelConfig blobModelConfig, Object id)
		throws SQLException, IOException
	{
		return streamBlobToDatabase(entityManager.unwrap(Connection.class), in, null, blobModelConfig, id);
	}
	
	/**
	 * Write a BLOB to a database tables's BLOB column by reading the content from a given stream.
	 * @param connection			A valid and open JDBC connection
	 * @param in					The input stream from where the content is read
	 * @param inLength				The input length of the data. Maybe null if not known,
	 * @param blobModelConfig		The configuration (table, columns) where to write the BLOB database record
	 * @param id					The unique id of the document record
	 * @return true on success, false if the database record with the given id was not found
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean streamBlobToDatabase(Connection connection, InputStream in, Long inLength, BlobModelConfig blobModelConfig, Object id)
		throws SQLException, IOException
	{
		try
		{
			String stmtCode = blobModelConfig.toPreparedUpdateStatement();
			
			if (logger != null && logger.isDebugEnabled())
				logger.debug(LOG_TAG, "BlobManager.streamBlobToDatabase: inLength=" + inLength + ", stmtCode=" + stmtCode);
			
			PreparedStatement ps = connection.prepareStatement(stmtCode);
					
			try
			{
				assignId(ps, 3, id);
								
				if (inLength == null || inLength.longValue() == -1)
				{
					// Version 1 - This will not work with JDBC 3.0 drivers
				    ps.setBinaryStream(1, in);
				    ps.setLong(2, -1L);
				}
				else
				{
					long length = inLength.longValue();
					if (length < Integer.MAX_VALUE)
					{
						// Version 2 - This works with JDBC 3.0 drivers
					    ps.setBinaryStream(1, in, (int) length);
					    ps.setLong(2, length);
					}
					else
					{
						// Version 3 - This will not work with JDBC 3.0 drivers
					    ps.setBinaryStream(1, in, length);
					    ps.setLong(2, length);
					}	    
				}
				
				int count = ps.executeUpdate();
				if (count == 0)
				{
					return false;
				}
				else if (count > 1)
				{
					throw new IllegalArgumentException("Id " + id + " for " + blobModelConfig.getTableName() + " not unique!");
				}
				else
				{
					return true;
				}
			}
			finally
			{
				ps.close();
			}
		}
		finally
		{
			in.close();
		}	    
	}
	
	/**
	 * Read a BLOB from the database and write it into a given output stream
	 * @param connection			A valid and open JDBC connection
	 * @param out					The output stream to which the BLOB is written
	 * @param blobModelConfig		The configuration (table, columns) where to fetch the BLOB database record
	 * @param id					The unique id of the document record
	 * @return	The number of streamed bytes or -1L, if the database record with the given id was not found or the BLOB was NULL.
	 * @throws SQLException
	 * @throws IOException
	 */
	public long streamBlobFromDatabase(Connection connection, OutputStream out, BlobModelConfig blobModelConfig, Object id)
		throws SQLException, IOException
	{				
		String stmtCode = blobModelConfig.toPreparedSelectBlobStatement();
		
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "BlobManager.streamBlobFromDatabase: " + stmtCode);
		
		PreparedStatement ps = connection.prepareStatement(stmtCode);
		
		try
		{
			this.assignId(ps, 1, id);
			ResultSet res = ps.executeQuery();
			long ret;
			try
			{
				boolean ok = res.next();
				if (ok)
				{
					InputStream in = res.getBinaryStream(1);
					if (res.wasNull())
					{
						if (logger != null && logger.isDebugEnabled())
							logger.debug(LOG_TAG, "BlobManager.streamBlobFromDatabase BLOB was NULL!");
						return -1;
					}
					ret = this.pipeBlobStream(in, out, BUFSIZE);
				}
				else
				{
					if (logger != null && logger.isDebugEnabled())
						logger.debug(LOG_TAG, "BlobManager.streamBlobFromDatabase BLOB not found!");
					return -1;
				}
			}
			finally
			{
				res.close();
			}
			return ret;
		}
		finally
		{
			ps.close();
		}
	}
	
	private void assignId(PreparedStatement ps, int parameterIndex, Object id) throws SQLException
	{
		if (id instanceof String)
		{
			ps.setString(parameterIndex, (String) id);
		}
		else if (id instanceof Long)
		{
			ps.setLong(parameterIndex, (Long) id);
		}
		else if (id instanceof Integer)
		{
			ps.setInt(parameterIndex, (Integer) id);
		}
		else
		{
			throw new IllegalArgumentException("Type " + id.getClass() + " not supported!");
		}
	}
	
    private long pipeBlobStream(InputStream in, OutputStream out, int bufsize)
        throws IOException
    {
        long size = 0L;
        byte[] buf = new byte[bufsize];
        int bytesRead;
        try
        {
            while ((bytesRead = in.read(buf)) > 0)
            {
                out.write(buf, 0, bytesRead);
                size += (long) bytesRead;
                /*
                if (logger != null && logger.isDebugEnabled())
					logger.debug(LOG_TAG, "BlobManager.streamBlobFromDatabase size=" + size);
				*/
            }
            return size;
        }
        catch (IOException ioe)
        {
            logger.warn(LOG_TAG, "BlobManager.pipeBlobStream failed: " + ioe.getMessage() + ". After sending " + size + " bytes", ioe);
            throw ioe;
        }
    }
}
