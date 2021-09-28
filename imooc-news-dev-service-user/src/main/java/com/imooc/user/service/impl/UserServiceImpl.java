package com.imooc.user.service.impl;

import com.imooc.enums.Sex;
import com.imooc.enums.UserStatus;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.bo.UpdateUserInfoBO;
import com.imooc.user.mapper.AppUserMapper;
import com.imooc.user.service.UserService;
import com.imooc.utils.DateUtil;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import org.n3r.idworker.Sid;
import com.imooc.utils.DesensitizationUtil;
import org.springframework.beans.BeanUtils;
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
    @Autowired
    public RedisOperator redis;

    public static final String REDIS_USER_INFO = "redis_user_info";

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

    /**
     * 用户修改信息，完善资料，并且激活
     * @param updateUserInfoBO
     */
    @Override
    public void updateUserInfo(UpdateUserInfoBO updateUserInfoBO) {
        AppUser appUser = new AppUser();
        BeanUtils.copyProperties(updateUserInfoBO,appUser);
        appUser.setUpdatedTime(new Date());
        appUser.setActiveStatus(UserStatus.ACTIVE.type);
        int result = appUserMapper.updateByPrimaryKeySelective(appUser);
        if (result!=1){
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }
//      再次查询用户的最新数据，放入Redis中
        String userId = updateUserInfoBO.getId();
        AppUser user = getUser(userId);
        redis.set(REDIS_USER_INFO+":"+userId, JsonUtils.objectToJson(user));
    }
}
