package org.simject.simject.demo.client;

import java.util.List;

import org.apache.log4j.Logger;
import org.simject.SimFactory;
import org.simject.demo.model.Employee;
import org.simject.demo.service.EmployeeService;

public class DemoClient {

	private final static Logger logger = Logger.getLogger(DemoClient.class); 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimFactory factory = new SimFactory("resources.xml");

		EmployeeService service = factory.getResource(EmployeeService.class);
		Employee employee = new Employee();
		employee.setName("Simon Martinelli");

		service.insertEmployee(employee);
		
		List<Employee> list = service.listEmployees();
		for (Employee employee2 : list) {
			logger.info(employee2);
		}
		
	}

}
