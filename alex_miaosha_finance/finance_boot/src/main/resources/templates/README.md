# 联系人模版文件说明

## 文件位置

模版文件应该放在此目录下，文件名为：`contacts_user_template.xlsx`

## 模版文件结构

### Excel 文件信息

- **工作表名：** `联系人`（中文）
- **表头行：** 第 1 行（全部为中文列名）
- **数据行：** 第 2 行开始

### 中文列名对照表

| 列号 | 中文名 | 英文字段名   | 数据类型 | 必填 | 说明                                                                               |
| ---- | ------ | ------------ | -------- | ---- | ---------------------------------------------------------------------------------- |
| A    | 姓名   | name         | 文本     | 是   | 联系人姓名，最多 100 个字符                                                        |
| B    | 电话   | phone        | 文本     | 是   | 电话号码，格式：1[3-9]开头的 11 位数字（如：13812345678）                          |
| C    | 关系   | relationship | 文本     | 否   | 关系类型，可选值：friend（朋友）、family（家人）、colleague（同事）、other（其他） |
| D    | 邮箱   | email        | 文本     | 否   | 邮箱地址，格式需符合邮箱规范                                                       |
| E    | 地址   | address      | 文本     | 否   | 联系地址，最多 500 个字符                                                          |
| F    | 备注   | remarks      | 文本     | 否   | 备注信息，最多 1000 个字符                                                         |

## 模版示例

| 姓名 | 电话        | 关系      | 邮箱                 | 地址           | 备注     |
| ---- | ----------- | --------- | -------------------- | -------------- | -------- |
| 张三 | 13812345678 | friend    | zhangsan@example.com | 北京市朝阳区   | 高中同学 |
| 李四 | 13912345678 | family    | lisi@example.com     | 上海市浦东新区 | 表哥     |
| 王五 | 13712345678 | colleague | wangwu@example.com   | 深圳市南山区   | 项目经理 |

## 创建模版文件步骤

1. **使用 Excel 创建模版**

   - 打开 Microsoft Excel 或 WPS 等表格软件
   - 创建新工作簿
   - 第一行添加表头：姓名、电话、关系、邮箱、地址、备注
   - 保存为 `.xlsx` 格式

2. **使用 Python 脚本创建模版**

   ```python
   from openpyxl import Workbook
   from openpyxl.styles import Font, PatternFill, Alignment, Border, Side

   wb = Workbook()
   ws = wb.active
   ws.title = "联系人"

   # 设置中文表头
   headers = ["姓名", "电话", "关系", "邮箱", "地址", "备注"]
   ws.append(headers)

   # 设置表头样式
   header_fill = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
   header_font = Font(color="FFFFFF", bold=True)

   for cell in ws[1]:
       cell.fill = header_fill
       cell.font = header_font

   # 添加示例数据行（可选）
   ws.append(["示例姓名", "13812345678", "friend", "example@mail.com", "示例地址", "示例备注"])

   # 调整列宽
   ws.column_dimensions['A'].width = 15
   ws.column_dimensions['B'].width = 18
   ws.column_dimensions['C'].width = 18
   ws.column_dimensions['D'].width = 25
   ws.column_dimensions['E'].width = 25
   ws.column_dimensions['F'].width = 30

   # 保存文件
   wb.save('contacts_user_template.xlsx')
   ```

3. **将模版文件放置到正确位置**
   - 将 `contacts_user_template.xlsx` 文件复制到此目录
   - 文件路径应为：`src/main/resources/templates/contacts_user_template.xlsx`

## 后端导入处理

### AI Agent: 导入类结构

系统使用 `ContactsUserImportVo` 类来接收和解析 Excel 数据：

```java
@Getter
@Setter
@ApiModel(value = "ContactsUserImportVo", description = "联系人导入VO")
public class ContactsUserImportVo {
    @Excel(name = "姓名", orderNum = 1)
    private String name;

    @Excel(name = "电话", orderNum = 2)
    private String phone;

    @Excel(name = "关系", orderNum = 3)
    private String relationship;

    @Excel(name = "邮箱", orderNum = 4)
    private String email;

    @Excel(name = "地址", orderNum = 5)
    private String address;

    @Excel(name = "备注", orderNum = 6)
    private String remarks;
}
```

**工作流程：**

1. Excel 导入 → 使用 `@Excel(name="xxx")` 注解按中文名称自动映射列
2. 转换为 `ContactsUserVo` → 进行数据验证
3. 保存到数据库

## 验证规则

导入时，系统会对数据进行以下验证：

- ✅ 姓名不能为空，最多 100 个字符
- ✅ 电话号码不能为空，必须是 1[3-9] 开头的 11 位数字
- ✅ 关系类型如果填写，必须是指定的四种之一
- ✅ 邮箱格式必须符合规范（如果填写）
- ✅ 地址最多 500 个字符（如果填写）
- ✅ 备注最多 1000 个字符（如果填写）
- ✅ 姓名和电话不能重复

## 导入流程

1. 用户点击"下载模版"按钮，获得模版文件
2. 用户按照模版格式填充数据（注意列名必须是中文）
3. 用户点击"导入联系人"按钮，选择填充好的 Excel 文件
4. 后端使用 `ContactsUserImportVo` 按中文列名解析 Excel
5. 系统对每行数据进行验证
6. 验证通过的数据保存到数据库
7. 显示导入结果

## 注意事项

- ⚠️ **列名必须使用中文**，系统会按照中文列名 (姓名、电话、关系、邮箱、地址、备注) 来自动映射数据
- ⚠️ 每个导入的联系人名称和电话号码必须唯一（不能与已有数据重复）
- ⚠️ 不支持 `.xls` 格式，请使用 `.xlsx` 格式
- ⚠️ 单个文件最大 10MB
- ⚠️ 建议单次导入不超过 1000 条记录
- ⚠️ 确保使用最新版本的模版文件，列名顺序不能改变
