package com.financetracker.app.expense.repositories;

import com.financetracker.app.expense.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CustomExpenseRepository {
    Page<Expense> findExpensesBySortingCriteria(String userId,
                                                LocalDate minDate,
                                                LocalDate maxDate,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                List<String> categoryIds,
                                                Pageable pageable);
}
