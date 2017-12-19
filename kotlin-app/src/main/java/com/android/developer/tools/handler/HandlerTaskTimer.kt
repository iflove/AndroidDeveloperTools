package com.android.developer.tools.handler


import android.os.Handler
import android.os.Message
import com.android.developer.tools.MyApplication
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

//Handler 定时器 用于倒计时 、延迟执行、循环执行 任务
object HandlerTaskTimer {
    private val uiHandler: WeakHandler
    private val builderTagsMap = ConcurrentHashMap<String, Builder>()
    private const val RUNNING_WHAT = 0x0001

    private val callback = Handler.Callback { msg ->
        if (msg.obj != null && msg.obj is Builder) {
            (msg.obj as Builder).executeTask()
        }
        true
    }

    private enum class TaskType {
        COUNT_DOWN,
        LOOP_EXECUTE,
        DELAY_EXECUTE
    }

    init {
        uiHandler = WeakHandler(MyApplication.instance.mainLooper, callback)
    }

    fun newBuilder(): Builder {
        return Builder()
    }

    fun cancel(tag: String): HandlerTaskTimer {
        val builder = builderTagsMap[tag]
        builder?.cancel()
        return this
    }

    fun pause(tag: String): HandlerTaskTimer {
        val builder = builderTagsMap[tag]
        builder?.pause()
        return this
    }

    fun resume(tag: String): HandlerTaskTimer {
        val builder = builderTagsMap[tag]
        builder?.resume()
        return this
    }

    class Builder {
        private var initialDelay: Long = 0
        private var period: Long = 0
        private var takeWhile: Long = 0
        private lateinit var unit: TimeUnit
        private var longConsumer: ((aLong: Long) -> Unit)? = null
        private var action: (() -> Unit)? = null
        private lateinit var tag: String
        private var taskType: TaskType? = null
        // TODO: Task Running state

        /**
         * 一段时间后执行
         *
         * @param period
         * @param unit
         * @return
         */
        fun period(period: Long, unit: TimeUnit): Builder {
            this.period = period
            this.initialDelay = period
            this.unit = unit
            return this
        }

        /**
         * 首次延迟执行时间
         *
         * @param period
         * @param unit
         * @return
         */
        fun initialDelay(period: Long, unit: TimeUnit): Builder {
            this.initialDelay = period
            this.unit = unit
            return this
        }

        /**
         * @param period       一段时间后执行
         * @param initialDelay 首次延迟执行时间
         * @param unit
         * @return
         */
        fun period(period: Long, initialDelay: Long, unit: TimeUnit): Builder {
            this.period = period
            this.initialDelay = initialDelay
            this.unit = unit
            return this
        }

        /**
         * @param takeWhile 倒计时Count
         * @return
         */
        fun takeWhile(takeWhile: Long): Builder {
            this.takeWhile = takeWhile
            return this
        }

        fun tag(tag: String): Builder {
            this.tag = tag
            return this
        }

        /**
         * @param action 任务执行完成CallBack
         * @return
         */
        fun accept(action: () -> Unit): Builder {
            this.action = action
            return this
        }

        /**
         * @param longConsumer 倒计时回调
         * @return
         */
        fun accept(longConsumer: (aLong: Long) -> Unit): Builder {
            this.longConsumer = longConsumer
            return this
        }

        fun accept(longConsumer: (aLong: Long) -> Unit, action: () -> Unit): Builder {
            this.longConsumer = longConsumer
            this.action = action
            return this
        }

        /**
         * 倒计时模式任务
         */
        fun countDown(): Builder {
            this.taskType = TaskType.COUNT_DOWN
            return this
        }

        /**
         * 循环模式任务
         */
        fun loopExecute(): Builder {
            this.taskType = TaskType.LOOP_EXECUTE
            return this
        }

        /**
         * 延迟模式任务
         */
        fun delayExecute(): Builder {
            this.taskType = TaskType.DELAY_EXECUTE
            return this
        }

        /**
         * 暂停任务
         */
        fun pause() {
            uiHandler.removeCallbacksAndMessages(this)
        }

        /**
         * 恢复任务
         */
        fun resume() {
            if (!uiHandler.hasMessages(RUNNING_WHAT, this)) {
                uiHandler.sendMessageDelayed(obtainThis(), period)
                if (taskType == TaskType.COUNT_DOWN) {
                    acceptTakeWhile()
                }
            }
        }

        /**
         * 取消任务
         */
        fun cancel() {
            builderTagsMap.remove(tag)
            uiHandler.removeCallbacksAndMessages(this)
        }

        /**
         * 启动任务
         */
        fun start(): Builder {
            if (taskType == null) {
                return this
            }

            builderTagsMap.put(tag, this)
            this.initialDelay = Math.max(0L, unit.toMillis(initialDelay))
            this.period = Math.max(0L, unit.toMillis(period))
            this.takeWhile = Math.max(0L, takeWhile)
            when (taskType) {
                HandlerTaskTimer.TaskType.COUNT_DOWN -> {
                    if (longConsumer == null) {
                        return this
                    }
                    acceptTakeWhile()
                }
                HandlerTaskTimer.TaskType.LOOP_EXECUTE, HandlerTaskTimer.TaskType.DELAY_EXECUTE -> if (action == null) {
                    return this
                }
                else -> {
                }
            }
            uiHandler.sendMessageDelayed(obtainThis(), initialDelay)
            return this
        }

        fun obtainThis(): Message {
            val msg = Message.obtain()
            msg.what = RUNNING_WHAT
            msg.obj = this
            return msg
        }

        fun executeTask() {
            if (taskType == null) {
                return
            }
            when (taskType) {
                HandlerTaskTimer.TaskType.COUNT_DOWN -> performCountDown()
                HandlerTaskTimer.TaskType.LOOP_EXECUTE -> performLoopExecute()
                HandlerTaskTimer.TaskType.DELAY_EXECUTE -> {
                    performActionExecute()
                    cancel()
                }
                else -> {
                }
            }
        }

        private fun performCountDown() {
            if (longConsumer != null) {
                if (this@Builder.takeWhile > 0) {
                    this@Builder.takeWhile -= 1
                    acceptTakeWhile()
                    uiHandler.sendMessageDelayed(obtainThis(), period)
                    return
                }
                performActionExecute()
                cancel()
            }
        }

        private fun performLoopExecute() {
            if (action != null) {
                uiHandler.sendMessageDelayed(obtainThis(), period)
                performActionExecute()
            }
        }

        private fun performActionExecute() {
            if (action != null) {
                try {
                    action!!()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        private fun acceptTakeWhile() {
            try {
                longConsumer!!.invoke(this@Builder.takeWhile)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

}
