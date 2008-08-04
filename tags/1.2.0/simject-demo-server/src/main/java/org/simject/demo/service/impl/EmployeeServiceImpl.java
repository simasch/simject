package org.simject.demo.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.simject.demo.business.EmployeeManager;
import org.simject.demo.model.Employee;
import org.simject.demo.service.EmployeeService;

public class EmployeeServiceImpl implements EmployeeService {

	@Resource
	private EmployeeManager employeeManager;

	/* (non-Javadoc)
	 * @see org.simject.demo.service.EmployeeService#listEmployees()
	 */
	public List<Employee> listEmployees() {
		return this.employeeManager.findAll();
	}

	/* (non-Javadoc)
	 * @see org.simject.demo.service.EmployeeService#insertEmployee(org.simject.demo.model.Employee)
	 */
	public void insertEmployee(final Employee employee) {
		this.employeeManager.saveOrUpdate(employee);
	}
}
