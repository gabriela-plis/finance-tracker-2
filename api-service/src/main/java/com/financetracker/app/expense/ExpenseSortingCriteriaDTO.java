package com.financetracker.app.expense;

import com.financetracker.app.utils.validation.date.ValidDateInterval;
import com.financetracker.app.utils.validation.numbers.ValidNumberRange;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ValidDateInterval(startDateField = "dateMin", endDateField = "dateMax")
@ValidNumberRange(startNumberField = "priceMin", endNumberField = "priceMax")
public record ExpenseSortingCriteriaDTO(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateMin,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateMax,

    @Min(0)
    BigDecimal priceMin,

    @Min(0)
    BigDecimal priceMax,

    List<String> categoryIds
) {
    @ConstructorProperties({"date-min", "date-max", "price-min", "price-max", "category-ids"})
    public ExpenseSortingCriteriaDTO(LocalDate dateMin, LocalDate dateMax, BigDecimal priceMin, BigDecimal priceMax, List<String> categoryIds) {
        this.dateMin = dateMin;
        this.dateMax = dateMax;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
        this.categoryIds = categoryIds;
    }
}
