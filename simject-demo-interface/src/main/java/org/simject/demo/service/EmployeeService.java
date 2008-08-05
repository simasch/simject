package org.simject.demo.service;

import java.util.List;

import org.simject.demo.model.Employee;

public interface EmployeeService {

    List<Employee> listEmployees();

    void insertEmployee(Employee employee);
}
