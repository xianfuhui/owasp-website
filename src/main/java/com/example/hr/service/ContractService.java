package com.example.hr.service;

import com.example.hr.entity.Contract;
import com.example.hr.entity.Account;
import com.example.hr.repository.ContractRepository;
import com.example.hr.util.AccessControlUtil;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class ContractService {

    private final ContractRepository repo;

    public ContractService(ContractRepository repo) {
        this.repo = repo;
    }

    public List<Contract> getContractsByEmployee(Account acc, String employeeId) {
        //-------------
        //A01 – Broken Access Control 
        // -------------
        // AccessControlUtil.checkViewOrDownload(acc, employeeId);
        // -------------
        return repo.findByEmployeeId(employeeId);
    }

    public Contract uploadContract(Account acc, String employeeId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new RuntimeException("File rỗng");

        String filename = file.getOriginalFilename();

        //-------------
        // A08 – Software & Data Integrity Failure
        //-------------
        // if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
        //     throw new RuntimeException("Chỉ cho phép file PDF");
        // }
        //-------------

        String dir = "uploads/contracts/" + employeeId;
        String path = dir + "/" + filename;

        Files.createDirectories(Paths.get(dir));
        Files.copy(file.getInputStream(), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);

        Contract c = new Contract();
        c.setEmployeeId(employeeId);
        c.setFileName(filename);
        c.setFilePath(path);

        return repo.save(c);
    }

    public void deleteContract(Account acc, String id) {
        Contract c = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract không tồn tại"));

        AccessControlUtil.checkViewOrDownload(acc, c.getEmployeeId());

        File f = new File(c.getFilePath());
        if (f.exists()) f.delete();

        repo.delete(c);
    }

    public FileSystemResource getFileResource(Account acc, String id) {
        Contract c = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract không tồn tại"));

        //-------------
        //A01 – Broken Access Control 
        // -------------
        // AccessControlUtil.checkViewOrDownload(acc, c.getEmployeeId());
        // -------------


        File file = new File(c.getFilePath());
        if (!file.exists()) throw new RuntimeException("File không tồn tại");

        return new FileSystemResource(file);
    }

    public Contract getContractById(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract không tồn tại"));
    }
}
