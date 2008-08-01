package org.simject.demo.business;

import org.simject.demo.model.Employee;

public class EmployeeManager {

	public String getHello(Employee employee) {
		return "Hello " + employee.getName();
	}
	
	public void printHelloWorld() {
		System.out.println("HELLO WORLD!!!");
	}
}
