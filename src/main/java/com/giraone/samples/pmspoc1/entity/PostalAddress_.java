package com.giraone.samples.pmspoc1.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.giraone.samples.common.entity.AbstractEntity_;

@StaticMetamodel(PostalAddress.class)
public class PostalAddress_ extends AbstractEntity_
{
	public static volatile SingularAttribute<PostalAddress, Integer> ranking;
	public static volatile SingularAttribute<PostalAddress, String> countryCode;
	public static volatile SingularAttribute<PostalAddress, String> postalCode;
	public static volatile SingularAttribute<PostalAddress, String> city;
	public static volatile SingularAttribute<PostalAddress, String> secondaryAddressLine;
	public static volatile SingularAttribute<PostalAddress, String> street;
	public static volatile SingularAttribute<PostalAddress, String> houseNumber;	
	public static volatile SingularAttribute<PostalAddress, String> poBoxNumber;	
	
	public static final String SQL_NAME_ranking = "ranking";
	public static final String SQL_NAME_countryCode = "countryCode";
	public static final String SQL_NAME_postalCode = "postalCode";
	public static final String SQL_NAME_city = "city";
	public static final String SQL_NAME_secondaryAddressLine = "secondaryAddressLine";
	public static final String SQL_NAME_street = "street";
	public static final String SQL_NAME_houseNumber = "houseNumber";
	public static final String SQL_NAME_poBoxNumber = "poBoxNumber";
}