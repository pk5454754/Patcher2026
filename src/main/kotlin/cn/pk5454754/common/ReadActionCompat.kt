package cn.pk5454754.common

import com.intellij.openapi.application.ReadAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

suspend fun <T> performReadAction(block: () -> T): T {
    return withContext(Dispatchers.Default) {
        ReadAction.computeCancellable<T, Throwable> { block() }
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
