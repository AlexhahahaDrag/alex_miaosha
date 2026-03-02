package com.alex.finance.contactsUser.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import com.alex.api.finance.contactsUser.vo.CheckContactsVo;
import com.alex.api.finance.contactsUser.vo.ContactsUserImportVo;
import com.alex.api.finance.contactsUser.vo.ContactsUserVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.string.StringUtils;
import com.alex.api.finance.contactsUserRelation.vo.ContactsUserRelationVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.contactsUser.entity.ContactsUser;
import com.alex.finance.contactsUser.mapper.ContactsUserMapper;
import com.alex.finance.contactsUser.service.ContactsUserService;
import com.alex.finance.contactsUserRelation.service.ContactsUserRelationService;
import com.alex.finance.handler.IExcelDictHandlerImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * description:  联系人信息表服务实现类
 * author:       alex
 * createDate:   2025-11-03 10:01:28
 * version:      1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactsUserServiceImpl extends ServiceImpl<ContactsUserMapper, ContactsUser> implements ContactsUserService {

    private final ContactsUserMapper contactsUserMapper;

    private final IExcelDictHandlerImpl iExcelDictHandler;

    private final ContactsUserRelationService contactsUserRelationService;

    private final UserUtils userUtils;

    /**
     * 电话号码正则表达式
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public Page<ContactsUserVo> getPage(Long pageNum, Long pageSize, ContactsUserVo contactsUserVo) {
        Page<ContactsUserVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return contactsUserMapper.getPage(page, contactsUserVo);
    }

    @Override
    public ContactsUserVo queryContactsUser(Long id) {
        if (id == null || id <= 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "查询联系人ID无效!");
        }
        return contactsUserMapper.queryContactsUser(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addContactsUser(ContactsUserVo contactsUserVo) {
        // 参数验证
        if (!validateContactsUser(contactsUserVo)) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "联系人信息验证失败!");
        }
        // 校验名称和电话不能重复
        validateContactsUserDuplicate(contactsUserVo, null);
        ContactsUser contactsUser = new ContactsUser();
        BeanUtils.copyProperties(contactsUserVo, contactsUser);
        contactsUserMapper.insert(contactsUser);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateContactsUser(ContactsUserVo contactsUserVo) {
        // 参数验证
        if (contactsUserVo.getId() == null || contactsUserVo.getId() <= 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "修改联系人ID无效!");
        }

        if (!validateContactsUser(contactsUserVo)) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "联系人信息验证失败!");
        }
        // 校验名称和电话不能重复（排除当前ID）
        validateContactsUserDuplicate(contactsUserVo, contactsUserVo.getId());
        ContactsUser contactsUser = new ContactsUser();
        BeanUtils.copyProperties(contactsUserVo, contactsUser);
        contactsUserMapper.updateById(contactsUser);
        log.info("修改联系人成功: id={}, name={}", contactsUser.getId(), contactsUser.getName());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteContactsUser(String ids) {
        if (StringUtils.isEmpty(ids)) {
            log.warn("删除联系人ID为空");
            return true;
        }

        List<String> idArr = Arrays.asList(ids.split(","));
        int deleteCount = contactsUserMapper.deleteBatchIds(idArr);

        log.info("删除联系人成功: 删除{}条记录", deleteCount);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importContactsUser(MultipartFile file) {
        log.info("导入联系人: fileName={}, fileSize={}", file.getOriginalFilename(), file.getSize());

        // 文件验证
        if (!validateFile(file)) {
            log.warn("文件验证失败: fileName={}", file.getOriginalFilename());
            return false;
        }

        // 使用 ContactsUserImportVo 解析中文列名的 Excel
        List<ContactsUserImportVo> excelInfo = getExcelInfo(file);
        if (excelInfo == null || excelInfo.isEmpty()) {
            log.warn("Excel文件内容为空");
            return true;
        }

        log.info("Excel读取数据成功: 共{}条记录", excelInfo.size());
        // 转换为 ContactsUserVo 并验证
        List<ContactsUserVo> validData = excelInfo.stream()
                .map(this::convertImportVoToVo)
                .filter(this::validateContactsUser)
                .toList();
        if (validData.size() < excelInfo.size()) {
            log.warn("部分数据验证失败: 有效{}条, 无效{}条", validData.size(), excelInfo.size() - validData.size());
        }
        if (validData.isEmpty()) {
            log.warn("没有有效的导入数据");
            return false;
        }
        // 查询当前人的联系人关系，并转换成名称为key的map
        TUserVo loginUser = userUtils.getLoginUser();
        Long userId = loginUser != null ? loginUser.getId() : null;
        List<ContactsUserRelationVo> relationList = contactsUserRelationService.queryEnabledRelationsByUser(userId);
        Map<String, ContactsUserRelationVo> relationshipMap = relationList.stream()
                .collect(Collectors.toMap(
                        ContactsUserRelationVo::getRelationshipTag,
                        relation -> relation,
                        (existing, replacement) -> existing // 如果有重复的key，保留第一个
                ));
        log.info("查询到关系分类数量: {}, 转换后的Map大小: {}", relationList.size(), relationshipMap.size());
        StringBuilder errorMessage = new StringBuilder();
        // 将导入文件转化为bean
        List<ContactsUser> contactsUserList = validData.parallelStream()
                .map(item -> {
                    ContactsUser contactsUser = new ContactsUser();
                    BeanUtils.copyProperties(item, contactsUser);
                    ContactsUserRelationVo relationship = relationshipMap.get(item.getRelationshipTag());
                    if (relationship != null) {
                        contactsUser.setRelationship(relationship.getId());
                    } else {
                        errorMessage.append("未找到对应的关系分类:").append(item.getRelationshipTag()).append(";");
                    }
                    return contactsUser;
                }).collect(Collectors.toList());
        if (!errorMessage.isEmpty()) {
            throw new SystemException(ResultEnum.PARAM_ERROR, errorMessage.toString());
        }
        this.saveBatch(contactsUserList);
        log.info("导入联系人成功: 导入{}条记录", contactsUserList.size());
        return true;
    }

    @Override
    public void downloadTemplate(javax.servlet.http.HttpServletResponse response) {
        log.info("下载联系人模版");
        try {
            // 直接下载预设的模版文件
            String templatePath = "templates/contacts_user_template.xlsx";
            ClassPathResource resource = new ClassPathResource(templatePath);

            if (!resource.exists()) {
                log.warn("模版文件不存在: path={}", templatePath);
                response.setStatus(404);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"404\",\"message\":\"模版文件不存在\"}");
                return;
            }

            // 设置响应头 - 必须在写入数据前设置
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=contacts_user_template.xlsx");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            // 设置文件长度，防止 Excel 文件打开报错
            response.setContentLength((int) resource.getFile().length());

            // 读取文件并写入响应流
            // 注意：不能在 try-with-resources 中关闭 response.getOutputStream()
            try (java.io.InputStream inputStream = resource.getInputStream()) {
                java.io.OutputStream outputStream = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
                log.info("下载联系人模版成功");
            }
        } catch (Exception e) {
            log.error("下载联系人模版失败", e);
            try {
                response.setStatus(500);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"500\",\"message\":\"下载模版失败: " + e.getMessage() + "\"}");
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * 文件验证
     *
     * @param file 上传的文件
     * @return 是否有效
     */
    private boolean validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            log.warn("上传文件为空");
            return false;
        }

        // 检查文件大小（最大10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            log.warn("文件过大: size={}MB", file.getSize() / (1024 * 1024));
            return false;
        }

        // 检查文件类型
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().matches(".*\\.(xlsx?|xls)$")) {
            log.warn("文件格式不正确: fileName={}", filename);
            return false;
        }

        return true;
    }

    /**
     * 联系人数据验证
     *
     * @param contactsUserVo 联系人信息
     * @return 是否有效
     */
    private boolean validateContactsUser(ContactsUserVo contactsUserVo) {
        // 验证必填字段
        if (contactsUserVo == null) {
            log.warn("联系人对象为空");
            return false;
        }
        if (StringUtils.isEmpty(contactsUserVo.getName())) {
            log.warn("联系人姓名为空");
            return false;
        }
        if (StringUtils.isEmpty(contactsUserVo.getPhone())) {
            log.warn("联系电话为空");
            return false;
        }
        // 验证姓名长度
        if (contactsUserVo.getName().length() > 100) {
            log.warn("联系人姓名过长: name={}", contactsUserVo.getName());
            return false;
        }
        // 验证电话格式
        if (!PHONE_PATTERN.matcher(contactsUserVo.getPhone()).matches()) {
            log.warn("电话格式不正确: phone={}", contactsUserVo.getPhone());
            return false;
        }
        // 验证邮箱格式（非必填）
        if (!StringUtils.isEmpty(contactsUserVo.getEmail()) &&
                !EMAIL_PATTERN.matcher(contactsUserVo.getEmail()).matches()) {
            log.warn("邮箱格式不正确: email={}", contactsUserVo.getEmail());
            return false;
        }
        // 验证地址长度
        if (contactsUserVo.getAddress() != null && contactsUserVo.getAddress().length() > 500) {
            log.warn("地址过长: address={}", contactsUserVo.getAddress());
            return false;
        }
        // 验证备注长度
        if (contactsUserVo.getRemarks() != null && contactsUserVo.getRemarks().length() > 1000) {
            log.warn("备注过长: remarks={}", contactsUserVo.getRemarks());
            return false;
        }
        return true;
    }

    /**
     * 验证联系人名称和电话是否重复
     *
     * @param contactsUserVo 联系人信息
     * @param currentId      当前联系人ID，用于排除自身
     */
    private void validateContactsUserDuplicate(ContactsUserVo contactsUserVo, Long currentId) {
        CheckContactsVo checkContactsVo = contactsUserMapper.checkDuplicate(
                contactsUserVo.getName(),
                contactsUserVo.getPhone(),
                currentId
        );
        if (checkContactsVo != null) {
            StringBuilder errorMessage = new StringBuilder();
            if (checkContactsVo.getNameCount() > 0) {
                errorMessage.append("联系人名称已存在: name=").append(contactsUserVo.getName()).append(";");
            }
            if (checkContactsVo.getPhoneCount() > 0) {
                errorMessage.append("联系人电话已存在: phone=").append(contactsUserVo.getPhone()).append(";");
            }
            if (!errorMessage.isEmpty()) {
                throw new SystemException(ResultEnum.PARAM_ERROR, errorMessage.toString());
            }
        }
    }

    /**
     * 解析 Excel 文件
     *
     * @param file 上传的文件
     * @return Excel 中的数据列表
     */
    private List<ContactsUserImportVo> getExcelInfo(MultipartFile file) {
        ExcelImportResult<ContactsUserImportVo> result;
        ImportParams importParams = new ImportParams();
        // 设置导入位置
        importParams.setHeadRows(1);
        // 设置首行
        importParams.setTitleRows(0);
        importParams.setStartRows(0);
        importParams.setStartSheetIndex(0);
        // 是否需要校验上传的 Excel
        importParams.setNeedVerify(false);
        // 告诉 easypoi 我们自定义的验证器
        importParams.setDictHandler(iExcelDictHandler);
        try {
            result = ExcelImportUtil.importExcelMore(file.getInputStream(), ContactsUserImportVo.class, importParams);
            log.info("Excel解析成功: 共{}条数据", result.getList().size());
            return result.getList();
        } catch (Exception e) {
            log.error("Excel解析失败", e);
            throw new RuntimeException("Excel文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 将 ContactsUserImportVo 转换为 ContactsUserVo
     *
     * @param importVo 导入的联系人信息
     * @return 转换后的联系人信息
     */
    private ContactsUserVo convertImportVoToVo(ContactsUserImportVo importVo) {
        ContactsUserVo contactsUserVo = new ContactsUserVo();
        BeanUtils.copyProperties(importVo, contactsUserVo);
        return contactsUserVo;
    }
}
