package com.nhom11.qldt.feature.department.service;

import org.springframework.stereotype.Service;

import com.nhom11.qldt.core.repository.BaseRepository;
import com.nhom11.qldt.core.service.BaseCrudService;
import com.nhom11.qldt.feature.department.model.entity.Department;
import com.nhom11.qldt.feature.department.repository.DepartmentRepository;

@Service
public class DepartmentService extends BaseCrudService<Department, Long> {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public BaseRepository<Department, Long> getRepository() {
        return departmentRepository;
    }
}
