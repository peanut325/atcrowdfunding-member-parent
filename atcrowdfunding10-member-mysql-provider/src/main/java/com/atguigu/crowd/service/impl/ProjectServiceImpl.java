package com.atguigu.crowd.service.impl;

import com.atguigu.crowd.entity.po.MemberConfirmInfoPO;
import com.atguigu.crowd.entity.po.MemberLaunchInfoPO;
import com.atguigu.crowd.entity.po.ProjectPO;
import com.atguigu.crowd.entity.po.ReturnPO;
import com.atguigu.crowd.entity.vo.*;
import com.atguigu.crowd.mapper.*;
import com.atguigu.crowd.service.api.ProjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional(readOnly = true)
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ReturnPOMapper returnPOMapper;

    @Autowired
    private MemberConfirmInfoPOMapper memberConfirmInfoPOMapper;

    @Autowired
    private MemberLaunchInfoPOMapper memberLaunchInfoPOMapper;

    @Autowired
    private ProjectPOMapper projectPOMapper;

    @Autowired
    private ProjectItemPicPOMapper projectItemPicPOMapper;

    @Transactional(readOnly = false, rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void saveProject(ProjectVO projectVO, Integer memberLoginVOId) {

        // 一：保存projectPO信息
        // 1.创建projectPO对象
        ProjectPO projectPO = new ProjectPO();
        // 2.将projectVO的值赋值给projectPO
        BeanUtils.copyProperties(projectVO, projectPO);
        // 3.把memberID设置到projectPO中
        projectPO.setMemberid(memberLoginVOId);
        // 4.生成创建的时间
        String createdate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        projectPO.setCreatedate(createdate);
        // 5.status设置为0，表示即将开始
        projectPO.setStatus(0);
        // 6.保存projectPO对象,为了获取projectPO的id属性，需要修改xml配置文件添加 useGeneratedKeys="true" keyProperty="id"
        projectPOMapper.insertSelective(projectPO);
        // 7.获取id属性
        Integer projectId = projectPO.getId();

        // 二： 保存项目分类的关联的相关信息
        // 1.从projectVO中获取typeIdList
        List<Integer> typeIdList = projectVO.getTypeIdList();
        // 2.保存typeIdList
        projectPOMapper.insertTypeRelationship(typeIdList, projectId);

        // 三：保存项目标签的关联的相关信息
        // 1.从projectVO中获取typeIdList
        List<Integer> tagIdList = projectVO.getTagIdList();
        // 2.保存tagIdList
        projectPOMapper.insertTagRelationship(tagIdList, projectId);

        // 四：保存详情图片路径
        // 1.从projectVO中获取detailPicturePathList
        List<String> detailPicturePathList = projectVO.getDetailPicturePathList();
        // 2.保存detailPicturePathList
        projectItemPicPOMapper.insertPathList(projectId, detailPicturePathList);

        // 五：保存项目发起人信息
        MemberLauchInfoVO memberLauchInfoVO = projectVO.getMemberLauchInfoVO();
        MemberLaunchInfoPO memberLaunchInfoPO = new MemberLaunchInfoPO();
        BeanUtils.copyProperties(memberLauchInfoVO, memberLaunchInfoPO);
        memberLaunchInfoPO.setMemberid(projectId);
        memberLaunchInfoPOMapper.insert(memberLaunchInfoPO);

        // 六：保存项目回报信息
        List<ReturnVO> returnVOList = projectVO.getReturnVOList();
        ArrayList<ReturnPO> returnPOList = new ArrayList<>();
        for (ReturnVO returnVO : returnVOList) {
            ReturnPO returnPO = new ReturnPO();
            BeanUtils.copyProperties(returnVO, returnPO);
            returnPOList.add(returnPO);
        }
        returnPOMapper.insertReturnPOBatch(returnPOList, projectId);

        // 七：保存项目确认信息
        MemberConfirmInfoVO memberConfirmInfoVO = projectVO.getMemberConfirmInfoVO();
        MemberConfirmInfoPO memberConfirmInfoPO = new MemberConfirmInfoPO();
        BeanUtils.copyProperties(memberConfirmInfoVO, memberConfirmInfoPO);
        memberConfirmInfoPO.setMemberid(projectId);
        memberConfirmInfoPOMapper.insert(memberConfirmInfoPO);
    }

    @Override
    public List<PortalTypeVO> selectPortalTypeVOList() {
        return projectPOMapper.selectPortalTypeVOList();
    }

    @Override
    public DetailProjectVO selectDetailProjectVO(Integer projectId) {
        DetailProjectVO detailProjectVO = projectPOMapper.selectDetailProjectVO(projectId);

        // 根据status设置当前筹集的状态
        Integer status = detailProjectVO.getStatus();
        switch (status) {
            case 0:
                detailProjectVO.setStatusText("即将开始");
                break;
            case 1:
                detailProjectVO.setStatusText("众筹中");
                break;
            case 2:
                detailProjectVO.setStatusText("众筹成功");
                break;
            case 3:
                detailProjectVO.setStatusText("众筹失败");
                break;
            default:
                break;
        }

        // 使用LocalDate计算众筹还剩余多少时间
        // 获取当前是当年的第多少天
        LocalDate todayDate = LocalDate.now();
        Integer todayDateDayOfYear = todayDate.getDayOfYear();

        // 获取众筹开始的时间
        String deployDate = detailProjectVO.getDeployDate();

        // 转化为LocalDate类型
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(deployDate, formatter);
        // 获取在当年的第多少天
        Integer deployDateOfYear= date.getDayOfYear();

        // 获取筹集持续时间
        Integer day = detailProjectVO.getDay();

        // 计算众筹的剩余时间
        Integer lastDay = day - (todayDateDayOfYear - deployDateOfYear);

        // 设置lastDay剩余时间
        detailProjectVO.setLastDay(lastDay);
        return detailProjectVO;
    }
}
