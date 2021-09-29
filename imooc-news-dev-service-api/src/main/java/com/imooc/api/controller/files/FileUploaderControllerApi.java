package com.imooc.api.controller.files;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "文件上传的Controller",tags = "{文件上传的Controller}")
@RequestMapping("/fs")
public interface FileUploaderControllerApi {
    @ApiOperation(value = "上传用户头像",notes = "上传用户头像",httpMethod = "POST")
    @PostMapping("/uploadFace")
    public GraceJSONResult uploadFace(@RequestParam String userId, MultipartFile file) throws Exception;

    /**
     * 文件上传到MongoDB的gridfs中
     * @param newAdminBO
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadToGridFS")
    public GraceJSONResult uploadToGridFS(@RequestBody NewAdminBO newAdminBO) throws Exception;

    /**
     * 从gridFS中读取内容
     * @param faceId
     * @param request
     * @param response
     */
    @GetMapping("/readInGridFS")
    public void readInGridFS(@RequestParam String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 从gridfs中读取图片内容，并且返回base64
     * @param faceId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping("/readFace64InGridFS")
    public GraceJSONResult readFace64InGridFS(@RequestParam String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
