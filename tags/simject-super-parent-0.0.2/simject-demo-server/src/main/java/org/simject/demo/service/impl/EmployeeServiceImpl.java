package org.simject.demo.service.impl;

import javax.annotation.Resource;

import org.simject.demo.business.EmployeeManager;
import org.simject.demo.model.Employee;
import org.simject.demo.service.EmployeeService;

public class EmployeeServiceImpl implements EmployeeService {

	@Resource
	private EmployeeManager employeeManager;

	public String getHello(Employee employee) {
		return this.employeeManager.getHello(employee);
	}
}
