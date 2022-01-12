package com.cqss.mapping.groovy.tool;


import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cqss.mapping.injector.entity.MappingClass;
import com.cqss.mapping.injector.entity.TableEntity;
import groovy.lang.GroovyClassLoader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ClassLoaderTool {
	
	private static GroovyClassLoader groovyClassLoader = init();
	
	private static GroovyClassLoader init() {
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
		groovyClassLoader.setShouldRecompile(true);
		return groovyClassLoader;
	}
	
	/**
	 * 编译源码并加载
	 */
	public static Class loadClass(String sourceCode) {
		log.debug("编译源码并加载:{}",sourceCode);
		return groovyClassLoader.parseClass(sourceCode);
	}
	/**
	 * 通过类名获取Class
	 */
	private @SneakyThrows static Class getClass(String className) {
		Iterator<Class> iterator = Arrays.stream(groovyClassLoader.getLoadedClasses()).iterator();
		while (iterator.hasNext()) {
			Class next = iterator.next();
			if (next.getSimpleName().equals(className)) return next;
		}
		throw new ClassNotFoundException(className);
	}
	
	public static MappingClass getMappingClss(String tableName) {
		MappingClass mappingClss = new MappingClass();
		String className = firstToUpperCase(tableName);
		mappingClss.setEntityClass(getClass(className));
		mappingClss.setMapperClass(getClass(className.concat("Mapper")));
		return mappingClss;
	}
	
	public static String generateEntitySourceCode(TableEntity tableEntity){
		String className = firstToUpperCase(tableEntity.getTableName());
		StringBuffer sourceCode = new StringBuffer();
		String pkage = String.format("package %s;\n",tableEntity.getPackageName());
		String importPkages  = "import java.math.*;\nimport java.util.*;\nimport java.lang.*;\nimport java.math.*;\nimport com.baomidou.mybatisplus.annotation.*;\n";
		String definitionClass = String.format("public class %s {\n", className);
		sourceCode.append(pkage).append(importPkages).append(definitionClass);
		Map<String, String> columns = tableEntity.getColumns();
		Set<Map.Entry<String, String>> entries = columns.entrySet();
		
		for (Map.Entry<String, String> column : entries) {
			String definitionField = "";
			if(column.getKey().equals(tableEntity.getPrimaryKeyColumnName())){
				definitionField = String.format("\t@TableId(type = IdType.ASSIGN_ID)\n\tprivate %s %s;\n",column.getValue(),column.getKey());
			}else{
				definitionField = String.format("\tprivate %s %s;\n",column.getValue(),column.getKey());
			}
			sourceCode.append(definitionField);
		}
		//Get/Set
		for (Map.Entry<String, String> column : entries) {
			String definitionGet = String.format("\tpublic %s get%s(){\n\t\treturn this.%s;\n\t}\n",column.getValue(), firstToUpperCase(column.getKey()),column.getKey());
			sourceCode.append(definitionGet);
			String definitionSet = String.format("\tpublic void set%s(%s %s){\n\t\tthis.%s=%s;\n\t}\n", firstToUpperCase(column.getKey()),column.getValue(),column.getKey(),column.getKey(),column.getKey());
			sourceCode.append(definitionSet);
		}
		//ToString
		sourceCode.append("\t@Override\n\tpublic String toString() { \n\t\treturn \""+className+"[\"+");
		for (Map.Entry<String, String> column : entries) {
			sourceCode.append("\" "+column.getKey()+"=\"+"+column.getKey()+"+");
		}
		sourceCode.append("\"]\";");
		sourceCode.append("\n\t}\n}");
		return sourceCode.toString();
	}
	
	public static String generateMapperSourceCode(TableEntity tableEntity){
		String className = firstToUpperCase(tableEntity.getTableName());
		StringBuffer sourceCode = new StringBuffer();
		String pkage = String.format("package %s;\n",tableEntity.getPackageName());
		String importPkages  = "import com.cqss.mapping.injector.base.DynamicMapper;\nimport " + tableEntity.getPackageName()+"."+className +";\n";
		String definitionClass = String.format("public interface classNameMapper extends DynamicMapper<%s> {}", className);
		sourceCode.append(pkage).append(importPkages).append(definitionClass);
		return sourceCode.toString();
	}
	
	
	public static String firstToUpperCase(String param) {
		if (StringUtils.isBlank(param)) {
			return StringPool.EMPTY;
		}
		return param.substring(0, 1).toUpperCase() + param.substring(1);
	}
	
//	public static void main(String[] args) {
//		TableEntity tableEntity = new TableEntity();
//		HashMap<String, String> map = new HashMap<>();
//		map.put("id", "String");
//		map.put("name", "String");
//		map.put("age", "Integer");
//		tableEntity.setColumns(map);
//		tableEntity.setTableName("student");
//		tableEntity.setPrimaryKeyColumnName("id");
//		tableEntity.setPackageName("com.cbest.sjt.admin.entity");
//		String entitySourceCode = ClassLoaderTool.generateEntitySourceCode(tableEntity);
//		System.err.println(entitySourceCode);
//		String mapperSourceCode = ClassLoaderTool.generateMapperSourceCode(tableEntity);
//		System.err.println(mapperSourceCode);
//	}
	
}
