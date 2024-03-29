package com.financetracker.app.expense


import com.financetracker.app.category.Category
import com.financetracker.app.category.CategoryDTO
import com.financetracker.app.category.CategoryMapper
import com.financetracker.app.expense.repositories.ExpenseRepository
import com.financetracker.app.security.authorization.Role
import com.financetracker.app.user.User
import com.financetracker.app.user.UserService
import com.financetracker.app.utils.converter.StringListToObjectIdListConverter
import com.financetracker.app.utils.exception.custom.DocumentNotFoundException
import org.mapstruct.factory.Mappers
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

import java.time.LocalDate

class ExpenseServiceTest extends Specification {

    CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper)

    ExpenseMapper expenseMapper = Mappers.getMapper(ExpenseMapper).tap {
        expenseMapper -> expenseMapper.categoryMapper = this.categoryMapper
    }
    ExpenseRepository expenseRepository = Mock()
    UserService userService = Mock()

    ExpenseService expenseService = new ExpenseService(expenseRepository, expenseMapper, userService)

    def "should get all user expenses"() {
        given:
        String userId = "1"
        PageRequest pageable = PageRequest.of(0, 5)

        when:
        Page<Expense> result = expenseService.getUserExpenses(userId, pageable)

        then:
        1 * expenseRepository.findExpensesByUserId(userId, pageable) >> getPagedExpenses()

        and:
        result == getPagedExpenses()
    }

    def "should get expense by id"() {
        given:
        String expenseId = "1"
        String userId = "1"

        when:
        Expense result = expenseService.getExpense(expenseId, userId)

        then:
        1 * expenseRepository.findExpenseByIdAndUserId(expenseId, userId) >> Optional.of(getExpense())

        and:
        result == getExpense()
    }

    def "should throw DocumentNotFoundException when expense was not found by id"() {
        given:
        String expenseId = "1"
        String userId = "1"

        when:
        expenseService.getExpense(expenseId, userId)

        then:
        1 * expenseRepository.findExpenseByIdAndUserId(expenseId, userId) >> Optional.empty()

        and:
        thrown(DocumentNotFoundException)
    }

    def "should get user sorted expenses"() {
        given:
        String userId = "1"
        PageRequest pageable = PageRequest.of(0, 5)
        ExpenseSortingCriteriaDTO sortingCriteria = new ExpenseSortingCriteriaDTO(LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1), BigDecimal.valueOf(50), BigDecimal.valueOf(300), List.of("1", "2"))

        when:
        Page<Expense> result = expenseService.getUserSortedExpenses(userId, sortingCriteria, pageable)

        then:
        1 * expenseRepository.findExpensesBySortingCriteria(userId, sortingCriteria.minDate(), sortingCriteria.maxDate(), sortingCriteria.minPrice(), sortingCriteria.maxPrice(), sortingCriteria.categoryIds(), pageable) >> getPagedExpenses()

        and:
        result == getPagedExpenses()
    }

    def "should create expense"() {
        given:
        String userId = "1"
        AddExpenseDTO expenseToAdd = new AddExpenseDTO(LocalDate.of(2020, 1, 3), new CategoryDTO("1", "Food"), BigDecimal.valueOf(27.50))

        when:
        expenseService.createExpense(userId, expenseToAdd)

        then:
        1 * userService.getUserById(userId)
        1 * expenseRepository.insert(_ as Expense)
    }

    def "should update expense"() {
        given:
        String userId = "1"
        ExpenseDTO expenseToUpdate = getExpenseDTO()

        when:
        expenseService.updateExpense(userId, expenseToUpdate)

        then:
        1 * userService.getUserById(userId)
        1 * expenseRepository.save(_ as Expense)
    }

    def "should delete expense"() {
        given:
        String expenseId = "1"
        String userId = "1"

        when:
        expenseService.deleteExpense(expenseId, userId)

        then:
        1 * expenseRepository.deleteExpenseByIdAndUserId(expenseId, userId)
    }

    private Page<Expense> getPagedExpenses() {
        List<Expense> expenses = List.of(
            getExpense("1"),
            getExpense("2")
        )

        return new PageImpl<Expense>(expenses, PageRequest.of(0, 5), 5)
    }

    private Expense getExpense() {
        return new Expense("1", LocalDate.of(2020, 1, 1), new Category("1", "Food", null), BigDecimal.valueOf(100.50), getUser())
    }

    private Expense getExpense(String id) {
        return new Expense(id, LocalDate.of(2020, 1, 1), new Category("1", "Food", null), BigDecimal.valueOf(100.50), getUser())
    }

    private ExpenseDTO getExpenseDTO() {
        new ExpenseDTO("1", LocalDate.of(2020, 1, 1), new CategoryDTO("1", "Food"), BigDecimal.valueOf(100.50))
    }

    private User getUser() {
        return new User("1", "Anne", "anne@gmail.com", "anne123", List.of(Role.USER))
    }
}
