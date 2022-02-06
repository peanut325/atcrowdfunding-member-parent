package com.atguigu.crowd.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderProjectVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String projectName;

    private String launchName;

    // 回报描述
    private String returnContent;

    // 回报单数
    private Integer returnCount;

    // 回报价格
    private Integer supportPrice;

    // 是否免运费
    private Integer freight;

    // 订单id
    private Integer orderId;

    //	是否限制单笔购买数量，0 表示不限购，1 表示限购
    private Integer signalPurchase;

    //	如果单笔限购，那么具体的限购数量
    private Integer purchase;

}
