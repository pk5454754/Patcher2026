package cn.pk5454754.common

import com.intellij.openapi.application.readActionBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

suspend fun <T> performReadAction(block: () -> T): T {
    return withContext(Dispatchers.Default) {
        // 使用协程版读操作 API 替代已弃用的 ReadAction.computeCancellable
        readActionBlocking { block() }
    }
}

object ReadActionCompat {
    @JvmStatic
    fun computeInReadAction(action: Runnable) {
        runBlocking {
            performReadAction { action.run() }
        }
    }

    @JvmStatic
    fun <T> computeInReadActionWithResult(block: () -> T): T {
        return runBlocking {
            performReadAction(block)
        }
    }
}
