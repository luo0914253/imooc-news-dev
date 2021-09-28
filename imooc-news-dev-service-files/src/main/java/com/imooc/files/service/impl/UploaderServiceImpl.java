package com.imooc.files.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.imooc.files.resource.FileResource;
import com.imooc.files.service.UploaderService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
@Service
public class UploaderServiceImpl implements UploaderService {
    @Autowired
    private FileResource fileResource;
    @Autowired
    private Sid sid;
    /**
     * 使用OSS上传文件
     */
    @Override
    public String uploadOSS(MultipartFile file, String userId, String fileExtName) throws Exception {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = fileResource.getEndpoint();
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = fileResource.getAccessKeyID();
        String accessKeySecret = fileResource.getAccessKeySecret();

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写网络流地址。
        InputStream inputStream = file.getInputStream();
        String fileName = Sid.next();
        String myObjectName = fileResource.getObjectName()+"/"+userId+"/"+fileName+"."+fileExtName;
        // 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
        ossClient.putObject(fileResource.getBucketName(), myObjectName, inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();
        return myObjectName;
    }
}
