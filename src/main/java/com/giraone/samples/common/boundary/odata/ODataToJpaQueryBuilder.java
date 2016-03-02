package com.giraone.samples.common.boundary.odata;

import java.lang.reflect.Field;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.exception.ODataMessageException;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.expression.BinaryExpression;
import org.apache.olingo.odata2.api.uri.expression.BinaryOperator;
import org.apache.olingo.odata2.api.uri.expression.CommonExpression;
import org.apache.olingo.odata2.api.uri.expression.FilterExpression;
import org.apache.olingo.odata2.api.uri.expression.LiteralExpression;
import org.apache.olingo.odata2.api.uri.expression.MethodExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderByExpression;
import org.apache.olingo.odata2.api.uri.expression.OrderExpression;
import org.apache.olingo.odata2.api.uri.expression.PropertyExpression;
import org.apache.olingo.odata2.api.uri.expression.SortOrder;
import org.apache.olingo.odata2.api.uri.expression.UnaryExpression;
import org.apache.olingo.odata2.api.uri.expression.UnaryOperator;

import com.giraone.samples.common.StringUtil;

public class ODataToJpaQueryBuilder<T>
{
	public static final Marker LOG_TAG = MarkerManager.getMarker("OData");

	// TODO: Logger is not always injected on problems with CDI!
	// @Inject
	private Logger logger;

	public ODataToJpaQueryBuilder()
	{
		logger = LogManager.getLogger(ODataToJpaQueryBuilder.class);
	}

	/**
	 * Parse an OData filter expression and return a JPA predicate
	 * 
	 * @param oDataFilter OData filter string e.g. "displayName eq 'Test' and version = 1"
	 * @return
	 */
	public Predicate parseFilterExpression(CriteriaBuilder cb, Root<T> table, String oDataFilter)
	{
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "ODataFilterToJpaQueryBuilder.parseFilterExpression \"" + oDataFilter + "\"");

		if (oDataFilter == null || oDataFilter.trim().length() == 0)
		{
			return null;
		}

		Edm edm = null;
		EdmEntityType edmType = null;
		FilterExpression filterExpression;
		try
		{
			filterExpression = UriParser.parseFilter(edm, edmType, oDataFilter);
		}
		catch (ODataMessageException e)
		{
			throw new IllegalArgumentException("Cannot parse OData filter \"" + oDataFilter + "\"", e);
		}
		CommonExpression commonExpression = filterExpression.getExpression();
		return this.processFilterExpression(cb, table, commonExpression);
	}

	/**
	 * Parse an OData orderby expression and set it on a given criteria API query object.
	 * 
	 * @param cb The criteria builder for the request
	 * @param cb The root table
	 * @param select The query to which the order by statement is added
	 * @param oDataOrder The orderby expression, e.g. "orderby=name asc, age desc"
	 */
	public void parseOrderExpression(CriteriaBuilder cb, Root<T> table, CriteriaQuery<T> select, String oDataOrder)
	{
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "ODataFilterToJpaQueryBuilder.parseOrderExpression \"" + oDataOrder + "\"");

		if (oDataOrder == null || oDataOrder.trim().length() == 0)
		{
			return;
		}

		final Edm edm = null;
		final EdmEntityType edmType = null;
		final OrderByExpression orderByExpression;
		try
		{
			orderByExpression = UriParser.parseOrderBy(edm, edmType, oDataOrder);
		}
		catch (ODataMessageException e)
		{
			throw new IllegalArgumentException("Cannot parse OData order \"" + oDataOrder + "\"", e);
		}

		final ArrayList<Order> orderList = new ArrayList<Order>();
		for (OrderExpression orderExpression : orderByExpression.getOrders())
		{
			final CommonExpression commonExpression = orderExpression.getExpression();
			if (commonExpression == null || !(commonExpression instanceof PropertyExpression))
				continue;
			final PropertyExpression propertyExpression = (PropertyExpression) commonExpression;
			final String propertyName = propertyExpression.getUriLiteral();
			@SuppressWarnings("rawtypes")
			final Path propertyPath = this.getPropertyPathByDotName(table, propertyName);
			if (propertyPath != null)
			{
				final Order order = SortOrder.desc == orderExpression.getSortOrder() ? cb.desc(propertyPath)
					: cb.asc(propertyPath);
				if (logger != null && logger.isDebugEnabled())
					logger.debug(LOG_TAG, "ODataFilterToJpaQueryBuilder.parseOrderExpression order=" + order);
				orderList.add(order);
			}
		}
		select.orderBy(orderList);
		return;
	}

	public List<Attribute> parseExpandExpression(CriteriaBuilder cb, Root<T> table, Class metaModel, String oDataExpand)
	{
		ArrayList<Attribute> ret = new ArrayList<Attribute>();
		
		if (oDataExpand.startsWith(",") || oDataExpand.endsWith(","))
		{
			throw new IllegalArgumentException("Cannot parse OData expand \"" + oDataExpand + "\": misplaced colon!");
		}

		for (String expandItemString : oDataExpand.split(","))
		{
			expandItemString = expandItemString.trim();
			if ("".equals(expandItemString))
			{
				throw new IllegalArgumentException("Cannot parse OData expand \"" + oDataExpand + "\": empty segment!");
			}
			if (expandItemString.startsWith("/") || expandItemString.endsWith("/"))
			{
				throw new IllegalArgumentException("Cannot parse OData expand \"" + oDataExpand + "\": misplaced slash!");
			}

			Attribute attribute = null;
			for (String expandPropertyName : expandItemString.split("/"))
			{
				if ("".equals(expandPropertyName))
				{
					throw new IllegalArgumentException("Cannot parse OData expand \"" + oDataExpand + "\": empty slash segment!");
				}
				
				try
				{
					attribute = this.getAttributeFromMetaModel(metaModel, expandPropertyName);
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException("Cannot parse OData expand \"" + oDataExpand + "\": unknown property \""
						+ expandPropertyName + "\" for table \"" + table.getModel().getName());
				}			
			}
			ret.add(attribute);
		}
		return ret;
	}

	protected Predicate processFilterExpression(CriteriaBuilder cb, Root<T> table, CommonExpression commonExpression)
	{
		if (commonExpression instanceof BinaryExpression)
		{
			return this.processFilterExpression(cb, table, (BinaryExpression) commonExpression);
		}
		else if (commonExpression instanceof UnaryExpression)
		{
			return this.process(cb, table, (UnaryExpression) commonExpression);
		}
		else
		{
			throw new IllegalArgumentException("Filter like " + commonExpression.getClass()
				+ " that are not binary expressions are not supported yet!");
		}
	}

	private Predicate processFilterExpression(CriteriaBuilder cb, Root<T> table, BinaryExpression binaryExpression)
	{
		final CommonExpression leftOperand = binaryExpression.getLeftOperand();
		final CommonExpression rightOperand = binaryExpression.getRightOperand();
		final BinaryOperator binaryOperator = binaryExpression.getOperator();

		if (logger != null && logger.isDebugEnabled())
			logger.debug(
				LOG_TAG + "ODataFilterToJpaQueryBuilder.processFilterExpression operatator=" + binaryOperator.name());

		if (binaryOperator == BinaryOperator.AND)
		{
			return cb.and(new Predicate[] { this.processFilterExpression(cb, table, leftOperand),
				this.processFilterExpression(cb, table, rightOperand) });
		}
		else if (binaryOperator == BinaryOperator.OR)
		{
			return cb.or(new Predicate[] { this.processFilterExpression(cb,table, leftOperand),
				this.processFilterExpression(cb, table, rightOperand) });
		}
		else
		{
			final MethodOrPropertyExpression methodOrPropertyExpression = this.getLeftOperand(leftOperand);
			String leftPropertyName, methodName = null;
			Object rightValue = null;
			boolean rightMethodBoolean = true;

			if (methodOrPropertyExpression.isMethodExpression())
			{
				methodName = methodOrPropertyExpression.getMethodName();
				leftPropertyName = methodOrPropertyExpression.getMethodExpressionParameter1().getUriLiteral();
				rightValue = this.getValue(methodOrPropertyExpression.getMethodExpressionParameter2().getUriLiteral());
				LiteralExpression rightLiteralExpression = this.getRightOperand(rightOperand);
				Object rightValue2 = this.getValue(rightLiteralExpression.getUriLiteral());
				rightMethodBoolean = rightValue2 instanceof Boolean && ((Boolean) rightValue2).booleanValue();
			}
			else
			{
				leftPropertyName = methodOrPropertyExpression.getPropertyExpression().getUriLiteral();
				LiteralExpression rightLiteralExpression = this.getRightOperand(rightOperand);
				rightValue = this.getValue(rightLiteralExpression.getUriLiteral());
			}

			// TODO: A bit ugly and not fully correct, but type safe (hs).
			Path<String> propertyPathString = null;
			Path<Date> propertyPathDate = null;
			Path<Integer> propertyPathInteger = null;
			@SuppressWarnings("rawtypes")
			final Path p = this.getPropertyPathByDotName(table, leftPropertyName);
			if (p.getJavaType() == Calendar.class)
			{
				propertyPathDate = p;
			}
			else if (rightValue instanceof Integer)
			{
				propertyPathInteger = p;
			}
			else
			{
				propertyPathString = p;
			}

			switch (binaryOperator)
			{
				case EQ:
					if (rightValue instanceof Integer)
					{
						if (propertyPathDate != null)
						{
							return cb.equal(propertyPathDate, new Date(((Integer) rightValue).longValue()));
						}
						else
						{
							return cb.equal(propertyPathInteger, rightValue);
						}
					}
					else if (methodName != null) // currently only one method (startsWith) supported and eq/ne with
													// true/false.
					{
						if (rightMethodBoolean)
						{
							return cb.like(propertyPathString, (String) rightValue + "%");
						}
						else
						{
							return cb.notLike(propertyPathString, (String) rightValue + "%");
						}
					}
					else
					{
						if (propertyPathDate != null)
						{
							return cb.equal(propertyPathDate, convertToCalendar((String) rightValue, leftPropertyName));
						}
						else
						{
							return cb.equal(propertyPathString, rightValue);
						}
					}
				case NE:
					if (rightValue instanceof Integer)
					{
						return cb.notEqual(propertyPathInteger, rightValue);
					}
					else if (methodName != null) // currently only one method (startsWith) supported and eq/ne with
													// true/false.
					{
						if (rightMethodBoolean)
							return cb.notLike(propertyPathString, (String) rightValue + "%");
						else
							return cb.like(propertyPathString, (String) rightValue + "%");
					}
					else
					{
						if (propertyPathDate != null)
						{
							return cb.notEqual(propertyPathDate,
								convertToCalendar((String) rightValue, leftPropertyName));
						}
						else
						{
							return cb.notEqual(propertyPathString, rightValue);
						}
					}
				case LT:
					if (rightValue instanceof Integer)
					{
						return cb.lessThan(propertyPathInteger, (Integer) rightValue);
					}
					else
					{
						if (propertyPathDate != null)
						{
							return cb.lessThan(propertyPathDate, convertToDate((String) rightValue, leftPropertyName));
						}
						else
						{
							return cb.lessThan(propertyPathString, (String) rightValue);
						}
					}
				case GT:
					if (rightValue instanceof Integer)
					{
						return cb.greaterThan(propertyPathInteger, (Integer) rightValue);
					}
					else
					{
						if (propertyPathDate != null)
						{
							return cb.greaterThan(propertyPathDate,
								convertToDate((String) rightValue, leftPropertyName));
						}
						else
						{
							return cb.greaterThan(propertyPathString, (String) rightValue);
						}
					}
				case LE:
					if (rightValue instanceof Integer)
					{
						return cb.lessThanOrEqualTo(propertyPathInteger, (Integer) rightValue);
					}
					else
					{
						if (propertyPathDate != null)
						{
							return cb.lessThanOrEqualTo(propertyPathDate,
								convertToDate((String) rightValue, leftPropertyName));
						}
						else
						{
							return cb.lessThanOrEqualTo(propertyPathString, (String) rightValue);
						}
					}
				case GE:
					if (rightValue instanceof Integer)
					{
						return cb.greaterThanOrEqualTo(propertyPathInteger, (Integer) rightValue);
					}
					else
					{
						if (propertyPathDate != null)
						{
							return cb.greaterThanOrEqualTo(propertyPathDate,
								convertToDate((String) rightValue, leftPropertyName));
						}
						else
						{
							return cb.greaterThanOrEqualTo(propertyPathString, (String) rightValue);
						}
					}
				default:
					throw new IllegalArgumentException("Operator " + binaryOperator.name() + " not supported!");
			}
		}
	}

	private Predicate process(CriteriaBuilder cb, Root<T> table, UnaryExpression unaryExpression)
	{
		CommonExpression operand = unaryExpression.getOperand();
		UnaryOperator unaryOperator = unaryExpression.getOperator();
		switch (unaryOperator)
		{
			case NOT:
				return cb.not(this.processFilterExpression(cb, table, operand));
			default:
				throw new IllegalArgumentException("Unary Operator " + unaryOperator.name() + " not supported!");
		}
	}

	private MethodOrPropertyExpression getLeftOperand(CommonExpression leftOperand)
	{
		if (leftOperand instanceof PropertyExpression)
		{
			return new MethodOrPropertyExpression((PropertyExpression) leftOperand);
		}
		else if (leftOperand instanceof MethodExpression)
		{
			return new MethodOrPropertyExpression((MethodExpression) leftOperand);
		}
		else if (leftOperand instanceof LiteralExpression)
		{
			throw new IllegalArgumentException("LeftOperands must not be a literal!");
		}
		else
		{
			throw new IllegalArgumentException(
				"LeftOperand of type " + leftOperand.getKind().name() + " not supported!");
		}
	}

	private LiteralExpression getRightOperand(CommonExpression rightOperand)
	{
		if (rightOperand instanceof PropertyExpression)
		{
			String name = ((PropertyExpression) rightOperand).getUriLiteral();
			// long numbers are treated as properties!
			if (name.matches("[0-9]+"))
			{
				// ???
			}
			throw new IllegalArgumentException("RightOperand must not be a property! Property=\"" + name + "\"");
		}
		else if (rightOperand instanceof LiteralExpression)
		{
			return (LiteralExpression) rightOperand;
		}
		else
		{
			throw new IllegalArgumentException("RightOperand " + rightOperand.getKind().name() + " not supported!");
		}
	}

	private Object getValue(String uriLiteral)
	{
		if (uriLiteral.startsWith("'"))
		{
			return uriLiteral.substring(1, uriLiteral.length() - 1);
		}
		else if ("true".equals(uriLiteral))
		{
			return Boolean.TRUE;
		}
		else if ("false".equals(uriLiteral))
		{
			return Boolean.FALSE;
		}
		else
		{
			return Integer.parseInt(uriLiteral);
		}
	}

	private Calendar convertToCalendar(String rightValue, String leftPropertyName)
	{
		try
		{
			return StringUtil.parseIsoDateInput((String) rightValue);
		}
		catch (ParseException e)
		{
			throw new IllegalArgumentException(
				"Cannot parse date value " + rightValue + " for property \"" + leftPropertyName + "\"!");
		}
	}

	private Date convertToDate(String rightValue, String leftPropertyName)
	{
		try
		{
			return new Date(StringUtil.parseIsoDateInput((String) rightValue).getTime().getTime());
		}
		catch (ParseException e)
		{
			throw new IllegalArgumentException(
				"Cannot parse date value " + rightValue + " for property \"" + leftPropertyName + "\"!");
		}
	}

	class MethodOrPropertyExpression
	{
		private static final String METHOD_startsWith = "STARTSWITH";

		PropertyExpression propertyExpression;
		MethodExpression methodExpression;

		public MethodOrPropertyExpression(PropertyExpression propertyExpression)
		{
			this.propertyExpression = propertyExpression;
		}

		public MethodOrPropertyExpression(MethodExpression methodExpression)
		{
			if (!METHOD_startsWith.equals(methodExpression.getMethod().name()))
			{
				throw new IllegalArgumentException("Method \"" + methodExpression.getMethod().name()
					+ "\" not supported! " + "Only \"" + METHOD_startsWith + "\"");
			}
			if (methodExpression.getParameterCount() != 2)
			{
				throw new IllegalArgumentException("Method expression " + methodExpression.getUriLiteral()
					+ " must have 2 arguments, not " + methodExpression.getParameterCount() + "!");
			}
			if (!(methodExpression.getParameters().get(0) instanceof PropertyExpression))
			{
				throw new IllegalArgumentException(
					"Method expression parameter1 " + methodExpression.getParameters().get(0).getClass() + " of method "
						+ methodExpression.getUriLiteral() + " must be a property!");
			}
			if (!(methodExpression.getParameters().get(1) instanceof LiteralExpression))
			{
				throw new IllegalArgumentException(
					"Method expression parameter2 " + methodExpression.getParameters().get(1).getClass() + " of method "
						+ methodExpression.getUriLiteral() + " must be a literal!");
			}
			this.methodExpression = methodExpression;
		}

		boolean isMethodExpression()
		{
			return this.methodExpression != null;
		}

		public PropertyExpression getPropertyExpression()
		{
			return propertyExpression;
		}

		public MethodExpression getMethodExpression()
		{
			return methodExpression;
		}

		public String getMethodName()
		{
			return methodExpression.getMethod().name();
		}

		public PropertyExpression getMethodExpressionParameter1()
		{
			return (PropertyExpression) methodExpression.getParameters().get(0);
		}

		public LiteralExpression getMethodExpressionParameter2()
		{
			return (LiteralExpression) methodExpression.getParameters().get(1);
		}

		public String getUriLiteral()
		{
			if (this.methodExpression != null)
				return this.methodExpression.getUriLiteral();
			else
				return this.propertyExpression.getUriLiteral();
		}
	}

	private Path getPropertyPathByDotName(Root<T> table, String propertyName)
	{
		if (logger != null && logger.isDebugEnabled())
		{
			logger.debug(LOG_TAG, "ODataFilterToJpaQueryBuilder.getPropertyPathByDotName \"" + propertyName + "\"");
		}

		Path<Object> ret = null;
		int i = 0;
		while (propertyName.length() > 0 && ((i = propertyName.indexOf('.')) > 0))
		{
			final String nextProperty = propertyName.substring(0, i);
			if (logger != null && logger.isDebugEnabled())
			{
				logger.debug(LOG_TAG,
					"ODataFilterToJpaQueryBuilder.getPropertyPathByDotName nextProperty=\"" + nextProperty + "\"");
			}
			if (ret == null)
			{
				ret = table.get(nextProperty);
			}
			else
			{
				ret = ret.get(nextProperty);
			}
			propertyName = propertyName.substring(i + 1);
		}
		if (logger != null && logger.isDebugEnabled())
		{
			logger.debug(LOG_TAG, "ODataFilterToJpaQueryBuilder.getPropertyPathByDotName ret=" + ret);
		}
		return ret == null ? table.get(propertyName) : ret.get(propertyName);
	}
	
	private Attribute getAttributeFromMetaModel(Class metaModel, String fieldName)
	{
		Field f;
		try
		{
			f = metaModel.getDeclaredField(fieldName);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("MetaModel " + metaModel + " has no field \"" + fieldName + "\"");
		}
		try
		{
			return (Attribute) f.get(null);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("MetaModel " + metaModel + " has no attribute field \"" + fieldName + "\"");
		}
	}
}
