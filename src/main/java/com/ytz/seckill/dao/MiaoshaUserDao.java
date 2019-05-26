package com.ytz.seckill.dao;

import com.ytz.seckill.domain.MiaoshaUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MiaoshaUserDao {

    @Select("select * from seckill_user where id = #{id}")
    public MiaoshaUser getById(@Param("id")long id);

    @Update("update seckill_user set password = #{password} where id = #{id}")
    public void update(MiaoshaUser toBeUpdate);

    @Insert("insert into seckill_user (id, nickname, password, salt, login_count)values(#{id}, #{nickname}, #{password}, #{salt}, #{loginCount})")
    public void addUser(MiaoshaUser toBeInsert);
}