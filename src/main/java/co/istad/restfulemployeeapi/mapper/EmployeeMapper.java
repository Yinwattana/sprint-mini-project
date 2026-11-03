package co.istad.restfulemployeeapi.mapper;

import co.istad.restfulemployeeapi.domain.Employee;
import co.istad.restfulemployeeapi.dto.CreateEmployeeRequest;
import co.istad.restfulemployeeapi.dto.EmployeeResponse;
import co.istad.restfulemployeeapi.dto.UpdateEmployeeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmployeeMapper {

    // Map from Domain to DTO
    EmployeeResponse toResponse(Employee employee);

    // Map from DTO to Domain
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Employee fromCreateEmployeeRequest(CreateEmployeeRequest createEmployeeRequest);

    // Replace editable fields while preserving the employee's ID.
    @Mapping(target = "id", ignore = true)
    void updateEmployeeFromRequest(UpdateEmployeeRequest request, @MappingTarget Employee employee);

}
