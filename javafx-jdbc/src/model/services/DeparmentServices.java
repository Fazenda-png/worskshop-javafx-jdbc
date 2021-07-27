package model.services;

import java.util.ArrayList;
import java.util.List;

import model.entities.Department;

public class DeparmentServices {

	public List<Department> findAll() {
		List<Department> list = new ArrayList();
		list.add(new Department(1, "TESTE"));
		list.add(new Department(2, "TESTE1"));
		list.add(new Department(3, "TESTE2"));
		list.add(new Department(4, "TESTE3"));
		return list;
	}
}
