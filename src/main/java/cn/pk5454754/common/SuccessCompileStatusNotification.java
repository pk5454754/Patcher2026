package cn.pk5454754.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import cn.pk5454754.util.ExceptionUtils;
import cn.pk5454754.util.PatcherUtil;

public class SuccessCompileStatusNotification implements CompileStatusNotification {
    private Consumer<CompileContext> consumer;
    private Runnable onComplete;

    public SuccessCompileStatusNotification(Consumer<CompileContext> consumer, Runnable onComplete) {
        this.consumer = consumer;
        this.onComplete = onComplete;
    }

    @Override
    public void finished(boolean aborted, int errors, int warnings, @NotNull CompileContext compileContext) {
        if (aborted) {
            PatcherUtil.showInfo("Code compilation has been aborted.", compileContext.getProject());
            invokeOnComplete();
            return;
        }
        if (errors != 0) {
            PatcherUtil.showError("Errors occurred while compiling code!", compileContext.getProject());
            invokeOnComplete();
            return;
        }
        // 在后台任务中执行慢操作，避免在 EDT 中执行
        Project project = compileContext.getProject();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Exporting Files...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    // 在 read-action 中执行 consumer，确保线程安全
                    ReadAction.run(() -> consumer.accept(compileContext));
                } catch (Exception e) {
                    e.printStackTrace();
                    PatcherUtil.showError(ExceptionUtils.getStructuredErrorString(e), project);
                }
            }

            @Override
            public void onSuccess() {
                // 任务成功完成后，在 EDT 中执行清理操作
                invokeOnComplete();
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                error.printStackTrace();
                PatcherUtil.showError(ExceptionUtils.getStructuredErrorString(error), project);
                invokeOnComplete();
            }
        });
    }

    private void invokeOnComplete() {
        if (onComplete != null) {
            ApplicationManager.getApplication().invokeLater(onComplete);
        }
    }
}
