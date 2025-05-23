package executor;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(
                2, 4, 5, TimeUnit.SECONDS, 5, 1
        );

        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("Task " + taskId + " started.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task " + taskId + " finished.");
            });
        }

        // Перегрузка
        for (int i = 11; i <= 15; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("Heavy load task " + taskId + " started.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Heavy load task " + taskId + " finished.");
            });
        }

        try {
            Thread.sleep(3000);
            System.out.println("\nShutting down pool...\n");
            executor.shutdown();
            executor.waitForTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}