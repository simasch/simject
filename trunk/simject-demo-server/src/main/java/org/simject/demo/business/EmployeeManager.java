package org.simject.demo.business;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.simject.demo.model.Employee;

public class EmployeeManager {

	@Resource
	private EntityManager em;

	public List<Employee> findAll() {
		Query query = this.em.createNamedQuery("Employee.findAll");
		List<Employee> list = query.getResultList();
		return list;
	}

	public void saveOrUpdate(Employee employee) {
		EntityTransaction trx = this.em.getTransaction();
		trx.begin();
		this.em.merge(employee);
		trx.commit();
		
		
	}
}
