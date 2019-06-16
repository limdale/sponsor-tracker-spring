package com.limdale.sponsortracker.controller;

import com.limdale.sponsortracker.exceptions.ResourceNotFoundException;
import com.limdale.sponsortracker.model.Company;
import com.limdale.sponsortracker.repository.CompanyRepository;
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
