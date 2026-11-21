package io.github.kizio806.gearprogress.adapter.storage.sqlite;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dedicated single-threaded persistence executor ensuring serialized database writes in SQLite WAL
 * mode.
 */
public final class SingleWriterPersistenceExecutor {

  private final BlockingQueue<Runnable> queue;
  private final Thread workerThread;
  private final AtomicBoolean running = new AtomicBoolean(true);

  /** Functional interface for persistence tasks that may throw checked exceptions. */
  @FunctionalInterface
  public interface PersistenceTask {
    void execute() throws Exception;
  }

  public SingleWriterPersistenceExecutor() {
    this(2048);
  }

  public SingleWriterPersistenceExecutor(int capacity) {
    this.queue = new ArrayBlockingQueue<>(capacity);
    this.workerThread = new Thread(this::runLoop, "gearprogress-sqlite-writer");
    this.workerThread.setDaemon(true);
    this.workerThread.start();
  }

  /**
   * Submits a value-returning task to the single writer queue.
   *
   * @param <T> task result type
   * @param task callable task
   * @return CompletableFuture completing with task result
   */
  public <T> CompletableFuture<T> submit(Callable<T> task) {
    Objects.requireNonNull(task, "task must not be null");
    CompletableFuture<T> future = new CompletableFuture<>();
    if (!running.get()) {
      future.completeExceptionally(new IllegalStateException("Persistence writer is closed"));
      return future;
    }
    boolean queued =
        queue.offer(
            () -> {
              try {
                future.complete(task.call());
              } catch (Exception ex) {
                future.completeExceptionally(ex);
              }
            });
    if (!queued) {
      future.completeExceptionally(new IllegalStateException("Persistence queue is full"));
    }
    return future;
  }

  /**
   * Submits a runnable task to the single writer queue.
   *
   * @param task runnable task
   * @return CompletableFuture completing when task finishes
   */
  public CompletableFuture<Void> run(PersistenceTask task) {
    Objects.requireNonNull(task, "task must not be null");
    return submit(
        () -> {
          task.execute();
          return null;
        });
  }

  private void runLoop() {
    while (running.get() || !queue.isEmpty()) {
      try {
        Runnable task = queue.poll(100, TimeUnit.MILLISECONDS);
        if (task != null) {
          task.run();
        }
      } catch (InterruptedException ex) {
        if (running.get()) {
          Thread.currentThread().interrupt();
          break;
        }
      } catch (RuntimeException ignored) {
        // Prevent an uncaught runtime exception in a task from killing the writer thread
      }
    }
  }

  /**
   * Shuts down the executor and awaits termination of queued tasks.
   *
   * @param timeout duration to wait
   * @param unit time unit
   * @return true if cleanly terminated
   * @throws InterruptedException if interrupted while waiting
   */
  public boolean shutdown(long timeout, TimeUnit unit) throws InterruptedException {
    if (!running.compareAndSet(true, false)) {
      return true;
    }
    workerThread.interrupt();
    workerThread.join(unit.toMillis(timeout));
    return !workerThread.isAlive();
  }
}
