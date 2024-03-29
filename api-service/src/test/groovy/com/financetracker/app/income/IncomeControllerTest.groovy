package com.financetracker.app.income

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
import java.time.LocalDate
import static IncomeCorrectData.*
import static groovy.json.JsonOutput.toJson
import static org.hamcrest.Matchers.hasSize
import static org.mapstruct.factory.Mappers.getMapper
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = IncomeController.class)
@WithMockUser
class IncomeControllerTest extends MvcTestsConfig {

    @Autowired
    MockMvc mvc

    @SpringBean
    IncomeService incomeService = Mock()

    @SpringBean
    IncomeMapper incomeMapper = Spy(getMapper(IncomeMapper))

    @SpringBean
    AuthenticationService authenticationService = Mock()

    def "should return 200 (OK) and paged incomes"() {
        given:
        String userId = "1"

        when:
        def result = mvc
            .perform(get("/users/me/incomes"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * incomeService.getUserIncomes(userId, _ as PageRequest) >> getPagedIncomes()
        1 * incomeMapper.toPagedDTO(_ as Page<Income>)

        and:
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
        String userId = "1"

        when:
        def result = mvc
            .perform(get("/users/me/incomes/$incomeId"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * incomeService.getIncome(incomeId, userId) >> getIncome()
        1 * incomeMapper.toDTO(_ as Income)

        and:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.id').value("1"))
        result.andExpect(jsonPath('$.date').value("2020-01-01"))
        result.andExpect(jsonPath('$.amount').value(100.5))
        result.andExpect(jsonPath('$.description').value("income1"))
    }

    def "should return 404 (NOT FOUND) when requesting income was not found"() {
        given:
        String incomeId = "1"
        String userId = "1"

        when:
        def result = mvc
            .perform(get("/users/me/incomes/$incomeId"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * incomeService.getIncome(incomeId, userId) >> { throw new DocumentNotFoundException() }
        0 * incomeMapper.toDTO(_ as Income)

        and:
        result.andExpect(status().isNotFound())
    }

    def "should return 200 (OK) and paged, sorted incomes"() {
        given:
        String userId = "1"

        when:
        def result = mvc
            .perform(get("/users/me/incomes")
                .param(minDate, "2019-01-01")
                .param(maxDate, "2022-01-01")
                .param(minAmount, "20")
                .param(maxAmount, "150")
                .param("keyword", "income"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * incomeService.getUserIncomes(userId, _ as PageRequest) >> getPagedIncomes()
        1 * incomeMapper.toPagedDTO(_ as Page<Income>)

        and:
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

        String userId = "1"

        when:
        def result = mvc
            .perform(post("/users/me/incomes")
                .contentType(APPLICATION_JSON)
                .content(toJson(incomeToCreate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * incomeService.createIncome(userId, _ as AddIncomeDTO)

        and:
        result.andExpect(status().isCreated())
    }

    def "should return 422 (UNPROCESSABLE ENTITY) when #scenario of income to create fail validation"() {
        given:
        LinkedHashMap<String, Serializable> incomeToCreate = [
            date       : date,
            amount     : amount,
            description: description
        ]

        String userId = "1"

        when:
        def result = mvc
            .perform(post("/users/me/incomes")
                .contentType(APPLICATION_JSON)
                .content(toJson(incomeToCreate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        0 * incomeService.createIncome(userId, _ as AddIncomeDTO)

        and:
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

        String userId = "1"

        when:
        def result = mvc
            .perform(put("/users/me/incomes/$incomeId")
                .contentType(APPLICATION_JSON)
                .content(toJson(incomeToUpdate))
                .accept(APPLICATION_JSON))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * incomeService.updateIncome(userId, _ as IncomeDTO)

        and:
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
        0 * incomeService.updateIncome(_ as String, _ as IncomeDTO)

        and:
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
        String userId = "1"

        when:
        def result = mvc
            .perform(delete("/users/me/incomes/$incomeId"))
            .andDo(print())

        then:
        1 * authenticationService.getUserId(_ as Authentication) >> userId
        1 * incomeService.deleteIncome(incomeId, userId)

        and:
        result.andExpect(status().isNoContent())
    }

    private Page<Income> getPagedIncomes() {
        List<Income> incomes = List.of(
            new Income("1", LocalDate.of(2020, 1, 1), 100.5, "income1", null),
            new Income("2", LocalDate.of(2021, 12, 15), 20.22, "income2", null)
        )

        return new PageImpl<Income>(incomes, PageRequest.of(0, 5), 5)
    }

    private Income getIncome() {
        return new Income("1", LocalDate.of(2020, 1, 1), 100.5, "income1", null)
    }
}
