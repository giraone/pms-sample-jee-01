package com.giraone.samples.pmspoc1.boundary.test.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.giraone.samples.common.StringUtil;
import com.giraone.samples.pmspoc1.entity.PostalAddress;
import com.giraone.samples.pmspoc1.entity.enums.EnumGender;

public class SimpleTestDataGenerator
{
	static final String DATA_PATH = "./src/test/resources/data";
	
	static final Random RANDOM = new Random();
	
	static final String[] DEPARTMENTS = {
		"Konstruktion", "Dokumentation", "Entwicklung", "Vertrieb", "Support",
		"Konstruktion", "Dokumentation", "Entwicklung", "Vertrieb", "Support",
		"Konstruktion", "Dokumentation", "Entwicklung", "Vertrieb", "Support",
		"Konstruktion", "Dokumentation", "Entwicklung", "Vertrieb", "Support",
		"Forschung", "Software", "IT", "Qualitätskontrolle", "Logistik", "Ausbildung",
		"Training", "Rechenzentrum", "Fuhrpark", "Service", "Datenschutz", "Security"  };
	
	static final String[] NATIONALITY = { "DEU", "DEU", "DEU", "DEU", "DEU", "ITA", "USA" };
	
	static final String[] MAIL_PROVIDER = { "gmail.com", "yahoo.com", "aol.com", "gmx.de", "web.de", "hotmail.com" };
	
	static final HashSet<String> LAST_NAME_PREFIXES = new HashSet<String>();
	
	static final HashMap<String, ArrayList<String>> RANDOM_FROM_FILE = new HashMap<String, ArrayList<String>>();
	static final HashMap<String, ArrayList<String>> RANDOM_FROM_WEIGHTED_FILE = new HashMap<String, ArrayList<String>>();
	static final HashMap<String, HashMap<Integer, Integer>> WEIGHT_FROM_WEIGHTED_FILE = new HashMap<String, HashMap<Integer, Integer>>();
	
	public static Random random()
	{		
		return RANDOM;
	}
	
	public static String randomDepartment()
	{		
		return DEPARTMENTS[RANDOM.nextInt(DEPARTMENTS.length)];
	}
	
	public static String randomMailProvider()
	{		
		return MAIL_PROVIDER[RANDOM.nextInt(MAIL_PROVIDER.length)];
	}
	
	public static EnumGender randomGender()
	{
		return RANDOM.nextBoolean() ? EnumGender.M : EnumGender.F;
	}
	
	public static String randomFirstName(EnumGender geschlecht)
	{	
		return randomFromWeightedFile("vornamen_" + (geschlecht == EnumGender.M ? "m" : "w") + ".txt");
	}

	public static String randomLastName()
	{		
		return randomFromWeightedFile("nachnamen.txt");
	}
	
	public static String randomNationality()
	{		
		return NATIONALITY[RANDOM.nextInt(NATIONALITY.length)];
	}
	
	public static Calendar randomDateOfBirth()
	{
		Calendar d = GregorianCalendar.getInstance();
		d.set(RANDOM.nextInt(50) + 1935, RANDOM.nextInt(12), RANDOM.nextInt(29));
		return d;
	}
	
	public static Calendar randomDateOfEntry()
	{
		Calendar d = GregorianCalendar.getInstance();
		d.set(2014 - RANDOM.nextInt(12), RANDOM.nextInt(12), RANDOM.nextInt(29));
		return d;
	}
	
	public static void fillRandomAddress(PostalAddress postalAddress)
	{
		postalAddress.setCountryCode("DE");
		postalAddress.setCity(randomFromFile("deutsche-staedte.txt"));
		postalAddress.setPostalCode(String.format("%05d", 10000 + RANDOM.nextInt(80000)));
		postalAddress.setStreet(randomFromFile("strassen_osm.txt"));
		postalAddress.setHouseNumber(1 + RANDOM.nextInt(20) + (RANDOM.nextInt(10) == 0 ? "a" : ""));
	}
	
	private static String randomFromFile(String file)
	{
		ArrayList<String> valueList = RANDOM_FROM_FILE.get(file);
		if (valueList == null)
		{	
			//StopWatch stopWatch = new StopWatch();
			//stopWatch.start();
			valueList = new ArrayList<String>();
			Path path = FileSystems.getDefault().getPath(DATA_PATH + "/" + file);
			try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
			{
			    String line = null;
			    while ((line = reader.readLine()) != null)
			    {
			    	valueList.add(line);
			    }
			} 
			catch (IOException io)
			{
				io.printStackTrace();
			}
			RANDOM_FROM_FILE.put(file, valueList);
			//stopWatch.stop();
			//System.out.println("Reading data file \"" + file + "\" took " + stopWatch.getTime() + " milliseconds");
			System.out.println("Reading data file \"" + file + "\"");
			System.out.println("  nr of entries = " + valueList.size());
		}
		return valueList.get(RANDOM.nextInt(valueList.size()));
	}
	
	private static String randomFromWeightedFile(String file)
	{
		ArrayList<String> valueList = RANDOM_FROM_WEIGHTED_FILE.get(file);
		HashMap<Integer, Integer> weightMap = WEIGHT_FROM_WEIGHTED_FILE.get(file);
		
		if (valueList == null)
		{
			//StopWatch stopWatch = new StopWatch();
			//stopWatch.start();
			valueList = new ArrayList<String>();
			weightMap = new HashMap<Integer, Integer>();
			Path path = FileSystems.getDefault().getPath(DATA_PATH + "/" + file);
			try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
			{
			    String line = null;
			    while ((line = reader.readLine()) != null)
			    {
			    	String[] pieces = line.split("[|]");
			    	if (pieces.length == 2 && StringUtil.isNotNullOrEmpty(pieces[0]) && StringUtil.isNotNullOrEmpty(pieces[1]))
			    	{
			    		int valuePos = valueList.size();
			    		int weight = Integer.parseInt(pieces[1]);
			    		valueList.add(pieces[0]);		    		
			    		for (int i = 0; i < weight; i++)
			    		{
			    			weightMap.put(weightMap.size(), valuePos);
			    		}
			    	}
			    	else
			    	{
			    		System.err.println("Invalid line in " + file + ": " + line);
			    	}
			    }
			} 
			catch (IOException io)
			{
				io.printStackTrace();
			}		
			RANDOM_FROM_WEIGHTED_FILE.put(file, valueList);
			WEIGHT_FROM_WEIGHTED_FILE.put(file, weightMap);
			//stopWatch.stop();
			//System.out.println("Reading weighted data files \"" + file + "\" took " + stopWatch.getTime() + " milliseconds");
			System.out.println("Reading weighted data files \"" + file + "\"");			
			System.out.println("  nr of entries = " + valueList.size() + ", nr of weights = " + weightMap.size());
		}
		
		return valueList.get(weightMap.get(RANDOM.nextInt(weightMap.size())));
	}
}
