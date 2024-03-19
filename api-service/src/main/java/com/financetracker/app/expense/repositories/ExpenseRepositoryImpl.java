package com.financetracker.app.expense.repositories;

import com.financetracker.app.expense.Expense;
import com.financetracker.app.utils.converter.StringListToObjectIdListConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.financetracker.app.expense.repositories.ExpenseRepositoryImpl.QueryUtil.*;

@RequiredArgsConstructor
public class ExpenseRepositoryImpl implements CustomExpenseRepository {

    private final MongoTemplate mongoTemplate;
    private static final String DATE_FIELD = "date";
    private static final String PRICE_FIELD = "price";
    private static final String USER_ID_FIELD = "user.id";
    private static final String CATEGORY_ID_FIELD = "category.$id";

    @Override
    public Page<Expense> findExpensesBySortingCriteria (
        String userId,
        LocalDate minDate,
        LocalDate maxDate,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<String> categoryIds,
        Pageable pageable
    ) {
        Query query = new Query();
        query.addCriteria(Criteria.where(USER_ID_FIELD).is(userId));

        if (minDate != null && maxDate != null) {
            query.addCriteria(ifDateBetween(minDate, maxDate, DATE_FIELD));
        } else if (minDate == null && maxDate != null) {
            query.addCriteria(ifDateTo(maxDate, DATE_FIELD));
        } else if (minDate != null) {
            query.addCriteria(ifDateFrom(minDate, DATE_FIELD));
        }

        if (minPrice != null && maxPrice != null) {
            query.addCriteria(ifPriceBetween(minPrice, maxPrice, PRICE_FIELD));
        } else if (minPrice == null && maxPrice != null) {
            query.addCriteria(ifPriceTo(maxPrice, PRICE_FIELD));
        } else if (minPrice != null) {
            query.addCriteria(ifPriceFrom(minPrice, PRICE_FIELD));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            query.addCriteria(ifIdIn(categoryIds, CATEGORY_ID_FIELD));
        }

        List<Expense> expenses = mongoTemplate.find(query, Expense.class);
        return PageableExecutionUtils.getPage(expenses, pageable, () -> mongoTemplate.count(query, Expense.class));
    }

    static class QueryUtil {

        private static final StringListToObjectIdListConverter converter = new StringListToObjectIdListConverter();

        public static CriteriaDefinition ifDateBetween(LocalDate dateMin, LocalDate dateMax, String field) {
            return Criteria.where(field).gte(dateMin).lte(dateMax);
        }

        public static CriteriaDefinition ifDateFrom(LocalDate dateMin, String field) {
            return Criteria.where(field).gte(dateMin);
        }

        public static CriteriaDefinition ifDateTo(LocalDate dateMax, String field) {
            return Criteria.where(field).lte(dateMax);
        }

        public static CriteriaDefinition ifPriceBetween(BigDecimal priceMin, BigDecimal priceMax, String field) {
            return Criteria.where(field).gte(priceMin).lte(priceMax);
        }

        public static CriteriaDefinition ifPriceFrom(BigDecimal priceMin, String field) {
            return Criteria.where(field).gte(priceMin);
        }

        public static CriteriaDefinition ifPriceTo(BigDecimal priceMax, String field) {
            return Criteria.where(field).lte(priceMax);
        }

        public static CriteriaDefinition ifIdIn(List<String> ids, String field) {
            return Criteria.where(field).in(converter.convert(ids));
        }
    }

}
