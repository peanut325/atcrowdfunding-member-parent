package com.atguigu.crowd.handler;

import com.atguigu.crowd.api.MysqlRemoteService;
import com.atguigu.crowd.config.OSSProperties;
import com.atguigu.crowd.entity.vo.*;
import org.fall.constant.CrowdConstant;
import org.fall.utils.CrowdUtils;
import org.fall.utils.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProjectConsumerHandler {

    @Autowired
    private MysqlRemoteService mysqlRemoteService;

    @Autowired
    private OSSProperties ossProperties;

    @RequestMapping("/create/confirm")
    public String saveConfirm(MemberConfirmInfoVO memberConfirmInfoVO, HttpSession session, ModelMap modelMap) {

        // 从session域读取之前的projectVO
        ProjectVO projectVO = (ProjectVO) session.getAttribute(CrowdConstant.ATTR_NAME_TEMPLE_PROJECT);

        // 判断是否为空
        if (projectVO == null) {
         throw new RuntimeException(CrowdConstant.MESSAGE_TEMPLE_PROJECT_MISSING);
        }

        // 将确认的信息保存在projectVO中
        projectVO.setMemberConfirmInfoVO(memberConfirmInfoVO);

        // 从session域读取当前访问的用户
        MemberLoginVO memberLoginVO = (MemberLoginVO) session.getAttribute(CrowdConstant.ATTR_NAME_LOGIN_MEMBER);

        // 从中取出保存进去的id值，进而保存在数据库中
        Integer memberLoginVOId = memberLoginVO.getId();

        // 调用mysql的方法将projectVO保存到数据库中
        ResultEntity resultEntity = mysqlRemoteService.saveProjectVORemote(projectVO , memberLoginVOId);

        // 保存失败，将错误信息保存到modelMap中
        if (ResultEntity.SUCCESS.equals(resultEntity.getResult())) {
            modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE,resultEntity.getMessage());
            return "project-confirm";
        }

        // 保存好后将session域清空
        session.removeAttribute(CrowdConstant.MESSAGE_TEMPLE_PROJECT_MISSING);

        return "redirect:http://localhost/projetc/create/success/page";
    }

    @RequestMapping("/get/project/detail/{projectId}")
    public String getDetailProjectVO(@PathVariable("projectId") Integer projectId, Model model) {

        ResultEntity<DetailProjectVO> detailProjectVOResultEntity = mysqlRemoteService.getDetailProjectVORemote(projectId);
        if (ResultEntity.SUCCESS.equals(detailProjectVOResultEntity.getResult())) {
            DetailProjectVO detailProjectVO = detailProjectVOResultEntity.getData();
            model.addAttribute(CrowdConstant.ATTR_NAME_DETAIL_PROJECT, detailProjectVO);
        }

        return "project-show-detail";
    }


    @ResponseBody
    @RequestMapping("/create/save/return.json")
    public ResultEntity<String> saveReturn(ReturnVO returnVO, HttpSession session) {
        try {
            // 从session中取出刚才保存的projectVO，需要进行强转
            ProjectVO projectVO = (ProjectVO) session.getAttribute(CrowdConstant.ATTR_NAME_TEMPLE_PROJECT);

            // 判断return数据是否为空
            if (returnVO == null) {
                return ResultEntity.failed(CrowdConstant.MESSAGE_TEMPLE_PROJECT_MISSING);
            }

            // 获取projectVO中的renturnVO
            List<ReturnVO> returnVOList = projectVO.getReturnVOList();

            // 如果returnVOList为空
            if (returnVOList == null || returnVOList.size() == 0) {
                // 初始化List
                returnVOList = new ArrayList<>();
                projectVO.setReturnVOList(returnVOList);
            }

            // returnVOList不为空,将值追加进去
            returnVOList.add(returnVO);

            // 重新把projectVO存入session中
            session.setAttribute(CrowdConstant.ATTR_NAME_TEMPLE_PROJECT, projectVO);

            // 返回结果集
            return ResultEntity.successWithoutData();

        } catch (Exception exception) {
            exception.printStackTrace();
            // 返回失败的结果集
            return ResultEntity.failed(exception.getMessage());
        }

    }

    @ResponseBody
    @RequestMapping("/create/upload/return/picture.json")
    public ResultEntity<String> uploadReturnPicture(@RequestParam("returnPicture") MultipartFile returnPicture) throws IOException {
        // 判断是否是有效上传
        boolean pictureIsEmpty = returnPicture.isEmpty();
        if (pictureIsEmpty) {
            // 如果上传文件为空
            ResultEntity.failed(CrowdConstant.MESSAGE_RETURN_PIC_EMPTY);
        }

        // 执行文件上传
        ResultEntity<String> returnPictureEntity = CrowdUtils.uploadFileToOSS(
                ossProperties.getEndPoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret(),
                returnPicture.getInputStream(),
                ossProperties.getBucketName(),
                ossProperties.getBucketDomain(),
                returnPicture.getOriginalFilename()
        );

        // 返回上传文件的结果
        return returnPictureEntity;
    }

    @RequestMapping("/create/project/information")
    public String saveProjectBasicInfo(
            // 接收表单的部分信息
            ProjectVO projectVO,
            // 接收上传的头图
            MultipartFile headerPicture,
            // 接收项目详情图片
            List<MultipartFile> detailPictureList,
            // 将收集的一部分数据的ProjectVO保存到session
            HttpSession session,
            // 将错误信息保存到ModelMap中
            ModelMap modelMap
    ) throws IOException {

        boolean headerPictureEmpty = headerPicture.isEmpty();

        // 如果头图为空，返回错误信息
        if (headerPictureEmpty) {
            // 返回错误消息
            modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, CrowdConstant.MESSAGE_HEADER_PIC_EMPTY);
            return "project-launch";
        }

        // 将头图上传到OSS上

        ResultEntity<String> resultEntity = CrowdUtils.uploadFileToOSS(
                ossProperties.getEndPoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret(),
                headerPicture.getInputStream(),
                ossProperties.getBucketName(),
                ossProperties.getBucketDomain(),
                headerPicture.getOriginalFilename()
        );

        if (ResultEntity.FAILED.equals(resultEntity.getResult())) {
            // 上传失败则显示错误信息
            modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, CrowdConstant.MESSAGE_HEADER_PIC_UPLOAD_FAILED);
            // 返回上一页面
            return "project-launch";
        }

        // 上传成功将信息保存到projectVO中
        String data = resultEntity.getData();
        projectVO.setHeaderPicturePath(data);


        // 将详情图片上传到OSS
        // 创建存放图片地址的list集合
        List<String> detailPicturePathList = new ArrayList<>();

        // 判断详情图片是否为空
        if (detailPictureList == null || detailPictureList.isEmpty()) {
            // 上传失败则显示错误信息
            modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, CrowdConstant.MESSAGE_DETAIL_PIC_EMPTY);
            // 返回上一页面
            return "project-launch";
        }

        // 图片不为空，遍历详情图片
        for (MultipartFile detailPicture : detailPictureList) {
            // 判断当前图片是否有效
            if (detailPicture == null) {
                // 上传失败则显示错误信息
                modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, CrowdConstant.MESSAGE_DETAIL_PIC_EMPTY);
                // 返回上一页面
                return "project-launch";
            }

            ResultEntity<String> detailPictureResultEntity = CrowdUtils.uploadFileToOSS(
                    ossProperties.getEndPoint(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret(),
                    detailPicture.getInputStream(),
                    ossProperties.getBucketName(),
                    ossProperties.getBucketDomain(),
                    detailPicture.getOriginalFilename()
            );

            if (ResultEntity.FAILED.equals(detailPictureResultEntity.getResult())) {
                // 上传失败则显示错误信息
                modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, CrowdConstant.MESSAGE_DETAIL_PIC_UPLOAD_FAILED);
                // 返回上一页面
                return "project-launch";
            }

            // 保存成功将路径存入list
            String pictureResultEntityData = detailPictureResultEntity.getData();
            detailPicturePathList.add(pictureResultEntityData);
        }

        // 将图片路径list集合保存在projectVO中
        projectVO.setDetailPicturePathList(detailPicturePathList);

        // 将projectVO对象存入session域
        session.setAttribute(CrowdConstant.ATTR_NAME_TEMPLE_PROJECT, projectVO);

        // 以网关的路径来重定向到return页面，才能保持session信息
        return "redirect:http://localhost/project/return/project/page";
    }
}
