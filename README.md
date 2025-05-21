# 电子图片管理程序 (ImageManagementSystem)

## 项目简介

随着数码相机和智能手机的普及，用户往往需要管理大量的电子图片。本项目基于 JavaFX 实现了一个跨平台的桌面应用，能够快速浏览、预览、管理本地磁盘中的图片文件，并支持幻灯片播放和 EXIF 元数据查看。

## 功能特点

1. **目录树浏览**：
   - 只显示文件夹，不显示文件。
   - 支持多级展开、动态加载子目录。

2. **缩略图预览**：
   - 流式布局，自动换行。
   - 缩略图保持原始比例，支持滚动浏览。

3. **图片选择**：
   - 单选、Ctrl+点击多选、Shift+点击区间选。
   - 框选功能（可选），通过鼠标拖拽矩形实现批量选中。

4. **文件操作**：
   - **删除**：选中图片后删除，带确认提示。
   - **复制 & 粘贴**：支持跨目录粘贴，自动处理同名冲突。
   - **重命名**：单张图片重命名；多张批量重命名，支持自定义前缀 + 编号格式。

5. **搜索与排序**：
   - 实时关键词过滤（名称匹配）。
   - 支持按名称、大小、最后修改日期升序/降序排序。

6. **幻灯片播放**：
   - 双击任意缩略图进入幻灯片模式，或通过按钮切换。
   - 提供上一张、下一张、放大、缩小、自动播放（定时播放）等操作。

7. **键盘快捷键**：
   - Delete：删除选中图片。
   - Ctrl+C / Ctrl+V：复制 / 粘贴。
   - F2：重命名。
   - Ctrl+F：聚焦搜索框。

8. **EXIF 元数据查看**：
   - 右键菜单"查看元数据"，弹出对话框显示完整 EXIF 信息。

## 技术栈

- Java 21
- JavaFX 21 (Controls, FXML)
- FXML + CSS UI 分离
- Maven 构建管理
- [metadata-extractor](https://github.com/drewnoakes/metadata-extractor) 2.16.0 用于读取 EXIF 元数据
- SLF4J + slf4j-simple 简单日志

## 项目结构

```
ImageManagementSystem/
├── pom.xml                    # Maven 配置
├── README.md                  # 项目说明（本文件）
└── src/
    ├── main/
    │   ├── java/
    │   │   └── edu/scau/imagema nagementsystem/
    │   │       ├── controllers/  # UI 控制器：主界面、幻灯片、元数据对话框等
    │   │       ├── model/        # 数据模型：ImageFileItem、BatchRenameParams
    │   │       ├── services/     # 异步任务服务：文件发现、图片加载、文件操作、剪贴板
    │   │       └── utils/        # 工具类：FXML 加载、对话框、文件操作工具
    │   └── resources/
    │       └── edu/scau/imagema nagementsystem/
    │           ├── fxml/         # FXML 界面定义
    │           └── css/          # 样式表
    └── test/                     # 单元测试（JUnit）
```

## 快速开始

### 环境要求

- JDK 21 或更高版本
- Maven 3.8+ 及以上

### 克隆仓库

```bash
git clone https://github.com/your-repo/ImageManagementSystem.git
cd ImageManagementSystem
```
