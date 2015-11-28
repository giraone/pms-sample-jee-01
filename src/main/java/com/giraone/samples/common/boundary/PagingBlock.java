package com.giraone.samples.common.boundary;

import java.util.List;

public class PagingBlock<T>
{
	protected int totalCount;
	protected int blockCounter;
	protected int blockSize;
	protected List<T> blockItems;
	
	public PagingBlock()
	{
	}
	
	public PagingBlock(int blockCounter, int blockSize, int totalCount, List<T> blockItems)
	{
		this.blockCounter = blockCounter;
		this.blockSize = blockSize;
		this.totalCount = totalCount;
		this.blockItems = blockItems;
	}

	public int getBlockCounter()
	{
		return blockCounter;
	}

	public void setBlockCounter(int blockCounter)
	{
		this.blockCounter = blockCounter;
	}

	public int getBlockSize()
	{
		return blockSize;
	}

	public void setBlockSize(int blockSize)
	{
		this.blockSize = blockSize;
	}

	public List<T> getBlockItems()
	{
		return blockItems;
	}

	public void setBlockItems(List<T> blockItems)
	{
		this.blockItems = blockItems;
	}

	public int getTotalCount()
	{
		return totalCount;
	}

	public void setTotalCount(int totalCount)
	{
		this.totalCount = totalCount;
	}
}
