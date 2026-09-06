package com.HR_Managment_System.demo.repository;

import com.HR_Managment_System.demo.entity.Employee;
import com.HR_Managment_System.demo.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmail(String email);

    long countByDepartmentId(String departmentId);

    java.util.List<Employee> findByDepartmentId(String departmentId);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    @Query("{ $and: [ " +
           "{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { $expr: { $eq: [?0, ''] } } ] }, " +
           "{ $or: [ { 'departmentId': ?1 }, { $expr: { $eq: [?1, ''] } } ] }, " +
           "{ $or: [ { 'status': ?2 }, { $expr: { $eq: [?2, null] } } ] }, " +
           "{ $or: [ { 'userRole': { $regex: ?3, $options: 'i' } }, { $expr: { $eq: [?3, ''] } } ] } " +
           "] }")
    Page<Employee> findByFilters(String name, String departmentId, EmployeeStatus status, String userRole, Pageable pageable);
}
