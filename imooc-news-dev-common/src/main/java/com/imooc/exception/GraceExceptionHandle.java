package com.imooc.exception;

import com.imooc.grace.result.GraceJSONResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常拦截
 */
@ControllerAdvice
public class GraceExceptionHandle {
    @ExceptionHandler(MyCustomException.class)
    @ResponseBody
    public GraceJSONResult returnMyException(MyCustomException e){
        e.printStackTrace();
        return GraceJSONResult.exception(e.getResponseStatusEnum());
    }
}
