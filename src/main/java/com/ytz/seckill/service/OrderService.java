package com.ytz.seckill.service;

import com.ytz.seckill.dao.OrderDao;
import com.ytz.seckill.domain.MiaoshaOrder;
import com.ytz.seckill.domain.MiaoshaUser;
import com.ytz.seckill.domain.OrderInfo;
import com.ytz.seckill.vo.GoodsVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class OrderService {

    @Resource
    OrderDao orderDao;

    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
        return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
    }

    @Transactional
    public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());

//        long orderId = orderDao.insert(orderInfo);
        orderDao.insert(orderInfo);
        long orderId = orderInfo.getId();

        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderId);
        miaoshaOrder.setUserId(user.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);
        return orderInfo;
    }

}
