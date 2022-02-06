package com.atguigu.crowd.handler;

import com.atguigu.crowd.api.MysqlRemoteService;
import com.atguigu.crowd.api.RedisRemoteService;
import com.atguigu.crowd.config.ShortMessageProperties;
import com.atguigu.crowd.entity.po.MemberPO;
import com.atguigu.crowd.entity.vo.MemberLoginVO;
import com.atguigu.crowd.entity.vo.MemberVO;
import org.fall.constant.CrowdConstant;
import org.fall.utils.CrowdUtils;
import org.fall.utils.ResultEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Controller
public class MemberHandler {

    @Autowired
    private ShortMessageProperties shortMessageProperties;

    @Autowired
    private RedisRemoteService redisRemoteService;

    @Autowired
    private MysqlRemoteService mysqlRemoteService;

    @RequestMapping("/auth/member/logout.html")
    public String doLoginOut(HttpSession session){
        session.invalidate();
        return "redirect:http://localhost/";
    }

    @RequestMapping("/auth/member/do/login.html")
    public String doLogin(
            @RequestParam("loginacct") String loginacct,
            @RequestParam("loginpswd") String userpswdForm,
            ModelMap modelMap,
            HttpSession session) {
        // 根据loginacct查找对象
        ResultEntity<MemberPO> memberPOResultEntity = mysqlRemoteService.getMemberPOByLoginAcctRemote(loginacct);

        // 如果没有对象
        if (CrowdConstant.MESSAGE_LOGIN_FAILED.equals(memberPOResultEntity.getResult())) {
            modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, memberPOResultEntity.getMessage());
            return "member-login";
        } else {
            // 有对象则取出对象
            MemberPO memberPO = memberPOResultEntity.getData();

            // 如果取出对象为空
            if (memberPO == null) {
                modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, CrowdConstant.MESSAGE_LOGIN_FAILED);
                return "member-login";
            }

            // 取出对象不为空，将密码进行比较
            // 注意此时是密码保存是使用的盐值加密，每次加密后的值都不相同，所以不能直接使用==判断，而是用matches方法
            String userpswdDb = memberPO.getUserpswd();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(userpswdForm, userpswdDb);
            if (matches) {
                // 密码正确时，将数据存入封装到MemberLoginVO，并保存到session中
                MemberLoginVO memberLoginVO = new MemberLoginVO(memberPO.getId(), memberPO.getUsername(), memberPO.getEmail());
                session.setAttribute(CrowdConstant.ATTR_NAME_LOGIN_MEMBER, memberLoginVO);
                return "redirect:http://localhost/auth/to/member/center/page";
            } else {
                modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, CrowdConstant.MESSAGE_LOGIN_FAILED);
                return "member-login";
            }

        }
    }

    @RequestMapping("/auth/do/member/register.html")
    public String register(MemberVO memberVO, ModelMap modelMap) {
        // 1.获取用户手机号
        String phoneNum = memberVO.getPhoneNum();

        // 2.拼redis中存储
        String redisCodeKey = CrowdConstant.REDIS_CODE_PREFIX + phoneNum;

        // 3.从redis读取key对应的value
        ResultEntity<String> redisValueByKeyRemote = redisRemoteService.getRedisValueByKeyRemote(redisCodeKey);

        // 4.检查查询操作是否有效
        // 未找到验证码
        if (ResultEntity.FAILED.equals(redisValueByKeyRemote.getResult())) {
            modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, redisValueByKeyRemote.getMessage());
            return "member-reg";
        }

        // 获取redis中的验证码
        String redisCode = redisValueByKeyRemote.getData();

        // redis中验证码为空
        if (redisCode == null) {
            modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, CrowdConstant.MESSAGE_CODE_NOT_EXISTS);
            return "member-reg";
        }

        // redis中验证码不为空
        if (ResultEntity.SUCCESS.equals(redisValueByKeyRemote.getResult())) {
            // 5.如果从redis能够查询到value则比较表单验证码和redis验证码
            String formCode = memberVO.getCode();

            // 验证码不一致
            if (!Objects.equals(formCode, redisCode)) {
                modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, CrowdConstant.MESSAGE_CODE_INVALID);
                return "member-reg";
            }

            // 验证码一致
            if (Objects.equals(formCode, redisCode)) {
                // 6.如果验证码一致，则从redis中删除
                redisRemoteService.RemoveRedisKeyByKeyRemote(redisCode);

                // 7.执行密码加密
                String userpswd = memberVO.getUserpswd();
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                String passWord = bCryptPasswordEncoder.encode(userpswd);
                memberVO.setUserpswd(passWord);

                // 8.执行保存，使用BeanUtil工具类进行属性拷贝
                MemberPO memberPO = new MemberPO();
                BeanUtils.copyProperties(memberVO, memberPO);
                ResultEntity<String> saveMemberResultEntity = mysqlRemoteService.saveMember(memberPO);

                // 保存失败
                if (ResultEntity.FAILED.equals(saveMemberResultEntity.getResult())) {
                    modelMap.addAttribute(CrowdConstant.ATTR_NAME_MESSAGE, saveMemberResultEntity.getMessage());
                    return "member-reg";
                }
            }
        }
        // 使用重定向避免重复提交表单
        return "redirect:http://localhost/auth/to/member/login/page";
    }

    @ResponseBody
    @RequestMapping("/auth/member/send/short/message.json")
    public ResultEntity<String> sendMessage(@RequestParam("phoneNum") String phoneNum) {
        // 发送验证码到phoneNum手机
        ResultEntity<String> sendShortMessage = CrowdUtils.sendShortMessage(
                shortMessageProperties.getHost(),
                shortMessageProperties.getPath(),
                shortMessageProperties.getAppcode(),
                phoneNum,
                shortMessageProperties.getSign(),
                shortMessageProperties.getSkin());
        // 判断短信是否发送成功
        if (ResultEntity.SUCCESS.equals(sendShortMessage.getResult())) {
            // 如果发送成功，将验证码存入redis
            String code = sendShortMessage.getData();
            // 拼接存入redis的key值
            String key = CrowdConstant.REDIS_CODE_PREFIX + phoneNum;
            ResultEntity<String> saveCodeResultEntity = redisRemoteService.setRedisKeyValueWithTimeoutRemote(key, code, 10, TimeUnit.MINUTES);
            // 判断redis中是否保存成功
            if (ResultEntity.SUCCESS.equals(saveCodeResultEntity.getResult())) {
                // 保存成功则发送消息即可
                return ResultEntity.successWithoutData();
            } else {
                // 失败直接返回保存的对象
                return saveCodeResultEntity;
            }
        } else {
            return sendShortMessage;
        }
    }

}
