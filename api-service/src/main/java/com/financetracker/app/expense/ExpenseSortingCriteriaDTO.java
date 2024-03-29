package com.financetracker.app.expense;

import com.financetracker.app.utils.validation.date.ValidDateInterval;
import com.financetracker.app.utils.validation.numbers.ValidNumberRange;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ValidDateInterval(startDateField = "minDate", endDateField = "maxDate")
@ValidNumberRange(startNumberField = "minPrice", endNumberField = "maxPrice")
public record ExpenseSortingCriteriaDTO(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate minDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate maxDate,

    @Min(0)
    BigDecimal minPrice,

    @Min(0)
    BigDecimal maxPrice,

    List<String> categoryIds
) {
    @ConstructorProperties({"min-date", "max-date", "min-price", "max-price", "category-ids"})
    public ExpenseSortingCriteriaDTO(LocalDate minDate, LocalDate maxDate, BigDecimal minPrice, BigDecimal maxPrice, List<String> categoryIds) {
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.categoryIds = categoryIds;
    }
}
