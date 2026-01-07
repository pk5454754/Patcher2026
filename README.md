## 介绍

Patcher2026是一款导出增量补丁文件的IDEA插件。

基于Layne666的代码改造而来，感谢：https://github.com/Layne666/mypatcher

## 下载

在IDEA的插件管理中搜索 Patcher2026

## 主要功能

1. 可以手动选择导出的修改过的编译文件或源码文件
2. 可以在Version Control中按修改日志导出的修改过的编译文件或源码文件
3. 可以手动选择文件或在Version Control中复制修改过的文件路径

## SVN 兼容性说明

### 旗舰版
- **SVN 集成状态**：旗舰版内置的 IntelliJ SVN 集成与插件存在兼容性问题
- **已知问题**：从 Version Control 面板的 SVN 变更列表中无法导出文件
- **临时解决方案**：推荐使用 Git 或手动选择文件方式导出

### 社区版
- **SVN 集成状态**：需要安装 SVN 插件并配置 SVN 命令行工具
- **配置步骤**：
  1. 打开 `File -> Settings -> Plugins`
  2. 搜索并安装 `Subversion Integration` 插件
  3. 安装 SVN 命令行工具（如 TortoiseSVN 或 CollabNet）
  4. 打开 `File -> Settings -> Version Control -> Subversion`
  5. 配置 `Use command line client`，指向 `svn.exe` 路径
- **使用状态**：配置完成后可以正常从 SVN 变更列表导出文件

### 推荐使用方式
- **Git 用户**：所有版本均可正常使用 Version Control 功能
- **SVN 用户（社区版）**：按上述步骤配置 SVN 插件
- **SVN 用户（旗舰版）**：建议使用手动选择文件方式导出

## 支持的 IDEA 版本

- 支持版本：IntelliJ IDEA 2022.3 - 2025.3
- 推荐版本：IntelliJ IDEA 2024.x 或更高版本

## 更新日志

1. 2026.01.06 修复线程安全问题，修复 "Read access is allowed from inside read-action only" 错误
2. 2026.01.02 修复idea的2025.1版本以上运行报错，无法导出class文件问题。
