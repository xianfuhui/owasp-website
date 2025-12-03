package com.example.hr.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contracts")
public class Contract {

    @Id
    private String id;

    private String employeeId;      // ai sở hữu hợp đồng
    private String fileName;        // tên file gốc
    private String filePath;        // đường dẫn file thực tế trên server

    public Contract() {}

    public Contract(String id, String employeeId, String fileName, String filePath) {
        this.id = id;
        this.employeeId = employeeId;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
