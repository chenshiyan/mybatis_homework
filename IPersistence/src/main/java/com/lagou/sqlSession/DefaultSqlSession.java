package com.lagou.sqlSession;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;

import java.beans.IntrospectionException;
import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <E> List<E> selectList(String stateMappedId, Object... params) throws IllegalAccessException, IntrospectionException, InstantiationException, NoSuchFieldException, SQLException, InvocationTargetException, ClassNotFoundException {
        //将要完成对simpleExecutor 里的query方法调用
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(stateMappedId);
        List<Object> query = simpleExecutor.query(configuration, mappedStatement, params);
        return (List<E>) query;
    }

    @Override
    public <T> T selectOne(String stateMappedId, Object... params) throws IllegalAccessException, ClassNotFoundException, IntrospectionException, InstantiationException, SQLException, InvocationTargetException, NoSuchFieldException {
        List<Object> objects = selectList(stateMappedId, params);
        if (null != objects && objects.size() == 1) {
            return (T) objects.get(0);
        } else {
            throw new RuntimeException("查询结果为空或者结果为多条");
        }
    }

    @Override
    public int update(String stateMappedId, Object... params) throws ClassNotFoundException, SQLException, IllegalAccessException, NoSuchFieldException {
        //完成simpleExecutor 里的update方法
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(stateMappedId);
        int update = simpleExecutor.update(configuration, mappedStatement, params);
        return update;
    }

    @Override
    public int insert(String stateMappedId, Object... params) throws ClassNotFoundException, SQLException, IllegalAccessException, NoSuchFieldException {
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(stateMappedId);
        int insert = simpleExecutor.insert(configuration, mappedStatement, params);
        return insert;
    }

    @Override
    public int delete(String stateMappedId, Object... params) throws ClassNotFoundException, SQLException, NoSuchFieldException, IllegalAccessException {
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(stateMappedId);
        int delete = simpleExecutor.delete(configuration, mappedStatement, params);
        return delete;
    }

    @Override
    public <T> T getMapper(Class<?> mapperClass) {

        //使用JDK动态代理来为Dao接口生成代理对象，并返回
        Object o = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //proxy 当前代理对象的引用
                //method 当前被调用方法的引用
                //args 被传递的参数
                //底层都还是去执行JDBC代码 根据不同情况来调用selectList或者selectOne
                //准备参数1：statementId : sql唯一标识： namespace.id = 接口全限名.方法名
                //方法名
                String methodName = method.getName();
                //接口全限定名
                String className = method.getDeclaringClass().getName();
                String statementId = className + "." + methodName;
                //准备参数2 params: args
                //获取被调用方法的返回值类型
                Type genericReturnType = method.getGenericReturnType();
                //判断是否进行 泛型参数化 （有泛型参数化则说明返回的是集合）
                if (methodName.contains("select")){
                    if (genericReturnType instanceof ParameterizedType) {
                        return selectList(statementId, args);
                    }
                    return selectOne(statementId, args);
                }else if (methodName.contains("update")){
                    return update(statementId,args);
                }else if(methodName.contains("insert")){
                    return insert(statementId,args);
                }else if(methodName.contains("delete")){
                    return delete(statementId,args);
                }else {
                    throw new RuntimeException("未定义的功能" +methodName);
                }
            }
        });
        return (T) o;
    }
}
