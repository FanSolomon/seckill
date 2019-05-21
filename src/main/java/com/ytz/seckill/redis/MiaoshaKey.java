package com.ytz.seckill.redis;

public class MiaoshaKey extends BasePrefix{

    private MiaoshaKey(String prefix) {
        super(prefix);
    }
    //默认永久不失效
    public static MiaoshaKey isGoodsOver = new MiaoshaKey("go");
}