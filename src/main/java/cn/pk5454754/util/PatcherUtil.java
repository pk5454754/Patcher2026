package cn.pk5454754.util;

import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

public class PatcherUtil {
    private static final String PLUGIN_NAME = "Patcher2026";
    private static final String NOTIFICATION_TITLE = "Patcher2026";
    private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(PLUGIN_NAME + "_log");
    private static final Pattern webPathPattern = Pattern.compile("(.+)/(webapp|WebRoot|web|webapps)/(.+)");

    public static PathResult getPathResult(Module module, ListModel<VirtualFile> selectedFiles, String pathPrefix,
                                           CompileContext compileContext, String webPath) {
        Project project = module.getProject();
        // 源码目录
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots();
        List<String> sourceRootPathList = new ArrayList<>(sourceRoots.length);
        for (VirtualFile sourceRoot : sourceRoots) {
            sourceRootPathList.add(sourceRoot.getPath());
        }
        PathResult pathResult = new PathResult();
        String contentRoot = moduleRootManager.getContentRoots()[0].getPath();
        for (int i = 0; i < selectedFiles.getSize(); i++) {
            VirtualFile element = selectedFiles.getElementAt(i);
            String elementName = element.getName();
            String elementPath = element.getPath();
            String fileType = element.getFileType().getName();

            if (compileContext == null) {
                String[] tmp = elementPath.split(contentRoot);
                if (tmp.length == 0) {
                    continue;
                }
                String outName = tmp[1];
                Path from = Paths.get(elementPath);
                Path to = Paths.get(pathPrefix + File.separator + outName);
                pathResult.put(from, to);
                continue;
            } else {
                String sourceRootPath = getSourceRootPath(sourceRootPathList, elementPath);
                if (sourceRootPath != null && !ProjectRootsUtil.isInTestSource(element, project)) {
                    // 编译输出目录
                    VirtualFile compilerOutputPath = compileContext.getModuleOutputDirectory(module);
                    if (compilerOutputPath == null) {
                        showInfo("The module (" + module.getName() + ") has no output directory!", project);
                        return new PathResult();
                    }
                    String compilerOutputUrl = compilerOutputPath.getPath();
                    String outName = elementPath.split(sourceRootPath)[1];
                    if ("java".equalsIgnoreCase(fileType)) {
                        outName = outName.replace("java", "");
                        String className = elementName.replace(".java", "");
                        String packageDir = outName.substring(0, outName.lastIndexOf("/") + 1);
                        String classLocation = compilerOutputUrl + packageDir;
                        // 针对一个Java文件编译出多个class文件的情况，如:Test$1.class
                        List<Path> fileList = FilesUtil.matchFiles("glob:**" + File.separator + className + "$*.class", classLocation);
                        // 添加本身class文件
                        fileList.add(Paths.get(classLocation + className + ".class"));
                        for (Path from : fileList) {
                            String toName = packageDir + from.getFileName().toString();
                            Path to = Paths.get(pathPrefix + webPath + File.separator + "classes" + toName);
                            pathResult.put(from, to);
                        }
                    } else {
                        Path from = Paths.get(getCompiledFilePath(compilerOutputUrl, outName));
                        Path to = Paths.get(pathPrefix + webPath + File.separator + "classes" + outName);
                        pathResult.put(from, to);
                    }
                    continue;
                }
            }
            Matcher webPathMatcher = webPathPattern.matcher(elementPath);
            if (webPathMatcher.find()) {
                Path from = Paths.get(elementPath);
                Path to = Paths.get(pathPrefix + webPathMatcher.group(3));
                pathResult.put(from, to);
            } else {
                pathResult.addUnsettled(elementPath);
            }
            try {
                // 一句话搞定：写入文件。如果文件不存在会自动创建；如果存在则覆盖
                Files.write(Paths.get("G:/log/log.txt"), pathResult.getFromTo().toString().getBytes());

                System.out.println("写入成功！");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pathResult;
    }
    // 在 PatcherUtil.java 中添加辅助方法
    private static String getCompiledFilePath(String compilerOutputUrl, String outName) {
        // 尝试从 classes 目录获取
        Path classPath = Paths.get(compilerOutputUrl + outName);
        if (Files.exists(classPath)) {
            return classPath.toString();
        }

        // 尝试从 resources 目录获取（将 classes 替换为 resources）
        String resourcePath = compilerOutputUrl.replace("classes/java/main", "resources/main") + outName;
        Path resourceFilePath = Paths.get(resourcePath);
        if (Files.exists(resourceFilePath)) {
            return resourceFilePath.toString();
        }

        // 都不存在，返回原始路径（让后续调用报错）
        return compilerOutputUrl + outName;
    }
    public static Module getModule(Module[] modules, AnActionEvent event) {
        // 如果只有一个模块，直接返回
        if (modules.length == 1) {
            return modules[0];
        }

        // 从事件中获取模块
        Module module = event.getData(LangDataKeys.MODULE);
        if (module != null) {
            return module;
        }

        // 获取选中的文件
        VirtualFile[] files = event.getData(LangDataKeys.VIRTUAL_FILE_ARRAY);
        if (files == null || files.length == 0 || isNotSameModule(files)) {
            return null;
        }

        // 获取项目
        Project project = event.getProject();
        if (project == null && modules.length > 0) {
            project = modules[0].getProject();
        }
        if (project == null) {
            return null;
        }

        // 使用 ProjectFileIndex 来查找文件所属的模块（这是官方推荐的方式）
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
        VirtualFile firstFile = files[0];
        Module foundModule = fileIndex.getModuleForFile(firstFile);

        return foundModule;
    }

    private static Pattern modulePattern = Pattern.compile("((.+)/(.+))/(src|WebRoot)/.*");

    public static boolean isNotSameModule(VirtualFile[] selectedFiles) {
        if (selectedFiles == null) {
            return false;
        }
        String moduleName = null;
        for (VirtualFile selectedFile : selectedFiles) {
            Matcher matcher = modulePattern.matcher(selectedFile.getPath());
            if (matcher.find()) {
                String newName = matcher.group(3);
                if (moduleName != null && !newName.equals(moduleName)) {
                    return true;
                }
                moduleName = newName;
            }
        }
        return false;
    }

    public static String getModuleDirectoryPath(VirtualFile[] selectedFiles) {
        if (selectedFiles == null) {
            return null;
        }
        for (VirtualFile selectedFile : selectedFiles) {
            Matcher matcher = modulePattern.matcher(selectedFile.getPath());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public static void showInfo(String content, Project project) {
        showNotification(content, NotificationType.INFORMATION, project);
    }

    public static void showError(String content, Project project) {
        showNotification(content, NotificationType.ERROR, project);
    }

    public static void showWarning(String content, Project project) {
        showNotification(content, NotificationType.WARNING, project);
    }

    private static void showNotification(String content, NotificationType type, Project project) {
        Notification notification = NOTIFICATION_GROUP.createNotification(
                NOTIFICATION_TITLE,
                content,
                type
        );
        Notifications.Bus.notify(notification, project);
    }

    private static String getSourceRootPath(List<String> sourceRootPathList, String elementPath) {
        for (String s : sourceRootPathList) {
            if (elementPath.contains(s)) {
                return s;
            }
        }
        return null;
    }

    public static void resolveVirtualFiles(VirtualFile[] data, List<VirtualFile> result) {
        if (data == null) {
            return;
        }
        for (VirtualFile virtualFile : data) {
            VfsUtilCore.visitChildrenRecursively(virtualFile, new VirtualFileVisitor() {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (!file.isDirectory()) {
                        result.add(file);
                    }
                    return super.visitFile(file);
                }
            });
        }
    }
}
