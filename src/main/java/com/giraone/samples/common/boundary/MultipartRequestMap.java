package com.giraone.samples.common.boundary;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class MultipartRequestMap extends HashMap<String, List<Object>>
{
	private static final long serialVersionUID = 1L;
	private static final int BUF_SIZE = 8192;
	private static final String DEFAULT_ENCODING = "UTF-8";

	protected static final Marker LOG_TAG = MarkerManager.getMarker("MULTIPART");

	@Inject
	protected Logger logger;

	private String tempDirectory;
	private String encoding;

	public MultipartRequestMap(HttpServletRequest request) throws IOException, ServletException
	{
		super();
		this.tempDirectory = System.getProperty("java.io.tmpdir");
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "MultipartRequestMap: tempDirectory=", this.tempDirectory);
		
		this.encoding = request.getCharacterEncoding();
		if (this.encoding == null)
		{
			try
			{
				request.setCharacterEncoding(this.encoding = DEFAULT_ENCODING);
			}
			catch (UnsupportedEncodingException ex)
			{
				logger.error(LOG_TAG, "Cannot set character encoding!", ex);
			}
		}

		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "MultipartRequestMap: Encoding=", this.encoding);
		
		for (Part part : request.getParts())
		{
			String fileName = part.getSubmittedFileName();
			if (fileName == null)
			{
				putMulti(part.getName(), getValue(part));
			}
			else
			{
				processFilePart(part, fileName);
			}
		}
	}

	public String getStringParameter(String name)
	{
		List<Object> list = get(name);
		return (list != null) ? (String) get(name).get(0) : null;
	}

	public File getFileParameter(String name)
	{
		List<Object> list = get(name);
		return (list != null) ? (File) get(name).get(0) : null;
	}
	
	public byte[] readFileToByteArray(File file) throws IOException
	{
		int size = (int) file.length();
		// TODO: Use a byte buffer pool
		byte[] buffer = new byte[size];
		FileInputStream in = new FileInputStream(file);
		try
		{
			in.read(buffer);
		}
		finally
		{
			in.close();
		}
		return buffer;
	}
	
	private void processFilePart(Part part, String fileName) throws IOException
	{
		File tempFile = new File(this.tempDirectory, fileName);
		tempFile.createNewFile();
		tempFile.deleteOnExit();

		try (BufferedInputStream input = new BufferedInputStream(part.getInputStream(), BUF_SIZE);
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(tempFile), BUF_SIZE);)
		{

			byte[] buffer = new byte[BUF_SIZE];
			for (int length = 0; ((length = input.read(buffer)) > 0);)
			{
				output.write(buffer, 0, length);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		part.delete();
		putMulti(part.getName(), tempFile);
	}

	private String getValue(Part part) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), encoding));
		StringBuilder value = new StringBuilder();
		char[] buffer = new char[BUF_SIZE];
		for (int length; (length = reader.read(buffer)) > 0;)
		{
			value.append(buffer, 0, length);
		}
		return value.toString();
	}

	private <T> void putMulti(final String key, final T value)
	{
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "MultipartRequestMap: key=" + value);
		
		List<Object> values = (List<Object>) super.get(key);

		if (values == null)
		{
			values = new ArrayList<>();
			values.add(value);
			put(key, values);
		}
		else
		{
			values.add(value);
		}
	}
}
