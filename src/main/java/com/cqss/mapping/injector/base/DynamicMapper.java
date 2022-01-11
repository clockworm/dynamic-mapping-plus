package com.cqss.mapping.injector.base;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.cqss.mapping.groovy.tool.ClassLoaderTool;
import com.cqss.mapping.injector.entity.MappingClass;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

public interface DynamicMapper<T>  extends BaseMapper<T> {
	
	default QueryChainWrapper<T> queryChain() {
		return new QueryChainWrapper<>(this);
	}
	
	default LambdaQueryChainWrapper<T> lambdaQueryChain() {
		return new LambdaQueryChainWrapper<>(this);
	}
	
	default UpdateChainWrapper<T> updateChain() {
		return new UpdateChainWrapper<>(this);
	}
	
	/**获取表的实体class*/
	default Class getEntityClass(String tableName){
		MappingClass mappingClass = ClassLoaderTool.getMappingClss(tableName);
		return mappingClass.getEntityClass();
	}
	
	default LambdaUpdateChainWrapper<T> lambdaUpdateChain() {
		return new LambdaUpdateChainWrapper<>(this);
	}
	
	int insertBatchSomeColumn(List<T> entityList);
	
	int alwaysUpdateSomeColumnById(@Param(Constants.ENTITY) T entity);
	
	int deleteByIdWithFill(T entity);
	
	
	
}
