/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2015 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.engine;

import jgnash.util.DateUtils;
import jgnash.util.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * Exchange rate history node for a {@code ExchangeRate}.
 * {@code ExchangeRateHistoryNode} objects are immutable.
 *
 * @author Craig Cavanaugh
 */
@Entity
public class ExchangeRateHistoryNode implements Comparable<ExchangeRateHistoryNode>, Serializable {

    @SuppressWarnings("UnusedDeclaration")
    @Id @GeneratedValue(strategy = GenerationType.TABLE)
    public long id;

    @Column(precision = 20, scale = 8)
    private BigDecimal rate = BigDecimal.ZERO;

    private transient LocalDate cachedLocalDate = LocalDate.now();

    @Temporal(TemporalType.DATE)
    private Date date = DateUtils.asDate(cachedLocalDate);

    /**
     * No argument constructor for reflection purposes.
     * <b>Do not use to create a new instance</b>
     */
    @SuppressWarnings("unused")
    ExchangeRateHistoryNode() {
    }

    /**
     * public constructor
     *
     * @param localDate date for this history node.  The date will be trimmed
     * @param rate exchange rate for the given date
     */
    ExchangeRateHistoryNode(final LocalDate localDate, final BigDecimal rate) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(rate);

        this.date = DateUtils.asDate(localDate);
        this.cachedLocalDate = localDate;
        this.rate = rate;
    }

    public LocalDate getLocalDate() {
        return cachedLocalDate;
    }

    @Override
    public int compareTo(@NotNull final ExchangeRateHistoryNode node) {
        return getLocalDate().compareTo(node.getLocalDate());
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof ExchangeRateHistoryNode
                && date.equals(((ExchangeRateHistoryNode) o).date);
    }

    @Override
    public int hashCode() {
        return date.hashCode();
    }

    public BigDecimal getRate() {
        return rate;
    }

    protected Object readResolve() {
        postLoad();
        return this;
    }

    @PostLoad
    private void postLoad() {
        cachedLocalDate = DateUtils.asLocalDate(date);
    }
}