package com.atguigu.crowd.handler;

import com.atguigu.crowd.api.MysqlRemoteService;
import com.atguigu.crowd.entity.vo.AddressVO;
import com.atguigu.crowd.entity.vo.MemberLoginVO;
import com.atguigu.crowd.entity.vo.OrderProjectVO;
import org.fall.constant.CrowdConstant;
import org.fall.utils.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class OrderHandler {

    @Autowired
    private MysqlRemoteService mysqlRemoteService;

    @RequestMapping("/save/address")
    public String saveAddressVO(AddressVO addressVO, HttpSession session) {
        // 调用远程接口保存address
        ResultEntity<String> resultEntity = mysqlRemoteService.saveAddressVORemote(addressVO);

        // 从session域中获取returnCount重定向页面
        OrderProjectVO orderProjectVO = (OrderProjectVO) session.getAttribute(CrowdConstant.ATTR_NAME_ORDER_PROJECT);
        Integer returnCount = orderProjectVO.getReturnCount();

        // 重定向页面
        return "redirect:http://localhost/order/confirm/order/" + returnCount;

    }

    @RequestMapping("/confirm/return/info/{returnId}")
    public String showReturnConfirmInfo(
            @PathVariable("returnId") Integer returnId,
            HttpSession session
    ) {
        ResultEntity<OrderProjectVO> orderProjectVOResultEntity = mysqlRemoteService.getOrderProjectVORemote(returnId);

        if (ResultEntity.SUCCESS.equals(orderProjectVOResultEntity.getResult())) {
            session.setAttribute(CrowdConstant.ATTR_NAME_ORDER_PROJECT, orderProjectVOResultEntity.getData());
        }

        return "confirm_return";
    }

    @RequestMapping("/confirm/order/{returnCount}")
    public String showConfirmOrderInfo(
            @PathVariable("returnCount") Integer returnCount,
            HttpSession session
    ) {
        // 从session中取出对象
        OrderProjectVO orderProjectVO = (OrderProjectVO) session.getAttribute(CrowdConstant.ATTR_NAME_ORDER_PROJECT);

        // 将接收的returnCount赋值进去，保存到session域
        orderProjectVO.setReturnCount(returnCount);
        session.setAttribute(CrowdConstant.ATTR_NAME_ORDER_PROJECT, orderProjectVO);

        // 从session域中获取发起人的id值
        MemberLoginVO memberLoginVO = (MemberLoginVO) session.getAttribute(CrowdConstant.ATTR_NAME_LOGIN_MEMBER);
        Integer memberLoginVOId = memberLoginVO.getId();

        // 根据memberLoginVOId查找地址信息
        ResultEntity<List<AddressVO>> addressVOListEntity = mysqlRemoteService.getAddressVORemote(memberLoginVOId);

        if (ResultEntity.SUCCESS.equals(addressVOListEntity.getResult())) {
            // 将地址信息保存到session中
            session.setAttribute(CrowdConstant.ATTR_NAME_ORDER_ADDRESS, addressVOListEntity.getData());
        }

        return "confirm_order";
    }
}
