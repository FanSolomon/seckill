package com.ytz.seckill.service;

import com.ytz.seckill.dao.MiaoshaUserDao;
import com.ytz.seckill.domain.MiaoshaUser;
import com.ytz.seckill.exception.GlobalException;
import com.ytz.seckill.redis.MiaoshaUserKey;
import com.ytz.seckill.redis.RedisService;
import com.ytz.seckill.result.CodeMsg;
import com.ytz.seckill.util.MD5Util;
import com.ytz.seckill.util.UUIDUtil;
import com.ytz.seckill.vo.LoginVo;
import com.ytz.seckill.vo.RegisterVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {


    public static final String COOKI_NAME_TOKEN = "token";

//    @Autowired
    @Resource
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    public MiaoshaUser getById(long id) {
        //取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
        if(user != null) {
            return user;
        }
        //取数据库
        user = miaoshaUserDao.getById(id);
        if(user != null) {
            redisService.set(MiaoshaUserKey.getById, ""+id, user);
        }
        return user;
    }

    // http://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323
    public boolean updatePassword(String token, long id, String formPass) {
        //取user
        MiaoshaUser user = getById(id);
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        MiaoshaUser toBeUpdate = new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        miaoshaUserDao.update(toBeUpdate);
        //处理缓存（十分重要，顺序为先更新数据库，后缓存）
        redisService.delete(MiaoshaUserKey.getById, ""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token, token, user);
        return true;
    }

    //2.0
    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        //延长有效期
        if(user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    //1.0
//    public MiaoshaUser getByToken(String token) {
//        if(StringUtils.isEmpty(token)) {
//            return null;
//        }
//        return redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
//    }


    public String login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if(!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token	 = UUIDUtil.uuid();
        addCookie(response, token, user);
        return token;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        //将登陆信息存入redis缓存中
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        //设置cookie有效期，同缓存中信息有效期
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public Boolean register(RegisterVo registerVo) {
        if(registerVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = registerVo.getMobile();
        String formPass = registerVo.getPassword();
        String nickname = registerVo.getNickname();
        long id = Long.parseLong(mobile);
        //user是否已经存在
        MiaoshaUser user = getById(id);
        if(user != null) {
            throw new GlobalException(CodeMsg.USER_EXIST_ERROR);
        }
        //更新数据库
        MiaoshaUser toBeInsert = new MiaoshaUser();
        toBeInsert.setId(id);
        toBeInsert.setNickname(nickname);
        toBeInsert.setPassword(MD5Util.formPassToDBPass(formPass, "1a2b3c"));
        toBeInsert.setSalt("1a2b3c");
        toBeInsert.setLoginCount(0);
        miaoshaUserDao.addUser(toBeInsert);
        return getById(id) != null;
    }
}
