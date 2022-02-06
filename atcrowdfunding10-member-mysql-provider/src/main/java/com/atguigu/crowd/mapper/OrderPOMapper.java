package com.atguigu.crowd.mapper;


import java.util.List;

import com.atguigu.crowd.entity.po.OrderPO;
import com.atguigu.crowd.entity.po.OrderPOExample;
import com.atguigu.crowd.entity.vo.OrderProjectVO;
import org.apache.ibatis.annotations.Param;

public interface OrderPOMapper {
    int countByExample(OrderPOExample example);

    int deleteByExample(OrderPOExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(OrderPO record);

    int insertSelective(OrderPO record);

    List<OrderPO> selectByExample(OrderPOExample example);

    OrderPO selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") OrderPO record, @Param("example") OrderPOExample example);

    int updateByExample(@Param("record") OrderPO record, @Param("example") OrderPOExample example);

    int updateByPrimaryKeySelective(OrderPO record);

    int updateByPrimaryKey(OrderPO record);

    OrderProjectVO selectOrderProjectVO(@Param("returnId") Integer returnId);
}