package com.imooc.admin.controller;

import com.github.pagehelper.PageInfo;
import com.imooc.admin.service.AdminUserService;
import com.imooc.api.controller.admin.AdminMngControllerApi;
import com.imooc.api.controller.user.BaseController;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AdminUser;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RestController
public class AdminMngController extends BaseController implements AdminMngControllerApi {
    final static Logger logger = LoggerFactory.getLogger(AdminMngController.class);

    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private RedisOperator redis;
    @Override
    public GraceJSONResult adminLogin(AdminLoginBO adminLoginBO
            , HttpServletRequest request, HttpServletResponse response) {
        AdminUser adminUser = adminUserService.queryAdminByUsername(adminLoginBO.getUsername());
        if (adminUser == null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
        boolean isPwdMatch = BCrypt.checkpw(adminLoginBO.getPassword(), adminUser.getPassword());
        if (isPwdMatch){
            doLoginSettings(adminUser,request,response);
            return GraceJSONResult.ok();
        }else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
    }

    @Override
    public GraceJSONResult adminIsExist(String username) {
        checkAdminExist(username);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult addNewAdmin(NewAdminBO newAdminBO
            , HttpServletRequest request, HttpServletResponse response) {
//      base64不为空，则代表人脸入库，否则需要用户输入密码个确认密码
        if (StringUtils.isBlank(newAdminBO.getImg64())){
            if (StringUtils.isBlank(newAdminBO.getPassword())
                    ||StringUtils.isBlank(newAdminBO.getConfirmPassword())){
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_ERROR);
            }
        }
        if (StringUtils.isNotBlank(newAdminBO.getPassword())){
            if (!newAdminBO.getPassword().equalsIgnoreCase(newAdminBO.getConfirmPassword())){
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_ERROR);
            }
        }
        checkAdminExist(newAdminBO.getUsername());
        return null;
    }

    @Override
    public GraceJSONResult getAdminList(Integer page, Integer pageSize) {
        if (page == null){
            page = COMMON_START_PAGE;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        return null;
    }


    private void checkAdminExist(String username){
        AdminUser adminUser = adminUserService.queryAdminByUsername(username);
        if (adminUser != null){
            GraceException.display(ResponseStatusEnum.ADMIN_USERNAME_EXIST_ERROR);
        }
    }

    private void doLoginSettings(AdminUser adminUser,HttpServletRequest request,HttpServletResponse response){
        String token = UUID.randomUUID().toString();
        redis.set(REDIS_ADMIN_TOKEN+":"+adminUser.getId(),token);
        setCookie(request,response,"atoken",token,COOKIE_MONTH);
        setCookie(request,response,"aid",adminUser.getId(),COOKIE_MONTH);
        setCookie(request,response,"aname",adminUser.getAdminName(),COOKIE_MONTH);
    }

}
