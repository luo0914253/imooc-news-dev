package com.imooc.admin.service;

import com.imooc.pojo.AdminUser;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.utils.PagedGridResult;

public interface AdminUserService {
    /**
     * 获取管理员的用户信息
     * @param username
     * @return
     */
    public AdminUser queryAdminByUsername(String username);

    /**
     * 新增管理员
     * @param newAdminBO
     */
    public void createAdminUser(NewAdminBO newAdminBO);

    /**
     * 查询admin列表
     * @param page
     * @param pageSize
     */
    public PagedGridResult queryAdminList(Integer page, Integer pageSize);
}
