package org.simject.demo.service;

import org.simject.demo.model.Employee;

public interface EmployeeService {

	String getHello(Employee employee);

	void find(String name, Integer age);
}
