package co.istad.restfulemployeeapi.service.impl;

import co.istad.restfulemployeeapi.domain.Employee;
import co.istad.restfulemployeeapi.dto.CreateEmployeeRequest;
import co.istad.restfulemployeeapi.dto.EmployeeResponse;
import co.istad.restfulemployeeapi.dto.UpdateEmployeeRequest;
import co.istad.restfulemployeeapi.mapper.EmployeeMapper;
import co.istad.restfulemployeeapi.repository.EmployeeRepository;
import co.istad.restfulemployeeapi.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper employeeMapper;
    private int nextId = 3;

    // Keep reads and writes to the shared in-memory list atomic.
    @Override
    public synchronized List<EmployeeResponse> getAll() {
        return repository.getEmployees().stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Override
    public synchronized EmployeeResponse getEmployeeById(Integer id) {
        return employeeMapper.toResponse(findEmployeeById(id));
    }

    @Override
    public synchronized List<EmployeeResponse> searchEmployeesByName(String name) {
        String keyword = name.strip().toLowerCase(Locale.ROOT);
        return repository.getEmployees().stream()
                .filter(employee -> employee.getName().toLowerCase(Locale.ROOT).contains(keyword))
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Override
    public synchronized EmployeeResponse createEmployee(CreateEmployeeRequest employeeRequest) {
        validateUniqueEmail(employeeRequest.email(), null);

        Employee employee = employeeMapper.fromCreateEmployeeRequest(employeeRequest);
        employee.setId(nextId++);
        employee.setIsActive(Boolean.TRUE);
        repository.getEmployees().add(employee);

        return employeeMapper.toResponse(employee);
    }

    @Override
    public synchronized EmployeeResponse updateEmployeeById(Integer id, UpdateEmployeeRequest request) {
        Employee employee = findEmployeeById(id);
        // The current employee can keep their own email address.
        validateUniqueEmail(request.email(), id);
        employeeMapper.updateEmployeeFromRequest(request, employee);
        return employeeMapper.toResponse(employee);
    }

    @Override
    public synchronized void deleteEmployeeById(Integer id) {
        Employee employee = findEmployeeById(id);
        repository.getEmployees().remove(employee);
    }

    private Employee findEmployeeById(Integer id) {
        return repository.getEmployees().stream()
                .filter(employee -> employee.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee with id: %d not found".formatted(id)
                ));
    }

    private void validateUniqueEmail(String email, Integer excludedId) {
        boolean exists = repository.getEmployees().stream()
                .anyMatch(employee -> !employee.getId().equals(excludedId)
                        && employee.getEmail().equalsIgnoreCase(email));
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
    }
}
