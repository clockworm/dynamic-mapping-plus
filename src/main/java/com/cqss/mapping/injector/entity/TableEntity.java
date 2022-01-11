package com.cqss.mapping.injector.entity;

import lombok.Data;

import java.util.Map;

@Data
public class TableEntity {
	private String tableName;
	private String primaryKeyColumnName;
	private String packageName;
	private Map<String,String> columns;
}
