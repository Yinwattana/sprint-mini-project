package co.istad.restfulemployeeapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEmployeeRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Names must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @NotNull(message = "Salary is required")
        @Positive(message = "Salary must be greater than 0")
        BigDecimal salary,

        @NotNull(message = "Hire date is required")
        LocalDate hireDate,

        @NotNull(message = "Active status is required")
        Boolean isActive
) {
    public UpdateEmployeeRequest {
        name = name == null ? null : name.strip();
        email = email == null ? null : email.strip();
    }
}
