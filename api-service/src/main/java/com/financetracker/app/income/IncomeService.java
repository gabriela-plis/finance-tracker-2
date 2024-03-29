package com.financetracker.app.income;

import com.financetracker.app.user.UserService;
import com.financetracker.app.utils.exception.custom.DocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;
    private final UserService userService;

    public Page<Income> getUserIncomes(String userId, Pageable pageable) {
        return incomeRepository.findIncomesByUserId(userId, pageable);
    }

    public Income getIncome(String incomeId, String userId) {
        return incomeRepository.findIncomeByIdAndUserId(incomeId, userId)
            .orElseThrow(DocumentNotFoundException::new);
    }

    public Page<Income> getUserSortedIncomes(String userId, IncomeSortingCriteriaDTO criteria, Pageable pageable) {
        return incomeRepository.findIncomesByUserIdAndDateBetweenAndAmountBetweenAndDescriptionContainingIgnoreCase(userId, criteria.minDate(), criteria.maxDate(), criteria.minAmount(), criteria.maxAmount(), criteria.keyword(), pageable);
    }

    public void createIncome(String userId, AddIncomeDTO incomeToAdd) {
        Income income = incomeMapper.toEntity(incomeToAdd);
        income.setUser(userService.getUserById(userId));

        incomeRepository.insert(income);
    }

    public void updateIncome(String userId, IncomeDTO incomeToUpdate) {
        Income income = incomeMapper.toEntity(incomeToUpdate);
        income.setUser(userService.getUserById(userId));

        incomeRepository.save(income);
    }

    public void deleteIncome(String incomeId, String userId) {
        incomeRepository.deleteIncomeByIdAndUserId(incomeId, userId);
    }

}
