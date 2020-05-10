package com.lagou.test;

import com.lagou.dao.UserDao;
import com.lagou.io.Resources;
import com.lagou.pojo.User;
import com.lagou.sqlSession.SqlSession;
import com.lagou.sqlSession.SqlSessionFactory;
import com.lagou.sqlSession.SqlSessionFactoryBuilder;
import org.dom4j.DocumentException;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.util.List;

public class IPersistenceTest {

    UserDao userDao;
    @Before
    public void before() throws PropertyVetoException, DocumentException {
        InputStream resourceAsSteam = Resources.getResourceAsSteam("sqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsSteam);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        userDao = sqlSession.getMapper(UserDao.class);
    }

    @Test
    public void test() throws Exception{
        User user = new User();
        user.setId(2);
        user.setUsername("tom");
//        Object o = sqlSession.selectOne("user.selectOne", user);
//        System.out.println(o);
//        List<Object> objects = sqlSession.selectList("user.selectList");
//        System.out.println(objects);
        List<User> all = userDao.findAll();
        for (User user1 : all) {
            System.out.println(user1);
        }
        User user1 = userDao.findCondition(user);
        System.out.println(user1);
    }

    @Test
    public void test1(){
        User user = new User();
        user.setId(3);
        user.setUsername("ronghua11");
        user.setPassword("123456");
        int update = userDao.update(user);
        System.out.println(update);
    }

    @Test
    public void test2(){
        User user = new User();
        user.setId(4);
        user.setUsername("test");
        user.setPassword("111");
        user.setBirthday("2020-10-10");
        int insert = userDao.insert(user);
        System.out.println(insert);
    }

    @Test
    public  void test3(){
        User user = new User();
        user.setId(4);
        int delete = userDao.delete(user);
        System.out.println(delete);
    }

}
