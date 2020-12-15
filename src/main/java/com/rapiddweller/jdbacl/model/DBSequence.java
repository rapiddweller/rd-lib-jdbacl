/*
 * (c) Copyright 2011 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License (GPL).
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED CONDITIONS,
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.rapiddweller.jdbacl.model;

import java.math.BigInteger;

import com.rapiddweller.commons.NullSafeComparator;

/**
 * Represents a database sequence.<br/><br/>
 * Created: 31.05.2011 17:55:05
 * @since 0.6.8
 * @author Volker Bergmann
 */
public class DBSequence extends AbstractDBObject implements ContainerComponent {

	private static final long serialVersionUID = 8602052311285255364L;

	private String catalogName;
	private String schemaName;
	private BigInteger start = BigInteger.ONE;
	private BigInteger increment = BigInteger.ONE;
	private BigInteger maxValue = null;
	private BigInteger minValue = null;
	private Boolean cycle;
	private Long cache;
	private Boolean order;
	private BigInteger lastNumber = BigInteger.ZERO;

	public DBSequence(String name, DBSchema owner) {
		super(name, "sequence", owner);
        if (owner != null)
    		owner.addSequence(this);
	}

	public DBSequence(String name, String catalogName, String schemaName) {
		super(name, "sequence", null);
		this.catalogName = catalogName;
		this.schemaName = schemaName;
	}

	@Override
	public void setOwner(CompositeDBObject<?> owner) {
		super.setOwner(owner);
        if (owner != null) {
    		DBSchema schema = (DBSchema) owner;
        	this.catalogName = schema.getCatalog().getName();
        	this.schemaName = schema.getName();
        }
	}

	public String getCatalogName() {
		return catalogName;
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public BigInteger getStart() {
		return start;
	}

	public BigInteger getStartIfNotDefault() {
		return (BigInteger.ONE.equals(this.start) ? null : this.start);
	}

	public void setStart(BigInteger start) {
		this.start = start;
	}

	public BigInteger getIncrement() {
		return increment;
	}
	
	public BigInteger getIncrementIfNotDefault() {
		return (BigInteger.ONE.equals(increment) ?  null : increment);
	}
	
	public void setIncrement(BigInteger increment) {
		this.increment = increment;
	}

	public BigInteger getMaxValue() {
		return maxValue;
	}

	public BigInteger getMaxValueIfNotDefault() {
		return maxValue;
	}

	public void setMaxValue(BigInteger maxValue) {
		this.maxValue = maxValue;
	}

	public BigInteger getMinValue() {
		return minValue;
	}

	public BigInteger getMinValueIfNotDefault() {
		return minValue;
	}

	public void setMinValue(BigInteger minValue) {
		this.minValue = minValue;
	}

	public Boolean isCycle() {
		return cycle;
	}

	public void setCycle(Boolean cycle) {
		this.cycle = cycle;
	}

	public Long getCache() {
		return cache;
	}
	
	public void setCache(Long cache) {
		this.cache = cache;
	}

	public Boolean isOrder() {
		return order;
	}

	public void setOrder(Boolean order) {
		this.order = order;
	}

	public BigInteger getLastNumber() {
		return lastNumber;
	}
	
	public void setLastNumber(BigInteger lastNumber) {
		this.lastNumber = lastNumber;
	}
	
	@Override
	public boolean isIdentical(DBObject other) {
		if (this == other)
			return true;
		if (!(other instanceof DBSequence))
			return false;
		DBSequence that = (DBSequence) other;
		return this.name.equals(that.getName()) &&
			this.start == that.getStart() &&
			this.increment == that.getIncrement() &&
			NullSafeComparator.equals(this.maxValue, that.getMaxValue()) &&
			NullSafeComparator.equals(this.minValue, that.getMinValue()) &&
			this.cycle == that.isCycle() &&
			this.cache == that.getCache() &&
			this.order == that.isOrder();
	}

	public String dropDDL() {
		return "drop sequence " + name;
	}

}
