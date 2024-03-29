package com.financetracker.app.income

import com.financetracker.app.category.Category
import com.financetracker.app.category.CategoryRepository
import com.financetracker.app.config.IntegrationTestConfig
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

import java.time.LocalDate


import static com.financetracker.app.income.IncomeCorrectData.correctAmount
import static com.financetracker.app.income.IncomeCorrectData.correctDate
import static com.financetracker.app.income.IncomeCorrectData.correctDescription
import static com.financetracker.app.income.IncomeCorrectData.correctId
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
class IncomeIntegrationTest extends IntegrationTestConfig {

    @Autowired
    MockMvc mvc

    @Autowired
    IncomeRepository incomeRepository

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
        incomeRepository.saveAll(getIncomes())
    }

    def cleanup() {
        incomeRepository.deleteAll()
    }

    def "should return 200 (OK) and paged incomes"() {
        given:
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(get("/users/me/incomes")
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.totalPages').value(1))
        result.andExpect(jsonPath('$.currentPage').value(0))
        result.andExpect(jsonPath('$.incomes[*]', hasSize(2)))
        result.andExpect(jsonPath('$.incomes[0].id').value("1"))
        result.andExpect(jsonPath('$.incomes[0].date').value("2020-01-01"))
        result.andExpect(jsonPath('$.incomes[0].amount').value(100.5))
        result.andExpect(jsonPath('$.incomes[0].description').value("income1"))
        result.andExpect(jsonPath('$.incomes[1].id').value("2"))
        result.andExpect(jsonPath('$.incomes[1].date').value("2021-12-15"))
        result.andExpect(jsonPath('$.incomes[1].amount').value(20.22))
        result.andExpect(jsonPath('$.incomes[1].description').value("income2"))
    }

    def "should return 200 (OK) and income"() {
        given:
        String incomeId = "1"
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(get("/users/me/incomes/$incomeId")
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.id').value("1"))
        result.andExpect(jsonPath('$.date').value("2020-01-01"))
        result.andExpect(jsonPath('$.amount').value(100.5))
        result.andExpect(jsonPath('$.description').value("income1"))
    }

    def "should return 404 (NOT FOUND) when requesting income was not found"() {
        given:
        String incomeId = "3"
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(get("/users/me/incomes/$incomeId")
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isNotFound())
    }

    def "should return 200 (OK) and paged, sorted incomes"() {
        given:
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(get("/users/me/incomes")
                .param(minDate, "2019-01-01")
                .param(maxDate, "2022-01-01")
                .param(minAmount, "20")
                .param(maxAmount, "150")
                .param("keyword", "income")
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.totalPages').value(1))
        result.andExpect(jsonPath('$.currentPage').value(0))
        result.andExpect(jsonPath('$.incomes[*]', hasSize(2)))
        result.andExpect(jsonPath('$.incomes[0].id').value("1"))
        result.andExpect(jsonPath('$.incomes[0].date').value("2020-01-01"))
        result.andExpect(jsonPath('$.incomes[0].amount').value(100.5))
        result.andExpect(jsonPath('$.incomes[0].description').value("income1"))
        result.andExpect(jsonPath('$.incomes[1].id').value("2"))
        result.andExpect(jsonPath('$.incomes[1].date').value("2021-12-15"))
        result.andExpect(jsonPath('$.incomes[1].amount').value(20.22))
        result.andExpect(jsonPath('$.incomes[1].description').value("income2"))

        where:
        minDate << ["minDate", "min-date"]
        maxDate << ["maxDate", "max-date"]
        minAmount << ["minAmount", "min-amount"]
        maxAmount << ["maxAmount", "max-amount"]
    }

    def "should return 201 (CREATED) when income is created"() {
        given:
        LinkedHashMap<String, Serializable> incomeToCreate = [
            date       : correctDate,
            amount     : correctAmount,
            description: correctDescription
        ]
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(post("/users/me/incomes")
                .contentType(APPLICATION_JSON)
                .content(toJson(incomeToCreate))
                .accept(APPLICATION_JSON)
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isCreated())
    }

    def "should return 422 (UNPROCESSABLE ENTITY) when #scenario of income to create fail validation"() {
        given:
        LinkedHashMap<String, Serializable> incomeToCreate = [
            date       : date,
            amount     : amount,
            description: description
        ]

        when:
        def result = mvc
            .perform(post("/users/me/incomes")
                .contentType(APPLICATION_JSON)
                .content(toJson(incomeToCreate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        result.andExpect(status().isUnprocessableEntity())

        where:
        date                  | amount        | description                                                                                             | scenario
        null                  | correctAmount | correctDescription                                                                                      | "null date"
        " "                   | correctAmount | correctDescription                                                                                      | "whitespace date"
        "12-14-2022"          | correctAmount | correctDescription                                                                                      | "mm-dd-yyyy date"
        "22-11-2022"          | correctAmount | correctDescription                                                                                      | "dd-mm-yyyy date"
        "2022-12-14 13:58:00" | correctAmount | correctDescription                                                                                      | "yyyy-mm-dd HH:mm:ss date"
        correctDate           | null          | correctDescription                                                                                      | "null amount"
        correctDate           | -1            | correctDescription                                                                                      | "negative amount"
        correctDate           | 0.0           | correctDescription                                                                                      | "0 amount"
        correctDate           | correctAmount | "bbgn4X5phRkUrtYSACFDCSKdlJgwPho27tQwLAFBwde76RNoEsG7dOwF7JOauPlvQHJhopwAJVruOHCDoKGkXBcYHOWa2eRoLt7se" | "too long (>100 characters) description"
    }

    def "should return 200 (OK) when income is updated"() {
        given:
        String incomeId = correctId
        LinkedHashMap<String, Serializable> incomeToUpdate = [
            id         : correctId,
            date       : correctDate,
            amount     : correctAmount,
            description: correctDescription
        ]
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(put("/users/me/incomes/$incomeId")
                .contentType(APPLICATION_JSON)
                .content(toJson(incomeToUpdate))
                .accept(APPLICATION_JSON)
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isOk())
    }

    def "should return 409 (CONFLICT) when ID income to update doesn't match ID in the URL"() {
        given:
        String incomeId = "2"
        LinkedHashMap<String, Serializable> incomeToUpdate = [
            id         : correctId,
            date       : correctDate,
            amount     : correctAmount,
            description: correctDescription
        ]

        when:
        def result = mvc
            .perform(put("/users/me/incomes/$incomeId")
                .contentType(APPLICATION_JSON)
                .content(toJson(incomeToUpdate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        result.andExpect(status().isConflict())
    }

    def "should return 422 (UNPROCESSABLE ENTITY) when #scenario of income to update fail validation"() {
        given:
        String incomeId = "2"
        LinkedHashMap<String, Serializable> incomeToUpdate = [
            id         : id,
            date       : date,
            amount     : amount,
            description: description
        ]

        when:
        def result = mvc
            .perform(put("/users/me/incomes/$incomeId")
                .contentType(APPLICATION_JSON)
                .content(toJson(incomeToUpdate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        result.andExpect(status().isUnprocessableEntity())

        where:
        id        | date                  | amount        | description                                                                                             | scenario
        null      | correctDate           | correctAmount | correctDescription                                                                                      | "null id"
        " "       | correctDate           | correctAmount | correctDescription                                                                                      | "whitespace id"
        correctId | null                  | correctAmount | correctDescription                                                                                      | "null date"
        correctId | " "                   | correctAmount | correctDescription                                                                                      | "whitespace date"
        correctId | "12-14-2022"          | correctAmount | correctDescription                                                                                      | "mm-dd-yyyy date"
        correctId | "22-11-2022"          | correctAmount | correctDescription                                                                                      | "dd-mm-yyyy date"
        correctId | "2022-12-14 13:58:00" | correctAmount | correctDescription                                                                                      | "yyyy-mm-dd HH:mm:ss date"
        correctId | correctDate           | null          | correctDescription                                                                                      | "null amount"
        correctId | correctDate           | -1            | correctDescription                                                                                      | "negative amount"
        correctId | correctDate           | 0.0           | correctDescription                                                                                      | "0 amount"
        correctId | correctDate           | correctAmount | "bbgn4X5phRkUrtYSACFDCSKdlJgwPho27tQwLAFBwde76RNoEsG7dOwF7JOauPlvQHJhopwAJVruOHCDoKGkXBcYHOWa2eRoLt7se" | "too long (>100 characters) description"
    }

    def "should return 204 (NO CONTENT) when income is deleted"() {
        given:
        String incomeId = "1"
        CustomUserDetails userDetails = getUserDetails()

        when:
        def result = mvc
            .perform(delete("/users/me/incomes/$incomeId")
                .with(user(userDetails)))
            .andDo(print())

        then:
        result.andExpect(status().isNoContent())
    }

    private List<Income> getIncomes() {
        return List.of(
            new Income("1", LocalDate.of(2020, 1, 1), 100.5, "income1", getUser()),
            new Income("2", LocalDate.of(2021, 12, 15), 20.22, "income2", getUser())
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
