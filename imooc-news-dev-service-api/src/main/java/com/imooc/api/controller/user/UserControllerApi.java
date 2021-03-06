package com.imooc.api.controller.user;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.UpdateUserInfoBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "用户相关信息Controller",tags = {"用户相关信息Controller"})
@RequestMapping("user")
public interface UserControllerApi {

    @ApiOperation(value = "获取用户相关信息",notes = "获取用户相关信息",httpMethod = "GET")
    @PostMapping("/getUserInfo")
    public GraceJSONResult getUserInfo(@RequestParam String userId);

    @ApiOperation(value = "获取短信验证码",notes = "获取短信验证码",httpMethod = "POST")
    @PostMapping("/getAccountInfo")
    public GraceJSONResult getAccountInfo(@RequestParam String userId);

    @ApiOperation(value = "修改/完善用户信息",notes = "修改/完善用户信息",httpMethod = "POST")
    @PostMapping("/updateUserInfo")
    public GraceJSONResult updateUserInfo(@RequestBody @Valid UpdateUserInfoBO updateUserInfoBO,BindingResult result);

}
