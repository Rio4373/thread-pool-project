package executor;

import java.util.concurrent.BlockingQueue;

public class Worker extends Thread {
    private final BlockingQueue<Runnable> taskQueue;
    private final CustomThreadPoolExecutor customExecutor;

    public Worker(BlockingQueue<Runnable> taskQueue, String name, CustomThreadPoolExecutor executor) {
        super(name);
        this.taskQueue = taskQueue;
        this.customExecutor = executor;
        setDaemon(false);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Runnable task = taskQueue.poll(customExecutor.keepAliveTime, customExecutor.timeUnit);
                if (task == null) {
                    if (customExecutor.workers.size() > customExecutor.corePoolSize) {
                        System.out.println("[Worker] " + getName() + " idle timeout, stopping.");
                        break;
                    }
                    continue;
                }

                if (customExecutor.isShutdown) {
                    System.out.println("[Worker] " + getName() + " shutting down without executing task.");
                    break;
                }

                System.out.println("[Worker] " + getName() + " executes " + task);
                task.run();
            }
        } catch (InterruptedException e) {
            System.out.println("[Worker] " + getName() + " interrupted.");
        } finally {
            customExecutor.workers.remove(this);
            System.out.println("[Worker] " + getName() + " terminated.");
        }
    }
}