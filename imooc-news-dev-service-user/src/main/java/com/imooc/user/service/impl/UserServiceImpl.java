package com.imooc.user.service.impl;

import com.imooc.enums.Sex;
import com.imooc.enums.UserStatus;
import com.imooc.pojo.BO.AppUser;
import com.imooc.user.mapper.AppUserMapper;
import com.imooc.user.service.UserService;
import com.imooc.utils.DateUtil;
import org.n3r.idworker.Sid;
import com.imooc.utils.DesensitizationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private AppUserMapper appUserMapper;
    @Autowired
    private Sid sid;

    private static final String USER_FACE0 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxFw_8_qAIlFXAAAcIhVPdSg994.png";
    private static final String USER_FACE1 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUySASMbOAABBAXhjY0Y649.png";
    private static final String USER_FACE2 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUx6ANoEMAABTntpyjOo395.png";

    /**
     * 判断用户是否存在，如果存在返回user信息
     *
     * @param mobile
     * @return
     */
    @Override
    public AppUser queryMobileIsExist(String mobile) {
        Example userExample = new Example(AppUser.class);
        Example.Criteria userCriteria = userExample.createCriteria();
        userCriteria.andEqualTo("mobile",mobile);
        AppUser appUser = appUserMapper.selectOneByExample(userExample);
        return appUser;
    }

    /**
     * 创建用户
     *
     * @param mobile
     * @return
     */
    @Override
    public AppUser createUser(String mobile) {
        String id = sid.nextShort();
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setMobile(mobile);
        appUser.setNickname("用户:"+ DesensitizationUtil.commonDisplay(mobile));
        appUser.setFace(USER_FACE0);
        appUser.setBirthday(DateUtil.stringToDate("2000-01-01"));
        appUser.setSex(Sex.secret.type);
        appUser.setActiveStatus(UserStatus.INACTIVE.type);
        appUser.setTotalIncome(0);
        appUser.setCreatedTime(new Date());
        appUser.setUpdatedTime(new Date());
        appUserMapper.insert(appUser);
        return appUser;
    }

    /**
     * 根据用户主键查询用户信息
     * @param userId
     * @return
     */
    @Override
    public AppUser getUser(String userId) {
        return appUserMapper.selectByPrimaryKey(userId);
    }
}
