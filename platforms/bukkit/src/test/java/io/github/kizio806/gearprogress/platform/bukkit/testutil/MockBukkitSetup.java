package io.github.kizio806.gearprogress.platform.bukkit.testutil;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/** Lightweight Bukkit server mock for platform testing. */
public final class MockBukkitSetup {

  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
  private static final Map<UUID, Player> ONLINE_PLAYERS = new ConcurrentHashMap<>();
  private static final Map<String, org.bukkit.plugin.Plugin> MOCK_PLUGINS =
      new ConcurrentHashMap<>();
  private static final Object INIT_LOCK = new Object();

  private MockBukkitSetup() {}

  public static void ensureInitialized() {
    synchronized (INIT_LOCK) {
      if (Bukkit.getServer() != null) {
        return;
      }

      ItemFactory itemFactory = createMockItemFactory();
      PluginManager pluginManager = createMockPluginManager();
      BukkitScheduler scheduler = createMockScheduler();
      ConsoleCommandSender consoleSender = createMockConsoleSender();

      Map<String, World> worlds = new ConcurrentHashMap<>();
      World defaultWorld = createMockWorld("world");
      worlds.put("world", defaultWorld);

      Server server =
          (Server)
              Proxy.newProxyInstance(
                  Server.class.getClassLoader(),
                  new Class<?>[] {Server.class},
                  (proxy, method, args) -> {
                    return switch (method.getName()) {
                      case "getItemFactory" -> itemFactory;
                      case "getPluginManager" -> pluginManager;
                      case "getScheduler" -> scheduler;
                      case "getConsoleSender" -> consoleSender;
                      case "getLogger" -> Logger.getLogger("MockServer");
                      case "getName" -> "MockServer";
                      case "getVersion" -> "1.21.11";
                      case "getMinecraftVersion" -> "1.21.11";
                      case "getBukkitVersion" -> "1.21.11-R0.1-SNAPSHOT";
                      case "getOnlinePlayers" -> List.copyOf(ONLINE_PLAYERS.values());
                      case "getWorlds" -> List.copyOf(worlds.values());
                      case "getWorld" ->
                          args != null && args.length > 0 && args[0] != null
                              ? worlds.computeIfAbsent(
                                  String.valueOf(args[0]), MockBukkitSetup::createMockWorld)
                              : null;
                      case "getAsyncScheduler",
                          "getRegionScheduler",
                          "getGlobalRegionScheduler" -> {
                        Class<?> returnType = method.getReturnType();
                        if (returnType.isInterface()) {
                          yield Proxy.newProxyInstance(
                              returnType.getClassLoader(),
                              new Class<?>[] {returnType},
                              (rp, rm, ra) -> {
                                if ("execute".equals(rm.getName()) && ra != null) {
                                  for (Object arg : ra) {
                                    if (arg instanceof Runnable r) {
                                      r.run();
                                      break;
                                    }
                                  }
                                  return null;
                                }
                                if (rm.getReturnType().isInterface()) {
                                  return Proxy.newProxyInstance(
                                      rm.getReturnType().getClassLoader(),
                                      new Class<?>[] {rm.getReturnType()},
                                      (tp, tm, ta) -> null);
                                }
                                if (boolean.class.equals(rm.getReturnType())
                                    || Boolean.class.equals(rm.getReturnType())) {
                                  return true;
                                }
                                return null;
                              });
                        }
                        yield null;
                      }
                      case "getRegistry" ->
                          (org.bukkit.Registry<?>)
                              Proxy.newProxyInstance(
                                  org.bukkit.Registry.class.getClassLoader(),
                                  new Class<?>[] {org.bukkit.Registry.class},
                                  (p, m, a) -> {
                                    if ("iterator".equals(m.getName()))
                                      return Collections.emptyIterator();
                                    if ("stream".equals(m.getName()))
                                      return java.util.stream.Stream.empty();
                                    return null;
                                  });
                      case "isPrimaryThread" -> true;
                      case "broadcast" -> 0;
                      default -> null;
                    };
                  });

      try {
        Bukkit.setServer(server);
        INITIALIZED.set(true);
      } catch (Exception ignored) {
        INITIALIZED.set(true);
      }
    }
  }

  private static ItemFactory createMockItemFactory() {
    return (ItemFactory)
        Proxy.newProxyInstance(
            ItemFactory.class.getClassLoader(),
            new Class<?>[] {ItemFactory.class},
            (proxy, method, args) -> {
              if ("getItemMeta".equals(method.getName())) {
                return createMockItemMeta();
              }
              if ("isApplicable".equals(method.getName())) {
                return true;
              }
              if ("equals".equals(method.getName())) {
                return args != null && args.length > 1 && Objects.equals(args[0], args[1]);
              }
              return null;
            });
  }

  private static ItemMeta createMockItemMeta() {
    Map<String, Object> metaData = new HashMap<>();
    List<String> lore = new ArrayList<>();

    return (ItemMeta)
        Proxy.newProxyInstance(
            ItemMeta.class.getClassLoader(),
            new Class<?>[] {ItemMeta.class, org.bukkit.inventory.meta.Damageable.class},
            (proxy, method, args) -> {
              return switch (method.getName()) {
                case "hasDamage" -> false;
                case "getDamage" -> 0;
                case "setDamage", "resetDamage" -> null;
                case "setDisplayName" -> {
                  metaData.put("displayName", args[0]);
                  yield null;
                }
                case "getDisplayName" -> (String) metaData.get("displayName");
                case "hasDisplayName" -> metaData.containsKey("displayName");
                case "setLore" -> {
                  lore.clear();
                  if (args[0] instanceof List<?> list) {
                    for (Object o : list) {
                      lore.add(String.valueOf(o));
                    }
                  }
                  yield null;
                }
                case "getLore" -> new ArrayList<>(lore);
                case "hasLore" -> !lore.isEmpty();
                case "clone" -> createMockItemMeta();
                default -> null;
              };
            });
  }

  private static PluginManager createMockPluginManager() {
    return (PluginManager)
        Proxy.newProxyInstance(
            PluginManager.class.getClassLoader(),
            new Class<?>[] {PluginManager.class},
            (proxy, method, args) -> {
              if ("isPluginEnabled".equals(method.getName())) {
                if (args != null && args.length > 0 && args[0] instanceof String n) {
                  org.bukkit.plugin.Plugin p = MOCK_PLUGINS.get(n);
                  return p != null && p.isEnabled();
                }
                return false;
              }
              if ("getPlugin".equals(method.getName())) {
                if (args != null && args.length > 0 && args[0] instanceof String n) {
                  return MOCK_PLUGINS.get(n);
                }
                return null;
              }
              return null;
            });
  }

  private static BukkitScheduler createMockScheduler() {
    return (BukkitScheduler)
        Proxy.newProxyInstance(
            BukkitScheduler.class.getClassLoader(),
            new Class<?>[] {BukkitScheduler.class},
            (proxy, method, args) -> {
              if ("runTask".equals(method.getName())) {
                if (args != null && args.length > 1 && args[1] instanceof Runnable r) {
                  r.run();
                }
                return createMockBukkitTask();
              }
              return createMockBukkitTask();
            });
  }

  private static BukkitTask createMockBukkitTask() {
    AtomicBoolean cancelled = new AtomicBoolean(false);
    return (BukkitTask)
        Proxy.newProxyInstance(
            BukkitTask.class.getClassLoader(),
            new Class<?>[] {BukkitTask.class},
            (proxy, method, args) -> {
              return switch (method.getName()) {
                case "cancel" -> {
                  cancelled.set(true);
                  yield null;
                }
                case "isCancelled" -> cancelled.get();
                case "getTaskId" -> 1;
                default -> null;
              };
            });
  }

  private static ConsoleCommandSender createMockConsoleSender() {
    return (ConsoleCommandSender)
        Proxy.newProxyInstance(
            ConsoleCommandSender.class.getClassLoader(),
            new Class<?>[] {ConsoleCommandSender.class},
            (proxy, method, args) -> {
              return switch (method.getName()) {
                case "getName" -> "CONSOLE";
                case "hasPermission", "isOp", "isPermissionSet" -> true;
                case "sendMessage" -> null;
                default -> null;
              };
            });
  }

  private static World createMockWorld(String name) {
    UUID worldId = UUID.randomUUID();
    return (World)
        Proxy.newProxyInstance(
            World.class.getClassLoader(),
            new Class<?>[] {World.class},
            (proxy, method, args) -> {
              return switch (method.getName()) {
                case "getName" -> name;
                case "getUID" -> worldId;
                case "getSpawnLocation" -> new Location((World) proxy, 0.0, 64.0, 0.0);
                default -> null;
              };
            });
  }
}
