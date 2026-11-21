# Rule: Concurrency & Folia Thread Safety

## Principles
1. **Zero Blocking I/O on Tick Threads:** Never perform disk reads, database queries, or network calls inside Bukkit event handlers or tick loops.
2. **Folia Regionized Schedulers:** Folia executes world chunks on distinct thread pools. Code must not make assumptions about a global main thread.
3. **Task Scheduling Abstraction:** Use the platform scheduler SPI (`TaskSchedulerPort`) rather than direct `Bukkit.getScheduler()` calls inside application layers.
4. **Thread Serialization for Storage:** All database modifications must be queued through `SingleWriterPersistenceExecutor`.
5. **No Thread Starvation:** Asynchronous operations must use bounded pools or non-blocking futures with timeouts.
