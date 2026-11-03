package co.istad.restfulemployeeapi.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private Integer id;

    private String name;

    private String email;

    private BigDecimal salary;

    private LocalDate hireDate;

    private Boolean isActive;

}
