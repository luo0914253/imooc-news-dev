package com.imooc.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.admin.mapper.AdminUserMapper;
import com.imooc.admin.service.AdminUserService;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AdminUser;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    @Autowired
    private Sid sid;
    @Autowired
    private AdminUserMapper adminUserMapper;
    /**
     * 获取管理员的用户信息
     * @param username
     * @return
     */
    @Override
    public AdminUser queryAdminByUsername(String username) {
        Example example = new Example(AdminUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",username);

        AdminUser adminUser = adminUserMapper.selectOneByExample(example);
        return adminUser;
    }

    /**
     * 新增管理员
     * @param newAdminBO
     */
    @Override
    public void createAdminUser(NewAdminBO newAdminBO) {
        String adminId = sid.nextShort();
        AdminUser user = new AdminUser();
        user.setId(adminId);
        user.setUsername(newAdminBO.getUsername());
        user.setAdminName(newAdminBO.getAdminName());
        if (StringUtils.isNotBlank(newAdminBO.getPassword())){
            user.setPassword(BCrypt.hashpw(newAdminBO.getPassword(),BCrypt.gensalt()));
        }
        if (StringUtils.isNotBlank(newAdminBO.getImg64())){
            user.setFaceId(newAdminBO.getFaceId());
        }
        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());
        int result = adminUserMapper.insert(user);
        if (result != 1){
            GraceException.display(ResponseStatusEnum.ADMIN_CREATE_ERROR);
        }
    }

    /**
     * 查询admin列表
     * @param page
     * @param pageSize
     */
    @Override
    public PagedGridResult queryAdminList(Integer page, Integer pageSize) {
        Example example = new Example(AdminUser.class);
        example.orderBy("createTime").desc();
        PageHelper.startPage(page,pageSize);
        List<AdminUser> adminUsers = adminUserMapper.selectByExample(example);
        return setterPagedGrid(adminUsers,page);
    }
    private PagedGridResult setterPagedGrid(List<?> adminUserList, Integer page){
        PageInfo<?> pageInfo = new PageInfo<>(adminUserList);
        PagedGridResult pagedGridResult = new PagedGridResult();
        pagedGridResult.setRows(adminUserList);
        pagedGridResult.setPage(page);
        pagedGridResult.setRecords(pageInfo.getPages());
        pagedGridResult.setTotal(pagedGridResult.getTotal());
        return pagedGridResult;
    }
}
