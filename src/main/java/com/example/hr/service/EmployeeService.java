package com.example.hr.service;

import com.example.hr.entity.Employee;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.EmployeeSearchRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
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

    public Employee update(String id, Employee newData) {
        Optional<Employee> optional = repo.findById(id);
        if (!optional.isPresent()) return null;

        Employee emp = optional.get();
        emp.setFullName(newData.getFullName());
        emp.setEmail(newData.getEmail());
        emp.setPhone(newData.getPhone());
        emp.setPosition(newData.getPosition());
        emp.setSalary(newData.getSalary());

        return repo.save(emp);
    }

    public boolean delete(String id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }


    //Search
    @Autowired
    private EmployeeSearchRepository searchRepository;

    public List<Employee> searchVulnerable(String keyword) {
        return searchRepository.searchVulnerable(keyword);
    }

    public List<Employee> searchSafe(String keyword) {
        return searchRepository.searchSafe(keyword);
    }
}
