package com.financetracker.app.expense.repositories;

import com.financetracker.app.expense.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ExpenseRepository extends MongoRepository<Expense, String>, CustomExpenseRepository {

    Page<Expense> findExpensesByUserId(String userId, Pageable pageable);

    Optional<Expense> findExpenseByIdAndUserId(String expenseId, String userId);

    void deleteExpenseByIdAndUserId(String expenseId, String userId);

}
