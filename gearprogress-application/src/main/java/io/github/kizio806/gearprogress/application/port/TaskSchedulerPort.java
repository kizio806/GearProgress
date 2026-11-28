package io.github.kizio806.gearprogress.application.port;

import java.time.Duration;

/**
 * Outbound SPI port abstracting platform-specific task scheduling (Paper/Folia async/region
 * schedulers).
 */
public interface TaskSchedulerPort {

  /**
   * Executes a task asynchronously once.
   *
   * @param task the runnable task
   */
  void runAsync(Runnable task);

  /**
   * Schedules a recurring asynchronous task.
   *
   * @param task the runnable task
   * @param initialDelay delay before first execution
   * @param period interval between subsequent executions
   * @return a cancellation handle
   */
  CancellableTask scheduleRepeatingAsync(Runnable task, Duration initialDelay, Duration period);

  /** Handle for cancelling a scheduled task. */
  @FunctionalInterface
  interface CancellableTask {
    /** Cancels the scheduled task. */
    void cancel();
  }
}
