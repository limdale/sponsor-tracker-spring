package com.limdale.sponsorpls.controller;

import com.limdale.sponsorpls.model.Company;
import com.limdale.sponsorpls.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {
    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping("")
    public List<Company> allCompanies() {
        return companyRepository.findAll();
    }

    @PostMapping("/new")
    public Company newCompany(@RequestBody Company company) {
        return companyRepository.save(company);
    }
}
