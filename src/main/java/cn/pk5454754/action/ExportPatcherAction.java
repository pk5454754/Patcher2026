package cn.pk5454754.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;

import cn.pk5454754.form.ExportPatcherDialog;
import cn.pk5454754.util.PatcherUtil;

public class ExportPatcherAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        VirtualFile[] selectedFiles = event.getData(LangDataKeys.VIRTUAL_FILE_ARRAY);
        if (selectedFiles == null || selectedFiles.length == 0) {
            PatcherUtil.showError("Please select at least one file!", event.getProject());
            return;
        } else if (PatcherUtil.isNotSameModule(selectedFiles)) {
            PatcherUtil.showWarning("Please select the module manually!", event.getProject());
            return;
        }

        // 在稍后的 EDT 循环中显示对话框，避免在线程上下文中执行
        ApplicationManager.getApplication().invokeLater(() -> {
            ExportPatcherDialog dialog = new ExportPatcherDialog(event);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            dialog.requestFocus();
        });
    }
}
