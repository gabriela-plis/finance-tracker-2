package com.financetracker.app.income;

import com.financetracker.app.utils.validation.date.ValidDateInterval;
import com.financetracker.app.utils.validation.numbers.ValidNumberRange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@ValidDateInterval(startDateField = "minDate", endDateField = "maxDate")
@ValidNumberRange(startNumberField = "minAmount", endNumberField = "maxAmount")
public record IncomeSortingCriteriaDTO(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate minDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate maxDate,

    @Min(0)
    BigDecimal minAmount,

    @Min(0)
    BigDecimal maxAmount,

    @Size(max = 64, message = "Exceeded maximum length for the keyword")
    String keyword
) { }
