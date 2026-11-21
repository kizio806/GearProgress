package io.github.kizio806.gearprogress.adapter.storage.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class SingleWriterPersistenceExecutorTest {

  @Test
  void testSubmitTask() throws Exception {
    SingleWriterPersistenceExecutor executor = new SingleWriterPersistenceExecutor();
    var future = executor.submit(() -> 42);
    assertEquals(42, future.get(5, TimeUnit.SECONDS));

    AtomicInteger counter = new AtomicInteger();
    executor.run(counter::incrementAndGet).get(5, TimeUnit.SECONDS);
    assertEquals(1, counter.get());

    assertTrue(executor.shutdown(5, TimeUnit.SECONDS));
  }

  @Test
  void testSubmitExceptionalTask() throws Exception {
    SingleWriterPersistenceExecutor executor = new SingleWriterPersistenceExecutor();
    var future =
        executor.submit(
            () -> {
              throw new IllegalStateException("Test exception");
            });
    assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
    assertTrue(executor.shutdown(5, TimeUnit.SECONDS));
  }

  @Test
  void testQueueDrainsCompletelyOnShutdown() throws Exception {
    SingleWriterPersistenceExecutor executor = new SingleWriterPersistenceExecutor(2048);
    int taskCount = 100;
    AtomicInteger completed = new AtomicInteger();
    List<CompletableFuture<Void>> futures = new ArrayList<>(taskCount);

    CountDownLatch pauseLatch = new CountDownLatch(1);
    var pauseFuture =
        executor.run(
            () -> {
              try {
                assertTrue(pauseLatch.await(5, TimeUnit.SECONDS));
              } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
              }
            });

    for (int i = 0; i < taskCount; i++) {
      futures.add(executor.run(completed::incrementAndGet));
    }

    pauseLatch.countDown();
    assertTrue(executor.shutdown(10, TimeUnit.SECONDS));
    pauseFuture.get(5, TimeUnit.SECONDS);

    for (CompletableFuture<Void> future : futures) {
      future.get(5, TimeUnit.SECONDS);
    }
    assertEquals(taskCount, completed.get());
  }

  @Test
  void testSubmitAfterShutdownFails() throws Exception {
    SingleWriterPersistenceExecutor executor = new SingleWriterPersistenceExecutor();
    assertTrue(executor.shutdown(5, TimeUnit.SECONDS));

    CompletableFuture<Integer> future = executor.submit(() -> 99);
    ExecutionException ex =
        assertThrows(ExecutionException.class, () -> future.get(1, TimeUnit.SECONDS));
    assertTrue(ex.getCause() instanceof IllegalStateException);
    assertTrue(ex.getCause().getMessage().contains("Persistence writer is closed"));
  }

  @Test
  void testQueueCapacityRejection() throws Exception {
    SingleWriterPersistenceExecutor executor = new SingleWriterPersistenceExecutor(2);
    CountDownLatch blocker = new CountDownLatch(1);

    var blockerFuture =
        executor.run(
            () -> {
              try {
                assertTrue(blocker.await(5, TimeUnit.SECONDS));
              } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
              }
            });

    var queued1 = executor.submit(() -> 1);
    var queued2 = executor.submit(() -> 2);

    CompletableFuture<Integer> rejected = executor.submit(() -> 3);
    ExecutionException ex =
        assertThrows(ExecutionException.class, () -> rejected.get(1, TimeUnit.SECONDS));
    assertTrue(ex.getCause() instanceof IllegalStateException);
    assertTrue(ex.getCause().getMessage().contains("Persistence queue is full"));

    blocker.countDown();
    blockerFuture.get(5, TimeUnit.SECONDS);
    assertEquals(1, queued1.get(5, TimeUnit.SECONDS));
    assertEquals(2, queued2.get(5, TimeUnit.SECONDS));
    assertTrue(executor.shutdown(5, TimeUnit.SECONDS));
  }

  @Test
  void testConcurrentSubmissionsUnderStress() throws Exception {
    int threadCount = 4;
    int submissionsPerThread = 250;
    int total = threadCount * submissionsPerThread;

    SingleWriterPersistenceExecutor executor = new SingleWriterPersistenceExecutor(total + 100);
    AtomicInteger processed = new AtomicInteger();
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threadCount);

    for (int t = 0; t < threadCount; t++) {
      new Thread(
              () -> {
                try {
                  start.await();
                  for (int i = 0; i < submissionsPerThread; i++) {
                    var unused = executor.run(processed::incrementAndGet);
                  }
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                } finally {
                  done.countDown();
                }
              })
          .start();
    }

    start.countDown();
    assertTrue(done.await(10, TimeUnit.SECONDS));
    assertTrue(executor.shutdown(10, TimeUnit.SECONDS));
    assertEquals(total, processed.get());
  }
}
