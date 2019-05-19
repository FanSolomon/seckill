package com.ytz.seckill.dao;

import com.ytz.seckill.domain.MiaoshaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MiaoshaUserDao {

    @Select("select * from seckill_user where id = #{id}")
    public MiaoshaUser getById(@Param("id")long id);

    @Update("update seckill_user set password = #{password} where id = #{id}")
    public void update(MiaoshaUser toBeUpdate);
}