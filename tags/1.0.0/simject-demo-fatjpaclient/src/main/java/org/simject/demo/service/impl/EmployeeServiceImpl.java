package org.simject.demo.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.simject.demo.business.EmployeeManager;
import org.simject.demo.model.Employee;
import org.simject.demo.service.EmployeeService;

public class EmployeeServiceImpl implements EmployeeService{

	@Resource
	private EmployeeManager employeeManager;

	@Override
	public List<Employee> listEmployees() {
		return this.employeeManager.findAll();
	}

	@Override
	public void insertEmployee(Employee employee) {
		this.employeeManager.saveOrUpdate(employee);
	}
}
