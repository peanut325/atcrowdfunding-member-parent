package com.atguigu.crowd.service.impl;

import com.atguigu.crowd.entity.po.AddressPO;
import com.atguigu.crowd.entity.po.AddressPOExample;
import com.atguigu.crowd.entity.po.OrderPO;
import com.atguigu.crowd.entity.po.OrderProjectPO;
import com.atguigu.crowd.entity.vo.AddressVO;
import com.atguigu.crowd.entity.vo.OrderProjectVO;
import com.atguigu.crowd.entity.vo.OrderVO;
import com.atguigu.crowd.mapper.AddressPOMapper;
import com.atguigu.crowd.mapper.OrderPOMapper;
import com.atguigu.crowd.mapper.OrderProjectPOMapper;
import com.atguigu.crowd.service.api.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderPOMapper orderPOMapper;

    @Autowired
    private OrderProjectPOMapper orderProjectPOMapper;

    @Autowired
    private AddressPOMapper addressPOMapper;


    @Override
    public OrderProjectVO selectOrderProjectVO(Integer returnId) {
        return orderPOMapper.selectOrderProjectVO(returnId);
    }

    @Override
    public List<AddressVO> selectAddressVO(Integer memberLoginVOId) {
        AddressPOExample addressPOExample = new AddressPOExample();
        AddressPOExample.Criteria criteria = addressPOExample.createCriteria();
        criteria.andMemberIdEqualTo(memberLoginVOId);
        ArrayList<AddressVO> addressVOList = new ArrayList<>();
        List<AddressPO> addressPOList = addressPOMapper.selectByExample(addressPOExample);
        for (AddressPO addressPO : addressPOList) {
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(addressPO, addressVO);
            addressVOList.add(addressVO);
        }
        return addressVOList;
    }

    @Transactional(readOnly = false, rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void insertAddressVO(AddressVO addressVO) {
        AddressPO addressPO = new AddressPO();
        BeanUtils.copyProperties(addressVO, addressPO);
        addressPOMapper.insert(addressPO);
    }

    @Transactional(readOnly = false , propagation = Propagation.REQUIRES_NEW , rollbackFor = Exception.class)
    @Override
    public void insertOrderVO(OrderVO orderVO) {
        OrderPO orderPO = new OrderPO();
        BeanUtils.copyProperties(orderVO , orderPO);
        OrderProjectPO orderProjectPO = new OrderProjectPO();
        BeanUtils.copyProperties(orderVO.getOrderProjectVO() , orderProjectPO);
        orderPOMapper.insert(orderPO);
        Integer orderId = orderPO.getId();
        orderProjectPO.setOrderId(orderId);
        orderProjectPOMapper.insert(orderProjectPO);
    }
}
