package com.atguigu.crowd.handler;

import com.atguigu.crowd.entity.vo.AddressVO;
import com.atguigu.crowd.entity.vo.OrderProjectVO;
import com.atguigu.crowd.entity.vo.OrderVO;
import com.atguigu.crowd.service.api.OrderService;
import org.fall.utils.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class OrderProviderHandler {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/save/ordervo/remote")
    ResultEntity<String> saveOrderVO(@RequestBody OrderVO orderVO) {
        try {
            orderService.insertOrderVO(orderVO);
            return ResultEntity.successWithoutData();
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResultEntity.failed(exception.getMessage());
        }
    }

    @RequestMapping("/get/order/projectvo/remote")
    public ResultEntity<OrderProjectVO> getOrderProjectVORemote(
            @RequestParam("returnId") Integer returnId,
            HttpSession session) {

        try {
            OrderProjectVO orderProjectVO = orderService.selectOrderProjectVO(returnId);
            return ResultEntity.successWithData(orderProjectVO);
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResultEntity.failed(exception.getMessage());
        }
    }

    @RequestMapping("/get/order/addressvo/remote")
    public ResultEntity<List<AddressVO>> getAddressVORemote(@RequestParam("memberLoginVOId") Integer memberLoginVOId) {
        try {
            List<AddressVO> addressPOList = orderService.selectAddressVO(memberLoginVOId);
            return ResultEntity.successWithData(addressPOList);
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResultEntity.failed(exception.getMessage());
        }
    }

    @RequestMapping("/save/order/addressvo/remote")
    public ResultEntity<String> saveAddressVORemote(@RequestBody AddressVO addressVO) {
        try {
            orderService.insertAddressVO(addressVO);
            return ResultEntity.successWithoutData();
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResultEntity.failed(exception.getMessage());
        }
    }
}
