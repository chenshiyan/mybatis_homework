package com.lagou.sqlSession;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public interface SqlSession {

    public <E> List<E> selectList(String stateMappedId,Object... params) throws IllegalAccessException, IntrospectionException, InstantiationException, NoSuchFieldException, SQLException, InvocationTargetException, ClassNotFoundException;

    public <T> T selectOne(String stateMappedId,Object... params) throws IllegalAccessException, ClassNotFoundException, IntrospectionException, InstantiationException, SQLException, InvocationTargetException, NoSuchFieldException;

    public <T> T getMapper(Class<?> mapperClass);

    public int update(String stateMappedId,Object... params) throws ClassNotFoundException, SQLException, IllegalAccessException, NoSuchFieldException;

    public int insert(String stateMappedId,Object... params) throws ClassNotFoundException, SQLException, IllegalAccessException, NoSuchFieldException;

    public int delete(String stateMappedId,Object... params) throws ClassNotFoundException, SQLException, NoSuchFieldException, IllegalAccessException;
}
