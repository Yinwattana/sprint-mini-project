package co.istad.restfulemployeeapi.controller;

import co.istad.restfulemployeeapi.dto.CreateEmployeeRequest;
import co.istad.restfulemployeeapi.dto.EmployeeResponse;
import co.istad.restfulemployeeapi.dto.UpdateEmployeeRequest;
import co.istad.restfulemployeeapi.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping
    public List<EmployeeResponse> getAllEmployees() {
        return service.getAll();
    }

    @GetMapping("/search")
    public List<EmployeeResponse> searchEmployeesByName(
            @RequestParam(name = "name")
            @NotBlank(message = "Search name is required")
            @Size(max = 100, message = "Search name must not exceed 100 characters")
            String name) {
        return service.searchEmployeesByName(name);
    }

    @GetMapping("/{id}")
    public EmployeeResponse getEmployeeById(@PathVariable(name = "id") Integer id) {
        return service.getEmployeeById(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest createRequest) {
        EmployeeResponse employee = service.createEmployee(createRequest);
        return ResponseEntity.created(URI.create("/api/v1/employees/" + employee.id()))
                .body(employee);
    }

    @PutMapping("/{id}")
    public EmployeeResponse updateEmployeeById(
            @PathVariable(name = "id") Integer id,
            @Valid @RequestBody UpdateEmployeeRequest updateRequest) {
        return service.updateEmployeeById(id, updateRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployeeById(@PathVariable(name = "id") Integer id) {
        service.deleteEmployeeById(id);
        return ResponseEntity.noContent().build();
    }
}
