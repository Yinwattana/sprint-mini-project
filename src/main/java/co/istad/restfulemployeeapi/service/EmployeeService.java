package co.istad.restfulemployeeapi.service;

import co.istad.restfulemployeeapi.dto.CreateEmployeeRequest;
import co.istad.restfulemployeeapi.dto.EmployeeResponse;
import co.istad.restfulemployeeapi.dto.UpdateEmployeeRequest;

import java.util.List;

public interface EmployeeService {

    // CRUD: Create, Read, Update, Delete
    List<EmployeeResponse> getAll();
    EmployeeResponse getEmployeeById(Integer id);

    List<EmployeeResponse> searchEmployeesByName(String name);

    EmployeeResponse createEmployee(CreateEmployeeRequest employeeRequest);

    EmployeeResponse updateEmployeeById(Integer id, UpdateEmployeeRequest updateEmployeeRequest);

    void deleteEmployeeById(Integer id);

}
