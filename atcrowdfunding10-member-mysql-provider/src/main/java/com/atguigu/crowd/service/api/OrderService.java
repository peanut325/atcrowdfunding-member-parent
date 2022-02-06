package com.atguigu.crowd.service.api;

import com.atguigu.crowd.entity.po.AddressPO;
import com.atguigu.crowd.entity.vo.AddressVO;
import com.atguigu.crowd.entity.vo.OrderProjectVO;
import com.atguigu.crowd.entity.vo.OrderVO;
import org.fall.utils.ResultEntity;

import java.util.List;

public interface OrderService {

    OrderProjectVO selectOrderProjectVO(Integer returnId);

    List<AddressVO> selectAddressVO(Integer memberLoginVOId);

    void insertAddressVO(AddressVO addressVO);

    void insertOrderVO(OrderVO orderVO);
}
