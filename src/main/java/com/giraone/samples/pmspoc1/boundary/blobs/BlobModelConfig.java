package com.giraone.samples.pmspoc1.boundary.blobs;

public class BlobModelConfig
{
	String tableName;
	String idColumn;
	String blobColumn;
	String byteSizeColumn;
	
	public BlobModelConfig(String className, String idColumn, String blobColumn, String byteSizeColumn)
	{
		super();
		this.tableName = className;
		this.idColumn = idColumn;
		this.blobColumn = blobColumn;
		this.byteSizeColumn = byteSizeColumn;
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public String getIdColumn()
	{
		return idColumn;
	}

	public void setIdColumn(String idColumn)
	{
		this.idColumn = idColumn;
	}

	public String getBlobColumn()
	{
		return blobColumn;
	}

	public void setBlobColumn(String blobColumn)
	{
		this.blobColumn = blobColumn;
	}
	
	public String getByteSizeColumn()
	{
		return byteSizeColumn;
	}

	public void setByteSizeColumn(String byteSizeColumn)
	{
		this.byteSizeColumn = byteSizeColumn;
	}

	public String toPreparedInsertStatement()
	{
		return "INSERT INTO " + this.tableName
			+ " (" + this.idColumn + ", " + this.blobColumn + ", " + this.byteSizeColumn + ") VALUES (?,?,?)";
	}
	
	public String toPreparedUpdateStatement()
	{
		return "UPDATE " + this.tableName
			+ " SET " + this.blobColumn + "=?, " + this.byteSizeColumn + "=? WHERE " + this.idColumn + "=?";
	}
	
	public String toPreparedSelectBlobStatement()
	{
		return "SELECT " + this.blobColumn + " FROM " + this.tableName
			+ " WHERE " + this.idColumn + "=?";
	}
}