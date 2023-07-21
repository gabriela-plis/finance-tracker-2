package com.financetracker.app.expense;

import com.financetracker.app.user.UserService;
import com.financetracker.app.utils.converter.StringListToObjectIdListConverter;
import com.financetracker.app.utils.exception.DocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    private final UserService userService;

    private final StringListToObjectIdListConverter converter;

    public Page<Expense> getUserExpenses(String userId, Pageable pageable) {
        return expenseRepository.findByUser_Id(userId, pageable);
    }

    public Expense getExpense(String expenseId) {
        return expenseRepository.findById(expenseId)
            .orElseThrow(DocumentNotFoundException::new);
    }

    public Page<Expense> getUserSortedExpenses(String userId, ExpenseSortingCriteriaDTO criteria, Pageable pageable) {
        Page<Expense> expenses;
        if (criteria.categoryIds() == null) {
            expenses = expenseRepository.findByUserIdAndDateBetweenAndPriceBetween(userId, criteria.dateMin(), criteria.dateMax(), criteria.priceMin(), criteria.priceMax(), pageable);
        } else {
            expenses = expenseRepository.findByUserIdAndDateBetweenAndPriceBetweenAndCategoryIdIn(userId, criteria.dateMin(), criteria.dateMax(), criteria.priceMin(), criteria.priceMax(), converter.convert(criteria.categoryIds()), pageable);
        }

        return expenses;
    }

    public void createExpense(String userId, AddExpenseDTO expenseToAdd) {
            Expense expense = expenseMapper.toEntity(expenseToAdd);
            expense.setUser(userService.getUserById(userId));

            expenseRepository.insert(expense);
    }

    public void updateExpense(String userId, ExpenseDTO expenseToUpdate) {
            Expense expense = expenseMapper.toEntity(expenseToUpdate);
            expense.setUser(userService.getUserById(userId));

            expenseRepository.save(expense);
    }

    public void deleteExpense(String expenseId) {
        expenseRepository.deleteById(expenseId);
    }



}