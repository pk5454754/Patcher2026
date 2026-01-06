package cn.pk5454754.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import cn.pk5454754.form.CvsChangeListDialog;
import cn.pk5454754.util.PatcherUtil;

public class CvsChangeListAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        VirtualFile[] selectedFiles = event.getData(LangDataKeys.VIRTUAL_FILE_ARRAY);
        if (selectedFiles == null || selectedFiles.length == 0) {
            PatcherUtil.showError("Please select at least one file!", event.getProject());
        } else if (PatcherUtil.isNotSameModule(selectedFiles)) {
            PatcherUtil.showWarning("Please select the module manually!", event.getProject());
            return;
        }
        // 在稍后的 EDT 循环中显示对话框，避免在线程上下文中执行
        ApplicationManager.getApplication().invokeLater(() -> {
            CvsChangeListDialog dialog = new CvsChangeListDialog(event);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            dialog.requestFocus();
        });
    }
}
