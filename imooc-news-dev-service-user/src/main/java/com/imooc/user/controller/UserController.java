package com.imooc.user.controller;

import com.imooc.api.controller.user.BaseController;
import com.imooc.api.controller.user.UserControllerApi;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.bo.AppUser;
import com.imooc.pojo.bo.UpdateUserInfoBO;
import com.imooc.pojo.vo.AppUserVO;
import com.imooc.pojo.vo.UserAccountInfoVO;
import com.imooc.user.service.UserService;
import com.imooc.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
public class UserController extends BaseController implements UserControllerApi {
    final static Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Override
    public GraceJSONResult getUserInfo(String userId) {
        if (StringUtils.isBlank(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }
        AppUser appUser = getUser(userId);
        AppUserVO appUserVO = new AppUserVO();
        BeanUtils.copyProperties(appUser,appUserVO);
        return GraceJSONResult.ok(appUserVO);
    }

    @Override
    public GraceJSONResult getAccountInfo(String userId) {
        if (StringUtils.isBlank(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }
        AppUser appUser = getUser(userId);
        UserAccountInfoVO userAccountInfoVO = new UserAccountInfoVO();
        BeanUtils.copyProperties(appUser,userAccountInfoVO);
        return GraceJSONResult.ok(userAccountInfoVO);
    }

    @Override
    public GraceJSONResult updateUserInfo(@Valid UpdateUserInfoBO updateUserInfoBO, BindingResult result) {
        if (result.hasErrors()){
            Map<String, String> errors = getErrors(result);
            return GraceJSONResult.errorMap(errors);
        }
        userService.updateUserInfo(updateUserInfoBO);
        return GraceJSONResult.ok();
    }

    private AppUser getUser(String userId){
        String userJson = redis.get(REDIS_USER_INFO + ":" + userId);
        AppUser user = null;
        if (StringUtils.isNotBlank(userJson)){
            user = JsonUtils.jsonToPojo(userJson,AppUser.class);
        }else {
            user = userService.getUser(userId);
            redis.set(REDIS_USER_INFO+":"+userId, JsonUtils.objectToJson(user));
        }
        return user;
    }
}
