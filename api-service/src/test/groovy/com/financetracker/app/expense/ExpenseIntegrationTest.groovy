package com.financetracker.app.expense

import com.financetracker.app.category.Category
import com.financetracker.app.category.CategoryRepository
import com.financetracker.app.config.IntegrationTestConfig
import com.financetracker.app.expense.repositories.ExpenseRepository
import com.financetracker.app.security.authentication.CustomUserDetails
import com.financetracker.app.security.authorization.Role
import com.financetracker.app.user.User
import com.financetracker.app.user.UserRepository
import org.spockframework.spring.EnableSharedInjection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Shared
import spock.lang.Unroll

import java.time.LocalDate

import static com.financetracker.app.expense.ExpenseCorrectData.correctCategory
import static com.financetracker.app.expense.ExpenseCorrectData.correctDate
import static com.financetracker.app.expense.ExpenseCorrectData.correctId
import static com.financetracker.app.expense.ExpenseCorrectData.correctPrice
import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.hasSize
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user

@AutoConfigureMockMvc
@EnableSharedInjection
@WithMockUser
class ExpenseIntegrationTest extends IntegrationTestConfig {

    @Autowired
    MockMvc mvc

    @Autowired
    ExpenseRepository expenseRepository

    @Shared
    @Autowired
    CategoryRepository categoryRepository

    @Shared
    @Autowired
    UserRepository userRepository

    def setupSpec() {
        categoryRepository.saveAll(getCategories())
        userRepository.save(getUser())
    }

    def setup() {
        expenseRepository.saveAll(getExpenses())
    }

    def cleanup() {
        expenseRepository.deleteAll()
    }

    def "should return 200 (OK) and paged expenses"() {
        given:
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(get("/users/me/expenses")
                .with(user(userDetails)))
            .andDo(print())

        then:
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

    def "should return 200 (OK) and expense"() {
        given:
        String expenseId = "1"
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(get("/users/me/expenses/$expenseId")
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.id').value("1"))
        result.andExpect(jsonPath('$.date').value("2020-01-01"))
        result.andExpect(jsonPath('$.category.id').value("1"))
        result.andExpect(jsonPath('$.category.name').value("Food"))
        result.andExpect(jsonPath('$.price').value(100.5))
    }

    def "should return 404 (NOT FOUND) when requesting expense was not found"() {
        given:
        String expenseId = "3"
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(get("/users/me/expenses/$expenseId")
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isNotFound())
    }

    def "should return 200 (OK) and paged, sorted expenses"() {
        given:
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(get("/users/me/expenses")
                .param("dateMin", "2019-01-01")
                .param("dateMax", "2022-01-01")
                .param("priceMin", "10")
                .param("priceMax", "150")
                .param("categoryIds", "1", "2")
                .with(user(userDetails)))
            .andDo(print())

        then:
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

    def "should return 201 (CREATED) when expense is created"() {
        given:
        LinkedHashMap<String, Serializable> expenseToCreate = [
            date    : correctDate,
            category: correctCategory,
            price   : correctPrice
        ]
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(post("/users/me/expenses")
                .contentType(APPLICATION_JSON)
                .content(toJson(expenseToCreate))
                .accept(APPLICATION_JSON)
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isCreated())
    }

    def "should return 422 (UNPROCESSABLE ENTITY) when #scenario of expense to create fail validation"() {
        given:
        LinkedHashMap<String, Serializable> expenseToCreate = [
            date    : date,
            category: category,
            price   : price
        ]

        when:
        def result = mvc
            .perform(post("/users/me/expenses")
                .contentType(APPLICATION_JSON)
                .content(toJson(expenseToCreate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
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


    def "should return 200 (OK) when expense is updated"() {
        given:
        String expenseId = correctId
        LinkedHashMap<String, Serializable> expenseToUpdate = [
            id      : correctId,
            date    : correctDate,
            category: correctCategory,
            price   : correctPrice
        ]
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(put("/users/me/expenses/$expenseId")
                .contentType(APPLICATION_JSON)
                .content(toJson(expenseToUpdate))
                .accept(APPLICATION_JSON)
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isOk())
    }

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

    def "should return 204 (NO CONTENT) when expense is deleted"() {
        given:
        String expenseId = "1"
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(delete("/users/me/expenses/$expenseId")
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isNoContent())
    }

    private List<Expense> getExpenses() {
        return List.of(
            new Expense("1", LocalDate.of(2020, 1, 1), new Category("1", "Food", null), BigDecimal.valueOf(100.5), getUser()),
            new Expense("2", LocalDate.of(2021, 12, 15), new Category("2", "Healthcare", null), BigDecimal.valueOf(20.22), getUser())
        )
    }

    private User getUser() {
        return new User("1", "anne", "anne@gmail.com", "anne123", List.of(Role.USER))
    }

    private List<Category> getCategories() {
        return List.of(
            new Category("1", "Food", List.of(getUser())),
            new Category("2", "Healthcare", List.of(getUser())),
        )
    }

    private CustomUserDetails getUserDetails() {
        return new CustomUserDetails("1", "abc", List.of(Role.USER))
    }
}
