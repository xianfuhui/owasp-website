package com.example.hr.service;

import com.example.hr.entity.Contract;
import com.example.hr.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService {

    private final ContractRepository repo;

    public ContractService(ContractRepository repo) {
        this.repo = repo;
    }

    public List<Contract> getByEmployee(String employeeId) {
        return repo.findByEmployeeId(employeeId);
    }

    public Contract save(Contract file) {
        return repo.save(file);
    }

    public Contract getById(String id) {
        return repo.findById(id).orElse(null);
    }
}
