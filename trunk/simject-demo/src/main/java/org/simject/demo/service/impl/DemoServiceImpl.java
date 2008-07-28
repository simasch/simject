package org.simject.demo.service.impl;

import javax.annotation.Resource;

import org.simject.demo.business.DemoManager;
import org.simject.demo.service.DemoService;

public class DemoServiceImpl implements DemoService{

	@Resource
	private DemoManager demoManager;

	@Override
	public String helloWorld() {
		return this.demoManager.helloWorld();
	}
}
