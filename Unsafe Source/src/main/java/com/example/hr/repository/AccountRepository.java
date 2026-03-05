package com.example.hr.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.hr.entity.Account;

public interface AccountRepository extends MongoRepository<Account, String> {
    Account findByUsername(String username);
    Account findByEmployeeId(String employeeId);
    List<Account> findAllByEmployeeId(String employeeId);
}
