package io.github.kizio806.gearprogress.platform.paper.command;

import com.mojang.brigadier.CommandDispatcher;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.lang.reflect.Proxy;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

final class PaperCommandTestSupport {

  private PaperCommandTestSupport() {}

  static CommandSender createRecordingSender(List<Component> recordedMessages) {
    return (CommandSender)
        Proxy.newProxyInstance(
            CommandSender.class.getClassLoader(),
            new Class<?>[] {CommandSender.class},
            (p, m, a) -> {
              if ("sendMessage".equals(m.getName())) {
                if (a != null && a.length > 0 && a[0] instanceof Component c) {
                  recordedMessages.add(c);
                }
                return null;
              }
              if ("hasPermission".equals(m.getName())) {
                return true;
              }
              if ("getName".equals(m.getName())) {
                return "MockAdmin";
              }
              if ("isOp".equals(m.getName())) {
                return true;
              }
              return null;
            });
  }

  static CommandSourceStack createMockSource(CommandSender sender) {
    return (CommandSourceStack)
        Proxy.newProxyInstance(
            CommandSourceStack.class.getClassLoader(),
            new Class<?>[] {CommandSourceStack.class},
            (proxy, method, args) -> {
              if ("getSender".equals(method.getName())) {
                return sender;
              }
              return null;
            });
  }

  static CommandDispatcher<CommandSourceStack> createDispatcher() {
    return new CommandDispatcher<>();
  }
}
