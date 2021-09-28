package com.imooc.files.controller;

import com.imooc.grace.result.GraceJSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    final static Logger logger = LoggerFactory.getLogger(HelloController.class);
    @GetMapping("/hello")
    public Object hello(){
        return GraceJSONResult.ok("hello world");
    }
}
