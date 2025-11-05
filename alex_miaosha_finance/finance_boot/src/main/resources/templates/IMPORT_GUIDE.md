# 联系人导入完整指南（中文列名版）

## 🎯 概述

系统已升级为支持 **中文列名的 Excel 导入**，使得导入过程更加直观和用户友好。

---

## 📋 模版文件结构

### 中文列名对照表

| 序号 | 中文列名 | 数据类型 | 必填    | 示例值                        |
| ---- | -------- | -------- | ------- | ----------------------------- |
| 1    | **姓名** | 文本     | ✅ 必填 | 张三                          |
| 2    | **电话** | 文本     | ✅ 必填 | 13812345678                   |
| 3    | **关系** | 文本     | ❌ 可选 | friend/family/colleague/other |
| 4    | **邮箱** | 文本     | ❌ 可选 | zhangsan@example.com          |
| 5    | **地址** | 文本     | ❌ 可选 | 北京市朝阳区                  |
| 6    | **备注** | 文本     | ❌ 可选 | 高中同学                      |

### 完整示例

```
姓名    | 电话       | 关系      | 邮箱                | 地址         | 备注
--------|-----------|----------|-------------------|-------------|-------
张三    | 13812345678 | friend   | zhangsan@example.com | 北京市朝阳区 | 高中同学
李四    | 13912345678 | family   | lisi@example.com    | 上海市浦东新区 | 表哥
王五    | 13712345678 | colleague | wangwu@example.com | 深圳市南山区 | 项目经理
```

---

## 🔧 后端实现详解

### 核心导入类 - ContactsUserImportVo

**文件位置：** `finance_api/src/main/java/com/alex/api/finance/contactsUser/vo/ContactsUserImportVo.java`

```java
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ContactsUserImportVo", description = "联系人导入VO")
public class ContactsUserImportVo {
    // 使用 @Excel 注解按中文列名自动映射

    @Excel(name = "姓名")        // Excel 中的列名：姓名
    private String name;          // 映射到对象的 name 字段

    @Excel(name = "电话")        // Excel 中的列名：电话
    private String phone;         // 映射到对象的 phone 字段

    @Excel(name = "关系")        // Excel 中的列名：关系
    private String relationship;  // 映射到对象的 relationship 字段

    @Excel(name = "邮箱")        // Excel 中的列名：邮箱
    private String email;         // 映射到对象的 email 字段

    @Excel(name = "地址")        // Excel 中的列名：地址
    private String address;       // 映射到对象的 address 字段

    @Excel(name = "备注")        // Excel 中的列名：备注
    private String remarks;       // 映射到对象的 remarks 字段
}
```

**关键点：**

- ✅ 使用 `@Excel(name="xxx")` 注解指定列名（必须是中文）
- ✅ Easypoi 会自动按照中文列名从 Excel 中读取数据
- ✅ 字段名保持英文，便于后端处理

### 导入流程（在 ContactsUserServiceImpl）

```
Excel 文件（中文列名）
        ↓
使用 ContactsUserImportVo 解析
        ↓
通过 convertImportVoToVo 转换为 ContactsUserVo
        ↓
进行数据验证
        ↓
保存到数据库
```

### 关键方法

#### 1. getExcelInfo() - 解析 Excel

```java
private List<ContactsUserImportVo> getExcelInfo(MultipartFile file) throws Exception {
    ExcelImportResult<ContactsUserImportVo> result;
    ImportParams importParams = new ImportParams();

    // Easypoi 配置
    importParams.setHeadRows(1);           // 第1行是表头
    importParams.setStartRows(0);          // 从第0行开始

    // 使用 ContactsUserImportVo 按中文列名解析
    result = ExcelImportUtil.importExcelMore(
        file.getInputStream(),
        ContactsUserImportVo.class,        // 指定导入类
        importParams
    );
    return result.getList();
}
```

#### 2. convertImportVoToVo() - 转换对象

```java
private ContactsUserVo convertImportVoToVo(ContactsUserImportVo importVo) {
    ContactsUserVo contactsUserVo = new ContactsUserVo();
    // 复制所有字段（名称相同会自动映射）
    BeanUtils.copyProperties(importVo, contactsUserVo);
    return contactsUserVo;
}
```

#### 3. importContactsUser() - 完整导入流程

```java
@Override
public Boolean importContactsUser(MultipartFile file) throws Exception {
    // 步骤1：文件验证
    if (!validateFile(file)) {
        return false;
    }

    // 步骤2：使用 ContactsUserImportVo 解析 Excel
    List<ContactsUserImportVo> excelInfo = getExcelInfo(file);

    // 步骤3：转换为 ContactsUserVo
    List<ContactsUserVo> validData = excelInfo.stream()
            .map(this::convertImportVoToVo)  // 转换
            .filter(this::validateContactsUser)  // 验证
            .collect(Collectors.toList());

    // 步骤4：保存到数据库
    List<ContactsUser> contactsUserList = validData.stream()
            .map(item -> {
                ContactsUser contactsUser = new ContactsUser();
                BeanUtils.copyProperties(item, contactsUser);
                return contactsUser;
            })
            .collect(Collectors.toList());

    this.saveBatch(contactsUserList);
    return true;
}
```

---

## ✅ 验证规则

系统在导入时自动验证：

| 字段   | 验证规则                                                |
| ------ | ------------------------------------------------------- |
| 姓名   | 不能为空、最多 100 个字符                               |
| 电话   | 不能为空、必须是 1[3-9] 开头的 11 位数字                |
| 关系   | 如果填写，必须是：friend、family、colleague、other 之一 |
| 邮箱   | 格式必须符合邮箱规范（如果填写）                        |
| 地址   | 最多 500 个字符（如果填写）                             |
| 备注   | 最多 1000 个字符（如果填写）                            |
| 唯一性 | 姓名和电话不能与已有数据重复                            |

---

## 🔄 完整导入工作流

```
用户操作
   ↓
1. 点击"下载模版"按钮
   ↓
2. 获得 contacts_user_template.xlsx（中文列名）
   ↓
3. 填充数据（按照中文列名填写）
   ↓
4. 点击"导入联系人"按钮
   ↓
【后端处理】
   ↓
5. 接收 Excel 文件
   ↓
6. 使用 ContactsUserImportVo 按中文列名解析
   ↓
7. 验证数据
   ↓
8. 保存到数据库
   ↓
9. 返回导入结果
   ↓
用户收到反馈
```

---

## ⚠️ 重要注意事项

### 1. **列名必须使用中文**

- ✅ 正确：姓名、电话、关系、邮箱、地址、备注
- ❌ 错误：name、phone、relationship、email、address、remarks
- ❌ 错误：Name、Phone、Relationship、Email、Address、Remarks

### 2. **列的顺序可以改变**

- Easypoi 会根据 `@Excel(name="xxx")` 的列名来匹配，不依赖列的顺序
- 例如：可以把"邮箱"放在"电话"之前

### 3. **可以有多余的列**

- Excel 中可以有额外的列，系统会自动忽略
- 例如：可以添加"创建时间"、"来源"等列

### 4. **必填字段不能为空**

- "姓名"和"电话"是必填的，不能为空

### 5. **关系类型的正确值**

```
friend    - 朋友
family    - 家人
colleague - 同事
other     - 其他
```

---

## 📊 技术实现总结

| 组件                         | 作用                                  |
| ---------------------------- | ------------------------------------- |
| **ContactsUserImportVo**     | 接收 Excel 数据（中文列名）           |
| **@Excel 注解**              | 指定 Excel 列名与 Java 字段的映射关系 |
| **Easypoi**                  | 根据 @Excel 注解自动解析 Excel        |
| **BeanUtils.copyProperties** | 将 ImportVo 转换为 ContactsUserVo     |
| **validate 方法**            | 对转换后的数据进行业务验证            |

---

## 🎓 最佳实践

✅ **推荐做法：**

1. 使用提供的模版文件（确保列名准确）
2. 按照示例数据格式填写
3. 一次导入不超过 1000 条数据
4. 在导入前备份现有数据
5. 检查导入报告确认成功条数

❌ **不推荐做法：**

1. 手工修改列名
2. 改变列的顺序（虽然系统支持，但容易出错）
3. 一次导入超大文件
4. 导入包含重复数据
5. 跳过验证错误信息

---

## 📞 故障排查

| 问题             | 原因                       | 解决方案                                                        |
| ---------------- | -------------------------- | --------------------------------------------------------------- |
| "模版文件不存在" | 后端文件丢失               | 检查 `src/main/resources/templates/contacts_user_template.xlsx` |
| "列名不匹配"     | 使用了英文或错误的中文列名 | 下载最新模版，按照模版填写                                      |
| "电话格式不正确" | 电话号码不是 11 位数字     | 检查电话号码格式                                                |
| "姓名已存在"     | 导入的姓名与数据库重复     | 修改姓名或删除原有数据                                          |
| 导入后数据缺失   | 某些字段没有正确映射       | 确保列名完全正确（包括中文）                                    |

---

## 📝 常见问题

**Q: 为什么改成中文列名？**
A: 使用中文列名让非技术人员更容易理解和填写数据，提升用户体验。

**Q: 旧的英文列名模版还能用吗？**
A: 不能。系统现在只支持中文列名。请使用新的模版。

**Q: 可以自定义列名吗？**
A: 不可以。列名必须与后端 `@Excel` 注解中的名称完全匹配。

**Q: 如何修改关系类型的值？**
A: 在 `ContactsUserServiceImpl` 中的 `VALID_RELATIONSHIPS` 常量中修改。

**Q: 导入失败了怎么办？**
A: 检查错误日志，查看是否有数据验证失败的提示，按照提示修改数据。
