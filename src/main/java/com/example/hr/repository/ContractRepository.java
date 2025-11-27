package com.example.hr.repository;

import com.example.hr.entity.ContractFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContractRepository extends MongoRepository<ContractFile, String> {
}
