package com.ytz.seckill.service;

import com.ytz.seckill.dao.UserDao;
import com.ytz.seckill.domain.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.beans.Transient;

@Service
public class UserService {

    @Resource
    UserDao userDao;

    public User getById(int id) {
        return userDao.getById(id);
    }

    //标注为事务
    @Transient
    public boolean tx() {
        User u1 = new User();
        u1.setId(2);
        u1.setName("Colin");
        userDao.insert(u1);

        User u2 = new User();
        u2.setId(1);
        u2.setName("Elise");
        userDao.insert(u2);

        return true;
    }
}
