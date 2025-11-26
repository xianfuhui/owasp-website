package com.example.hr.controller;

import com.example.hr.entity.Employee;
import com.example.hr.service.EmployeeService;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", service.getAll());
        return "employees/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employees/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Employee emp) {
        service.create(emp);
        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable String id, Model model) {
        Employee emp = service.getById(id);
        model.addAttribute("employee", emp);
        return "employees/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable String id, @ModelAttribute Employee emp) {
        service.update(id, emp);
        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id) {
        service.delete(id);
        return "redirect:/employees";
    }


    //Search
    @GetMapping("/search")
    public String showLogin() {
        return "employees/search"; 
    }

    // Dễ bị Injection
    @GetMapping("/search-vulnerable")
    public String searchVuln(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Employee> list = service.searchVulnerable(q);
        model.addAttribute("employees", list);
        return "employees/search";
    }

    // Đã fix
    @GetMapping("/search-safe")
    public String searchSafe(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Employee> list = service.searchSafe(q);
        model.addAttribute("employees", list);
        return "employees/search";
    }
}
