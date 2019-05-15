package com.ytz.seckill.dao;

import com.ytz.seckill.domain.MiaoshaGoods;
import com.ytz.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {

    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.miaosha_price from seckill_goods mg left join goods g on mg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.miaosha_price from seckill_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId")long goodsId);

    @Update("update seckill_goods set stock_count = stock_count - 1 where goods_id = #{goodsId}")
    public int reduceStock(MiaoshaGoods g);

}
