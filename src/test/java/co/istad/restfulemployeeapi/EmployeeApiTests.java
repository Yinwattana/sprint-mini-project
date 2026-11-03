package co.istad.restfulemployeeapi;

import co.istad.restfulemployeeapi.domain.Employee;
import co.istad.restfulemployeeapi.repository.EmployeeRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApiTests {

    private static final String URL = "/api/v1/employees";
    private static final String CREATE_BODY = """
            {
              "name": "Steve Rogers",
              "email": "steve@example.com",
              "salary": 1200.00,
              "hireDate": "2026-01-15"
            }
            """;
    private static final String UPDATE_BODY = """
            {
              "name": "Bruce Banner Updated",
              "email": "bruce@example.com",
              "salary": 1750.50,
              "hireDate": "2026-02-20",
              "isActive": false
            }
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EmployeeRepository repository;

    @BeforeEach
    void resetEmployees() {
        repository.getEmployees().clear();
        repository.getEmployees().add(new Employee(1, "Tony Stark", "ironman@gmail.com",
                new BigDecimal("1500.00"), LocalDate.of(2025, 10, 30), true));
        repository.getEmployees().add(new Employee(2, "Bruce Banner", "hulk@gmail.com",
                new BigDecimal("800.00"), LocalDate.of(2026, 10, 30), true));
    }

    @Test
    void listsSeededEmployees() throws Exception {
        mvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", hasItems("Tony Stark", "Bruce Banner")));
    }

    @Test
    void completesCreateReadUpdateAndDeleteLifecycle() throws Exception {
        String created = mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Steve Rogers"))
                .andExpect(jsonPath("$.email").value("steve@example.com"))
                .andExpect(jsonPath("$.salary").value(1200.0))
                .andExpect(jsonPath("$.hireDate").value("2026-01-15"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");

        mvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mvc.perform(put(URL + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Bruce Banner Updated"))
                .andExpect(jsonPath("$.email").value("bruce@example.com"))
                .andExpect(jsonPath("$.salary").value(1750.5))
                .andExpect(jsonPath("$.hireDate").value("2026-02-20"))
                .andExpect(jsonPath("$.isActive").value(false));

        mvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bruce Banner Updated"))
                .andExpect(jsonPath("$.email").value("bruce@example.com"))
                .andExpect(jsonPath("$.salary").value(1750.5))
                .andExpect(jsonPath("$.hireDate").value("2026-02-20"))
                .andExpect(jsonPath("$.isActive").value(false));

        mvc.perform(delete(URL + "/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(get(URL + "/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(delete(URL + "/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(get(URL)).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void returnsLocationOfNewEmployee() throws Exception {
        var result = mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isCreated()).andReturn();
        Integer id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        mvc.perform(get(result.getResponse().getHeader("Location")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void allowsEmployeeToKeepTheirOwnEmailAndAcceptsFalseActiveStatus() throws Exception {
        mvc.perform(put(URL + "/2").contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY.replace("bruce@example.com", "  HULK@gmail.com  ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("HULK@gmail.com"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void rejectsDuplicateEmailOnCreateIgnoringCaseAndWhitespace() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY.replace("steve@example.com", "  IRONMAN@gmail.com  ")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("Email already exists"));
        mvc.perform(get(URL)).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void rejectsAnotherEmployeesEmailOnUpdateWithoutChangingStoredData() throws Exception {
        mvc.perform(put(URL + "/2").contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY.replace("bruce@example.com", "IRONMAN@gmail.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
        mvc.perform(get(URL + "/2"))
                .andExpect(jsonPath("$.name").value("Bruce Banner"))
                .andExpect(jsonPath("$.email").value("hulk@gmail.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void returns404ForMissingEmployee(String method) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Employee with id: 99999 not found"));
    }

    @ParameterizedTest
    @MethodSource("invalidUpdates")
    void validatesEveryUpdateFieldAndLeavesEmployeeUnchanged(String body, String field) throws Exception {
        mvc.perform(put(URL + "/2").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.errors[*].field", hasItem(field)));
        mvc.perform(get(URL + "/2"))
                .andExpect(jsonPath("$.name").value("Bruce Banner"))
                .andExpect(jsonPath("$.email").value("hulk@gmail.com"))
                .andExpect(jsonPath("$.salary").value(800.0))
                .andExpect(jsonPath("$.hireDate").value("2026-10-30"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    static Stream<Arguments> invalidUpdates() {
        return Stream.of(
                arguments(UPDATE_BODY.replace("Bruce Banner Updated", "   "), "name"),
                arguments(UPDATE_BODY.replace("Bruce Banner Updated", " A "), "name"),
                arguments(UPDATE_BODY.replace("Bruce Banner Updated", "A".repeat(101)), "name"),
                arguments(UPDATE_BODY.replace("\"Bruce Banner Updated\"", "null"), "name"),
                arguments(UPDATE_BODY.replace("bruce@example.com", ""), "email"),
                arguments(UPDATE_BODY.replace("bruce@example.com", "invalid-email"), "email"),
                arguments(UPDATE_BODY.replace("\"bruce@example.com\"", "null"), "email"),
                arguments(UPDATE_BODY.replace("1750.50", "0"), "salary"),
                arguments(UPDATE_BODY.replace("1750.50", "-1"), "salary"),
                arguments(UPDATE_BODY.replace("1750.50", "null"), "salary"),
                arguments(UPDATE_BODY.replace("\"2026-02-20\"", "null"), "hireDate"),
                arguments(UPDATE_BODY.replace("false", "null"), "isActive")
        );
    }

    @Test
    void requiresAllFieldsForPut() throws Exception {
        mvc.perform(put(URL + "/2").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItems("name", "email", "salary", "hireDate", "isActive")));
    }

    @Test
    void validatesCreateFields() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItems("name", "email", "salary", "hireDate")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"tOnY", "ony Sta", "  TONY  "})
    void searchesByPartialNameIgnoringCaseAndOuterWhitespace(String name) throws Exception {
        mvc.perform(get(URL + "/search").param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Tony Stark"));
    }

    @Test
    void searchReturnsAllMatches() throws Exception {
        mvc.perform(get(URL + "/search").param("name", "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void searchUsesUpdatedNamesAndExcludesDeletedEmployees() throws Exception {
        mvc.perform(put(URL + "/2").contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY))
                .andExpect(status().isOk());
        mvc.perform(get(URL + "/search").param("name", "updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
        mvc.perform(delete(URL + "/2")).andExpect(status().isNoContent());
        mvc.perform(get(URL + "/search").param("name", "updated"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void searchReturnsEmptyArrayWhenNoNameMatches() throws Exception {
        mvc.perform(get(URL + "/search").param("name", "no-such-employee"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankSearchName(String name) throws Exception {
        mvc.perform(get(URL + "/search").param("name", name))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("name")));
    }

    @Test
    void rejectsMissingOrOverlongSearchName() throws Exception {
        mvc.perform(get(URL + "/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("name")));
        mvc.perform(get(URL + "/search").param("name", "A".repeat(101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("name")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-date", "2026-02-30"})
    void rejectsInvalidHireDate(String date) throws Exception {
        mvc.perform(put(URL + "/2").contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY.replace("2026-02-20", date)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void rejectsMalformedJsonAndNonNumericId() throws Exception {
        mvc.perform(put(URL + "/2").contentType(MediaType.APPLICATION_JSON).content("{invalid"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        mvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("id")));
    }
}
