package com.android.developer.tools.handler;

import android.os.Handler;
import android.os.Message;


import com.android.developer.tools.MyApplication;
import com.android.developer.tools.interfaces.Action;
import com.android.developer.tools.interfaces.Consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

//Handler 定时器 用于倒计时 、延迟执行、循环执行 任务
public final class HandlerTaskTimer {
    private final WeakHandler uiHandler;
    private static HandlerTaskTimer instance;
    private Map<String, Builder> builderTagsMap = new ConcurrentHashMap<>();

    private final Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.obj != null && msg.obj instanceof Builder) {
                ((Builder) msg.obj).executeTask();
            }
            return true;
        }
    };

    private enum TaskType {
        COUNT_DOWN,
        LOOP_EXECUTE,
        DELAY_EXECUTE;
    }

    private HandlerTaskTimer() {
        uiHandler = new WeakHandler(MyApplication.instance.getMainLooper(), callback);
    }

    public static HandlerTaskTimer getInstance() {
        if (instance == null) {
            synchronized (HandlerTaskTimer.class) {
                if (instance == null) {
                    instance = new HandlerTaskTimer();
                }
            }
        }
        return instance;
    }

    public Builder newBuilder() {
        return new Builder();
    }

    public void cancel(String tag) {
        if (tag == null) {
            return;
        }
        Builder builder = builderTagsMap.get(tag);
        if (builder != null) {
            builder.cancel();
        }
    }

    public void pause(String tag) {
        if (tag == null) {
            return;
        }
        Builder builder = builderTagsMap.get(tag);
        if (builder != null) {
            builder.pause();
        }
    }

    public void resume(String tag) {
        if (tag == null) {
            return;
        }
        Builder builder = builderTagsMap.get(tag);
        if (builder != null) {
            builder.resume();
        }
    }

    public class Builder {
        long initialDelay;
        long period;
        long takeWhile;
        TimeUnit unit;
        Consumer<Long> longConsumer;
        private Action action;
        String tag;
        TaskType taskType;
        static final int RUNNING_WHAT = 0x0001;
        // TODO: Task Running state

        /**
         * 一段时间后执行
         *
         * @param period
         * @param unit
         * @return
         */
        public Builder period(long period, TimeUnit unit) {
            this.period = period;
            this.initialDelay = period;
            this.unit = unit;
            return this;
        }

        /**
         * 首次延迟执行时间
         *
         * @param period
         * @param unit
         * @return
         */
        public Builder initialDelay(long period, TimeUnit unit) {
            this.initialDelay = period;
            this.unit = unit;
            return this;
        }

        /**
         * @param period       一段时间后执行
         * @param initialDelay 首次延迟执行时间
         * @param unit
         * @return
         */
        public Builder period(long period, long initialDelay, TimeUnit unit) {
            this.period = period;
            this.initialDelay = initialDelay;
            this.unit = unit;
            return this;
        }

        /**
         * @param takeWhile 倒计时Count
         * @return
         */
        public Builder takeWhile(long takeWhile) {
            this.takeWhile = takeWhile;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        /**
         * @param action 任务执行完成CallBack
         * @return
         */
        public Builder accept(Action action) {
            this.action = action;
            return this;
        }

        /**
         * @param longConsumer 倒计时回调
         * @return
         */
        public Builder accept(Consumer<Long> longConsumer) {
            this.longConsumer = longConsumer;
            return this;
        }

        public Builder accept(Consumer<Long> longConsumer, Action action) {
            this.longConsumer = longConsumer;
            this.action = action;
            return this;
        }

        /**
         * 倒计时模式任务
         */
        public Builder countDown() {
            this.taskType = TaskType.COUNT_DOWN;
            return this;
        }

        /**
         * 循环模式任务
         */
        public Builder loopExecute() {
            this.taskType = TaskType.LOOP_EXECUTE;
            return this;
        }

        /**
         * 延迟模式任务
         */
        public Builder delayExecute() {
            this.taskType = TaskType.DELAY_EXECUTE;
            return this;
        }

        /**
         * 暂停任务
         */
        public void pause() {
            uiHandler.removeCallbacksAndMessages(this);
        }

        /**
         * 恢复任务
         */
        public void resume() {
            if (!uiHandler.hasMessages(RUNNING_WHAT, this)) {
                uiHandler.sendMessageDelayed(obtainThis(), period);
                if (taskType == TaskType.COUNT_DOWN) {
                    acceptTakeWhile();
                }
            }
        }

        /**
         * 取消任务
         */
        public void cancel() {
            if (tag != null) {
                builderTagsMap.remove(tag);
            }
            uiHandler.removeCallbacksAndMessages(this);
        }

        /**
         * 启动任务
         */
        public Builder start() {
            if (taskType == null) {
                return this;
            }
            if (tag == null) {
                return this;
            }
            builderTagsMap.put(tag, this);
            this.initialDelay = Math.max(0L, unit.toMillis(initialDelay));
            this.period = Math.max(0L, unit.toMillis(period));
            this.takeWhile = Math.max(0L, takeWhile);
            switch (taskType) {
                case COUNT_DOWN:
                    if (longConsumer == null) {
                        return this;
                    }
                    acceptTakeWhile();
                    break;
                case LOOP_EXECUTE:
                case DELAY_EXECUTE:
                    if (action == null) {
                        return this;
                    }
                    break;
                default:
                    break;
            }
            uiHandler.sendMessageDelayed(obtainThis(), initialDelay);
            return this;
        }

        public Message obtainThis() {
            Message msg = Message.obtain();
            msg.what = RUNNING_WHAT;
            msg.obj = this;
            return msg;
        }

        private void executeTask() {
            if (taskType == null) {
                return;
            }
            switch (taskType) {
                case COUNT_DOWN:
                    performCountDown();
                    break;
                case LOOP_EXECUTE:
                    performLoopExecute();
                    break;
                case DELAY_EXECUTE:
                    performActionExecute();
                    cancel();
                    break;
                default:
                    break;
            }
        }

        private void performCountDown() {
            if (longConsumer != null) {
                if (Builder.this.takeWhile > 0) {
                    Builder.this.takeWhile -= 1;
                    acceptTakeWhile();
                    uiHandler.sendMessageDelayed(obtainThis(), period);
                    return;
                }
                performActionExecute();
                cancel();
            }
        }

        private void performLoopExecute() {
            if (action != null) {
                uiHandler.sendMessageDelayed(obtainThis(), period);
                performActionExecute();
            }
        }

        private void performActionExecute() {
            if (action != null) {
                try {
                    action.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void acceptTakeWhile() {
            try {
                longConsumer.accept(Builder.this.takeWhile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
