package com.imooc.api.controller.admin;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.pojo.bo.UpdateUserInfoBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(value = "管理员admin维护",tags = {"管理员admin维护"})
@RequestMapping("adminMng")
public interface AdminMngControllerApi {

    @ApiOperation(value = "管理员登录接口",notes = "管理员登录接口",httpMethod = "POST")
    @PostMapping("/adminLogin")
    public GraceJSONResult adminLogin(@RequestBody AdminLoginBO adminLoginBO
            , HttpServletRequest request, HttpServletResponse response);

    @ApiOperation(value = "检查admin用户名是否存在",notes = "检查admin用户名是否存在",httpMethod = "POST")
    @PostMapping("/adminIsExist")
    public GraceJSONResult adminIsExist(@RequestParam String username);

    @ApiOperation(value = "创建admin",notes = "创建admin",httpMethod = "POST")
    @PostMapping("/addNewAdmin")
    public GraceJSONResult addNewAdmin(@RequestBody NewAdminBO newAdminBO,HttpServletRequest request,HttpServletResponse response);

    @ApiOperation(value = "查询admin列表",notes = "查询admin列表",httpMethod = "GET")
    @GetMapping("/getAdminList")
    public GraceJSONResult getAdminList(
            @ApiParam(name = "page",value = "查询下一页的第几页",required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize",value = "分页查询每一页显示的条数",required = false)
            @RequestParam Integer pageSize);
}
