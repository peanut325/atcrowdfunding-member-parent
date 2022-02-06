package com.atguigu.crowd.handler;

import com.atguigu.crowd.api.MysqlRemoteService;
import com.atguigu.crowd.entity.vo.PortalTypeVO;
import org.fall.constant.CrowdConstant;
import org.fall.utils.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class PortalHandler {

    @Autowired
    private MysqlRemoteService mysqlRemoteService;

    @RequestMapping("/")
    public String PortalPage(Model model) {

        // 调用远程mysql工程方法
        ResultEntity<List<PortalTypeVO>> portalTypeVOListEntity = mysqlRemoteService.getPortalTypeVOList();

        // 判断是否查询成功
        if (ResultEntity.SUCCESS.equals(portalTypeVOListEntity.getResult())) {
            // 将数据存入model用于前端显示
            List<PortalTypeVO> portalTypeVOList = portalTypeVOListEntity.getData();
            model.addAttribute(CrowdConstant.ATTR_NAME_PORTAL_TYPE_LIST, portalTypeVOList);
        }

        return "portal";
    }

}
