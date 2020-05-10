package com.lagou.sqlSession;

import com.lagou.config.BoundSql;
import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;
import com.lagou.util.GenericTokenParser;
import com.lagou.util.ParameterMapping;
import com.lagou.util.ParameterMappingTokenHandler;
import com.mysql.cj.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleExecutor implements Executor {


    @Override
    public <E> List<E>  query(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, IntrospectionException, InstantiationException, InvocationTargetException {
        //1、注册驱动 获取连接
        Connection connection = configuration.getDataSource().getConnection();

        //2、获取sql语句 ： select * from user where id = #{id} and username = #{username}
        //转换sql语句：select * from user where id = ? and user username = ? ,
        //转换过程中，还需要对#{}中值进行解析存储
        String sql = mappedStatement.getSql();
        BoundSql boundSql = this.getBoundSql(sql);

        //3、获取预处理对象：preparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());

        //4、设置参数
        //获取到参数的全路径
        String parameterType = mappedStatement.getParameterType();
        Class<?> parameterTypeClass = this.getClassType(parameterType);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            //字段名称
            String context = parameterMapping.getContext();
            //反射获取属性队形
            Field declaredField = parameterTypeClass.getDeclaredField(context);
            //暴力访问
            declaredField.setAccessible(true);
            Object o = declaredField.get(params[0]);
            preparedStatement.setObject(i + 1, o);
        }
        //5、执行sql
        ResultSet resultSet = preparedStatement.executeQuery();


        //6、封装返回结果
        //获取返回值类型
        String resultType = mappedStatement.getResultType();
        Class<?> resultTypeClass = this.getClassType(resultType);

        ArrayList<Object> objects = new ArrayList<>();

        while (resultSet.next()) {
            Object o = resultTypeClass.newInstance();
            //元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                //获取字段名
                String columnName = metaData.getColumnName(i);
                //字段的值
                Object value = resultSet.getObject(columnName);
                //使用反射或者内省，根据数据库表和实体的对应关系，完成封装
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, resultTypeClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(o, value);
            }
            objects.add(o);
        }

        return (List<E>) objects;
    }

    /**
     * 根据类的全路径获取实体类对象
     *
     * @param parameterType
     * @return
     */
    private Class<?> getClassType(String parameterType) throws ClassNotFoundException {
        if (!StringUtils.isNullOrEmpty(parameterType)) {
            Class<?> aClass = Class.forName(parameterType);
            return aClass;
        }
        return null;
    }

    /**
     * 完成解析工作：1、将#{}使用？进行替代。
     * 2、解析出#{}里面的值进行存储
     *
     * @param sql
     * @return
     */
    private BoundSql getBoundSql(String sql) {
        ParameterMappingTokenHandler tokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{", "}", tokenHandler);
        //解析过后的sql
        String parse = genericTokenParser.parse(sql);
        //#{}中的参数名称
        List<ParameterMapping> parameterMappings = tokenHandler.getParameterMappings();
        BoundSql boundSql = new BoundSql(parse, parameterMappings);
        return boundSql;

    }

    @Override
    public int update(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        //1、注册驱动
        Connection connection = configuration.getDataSource().getConnection();
        String sql = mappedStatement.getSql();
        BoundSql boundSql = this.getBoundSql(sql);
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());
        //设置参数
        String parameterType = mappedStatement.getParameterType();
        Class<?> parameterTypeClass = this.getClassType(parameterType);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            //字段名称
            String context = parameterMapping.getContext();
            Field declaredField = parameterTypeClass.getDeclaredField(context);
            declaredField.setAccessible(true);
            Object o = declaredField.get(params[0]);
            preparedStatement.setObject(i+1,o);
        }
        int i = preparedStatement.executeUpdate();
        return i;
    }

    @Override
    public int insert(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Connection connection = configuration.getDataSource().getConnection();
        String sql = mappedStatement.getSql();
        BoundSql boundSql = this.getBoundSql(sql);
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());
        String parameterType = mappedStatement.getParameterType();
        Class<?> parameterClass = this.getClassType(parameterType);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for(int i = 0; i< parameterMappings.size(); i++){
            ParameterMapping parameterMapping = parameterMappings.get(i);
            String context = parameterMapping.getContext();
            Field declaredField = parameterClass.getDeclaredField(context);
            declaredField.setAccessible(true);
            Object o = declaredField.get(params[0]);
            preparedStatement.setObject(i+1,o);
        }
        int i = preparedStatement.executeUpdate();
        return i;
    }

    @Override
    public int delete(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        Connection connection = configuration.getDataSource().getConnection();
        String sql = mappedStatement.getSql();
        BoundSql boundSql = this.getBoundSql(sql);
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());
        String parameterType = mappedStatement.getParameterType();
        Class<?> parameterClass = this.getClassType(parameterType);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for(int i = 0; i< parameterMappings.size(); i++){
            ParameterMapping parameterMapping = parameterMappings.get(i);
            String context = parameterMapping.getContext();
            Field declaredField = parameterClass.getDeclaredField(context);
            declaredField.setAccessible(true);
            Object o = declaredField.get(params[0]);
            preparedStatement.setObject(i+1,o);
        }
        int i = preparedStatement.executeUpdate();
        return i;
    }
}
