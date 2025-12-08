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

    @Autowired
    private EmployeeSearchRepository searchRepository;

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

    public Employee update(String oldId, Employee newData) {
        Optional<Employee> optional = repo.findById(oldId);
        if (!optional.isPresent()) return null;

        repo.deleteById(oldId);

        Employee newEmployee = new Employee();
        newEmployee.setId(newData.getId());
        newEmployee.setFullName(newData.getFullName());
        newEmployee.setEmail(newData.getEmail());
        newEmployee.setPhone(newData.getPhone());
        newEmployee.setPosition(newData.getPosition());
        newEmployee.setSalary(newData.getSalary());
        newEmployee.setAvatarPath(newData.getAvatarPath());

        return repo.save(newEmployee);
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
