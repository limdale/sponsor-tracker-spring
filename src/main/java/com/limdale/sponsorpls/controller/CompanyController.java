package com.limdale.sponsorpls.controller;

import com.limdale.sponsorpls.exceptions.ResourceNotFoundException;
import com.limdale.sponsorpls.model.Company;
import com.limdale.sponsorpls.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/companies")
@CrossOrigin("http://localhost:3000")
public class CompanyController {
    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping("")
    public List<Company> allCompanies() {
        return companyRepository.findAll();
    }

    @GetMapping("/{id}")
    public Company getCompany(@PathVariable Long id) {
        Optional<Company> optional = companyRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException();
        }
    }

    @PostMapping("/new")
    public Company newCompany(@RequestBody Company company) {
        return companyRepository.save(company);
    }
}
