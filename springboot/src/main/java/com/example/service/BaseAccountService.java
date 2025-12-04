package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 账户服务基类
 * 提供通用的账户操作方法，减少代码重复
 */
public abstract class BaseAccountService {

    /**
     * 获取角色枚举
     */
    protected abstract RoleEnum getRoleEnum();

    /**
     * 登录验证
     */
    public Account login(Account account, Account dbAccount) {
        if (ObjectUtil.isNull(dbAccount)) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        if (!account.getPassword().equals(dbAccount.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        
        // 生成token
        String tokenData = dbAccount.getId() + "-" + dbAccount.getRole();
        String token = TokenUtils.createToken(tokenData, dbAccount.getPassword());
        dbAccount.setToken(token);
        
        return dbAccount;
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account, Account dbAccount) {
        if (ObjectUtil.isNull(dbAccount)) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        if (!account.getPassword().equals(dbAccount.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        
        dbAccount.setPassword(account.getNewPassword());
        // 需要在具体服务类中实现更新操作
    }

    /**
     * 分页查询
     */
    public <T> PageInfo<T> selectPage(Integer pageNum, Integer pageSize, String name, List<T> list) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(list);
    }
}