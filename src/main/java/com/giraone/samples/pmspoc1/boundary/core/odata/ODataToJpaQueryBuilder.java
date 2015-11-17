package com.giraone.samples.pmspoc1.boundary.core.odata;

import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
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

@Stateless
public class ODataToJpaQueryBuilder<T>
{
	public static final String LOG_TAG = "OData";
	
	// TODO: Logger is not injected !!!!!!!!!!!!!!!!!!!!!!!
	@Inject
	private Logger logger;

	CriteriaBuilder cb;
	Root<T> table;

	public ODataToJpaQueryBuilder()
	{
	}

	public void setCriteriaTable(CriteriaBuilder cb, Root<T> table)
	{
		this.cb = cb;
		this.table = table;
	}

	/**
	 * Parse an OData filter expression and return a JPA predicate
	 * 
	 * @param oDataFilter
	 *            OData filter string e.g. "displayName eq 'Test' and version = 1"
	 * @return
	 */
	public Predicate parseFilterExpression(String oDataFilter)
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
		return this.processFilterExpression(commonExpression);
	}
	
	/**
	 * Parse an OData orderby expression and set it on a given criteria API query object.
	 * 
	 * @param cb
	 *            The criteria builder for the request
	 * @param select
	 *            The query to which the order by statement is added
	 * @param oDataOrder
	 *            The orderby expression, e.g. "orderby=name asc, age desc"
	 */
	public void parseOrderExpression(CriteriaBuilder cb, CriteriaQuery<T> select, String oDataOrder)
	{
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "ODataFilterToJpaQueryBuilder.parseOrderExpression \"" + oDataOrder + "\"");

		if (oDataOrder == null || oDataOrder.trim().length() == 0)
		{
			return;
		}

		Edm edm = null;
		EdmEntityType edmType = null;
		OrderByExpression orderByExpression;
		try
		{
			orderByExpression = UriParser.parseOrderBy(edm, edmType, oDataOrder);
		}
		catch (ODataMessageException e)
		{
			throw new IllegalArgumentException("Cannot parse OData order \"" + oDataOrder + "\"", e);
		}

		ArrayList<Order> orderList = new ArrayList<Order>();
		for (OrderExpression orderExpression : orderByExpression.getOrders())
		{
			CommonExpression commonExpression = orderExpression.getExpression();
			if (commonExpression == null || !(commonExpression instanceof PropertyExpression))
				continue;
			PropertyExpression propertyExpression = (PropertyExpression) commonExpression;
			String propertyName = propertyExpression.getUriLiteral();
			orderList.add(SortOrder.desc == orderExpression.getSortOrder() ? cb.desc(this.table.get(propertyName))
				: cb.asc(this.table.get(propertyName)));
		}
		select.orderBy(orderList);
		return;
	}

	protected Predicate processFilterExpression(CommonExpression commonExpression)
	{
		if (commonExpression instanceof BinaryExpression)
		{
			return this.processFilterExpression((BinaryExpression) commonExpression);
		}
		else if (commonExpression instanceof UnaryExpression)
		{
			return this.process((UnaryExpression) commonExpression);
		}
		else
		{
			throw new IllegalArgumentException("Filter like " + commonExpression.getClass() + " that are not binary expressions are not supported yet!");
		}
	}

	private Predicate processFilterExpression(BinaryExpression binaryExpression)
	{
		CommonExpression leftOperand = binaryExpression.getLeftOperand();
		CommonExpression rightOperand = binaryExpression.getRightOperand();
		BinaryOperator binaryOperator = binaryExpression.getOperator();

		if (logger != null && logger.isDebugEnabled())
			System.err.println(LOG_TAG + 
				"ODataFilterToJpaQueryBuilder.processFilterExpression operatator=" + binaryOperator.name());

		if (binaryOperator == BinaryOperator.AND)
		{
			return this.cb.and(new Predicate[] { this.processFilterExpression(leftOperand),
				this.processFilterExpression(rightOperand) });
		}
		else if (binaryOperator == BinaryOperator.OR)
		{
			return this.cb.or(new Predicate[] { this.processFilterExpression(leftOperand),
				this.processFilterExpression(rightOperand) });
		}
		else
		{
			MethodOrPropertyExpression methodOrPropertyExpression = this.getLeftOperand(leftOperand);
			String leftPropertyName, methodName = null;
			Object rightValue = null; boolean rightMethodBoolean = true;
			
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
					
			// TODO: Very ugly and not fully correct, but type safe (hs).
			Path<String> propertyPathString = null;
			Path<Integer> propertyPathInteger = null;
			if (rightValue instanceof Integer)
			{
				propertyPathInteger = this.table.get(leftPropertyName);
			}
			else
			{
				propertyPathString = this.table.get(leftPropertyName);
			}
			
			switch (binaryOperator)
			{
				case EQ:
					if (rightValue instanceof Integer)
					{
						return cb.equal(propertyPathInteger, rightValue);
					}
					else if (methodName != null) // currently only one method (startsWith) supported and eq/ne with true/false.
					{
						if (rightMethodBoolean)
							return cb.like(propertyPathString, (String) rightValue + "%");
						else
							return cb.notLike(propertyPathString, (String) rightValue + "%");
					}
					else
					{
						return cb.equal(propertyPathString, rightValue);
					}
				case NE:
					if (rightValue instanceof Integer)
					{
						return cb.notEqual(propertyPathInteger, rightValue);
					}				
					else if (methodName != null) // currently only one method (startsWith) supported and eq/ne with true/false.
					{
						if (rightMethodBoolean)
							return cb.notLike(propertyPathString, (String) rightValue + "%");
						else
							return cb.like(propertyPathString, (String) rightValue + "%");
					}
					else
					{
						return cb.notEqual(propertyPathString, rightValue);
					}
				case LT:
					if (rightValue instanceof Integer)
						return cb.lessThan(propertyPathInteger, (Integer) rightValue);
					else
						return cb.lessThan(propertyPathString, (String) rightValue);
				case GT:
					if (rightValue instanceof Integer)
						return cb.greaterThan(propertyPathInteger, (Integer) rightValue);
					else
						return cb.greaterThan(propertyPathString, (String) rightValue);
				case LE:
					if (rightValue instanceof Integer)
						return cb.lessThanOrEqualTo(propertyPathInteger, (Integer) rightValue);
					else
						return cb.lessThanOrEqualTo(propertyPathString, (String) rightValue);
				case GE:
					if (rightValue instanceof Integer)
						return cb.greaterThanOrEqualTo(propertyPathInteger, (Integer) rightValue);
					else
						return cb.greaterThanOrEqualTo(propertyPathString, (String) rightValue);
				default:
					throw new IllegalArgumentException("Operator " + binaryOperator.name() + " not supported!");
			}
		}
	}

	private Predicate process(UnaryExpression unaryExpression)
	{
		CommonExpression operand = unaryExpression.getOperand();
		UnaryOperator unaryOperator = unaryExpression.getOperator();
		switch (unaryOperator)
		{
			case NOT:
				return this.cb.not(this.processFilterExpression(operand));
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
			throw new IllegalArgumentException("RightOperand must not be a properties!");
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
				throw new IllegalArgumentException("Method \"" + methodExpression.getMethod().name() + "\" not supported! " +
					"Only \"" + METHOD_startsWith + "\"");
			}	
			if (methodExpression.getParameterCount() != 2)
			{
				throw new IllegalArgumentException("Method expression " + methodExpression.getUriLiteral()
					+ " must have 2 arguments, not " + methodExpression.getParameterCount() + "!");
			}	
			if (!(methodExpression.getParameters().get(0) instanceof PropertyExpression))
			{
				throw new IllegalArgumentException("Method expression parameter1 " + methodExpression.getParameters().get(0).getClass()
					+ " of method " + methodExpression.getUriLiteral() + " must be a property!");
			}	
			if (!(methodExpression.getParameters().get(1) instanceof LiteralExpression))
			{
				throw new IllegalArgumentException("Method expression parameter2 " + methodExpression.getParameters().get(1).getClass()
					+ " of method " + methodExpression.getUriLiteral() + " must be a literal!");
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
}
