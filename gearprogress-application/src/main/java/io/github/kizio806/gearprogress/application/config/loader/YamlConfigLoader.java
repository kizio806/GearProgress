package io.github.kizio806.gearprogress.application.config.loader;

import io.github.kizio806.gearprogress.application.config.spec.CategoryConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.EquipmentConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.PluginConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.StorageConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.TierConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.TimersConfigSpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Loads and parses YAML configuration files into strongly-typed specification records. */
public final class YamlConfigLoader {

  private final Yaml yaml;

  public YamlConfigLoader() {
    LoaderOptions options = new LoaderOptions();
    this.yaml = new Yaml(new SafeConstructor(options));
  }

  /**
   * Parses raw YAML text into a generic map.
   *
   * @param content raw YAML string
   * @return parsed map
   */
  public Map<String, Object> parseMap(String content) {
    if (content == null || content.isBlank()) {
      return Collections.emptyMap();
    }
    Object loaded = yaml.load(content);
    if (loaded instanceof Map<?, ?> rawMap) {
      Map<String, Object> result = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
        if (entry.getKey() != null) {
          result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
      }
      return Collections.unmodifiableMap(result);
    }
    return Collections.emptyMap();
  }

  /**
   * Loads a YAML file from disk into a map.
   *
   * @param path file path
   * @return parsed map
   * @throws IOException on I/O failure
   */
  public Map<String, Object> loadFile(Path path) throws IOException {
    Objects.requireNonNull(path, "path must not be null");
    if (!Files.exists(path)) {
      return Collections.emptyMap();
    }
    String text = Files.readString(path, StandardCharsets.UTF_8);
    return parseMap(text);
  }

  /**
   * Loads a YAML stream from a class resource.
   *
   * @param resourcePath path to classpath resource
   * @return parsed map
   * @throws IOException on failure
   */
  public Map<String, Object> loadResource(String resourcePath) throws IOException {
    Objects.requireNonNull(resourcePath, "resourcePath must not be null");
    try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IOException("Missing resource: " + resourcePath);
      }
      return parseMap(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  /**
   * Parses an {@link EquipmentConfigSpec} from an equipment YAML map.
   *
   * @param root the root YAML map
   * @return compiled equipment spec
   */
  public EquipmentConfigSpec parseEquipmentSpec(Map<String, Object> root) {
    Objects.requireNonNull(root, "root must not be null");
    Map<String, CategoryConfigSpec> categories = new LinkedHashMap<>();

    for (Map.Entry<String, Object> categoryEntry : root.entrySet()) {
      String categoryName = categoryEntry.getKey();
      if ("schema-version".equalsIgnoreCase(categoryName)) {
        continue;
      }
      if (categoryEntry.getValue() instanceof Map<?, ?> rawTiersMap) {
        Map<String, TierConfigSpec> tiers = new LinkedHashMap<>();
        for (Map.Entry<?, ?> tierEntry : rawTiersMap.entrySet()) {
          String tierName = String.valueOf(tierEntry.getKey());
          if (tierEntry.getValue() instanceof Map<?, ?> rawTierData) {
            tiers.put(tierName, parseTierSpec(rawTierData));
          }
        }
        categories.put(categoryName, new CategoryConfigSpec(tiers));
      }
    }
    return new EquipmentConfigSpec(categories);
  }

  private TierConfigSpec parseTierSpec(Map<?, ?> raw) {
    Object unlockObj = raw.get("unlock-method");
    String unlockType = "always";
    String timerAfter = null;
    String dateValue = null;

    if (unlockObj instanceof String str) {
      unlockType = str;
    } else if (unlockObj instanceof Map<?, ?> map) {
      Object typeObj = map.get("type");
      if (typeObj != null) {
        unlockType = String.valueOf(typeObj);
      }
      Object afterObj = map.get("after");
      if (afterObj != null) {
        timerAfter = String.valueOf(afterObj);
      }
      Object dateObj = map.get("value");
      if (dateObj != null) {
        dateValue = String.valueOf(dateObj);
      }
    }

    boolean allowEnchantments = true;
    Object allowEnchObj = raw.get("allow-enchantments");
    if (allowEnchObj instanceof Boolean b) {
      allowEnchantments = b;
    }

    Map<String, Boolean> restrictions = new LinkedHashMap<>();
    Object restObj = raw.get("restrictions");
    if (restObj instanceof Map<?, ?> restMap) {
      for (Map.Entry<?, ?> rEntry : restMap.entrySet()) {
        String key = String.valueOf(rEntry.getKey());
        boolean val = Boolean.parseBoolean(String.valueOf(rEntry.getValue()));
        restrictions.put(key, val);
      }
    }

    return new TierConfigSpec(
        unlockType,
        Optional.ofNullable(timerAfter),
        Optional.ofNullable(dateValue),
        allowEnchantments,
        restrictions);
  }

  /**
   * Parses {@link PluginConfigSpec} from config.yml root map.
   *
   * @param root root YAML map
   * @return parsed plugin spec
   */
  public PluginConfigSpec parsePluginSpec(Map<String, Object> root) {
    String locale = String.valueOf(root.getOrDefault("locale", "en-US"));
    long interval = 20L;
    Object intObj = root.get("timer-check-interval-ticks");
    if (intObj instanceof Number num) {
      interval = num.longValue();
    }

    boolean broadcastUnlocks = true;
    boolean actionBar = true;
    boolean chatMsg = true;
    String sound = "entity.villager.no";

    Object notifObj = root.get("notifications");
    if (notifObj instanceof Map<?, ?> nMap) {
      if (nMap.get("broadcast-unlocks") instanceof Boolean b) {
        broadcastUnlocks = b;
      }
      if (nMap.get("action-bar-on-blocked") instanceof Boolean b) {
        actionBar = b;
      }
      if (nMap.get("chat-message-on-blocked") instanceof Boolean b) {
        chatMsg = b;
      }
      if (nMap.get("blocked-sound") != null) {
        sound = String.valueOf(nMap.get("blocked-sound"));
      }
    }

    boolean enableBypass = true;
    String bypassPerm = "gearprogress.bypass";
    Object bypassObj = root.get("bypass");
    if (bypassObj instanceof Map<?, ?> bMap) {
      if (bMap.get("enable-permission-bypass") instanceof Boolean b) {
        enableBypass = b;
      }
      if (bMap.get("permission") != null) {
        bypassPerm = String.valueOf(bMap.get("permission"));
      }
    }

    return new PluginConfigSpec(
        locale, interval, broadcastUnlocks, actionBar, chatMsg, sound, enableBypass, bypassPerm);
  }

  /**
   * Parses {@link TimersConfigSpec} from timers.yml root map.
   *
   * @param root root map
   * @param fallbackNow fallback anchor timestamp if missing
   * @return timers spec
   */
  public TimersConfigSpec parseTimersSpec(Map<String, Object> root, Instant fallbackNow) {
    Instant epoch = fallbackNow;
    Object epochObj = root.get("server-start-epoch");
    if (epochObj != null) {
      try {
        epoch = Instant.parse(String.valueOf(epochObj));
      } catch (java.time.format.DateTimeParseException ex) {
        epoch = fallbackNow;
      }
    }

    Map<String, Boolean> paused = new LinkedHashMap<>();
    Map<String, Long> accumulated = new LinkedHashMap<>();

    Object timersObj = root.get("timers");
    if (timersObj instanceof Map<?, ?> tMap) {
      for (Map.Entry<?, ?> entry : tMap.entrySet()) {
        String key = String.valueOf(entry.getKey());
        if (entry.getValue() instanceof Map<?, ?> details) {
          if (details.get("paused") instanceof Boolean p) {
            paused.put(key, p);
          }
          if (details.get("accumulated-seconds") instanceof Number num) {
            accumulated.put(key, num.longValue());
          }
        }
      }
    }

    return new TimersConfigSpec(epoch, paused, accumulated);
  }

  /**
   * Parses {@link StorageConfigSpec} from storage.yml root map.
   *
   * @param root root map
   * @return storage spec
   */
  public StorageConfigSpec parseStorageSpec(Map<String, Object> root) {
    Object storageObj = root.get("storage");
    if (storageObj instanceof Map<?, ?> sMap) {
      Object typeObj = sMap.get("type");
      String type = typeObj != null ? String.valueOf(typeObj) : "sqlite";
      Object fileObj = sMap.get("file");
      String file = fileObj != null ? String.valueOf(fileObj) : "data/gearprogress.db";
      int poolSize = 1;
      long timeout = 5000L;

      Object poolObj = sMap.get("pool");
      if (poolObj instanceof Map<?, ?> pMap) {
        if (pMap.get("maximum-pool-size") instanceof Number num) {
          poolSize = num.intValue();
        }
        if (pMap.get("connection-timeout-ms") instanceof Number num) {
          timeout = num.longValue();
        }
      }
      return new StorageConfigSpec(type, file, poolSize, timeout);
    }
    return StorageConfigSpec.defaultSqlite();
  }
}
