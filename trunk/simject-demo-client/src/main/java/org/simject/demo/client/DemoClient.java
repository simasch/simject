package org.simject.demo.client;

import java.util.List;

import org.apache.log4j.Logger;
import org.simject.SimFactory;
import org.simject.demo.model.Employee;
import org.simject.demo.service.EmployeeService;

public final class DemoClient {

    private final static Logger logger = Logger.getLogger(DemoClient.class);

    // Avoid construction of DemoClient
    private DemoClient() {
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final SimFactory factory = new SimFactory("resources.xml");

        final EmployeeService service = factory
                .getResource(EmployeeService.class);
        final Employee employee = new Employee();
        employee.setName("Simon Martinelli");

        service.insertEmployee(employee);

        final List<Employee> list = service.listEmployees();
        for (Employee employee2 : list) {
            logger.info(employee2);
        }

    }

}
