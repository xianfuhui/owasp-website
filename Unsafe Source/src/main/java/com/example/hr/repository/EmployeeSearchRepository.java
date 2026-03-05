package com.example.hr.repository;

import com.example.hr.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

@Repository
public class EmployeeSearchRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Employee> searchVulnerable(String keyword) {
        if (keyword == null) keyword = "";
        // **VULNERABLE**: nối chuỗi JSON trực tiếp (không escape)
        String raw = "{ \"fullName\": { \"$regex\": " + quoteForJson(keyword) + " } }";
        BasicQuery q = new BasicQuery(raw);
        return mongoTemplate.find(q, Employee.class);
    }

    // helper: chèn nguyên chuỗi vào JSON (để demo injection)
    private String quoteForJson(String s) {
        return "\"" + s + "\"";
    }

    // SECURE — phiên bản đã fix
    public List<Employee> searchSafe(String keyword) {
        String safe = Pattern.quote(keyword);

        Query query = new Query(
            Criteria.where("fullName").regex(safe, "i")
        );

        return mongoTemplate.find(query, Employee.class);
    }
}
