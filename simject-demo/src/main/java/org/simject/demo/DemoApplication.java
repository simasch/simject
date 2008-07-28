package org.simject.demo;

import org.simject.SimFactory;
import org.simject.demo.model.Employee;
import org.simject.demo.service.EmployeeService;


public class DemoApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimFactory factory = new SimFactory("resources.xml");
		
		EmployeeService service = factory.getResource(EmployeeService.class);
		Employee employee = new Employee();
		employee.setName("Simon Martinelli");
		service.insertEmployee(employee);
		System.out.println(service.listEmployees());
	}

}
