package net.hytaledepot.templates.plugin.smoke;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class SmokePluginTemplate extends JavaPlugin {
  private final Map<String, String> runtimeState = new ConcurrentHashMap<>();
  private final AtomicLong heartbeatTicks = new AtomicLong();
  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "hd-smoke-heartbeat");
            thread.setDaemon(true);
            return thread;
          });

  private ScheduledFuture<?> heartbeatTask;

  public SmokePluginTemplate(JavaPluginInit init) {
    super(init);
  }

  @Override
  public CompletableFuture<Void> preLoad() {
    getLogger().atInfo().log("[SmokeTemplate] preLoad -> %s", getIdentifier());
    return CompletableFuture.completedFuture(null);
  }

  @Override
  protected void setup() {
    runtimeState.put("profile", "smoke-template");
    runtimeState.put("dataDirectory", getDataDirectory().toString());
    runtimeState.put("setupComplete", "true");
    getCommandRegistry().registerCommand(new SmokeStatusCommand());
  }

  @Override
  protected void start() {
    heartbeatTask =
        scheduler.scheduleAtFixedRate(
            () -> {
              long tick = heartbeatTicks.incrementAndGet();
              if (tick % 60 == 0) {
                getLogger().atInfo().log("[SmokeTemplate] heartbeat=%d", tick);
              }
            },
            0,
            1,
            TimeUnit.SECONDS);

    getTaskRegistry().registerTask(CompletableFuture.completedFuture(null));
  }

  @Override
  protected void shutdown() {
    if (heartbeatTask != null) {
      heartbeatTask.cancel(true);
    }
    scheduler.shutdownNow();
    runtimeState.clear();
  }

  public static String buildLicenseValidatePayload(String assetId, String licenseKey, String serverIp) {
    return SmokeLicenseContract.buildLicenseValidatePayload(assetId, licenseKey, serverIp);
  }

  public static boolean isLicenseAllowed(String responseJson) {
    return SmokeLicenseContract.isLicenseAllowed(responseJson);
  }

  private final class SmokeStatusCommand extends AbstractCommand {
    private SmokeStatusCommand() {
      super("hdsmokestatus", "Shows heartbeat and setup state for the smoke template.");
      setAllowsExtraArguments(true);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
      String output =
          "[SmokeTemplate] sender="
              + ctx.sender().getUsername()
              + ", heartbeatTicks="
              + heartbeatTicks.get()
              + ", setupComplete="
              + runtimeState.getOrDefault("setupComplete", "false")
              + ", dataDirectory="
              + runtimeState.getOrDefault("dataDirectory", "unset");
      ctx.sendMessage(Message.raw(output));
      return CompletableFuture.completedFuture(null);
    }
  }
}
