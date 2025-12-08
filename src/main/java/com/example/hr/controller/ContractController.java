package com.example.hr.controller;

import com.example.hr.entity.Account;
import com.example.hr.entity.Contract;
import com.example.hr.service.AccountService;
import com.example.hr.service.ContractService;
import com.example.hr.util.SecurityUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/contracts")
public class ContractController {

        private static final Logger logger = LoggerFactory.getLogger(ContractController.class);

        private final ContractService contractService;
        private final AccountService accountService;

        public ContractController(ContractService contractService, AccountService accountService) {
                this.contractService = contractService;
                this.accountService = accountService;
        }

        // ============================================
        // USER xem hợp đồng của chính họ
        // ============================================
        @GetMapping("/")
        public String myContracts(@AuthenticationPrincipal UserDetails user, Model model) {

                logger.info("[ACTION=VIEW_MY_CONTRACTS] user={} IP=?", user.getUsername());

                Account currentAccount = accountService.getByUsername(user.getUsername());
                List<Contract> files = contractService.getContractsByEmployee(currentAccount,
                                currentAccount.getEmployeeId());

                logger.info("[RESULT=FOUND_CONTRACTS] user={} employeeId={} total={}",
                                user.getUsername(), currentAccount.getEmployeeId(), files.size());

                model.addAttribute("contracts", files);
                model.addAttribute("employeeId", currentAccount.getEmployeeId());
                model.addAttribute("currentAccount", currentAccount);

                return "contracts/list";
        }

        // ============================================
        // ADMIN/HR xem hợp đồng của nhân viên
        // ============================================
        @GetMapping("/list/{employeeId}")
        public String listByEmployee(@PathVariable String employeeId, Model model) {

                String username = SecurityUtil.getCurrentUsername();
                logger.info("[ACTION=ADMIN_VIEW] admin={} targetEmployee={}", username, employeeId);

                Account currentAccount = accountService.getByUsername(username);
                List<Contract> files = contractService.getContractsByEmployee(currentAccount, employeeId);

                logger.info("[RESULT=FOUND_CONTRACTS] admin={} employeeId={} total={}",
                                username, employeeId, files.size());

                model.addAttribute("contracts", files);
                model.addAttribute("employeeId", employeeId);
                model.addAttribute("currentAccount", currentAccount);

                return "contracts/list";
        }

        // ============================================
        // Upload hợp đồng
        // ============================================
        @PostMapping("/upload/{employeeId}")
        public String uploadContract(
                        @PathVariable String employeeId,
                        @RequestParam("file") MultipartFile file,
                        RedirectAttributes redirectAttributes) {

                String username = SecurityUtil.getCurrentUsername();
                logger.info("[ACTION=UPLOAD] user={} targetEmployee={} filename={} size={}",
                                username, employeeId, file.getOriginalFilename(), file.getSize());

                Account currentAccount = accountService.getByUsername(username);

                try {
                        Contract saved = contractService.uploadContract(currentAccount, employeeId, file);

                        logger.info("[RESULT=UPLOAD_SUCCESS] user={} employeeId={} savedPath={}",
                                        username, employeeId, saved.getFilePath());

                        redirectAttributes.addFlashAttribute("successMessage", "Upload thành công!");
                        return "redirect:/contracts/list/" + employeeId;

                } catch (IOException e) {
                        logger.error("[ERROR=UPLOAD_FAILED] IOException", e);
                        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu file: " + e.getMessage());
                } catch (RuntimeException e) {
                        logger.warn("[ERROR=UPLOAD_FAILED] RuntimeException", e);
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                }

                return "redirect:/contracts/list/" + employeeId;
        }

        // ============================================
        // Xóa hợp đồng
        // ============================================
        @GetMapping("/delete/{id}")
        public String deleteContract(@PathVariable String id) {

                String username = SecurityUtil.getCurrentUsername();
                logger.info("[ACTION=DELETE] user={} contractId={}", username, id);

                Account currentAccount = accountService.getByUsername(username);

                Contract c = contractService.getContractById(id);

                logger.info("[INFO=DELETE_TARGET] user={} employeeId={} fileName={}",
                                username, c.getEmployeeId(), c.getFileName());

                contractService.deleteContract(currentAccount, id);

                logger.info("[RESULT=DELETE_SUCCESS] user={} removedId={}", username, id);

                return "redirect:/contracts/list/" + c.getEmployeeId();
        }

        // ============================================
        // Download PDF
        // ============================================
        @GetMapping("/download/{id}")
        public ResponseEntity<FileSystemResource> download(@PathVariable String id) throws IOException {

                String username = SecurityUtil.getCurrentUsername();
                logger.info("[ACTION=DOWNLOAD] user={} contractId={}", username, id);

                Account currentAccount = accountService.getByUsername(username);

                FileSystemResource resource = contractService.getFileResource(currentAccount, id);
                File file = resource.getFile();

                String mimeType = Files.probeContentType(file.toPath());
                if (mimeType == null)
                        mimeType = "application/octet-stream";

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + file.getName() + "\"")
                                .contentType(MediaType.parseMediaType(mimeType))
                                .contentLength(file.length())
                                .body(resource);
        }

        // ============================================
        // View PDF trực tiếp
        // ============================================
        @GetMapping("/view/{id}")
        public ResponseEntity<FileSystemResource> view(@PathVariable String id) throws IOException {

                String username = SecurityUtil.getCurrentUsername();
                logger.info("[ACTION=VIEW_PDF] user={} contractId={}", username, id);

                Account currentAccount = accountService.getByUsername(username);

                FileSystemResource resource = contractService.getFileResource(currentAccount, id);
                File file = resource.getFile();

                logger.info("[CHECK=FILE] user={} path={} exists={}",
                                username, file.getAbsolutePath(), file.exists());

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .contentLength(file.length())
                                .body(resource);
        }
}
