package com.imagehosting.dao;

import com.imagehosting.model.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 * 定义对用户表的CRUD操作和查询方法
 * 由MyBatis实现具体的SQL映射
 */
public interface UserDao {

    /**
     * 插入用户
     * 将新用户信息插入到数据库中
     *
     * @param user 用户实体对象
     * @return 影响行数，成功插入返回1
     */
    int insert(User user);

    /**
     * 更新用户
     * 根据用户ID更新用户信息
     *
     * @param user 包含更新内容的用户对象
     * @return 影响行数，成功更新返回1
     */
    int update(User user);

    /**
     * 删除用户
     * 根据用户ID删除用户记录
     *
     * @param id 用户ID
     * @return 影响行数，成功删除返回1
     */
    int delete(Long id);

    /**
     * 根据ID查询用户
     * 查询指定ID的用户详细信息
     *
     * @param id 用户ID
     * @return 用户对象，不存在则返回null
     */
    User findById(Long id);

    /**
     * 根据用户名查询用户
     * 用于用户登录验证和用户名唯一性检查
     *
     * @param username 用户名
     * @return 用户对象，不存在则返回null
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     * 用于邮箱验证和邮箱唯一性检查
     *
     * @param email 邮箱
     * @return 用户对象，不存在则返回null
     */
    User findByEmail(String email);

    /**
     * 分页查询用户列表
     * 支持多条件查询和排序，用于用户管理功能
     *
     * @param keyword   关键字，可匹配用户名、昵称、邮箱等
     * @param roleType  角色类型，可为null表示不限制角色
     * @param status    状态，可为null表示不限制状态
     * @param offset    分页偏移量，从0开始
     * @param limit     每页记录数
     * @param sortField 排序字段
     * @param sortOrder 排序方式，asc或desc
     * @return 用户列表
     */
    List<User> findList(
            @Param("keyword") String keyword,
            @Param("roleType") Integer roleType,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 查询用户总数
     * 根据查询条件统计符合条件的用户数量
     *
     * @param keyword  关键字，可匹配用户名、昵称、邮箱等
     * @param roleType 角色类型，可为null表示不限制角色
     * @param status   状态，可为null表示不限制状态
     * @return 符合条件的用户总数
     */
    Long count(
            @Param("keyword") String keyword,
            @Param("roleType") Integer roleType,
            @Param("status") Integer status
    );

    /**
     * 查询总用户数
     * 统计系统中的所有用户数量
     *
     * @return 总用户数
     */
    Long countTotal();

    /**
     * 查询今天新增用户数
     * 统计当天注册的新用户数量
     *
     * @return 今天新增用户数
     */
    Long countTodayNew();
} 