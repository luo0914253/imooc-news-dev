package com.imooc.files.controller;

import com.imooc.api.controller.files.FileUploaderControllerApi;
import com.imooc.exception.GraceException;
import com.imooc.files.resource.FileResource;
import com.imooc.files.service.UploaderService;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.utils.FileUtils;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RestController
public class FileUploaderController implements FileUploaderControllerApi {

    final static Logger logger = LoggerFactory.getLogger(FileUploaderController.class);

    @Autowired
    private UploaderService uploaderService;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private FileResource fileResource;
    @Override
    public GraceJSONResult uploadFace(String userId, MultipartFile file) throws Exception {
        String path = "";
        if (file != null) {
            // 获得文件上传的名称
            String fileName = file.getOriginalFilename();

            // 判断文件名不能为空
            if (StringUtils.isNotBlank(fileName)) {
                String fileNameArr[] = fileName.split("\\.");
                // 获得后缀
                String suffix = fileNameArr[fileNameArr.length - 1];
                // 判断后缀符合我们的预定义规范
                if (!suffix.equalsIgnoreCase("png") &&
                        !suffix.equalsIgnoreCase("jpg") &&
                        !suffix.equalsIgnoreCase("jpeg")
                ) {
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
                }

                // 执行上传
//                path = uploaderService.uploadFdfs(file, suffix);
                path = uploaderService.uploadOSS(file, userId, suffix);

            } else {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
            }
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
        }

        logger.info("path = " + path);

        String finalPath = "";
        if (StringUtils.isNotBlank(path)) {
//            finalPath = fileResource.getHost() + path;
            finalPath = fileResource.getOssHost() + path;
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

//        return GraceJSONResult.ok(doAliImageReview(finalPath));
        return GraceJSONResult.ok(finalPath);
    }

    /**
     * 文件上传到MongoDB的gridfs中
     * @param newAdminBO
     * @return
     * @throws Exception
     */
    @Override
    public GraceJSONResult uploadToGridFS(NewAdminBO newAdminBO) throws Exception {
//      获取图片的base64字符串
        String img64 = newAdminBO.getImg64();
//      将base64转为byte[]
        byte[] bytes = new BASE64Decoder().decodeBuffer(img64.trim());
//      转为输入流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectId objectId = gridFSBucket.uploadFromStream(".png", byteArrayInputStream);
//      文件在gridfs中的主键id
        String fileIdStr = objectId.toString();
        return GraceJSONResult.ok(fileIdStr);
    }

    /**
     * 从gridFS中读取内容
     * @param faceId
     * @param request
     * @param response
     */
    @Override
    public void readInGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (StringUtils.isBlank(faceId)|| faceId.equalsIgnoreCase("null")){
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }
        File adminFace = readGridFSByFaceId(faceId);
//      把人脸图片输出到浏览器
        FileUtils.downloadFileByStream(response,adminFace);
    }

    /**
     * 从gridfs中读取图片内容，并且返回base64
     * @param faceId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public GraceJSONResult readFace64InGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
//      获取人脸文件
        File file = readGridFSByFaceId(faceId);
//      转化人脸为base64
        String base64 = FileUtils.fileToBase64(file);
        return GraceJSONResult.ok(base64);
    }


    private File readGridFSByFaceId(String faceId) throws Exception {
        GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(faceId)));
        GridFSFile gridFS = gridFSFindIterable.first();
        if (gridFS ==null){
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }
        String filename = gridFS.getFilename();
        File file = new File("F:/temp_face");
        if (!file.exists()){
            file.mkdirs();
        }
        File myFile = new File("F:/temp_face"+filename);
//      创建文件输出流
        OutputStream os = new FileOutputStream(myFile);
//      下载到服务器或者本地
        gridFSBucket.downloadToStream(new ObjectId(faceId),os);
        return myFile;
    }
}
