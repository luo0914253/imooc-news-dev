package com.imooc.files.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploaderService {
    /**
     * 使用OSS上传文件
     * @param file
     * @param userId
     * @param fileExtName
     * @return
     * @throws Exception
     */
    public String uploadOSS(MultipartFile file,String userId,String fileExtName) throws Exception;
}
