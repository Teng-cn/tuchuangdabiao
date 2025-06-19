package com.imagehosting.dao;

import com.imagehosting.model.entity.Image;
import com.imagehosting.model.dto.ImageStatDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图片数据访问接口
 * 定义对图片表的CRUD操作和查询方法
 * 由MyBatis实现具体的SQL映射
 */
public interface ImageDao {

    /**
     * 插入图片
     * 将新上传的图片信息插入到数据库中
     *
     * @param image 图片实体对象
     * @return 影响行数，成功插入返回1
     */
    int insert(Image image);

    /**
     * 更新图片
     * 根据图片ID更新图片信息
     *
     * @param image 包含更新内容的图片对象
     * @return 影响行数，成功更新返回1
     */
    int update(Image image);

    /**
     * 删除图片（逻辑删除）
     * 将图片标记为已删除状态，不实际删除数据
     *
     * @param id 图片ID
     * @return 影响行数，成功删除返回1
     */
    int delete(Long id);

    /**
     * 根据ID查询图片
     * 查询指定ID的图片详细信息
     *
     * @param id 图片ID
     * @return 图片对象，不存在则返回null
     */
    Image findById(Long id);

    /**
     * 根据用户ID和MD5查询图片
     * 用于图片去重，检查用户是否已上传过相同内容的图片
     *
     * @param userId 用户ID
     * @param md5    图片文件的MD5值
     * @return 图片对象，不存在则返回null
     */
    Image findByUserIdAndMd5(@Param("userId") Long userId, @Param("md5") String md5);

    /**
     * 根据用户ID查询图片列表
     * 查询指定用户上传的所有图片，支持分页和排序
     *
     * @param userId    用户ID
     * @param keyword   关键字，可匹配图片名称等
     * @param offset    分页偏移量，从0开始
     * @param limit     每页记录数
     * @param sortField 排序字段
     * @param sortOrder 排序方式，asc或desc
     * @return 图片列表
     */
    List<Image> findByUserId(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 根据用户ID统计图片数量
     * 统计指定用户上传的图片总数
     *
     * @param userId  用户ID
     * @param keyword 关键字，可匹配图片名称等
     * @return 图片数量
     */
    Long countByUserId(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 增加访问次数
     * 图片被访问时，增加其访问计数器
     *
     * @param id 图片ID
     * @return 影响行数，成功更新返回1
     */
    int incrementAccessCount(Long id);
    
    /**
     * 查询所有图片列表（管理员用）
     * 管理员查询系统中的所有图片，可按用户筛选
     *
     * @param userId 用户ID（可选），为null时查询所有用户的图片
     * @param offset 分页偏移量，从0开始
     * @param limit  每页记录数
     * @return 图片列表
     */
    List<Image> findAllImages(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );
    
    /**
     * 统计所有图片数量（管理员用）
     * 统计系统中的所有图片数量，可按用户筛选
     *
     * @param userId 用户ID（可选），为null时统计所有用户的图片
     * @return 图片数量
     */
    Long countAllImages(@Param("userId") Long userId);
    
    /**
     * 统计总存储空间大小
     * 计算所有图片占用的存储空间总和
     *
     * @return 总存储空间大小（字节）
     */
    Long sumStorageSize();
    
    /**
     * 统计今日上传图片数量
     * 统计当天上传的图片数量
     *
     * @return 今日上传图片数量
     */
    Long countTodayUploaded();
    
    /**
     * 统计过去7天的每日上传数量
     * 按天统计最近一周每天的图片上传数量
     *
     * @return 每日统计数据列表，包含日期和数量
     */
    List<ImageStatDTO> countWeekStats();
    
    /**
     * 统计过去30天的每日上传数量
     * 按天统计最近一个月每天的图片上传数量
     *
     * @return 每日统计数据列表，包含日期和数量
     */
    List<ImageStatDTO> countMonthStats();
} 