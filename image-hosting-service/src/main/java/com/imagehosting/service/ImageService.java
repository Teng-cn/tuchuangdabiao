package com.imagehosting.service;

import com.imagehosting.model.dto.ImageQueryDTO;
import com.imagehosting.model.vo.ImageUploadVO;
import com.imagehosting.model.vo.ImageVO;
import com.imagehosting.model.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片服务接口
 * 提供图片相关的业务逻辑操作
 * 包括图片上传、查询、删除等核心功能
 */
public interface ImageService {

    /**
     * 上传图片
     * 处理图片上传流程，包括图片验证、压缩、存储、元数据提取等
     *
     * @param file 用户上传的图片文件
     * @return 图片上传结果视图对象，包含图片URL和其他元信息
     * @throws com.imagehosting.common.exception.BusinessException 当文件格式不支持、超出大小限制或上传失败时抛出业务异常
     */
    ImageUploadVO uploadImage(MultipartFile file);

    /**
     * 分页查询图片列表
     * 根据查询条件获取当前用户上传的图片列表
     *
     * @param queryDTO 查询条件数据传输对象，包含页码、每页大小、关键字等
     * @return 分页结果视图对象，包含图片列表和分页信息
     * @throws com.imagehosting.common.exception.BusinessException 当用户未登录或查询参数无效时抛出业务异常
     */
    PageVO<ImageVO> getImageList(ImageQueryDTO queryDTO);

    /**
     * 获取图片详情
     * 查询指定ID的图片详细信息
     *
     * @param id 图片ID
     * @return 图片详情视图对象
     * @throws com.imagehosting.common.exception.BusinessException 当图片不存在或无权限访问时抛出业务异常
     */
    ImageVO getImageById(Long id);

    /**
     * 删除图片
     * 删除指定ID的图片，包括数据库记录和存储中的文件
     *
     * @param id 图片ID
     * @throws com.imagehosting.common.exception.BusinessException 当图片不存在、无权限删除或删除失败时抛出业务异常
     */
    void deleteImage(Long id);

    /**
     * 访问图片
     * 获取图片的访问URL，同时记录访问次数
     *
     * @param id 图片ID
     * @return 图片的访问URL
     * @throws com.imagehosting.common.exception.BusinessException 当图片不存在或已被删除时抛出业务异常
     */
    String accessImage(Long id);

    /**
     * 代理下载图片
     * 从指定URL下载图片并返回字节数组，解决CORS问题
     *
     * @param url 图片URL
     * @return 图片字节数组
     * @throws Exception 当下载或处理图片失败时抛出异常
     */
    byte[] proxyImage(String url) throws Exception;
} 