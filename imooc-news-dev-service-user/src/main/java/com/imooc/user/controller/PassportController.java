package com.imooc.user.controller;

import com.imooc.api.controller.user.BaseController;
import com.imooc.api.controller.user.PassportControllerApi;
import com.imooc.enums.UserStatus;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.bo.AppUser;
import com.imooc.pojo.bo.RegistLoginBO;
import com.imooc.user.service.UserService;
import com.imooc.utils.IPUtil;
import com.imooc.utils.SMSUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

@RestController
public class PassportController extends BaseController implements PassportControllerApi {
    final static Logger logger = LoggerFactory.getLogger(PassportController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private SMSUtils smsUtils;
    @Override
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request) {
        String userIp = IPUtil.getRequestIp(request);
//      根据用户IP进行限制，限制用户在60秒内只能获取一次验证码
        redis.setnx60s(MOBILE_SMSCODE+":"+userIp,userIp);
//      生成随机验证码并且发送短信
        String random = (int)((Math.random()*9+1)*100000)+"";
//        smsUtils.sendSMS(mobile,random);
//      把验证码存入redis，用户后续进行验证
        redis.set(MOBILE_SMSCODE+":"+mobile,random,30*60);
        return GraceJSONResult.ok(random);
    }

    @Override
    public GraceJSONResult doLogin(@Valid RegistLoginBO registLoginBO, BindingResult result
            ,HttpServletRequest request, HttpServletResponse response) {
        if (result.hasErrors()){
            Map<String, String> errors = getErrors(result);
            return GraceJSONResult.errorMap(errors);
        }
        String mobile = registLoginBO.getMobile();
        String smsCode = registLoginBO.getSmsCode();
//      校验验证码是否匹配
        String redisSMSCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisSMSCode)||!redisSMSCode.equalsIgnoreCase(smsCode)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
//      查询数据库，判断用户注册
        AppUser appUser = userService.queryMobileIsExist(mobile);
        if (appUser != null && appUser.getActiveStatus() == UserStatus.FROZEN.type){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_FROZEN);
        }else if (appUser == null){
            appUser = userService.createUser(mobile);
        }
//      保存用户分布式会话
        int activeStatus = appUser.getActiveStatus();
        if (activeStatus != UserStatus.FROZEN.type){
            String uToken = UUID.randomUUID().toString();
//          保存token到Redis
            redis.set(REDIS_USER_TOKEN+":"+appUser.getId(),uToken);
//          保存用户id和token到cookie
            setCookie(request,response,"utoken",uToken,COOKIE_MONTH);
            setCookie(request,response,"uid",appUser.getId(),COOKIE_MONTH);
        }
//      用户登录或注册成功后，删除验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);
        return GraceJSONResult.ok(activeStatus);
    }
    
}
