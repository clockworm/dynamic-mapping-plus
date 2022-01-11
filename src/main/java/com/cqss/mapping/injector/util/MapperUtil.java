package com.cqss.mapping.injector.util;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cqss.mapping.groovy.tool.ClassLoaderTool;
import com.cqss.mapping.injector.base.DynamicMapper;
import com.cqss.mapping.injector.entity.MappingClass;
import com.cqss.mapping.injector.entity.TableEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

@Slf4j
public class MapperUtil {
	
	/**加载/刷新表映射*/
	public static void loadMemory(TableEntity entity,SqlSessionFactory sqlSessionFactory){
		if(StringUtils.isBlank(entity.getPrimaryKeyColumnName())) throw new RuntimeException("请设置唯一主键字段");
		String entitySourceCode = ClassLoaderTool.generateEntitySourceCode(entity);
		String mapperSourceCode = ClassLoaderTool.generateMapperSourceCode(entity);
		Class entityClass = ClassLoaderTool.loadClass(entitySourceCode);
		Class mappingClass = ClassLoaderTool.loadClass(mapperSourceCode);
		MybatisConfiguration configuration = (MybatisConfiguration) sqlSessionFactory.getConfiguration();
		configuration.addNewMapper(mappingClass);
		log.info("[加载/刷新]表名:{}实体类:{}映射类:{}动态关系绑定完成",entity.getTableName(),entityClass.getSimpleName(),mappingClass.getSimpleName());
	}
	
	/**获取表的映射实体Class*/
	public static Class getEntityClass(String tableName){
		MappingClass mappingClass = ClassLoaderTool.getMappingClss(tableName);
		return mappingClass.getEntityClass();
	}
	
	/**实例化表的映射实体对象*/
	public  @SneakyThrows static Object getEntity(String tableName){
		MappingClass mappingClass = ClassLoaderTool.getMappingClss(tableName);
		return mappingClass.getEntityClass().newInstance();
	}
	
	/**获取表的Mapper*/
	public static DynamicMapper getMapper(String tableName, SqlSessionFactory sqlSessionFactory){
		MappingClass mappingClass = ClassLoaderTool.getMappingClss(tableName);
		DynamicMapper dynamicMapper = (DynamicMapper)sqlSessionFactory
				.getConfiguration()
				.getMapper(
						mappingClass.getMapperClass(), sqlSessionFactory.openSession()
				);
		return dynamicMapper;
	}
	
}
