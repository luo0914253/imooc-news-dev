package com.imooc.api.controller.user;

import com.imooc.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseController {
    @Autowired
    public RedisOperator redis;

    @Value("${website:domain-name}")
    public String DOMAIN_NAME;

    public static final String MOBILE_SMSCODE="mobile:smscode";

    public static final String REDIS_USER_TOKEN = "redis_user_token";

    public static final String REDIS_USER_INFO = "redis_user_info";

    public static final String REDIS_ADMIN_TOKEN = "redis_admin_token";

    public static final Integer COMMON_START_PAGE = 1;

    public static final Integer COMMON_PAGE_SIZE = 10;

    public static final Integer COOKIE_MONTH = 30*24*60*60;
    public static final Integer COOKIE_DELETE = 0;

    public Map<String,String> getErrors(BindingResult result){
        Map<String,String> map = new HashMap<>();
        List<FieldError> fieldErrors = result.getFieldErrors();
        for (FieldError error : fieldErrors) {
            String field = error.getField();
            String message = error.getDefaultMessage();
            map.put(field,message);
        }
        return map;
    }
    public void setCookie(HttpServletRequest request, HttpServletResponse response
            ,String cookieName,String cookieValue,Integer maxAge){
        try {
            cookieValue = URLEncoder.encode(cookieValue, "utf-8");
            Cookie cookie = new Cookie(cookieName, cookieValue);
            cookie.setMaxAge(maxAge);
            cookie.setDomain(DOMAIN_NAME);
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
