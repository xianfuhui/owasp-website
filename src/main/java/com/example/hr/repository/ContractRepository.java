package com.example.hr.repository;

import com.example.hr.entity.Contract;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContractRepository extends MongoRepository<Contract, String> {
    List<Contract> findByEmployeeId(String employeeId);
}
