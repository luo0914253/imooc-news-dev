package com.imooc.admin.controller;

import com.github.pagehelper.PageInfo;
import com.imooc.admin.service.AdminUserService;
import com.imooc.api.controller.admin.AdminMngControllerApi;
import com.imooc.api.controller.user.BaseController;
import com.imooc.enums.FaceVerifyType;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AdminUser;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.utils.FaceVerifyUtils;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

@RestController
public class AdminMngController extends BaseController implements AdminMngControllerApi {
    final static Logger logger = LoggerFactory.getLogger(AdminMngController.class);

    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private RedisOperator redis;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FaceVerifyUtils faceVerifyUtils;
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
    public GraceJSONResult adminFaceLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) throws Exception {
//      0、判断用户名和人脸信息不能为空
        if (StringUtils.isBlank(adminLoginBO.getUsername())){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
        if (StringUtils.isBlank(adminLoginBO.getImg64())){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
//      1、从数据库中查出faceid
        AdminUser adminUser = adminUserService.queryAdminByUsername(adminLoginBO.getUsername());
        String faceId = adminUser.getFaceId();
//      2、请求文件服务，获取人脸数据的base64数据
        String fileServerUrlExecute = "http://localhost:8004/fs/readFace64InGridFS?faceId="+faceId;
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate.getForEntity(fileServerUrlExecute, GraceJSONResult.class);
        GraceJSONResult graceJSONResult = responseEntity.getBody();
        String base64DB = (String)graceJSONResult.getData();
//      3、调用阿里云人脸比对
        boolean result = faceVerifyUtils.faceVerify(FaceVerifyType.BASE64.type, adminLoginBO.getImg64(), base64DB, 80);
        if (!result){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_LOGIN_ERROR);
        }
//      4、admin登录后的数据设置，redis与cookie
        doLoginSettings(adminUser,request,response);
        return GraceJSONResult.ok();
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
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult adminLogout(String adminId, HttpServletRequest request, HttpServletResponse response) {
        redis.del(REDIS_ADMIN_TOKEN+":"+adminId);
        deleteCookie(request,response,"atoken");
        deleteCookie(request,response,"aid");
        deleteCookie(request,response,"aname");
        return GraceJSONResult.ok();
    }
    private void deleteCookie(HttpServletRequest request,HttpServletResponse response,String cookieName){
        String deleteValue = null;
        try {
            deleteValue = URLEncoder.encode("","UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        setCookie(request,response,cookieName,deleteValue,COOKIE_DELETE);
    }
    @Override
    public GraceJSONResult getAdminList(Integer page, Integer pageSize) {
        if (page == null){
            page = COMMON_START_PAGE;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = adminUserService.queryAdminList(page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
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
