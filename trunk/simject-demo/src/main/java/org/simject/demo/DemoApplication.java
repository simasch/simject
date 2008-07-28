package org.simject.demo;

import org.simject.SimFactory;
import org.simject.demo.service.DemoService;


public class DemoApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimFactory factory = new SimFactory("resources.xml");
		
		DemoService service = factory.getResource(DemoService.class);
		
		System.out.println(service.helloWorld());
	}

}
