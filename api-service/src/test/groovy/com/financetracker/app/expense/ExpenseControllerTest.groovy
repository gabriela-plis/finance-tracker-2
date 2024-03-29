package com.financetracker.app.expense

import com.financetracker.app.category.Category
import com.financetracker.app.category.CategoryMapper
import com.financetracker.app.config.MvcTestsConfig
import com.financetracker.app.security.authentication.AuthenticationService
import com.financetracker.app.utils.exception.custom.DocumentNotFoundException
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.Authentication
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Unroll

import java.time.LocalDate

import static ExpenseCorrectData.*
import static groovy.json.JsonOutput.*
import static org.hamcrest.Matchers.*
import static org.springframework.http.MediaType.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.mapstruct.factory.Mappers.getMapper
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = ExpenseController.class)
class ExpenseControllerTest extends MvcTestsConfig {

    @Autowired
    MockMvc mvc

    @SpringBean
    ExpenseService expenseService = Mock()

    @SpringBean
    CategoryMapper categoryMapper = getMapper(CategoryMapper)

    @SpringBean
    ExpenseMapper expenseMapper = Spy(
        getMapper(ExpenseMapper).tap {
            expenseMapper -> expenseMapper.categoryMapper = this.categoryMapper
        }
    )

    @SpringBean
    AuthenticationService authenticationService = Mock()

    @WithMockUser
    def "should return 200 (OK) and paged expenses"() {
        given:
        String userId = "1"

        when:
        def result = mvc
            .perform(get("/users/me/expenses"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * expenseService.getUserExpenses(userId, _ as PageRequest) >> getPagedExpenses()
        1 * expenseMapper.toPagedDTO(_ as Page<Expense>)

        and:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.totalPages').value(1))
        result.andExpect(jsonPath('$.currentPage').value(0))
        result.andExpect(jsonPath('$.expenses[*]', hasSize(2)))
        result.andExpect(jsonPath('$.expenses[0].id').value("1"))
        result.andExpect(jsonPath('$.expenses[0].date').value("2020-01-01"))
        result.andExpect(jsonPath('$.expenses[0].category.id').value("1"))
        result.andExpect(jsonPath('$.expenses[0].category.name').value("Food"))
        result.andExpect(jsonPath('$.expenses[0].price').value(100.5))
        result.andExpect(jsonPath('$.expenses[1].id').value("2"))
        result.andExpect(jsonPath('$.expenses[1].date').value("2021-12-15"))
        result.andExpect(jsonPath('$.expenses[1].category.id').value("2"))
        result.andExpect(jsonPath('$.expenses[1].category.name').value("Healthcare"))
        result.andExpect(jsonPath('$.expenses[1].price').value(20.22))
    }

    @WithMockUser
    def "should return 200 (OK) and expense"() {
        given:
        String expenseId = "1"
        String userId = "1"

        when:
        def result = mvc
            .perform(get("/users/me/expenses/$expenseId"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * expenseService.getExpense(expenseId, userId) >> getExpense()
        1 * expenseMapper.toDTO(_ as Expense)

        and:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.id').value("1"))
        result.andExpect(jsonPath('$.date').value("2020-01-01"))
        result.andExpect(jsonPath('$.category.id').value("1"))
        result.andExpect(jsonPath('$.category.name').value("Food"))
        result.andExpect(jsonPath('$.price').value(100.5))
    }

    @WithMockUser
    def "should return 404 (NOT FOUND) when requesting expense was not found"() {
        given:
        String expenseId = "1"
        String userId = "1"

        when:
        def result = mvc
            .perform(get("/users/me/expenses/$expenseId"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * expenseService.getExpense(expenseId, userId) >> { throw new DocumentNotFoundException() }
        0 * expenseMapper.toDTO(_ as Expense)

        and:
        result.andExpect(status().isNotFound())
    }

    @WithMockUser
    def "should return 200 (OK) and paged, sorted expenses"() {
        given:
        String userId = "1"

        when:
        def result = mvc
            .perform(get("/users/me/expenses")
                .param(minDate, "2019-01-01")
                .param(maxDate, "2022-01-01")
                .param(minPrice, "10")
                .param(maxDate, "150")
                .param(categoryIds, "1", "2"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * expenseService.getUserExpenses(userId, _ as PageRequest) >> getPagedExpenses()
        1 * expenseMapper.toPagedDTO(_ as Page<Expense>)

        and:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.totalPages').value(1))
        result.andExpect(jsonPath('$.currentPage').value(0))
        result.andExpect(jsonPath('$.expenses[*]', hasSize(2)))
        result.andExpect(jsonPath('$.expenses[0].id').value("1"))
        result.andExpect(jsonPath('$.expenses[0].date').value("2020-01-01"))
        result.andExpect(jsonPath('$.expenses[0].category.id').value("1"))
        result.andExpect(jsonPath('$.expenses[0].category.name').value("Food"))
        result.andExpect(jsonPath('$.expenses[0].price').value(100.5))
        result.andExpect(jsonPath('$.expenses[1].id').value("2"))
        result.andExpect(jsonPath('$.expenses[1].date').value("2021-12-15"))
        result.andExpect(jsonPath('$.expenses[1].category.id').value("2"))
        result.andExpect(jsonPath('$.expenses[1].category.name').value("Healthcare"))
        result.andExpect(jsonPath('$.expenses[1].price').value(20.22))

        where:
        minDate << ["minDate", "min-date"]
        maxDate << ["maxDate", "max-date"]
        minPrice << ["minPrice", "min-price"]
        maxPrice << ["maxPrice", "max-price"]
        categoryIds << ["categoryIds", "category-ids"]
    }

    @WithMockUser
    def "should return 201 (CREATED) when expense is created"() {
        given:
        LinkedHashMap<String, Serializable> expenseToCreate = [
            date    : correctDate,
            category: correctCategory,
            price   : correctPrice
        ]

        String userId = "1"

        when:
        def result = mvc
            .perform(post("/users/me/expenses")
                .contentType(APPLICATION_JSON)
                .content(toJson(expenseToCreate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * expenseService.createExpense(userId, _ as AddExpenseDTO)

        and:
        result.andExpect(status().isCreated())
    }

    @WithMockUser
    def "should return 422 (UNPROCESSABLE ENTITY) when #scenario of expense to create fail validation"() {
        given:
        LinkedHashMap<String, Serializable> expenseToCreate = [
            date    : date,
            category: category,
            price   : price
        ]

        String userId = "1"

        when:
        def result = mvc
            .perform(post("/users/me/expenses")
                .contentType(APPLICATION_JSON)
                .content(toJson(expenseToCreate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        0 * expenseService.createExpense(userId, _ as AddExpenseDTO)

        and:
        result.andExpect(status().isUnprocessableEntity())

        where:
        date                  | category                 | price        | scenario
        null                  | correctCategory          | correctPrice | "null date"
        " "                   | correctCategory          | correctPrice | "whitespace date"
        "12-14-2022"          | correctCategory          | correctPrice | "mm-dd-yyyy date"
        "22-11-2022"          | correctCategory          | correctPrice | "dd-mm-yyyy date"
        "2022-12-14 13:58:00" | correctCategory          | correctPrice | "yyyy-mm-dd HH:mm:ss date"
        correctDate           | [id: null, name: "Food"] | correctPrice | "category null id"
        correctDate           | [id: " ", name: "Food"]  | correctPrice | "category whitespace id"
        correctDate           | [id: "1", name: null]    | correctPrice | "category null name"
        correctDate           | [id: "1", name: " "]     | correctPrice | "category whitespace name"
        correctDate           | correctCategory          | null         | "null price"
        correctDate           | correctCategory          | -1           | "negative price"
        correctDate           | correctCategory          | 0.0          | "0 price"
    }


    @WithMockUser
    def "should return 200 (OK) when expense is updated"() {
        given:
        String expenseId = correctId
        String userId = "1"
        LinkedHashMap<String, Serializable> expenseToUpdate = [
            id      : correctId,
            date    : correctDate,
            category: correctCategory,
            price   : correctPrice
        ]

        when:
        def result = mvc
            .perform(put("/users/me/expenses/$expenseId")
                .contentType(APPLICATION_JSON)
                .content(toJson(expenseToUpdate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * expenseService.updateExpense(userId, _ as ExpenseDTO)

        and:
        result.andExpect(status().isOk())
    }

    @WithMockUser
    def "should return 409 (CONFLICT) when ID expense to update doesn't match ID in the URL"() {
        given:
        String expenseId = "2"
        LinkedHashMap<String, Serializable> expenseToUpdate = [
            id      : correctId,
            date    : correctDate,
            category: correctCategory,
            price   : correctPrice
        ]

        when:
        def result = mvc
            .perform(put("/users/me/expenses/$expenseId")
                .contentType(APPLICATION_JSON)
                .content(toJson(expenseToUpdate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        result.andExpect(status().isConflict())
    }

    @WithMockUser
    @Unroll
    def "should return 422 (UNPROCESSABLE ENTITY) when #scenario of expense to update fail validation"() {
        given:
        String expenseId = "2"
        LinkedHashMap<String, Serializable> expenseToUpdate = [
            id      : id,
            date    : date,
            category: category,
            price   : price
        ]

        when:
        def result = mvc
            .perform(put("/users/me/expenses/$expenseId")
                .contentType(APPLICATION_JSON)
                .content(toJson(expenseToUpdate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        0 * expenseService.updateExpense(_ as String, _ as ExpenseDTO)

        and:
        result.andExpect(status().isUnprocessableEntity())

        where:
        id        | date                  | category                 | price        | scenario
        null      | correctDate           | correctCategory          | correctPrice | "null id"
        " "       | correctDate           | correctCategory          | correctPrice | "whitespace id"
        correctId | null                  | correctCategory          | correctPrice | "null date"
        correctId | " "                   | correctCategory          | correctPrice | "whitespace date"
        correctId | "12-14-2022"          | correctCategory          | correctPrice | "mm-dd-yyyy date"
        correctId | "22-11-2022"          | correctCategory          | correctPrice | "dd-mm-yyyy date"
        correctId | "2022-12-14 13:58:00" | correctCategory          | correctPrice | "yyyy-mm-dd HH:mm:ss date"
        correctId | correctDate           | [id: null, name: "Food"] | correctPrice | "category null id"
        correctId | correctDate           | [id: " ", name: "Food"]  | correctPrice | "category whitespace id"
        correctId | correctDate           | [id: "1", name: null]    | correctPrice | "category null name"
        correctId | correctDate           | [id: "1", name: " "]     | correctPrice | "category whitespace name"
        correctId | correctDate           | correctCategory          | null         | "null price"
        correctId | correctDate           | correctCategory          | -1           | "negative price"
        correctId | correctDate           | correctCategory          | 0.0          | "0 price"
    }

    @WithMockUser
    def "should return 204 (NO CONTENT) when expense is deleted"() {
        given:
        String expenseId = "1"
        String userId = "1"

        when:
        def result = mvc
            .perform(delete("/users/me/expenses/$expenseId"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * expenseService.deleteExpense(expenseId, userId)

        and:
        result.andExpect(status().isNoContent())
    }

    private Page<Expense> getPagedExpenses() {
        List<Expense> expenses = List.of(
            new Expense("1", LocalDate.of(2020, 1, 1), new Category("1", "Food", null), BigDecimal.valueOf(100.5), null),
            new Expense("2", LocalDate.of(2021, 12, 15), new Category("2", "Healthcare", null), BigDecimal.valueOf(20.22), null)
        )

        return new PageImpl<Expense>(expenses, PageRequest.of(0, 5), 5)
    }

    private Expense getExpense() {
        return new Expense("1", LocalDate.of(2020, 1, 1), new Category("1", "Food", null), BigDecimal.valueOf(100.5), null)
    }
}
