package org.simject.simject.demo.client;

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
		
		logger.info(service.getHello(employee));
		
		service.find("Peter", 20);
	}

}
