package co.istad.restfulemployeeapi.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEmployeeRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Names must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @Positive(message = "Salary must be greater than 0")
        @NotNull(message = "Salary is required")
        BigDecimal salary,


        @NotNull(message = "Hire date is required")
        LocalDate hireDate

) {
    public CreateEmployeeRequest {
        name = name == null ? null : name.strip();
        email = email == null ? null : email.strip();
    }
}
