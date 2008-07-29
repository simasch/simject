package org.simject.simject.demo.client;

import org.simject.SimFactory;
import org.simject.demo.model.Employee;
import org.simject.demo.service.EmployeeService;

public class DemoClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimFactory factory = new SimFactory("resources.xml");

		EmployeeService service = factory.getResource(EmployeeService.class);
		Employee employee = new Employee();
		employee.setName("Simon Martinelli");
		System.out.println(service.getHello(employee));
	}

}
