package com.HR_Managment_System.demo.repository;

import com.HR_Managment_System.demo.entity.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends MongoRepository<Department, String> {

    Optional<Department> findByDepartmentId(String departmentId);

    boolean existsByName(String name);

    boolean existsByDepartmentId(String departmentId);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Department> searchByName(String name);
}
