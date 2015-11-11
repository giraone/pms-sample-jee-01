package com.giraone.samples.pmspoc1.boundary.core.odata;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;

@Stateless
public class ODataToJpaQueryBuilder<T>
{
	public static final String LOG_TAG = "OData";

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
		return null;
	}
	/*
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
	*/

	public void parseOrderExpression(CriteriaBuilder cb, CriteriaQuery<T> select, String oDataOrder)
	{
		return;
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
	/*
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
			throw new IllegalArgumentException("Filter that are not binary expressions are not supported yet!");
		}
	}

	private Predicate processFilterExpression(BinaryExpression binaryExpression)
	{
		CommonExpression leftOperand = binaryExpression.getLeftOperand();
		CommonExpression rightOperand = binaryExpression.getRightOperand();
		BinaryOperator binaryOperator = binaryExpression.getOperator();

		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG,
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
			PropertyExpression propertyExpression = this.getLeftOperand(leftOperand);
			String propertyName = propertyExpression.getUriLiteral();
			// Hint: propertyExpression.getPropertyName() returns NULL (perhaps because there is no EDM)

			LiteralExpression literalExpression = this.getRightOperand(rightOperand);
			Object value = this.getValue(literalExpression.getUriLiteral());

			// TODO: Very ugly and not fully correct, but type safe (hs).
			Path<String> propertyPathString = null;
			Path<Integer> propertyPathInteger = null;
			if (value instanceof Integer)
			{
				propertyPathInteger = this.table.get(propertyName);
			}
			else
			{
				propertyPathString = this.table.get(propertyName);
			}

			switch (binaryOperator)
			{
				case EQ:
					if (value instanceof Integer)
						return cb.equal(propertyPathInteger, value);
					else if (((String) value).endsWith("%"))
						return cb.like(propertyPathString, (String) value);
					else
						return cb.equal(propertyPathString, value);
				case NE:
					if (value instanceof Integer)
						return cb.notEqual(propertyPathInteger, value);
					else
						return cb.notEqual(propertyPathString, value);
				case LT:
					if (value instanceof Integer)
						return cb.lessThan(propertyPathInteger, (Integer) value);
					else
						return cb.lessThan(propertyPathString, (String) value);
				case GT:
					if (value instanceof Integer)
						return cb.greaterThan(propertyPathInteger, (Integer) value);
					else
						return cb.greaterThan(propertyPathString, (String) value);
				case LE:
					if (value instanceof Integer)
						return cb.lessThanOrEqualTo(propertyPathInteger, (Integer) value);
					else
						return cb.lessThanOrEqualTo(propertyPathString, (String) value);
				case GE:
					if (value instanceof Integer)
						return cb.greaterThanOrEqualTo(propertyPathInteger, (Integer) value);
					else
						return cb.greaterThanOrEqualTo(propertyPathString, (String) value);
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

	private PropertyExpression getLeftOperand(CommonExpression leftOperand)
	{
		if (leftOperand instanceof PropertyExpression)
		{
			return (PropertyExpression) leftOperand;
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
		else
		{
			return Integer.parseInt(uriLiteral);
		}
	}
	*/
}
