package io.github.kizio806.gearprogress.platform.bukkit.lifecycle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BukkitLifecycleReporter Unit Tests")
class BukkitLifecycleReporterTest {

  private Plugin plugin;
  private Logger logger;
  private BukkitLifecycleReporter reporter;

  @BeforeEach
  void setUp() {
    plugin = mock(Plugin.class);
    logger = mock(Logger.class);
    when(plugin.getLogger()).thenReturn(logger);
    reporter = new BukkitLifecycleReporter(plugin);
  }

  @Test
  @DisplayName("Logs info, warning, and severe messages")
  void testLogging() {
    reporter.info("Info message");
    verify(logger).info("Info message");

    reporter.warning("Warning message");
    verify(logger).warning("Warning message");

    Exception ex = new RuntimeException("Test exception");
    reporter.severe("Severe message", ex);
    verify(logger).log(java.util.logging.Level.SEVERE, "Severe message", ex);
  }
}
