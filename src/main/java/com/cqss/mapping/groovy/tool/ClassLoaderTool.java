package com.cqss.mapping.groovy.tool;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cqss.mapping.injector.entity.MappingClass;
import com.cqss.mapping.injector.entity.TableEntity;
import groovy.lang.GroovyClassLoader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.*;

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
		log.info("编译源码并加载:{}",sourceCode);
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
		String pkage = "package ".concat(tableEntity.getPackageName()).concat(";\n");
		String importPkages  = "import java.math.*;\nimport java.util.*;\nimport java.lang.*;\nimport java.math.*;\nimport com.baomidou.mybatisplus.annotation.*;\n";
		String definitionClass = "public class ".concat(className).concat("{\n");
		sourceCode = sourceCode.append(pkage).append(importPkages).append(definitionClass);
		Map<String, String> columns = tableEntity.getColumns();
		Set<Map.Entry<String, String>> entries = columns.entrySet();
		
		for (Map.Entry<String, String> column : entries) {
			String definitionField = "";
			if(column.getKey().equals(tableEntity.getPrimaryKeyColumnName())){
				definitionField = "@TableId(type = IdType.ASSIGN_ID)\nprivate " + column.getValue() +" "+ column.getKey() + ";\n";
			}else{
				definitionField = "private " + column.getValue() +" "+ column.getKey() + ";\n";
			}
			sourceCode.append(definitionField);
		}
		//Get/Set
		for (Map.Entry<String, String> column : entries) {
			String definitionGet = "public " + column.getValue() +" get"+ firstToUpperCase(column.getKey()) + "(){\n";
			definitionGet = definitionGet + "return this."+column.getKey() +";\n}\n";
			sourceCode.append(definitionGet);
			String definitionSet = "public void" +" set"+ firstToUpperCase(column.getKey())+"(" + column.getValue() + " " + column.getKey() + ") {\n";
			definitionSet = definitionSet + "this."+column.getKey() + "=" + column.getKey() +";\n}\n";
			sourceCode.append(definitionSet);
		}
		//ToString
		sourceCode.append("@Override\npublic String toString() { \nreturn \""+className+"[\"+");
		for (Map.Entry<String, String> column : entries) {
			sourceCode.append("\" "+column.getKey()+"=\"+"+column.getKey()+"+");
		}
		sourceCode.append("\"]\";");
		sourceCode.append("\n}\n}");
		return sourceCode.toString();
	}
	
	
	@Deprecated
	public static String generateLombokEntitySourceCode(TableEntity tableEntity){
		String className = firstToUpperCase(tableEntity.getTableName());
		StringBuffer sourceCode = new StringBuffer();
		String pkage = "package ".concat(tableEntity.getPackageName()).concat(";\n");
		String importPkages  = "import java.math.*;\nimport lombok.Data;\nimport lombok.ToString;\nimport java.util.*;\nimport java.lang.*;\n";
		String definitionClass = "@Data\n"+"@ToString\npublic class ".concat(className).concat("{\n");
		sourceCode = sourceCode.append(pkage).append(importPkages).append(definitionClass);
		
		Map<String, String> columns = tableEntity.getColumns();
		Set<Map.Entry<String, String>> entries = columns.entrySet();
		
		for (Map.Entry<String, String> column : entries) {
			String definitionField = "private " + column.getValue() +" "+ column.getKey() + ";\n";
			sourceCode.append(definitionField);
		}
		sourceCode.append("}");
		return sourceCode.toString();
	}
	
	
	public static String generateMapperSourceCode(TableEntity tableEntity){
		String className = firstToUpperCase(tableEntity.getTableName());
		StringBuffer sourceCode = new StringBuffer();
		String pkage = "package ".concat(tableEntity.getPackageName()).concat(";\n");
		String importPkages  = "import com.cqss.mapping.injector.base.DynamicMapper;\n import " + tableEntity.getPackageName()+"."+className +";\n";
		String definitionClass = "public interface classNameMapper extends DynamicMapper<className> {}".replaceAll("className",className);
		sourceCode = sourceCode.append(pkage).append(importPkages).append(definitionClass);
		return sourceCode.toString();
	}
	
	
	public static String firstToUpperCase(String param) {
		if (StringUtils.isBlank(param)) {
			return StringPool.EMPTY;
		}
		return param.substring(0, 1).toUpperCase() + param.substring(1);
	}
	
}

