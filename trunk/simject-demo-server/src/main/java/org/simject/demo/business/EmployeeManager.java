package org.simject.demo.business;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.simject.demo.model.Employee;

public class EmployeeManager {

	@Resource
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<Employee> findAll() {
		final Query query = this.entityManager
				.createNamedQuery("Employee.findAll");
		return query.getResultList();
	}

	public void saveOrUpdate(final Employee employee) {
		final EntityTransaction trx = this.entityManager.getTransaction();
		trx.begin();
		this.entityManager.merge(employee);
		trx.commit();

	}
}
