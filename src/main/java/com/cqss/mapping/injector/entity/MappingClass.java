package com.cqss.mapping.injector.entity;

import lombok.Data;

@Data
public class MappingClass {
	private Class entityClass;
	private Class mapperClass;
}