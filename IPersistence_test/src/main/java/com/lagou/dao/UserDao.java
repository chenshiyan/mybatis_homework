package com.lagou.dao;

import com.lagou.pojo.User;

import java.util.List;

public interface UserDao {

    public List<User> findAll();

    public User findCondition(User user);

    public int update(User user);

    public int insert(User user);

    public int delete(User id);
}
