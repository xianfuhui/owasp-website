package com.example.hr.service;

import com.example.hr.entity.Account;
import com.example.hr.entity.Employee;
import com.example.hr.repository.AccountRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.EmployeeSearchRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository repo;
    private final AccountRepository accountRepo;

    @Autowired
    private EmployeeSearchRepository searchRepository;

    public EmployeeService(EmployeeRepository repo, AccountRepository accountRepo) {
        this.repo = repo;
        this.accountRepo = accountRepo;
    }

    public List<Employee> getAll() {
        return repo.findAll();
    }

    public Employee getById(String id) {
        return repo.findById(id).orElse(null);
    }

    public Employee create(Employee emp) {
        return repo.save(emp);
    }

    public Employee update(String id, Employee newData, String role) {
        Optional<Employee> optional = repo.findById(id);
        if (!optional.isPresent()) return null;

        Employee emp = optional.get();

        emp.setFullName(newData.getFullName());
        emp.setEmail(newData.getEmail());
        emp.setPhone(newData.getPhone());
        emp.setPosition(newData.getPosition());
        emp.setSalary(newData.getSalary());

        Employee updated = repo.save(emp);

        if (emp.getAccountId() != null) {

            // Tìm account bằng employeeId
            Account acc = accountRepo.findByEmployeeId(emp.getId());

            if (acc != null) {
                acc.setRole(role);
                accountRepo.save(acc);
            }
        }

        return updated;
    }

    public boolean delete(String id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    public List<Employee> searchVulnerable(String keyword) {
        return searchRepository.searchVulnerable(keyword);
    }

    public List<Employee> searchSafe(String keyword) {
        return searchRepository.searchSafe(keyword);
    }
}
