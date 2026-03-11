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
  // Keeps simple runtime values so command output can show live plugin state.
  private final Map<String, String> runtimeState = new ConcurrentHashMap<>();
  // Counts heartbeat ticks for diagnostics and smoke testing.
  private final AtomicLong heartbeatTicks = new AtomicLong();
  // Dedicated scheduler for periodic plugin jobs.
  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "smoke-template-heartbeat");
            thread.setDaemon(true);
            return thread;
          });

  private ScheduledFuture<?> heartbeatTask;

  public SmokePluginTemplate(JavaPluginInit init) {
    super(init);
  }

  @Override
  public CompletableFuture<Void> preLoad() {
    getLogger().atInfo().log("[%s] preLoad -> %s", getClass().getSimpleName(), getIdentifier());
    return CompletableFuture.completedFuture(null);
  }

  @Override
  protected void setup() {
    // State initialization example using plugin metadata and data directory.
    runtimeState.put("template", "DevDocsSmoke");
    runtimeState.put("dataDirectory", getDataDirectory().toString());

    runtimeState.put("setupEventSeen", "true");

    // Command example: register one operational status command.
    getCommandRegistry().registerCommand(new SmokePluginTemplateStatusCommand());
  }

  @Override
  protected void start() {
    // Task example: periodic heartbeat with controlled logging cadence.
    heartbeatTask =
        scheduler.scheduleAtFixedRate(
            () -> {
              long tick = heartbeatTicks.incrementAndGet();
              if (tick % 60 == 0) {
                getLogger().atInfo().log("[DevDocsSmoke] heartbeat=%d", tick);
              }
            },
            0,
            1,
            TimeUnit.SECONDS);

    // Task registry example: register a startup-complete marker future.
    getTaskRegistry().registerTask(CompletableFuture.completedFuture(null));
  }

  @Override
  protected void shutdown() {
    // Always cancel scheduled work before clearing runtime state.
    if (heartbeatTask != null) {
      heartbeatTask.cancel(true);
    }
    scheduler.shutdownNow();
    runtimeState.clear();
  }

  // Build the same payload shape shown in the Dev Docs licensing section.
  public static String buildLicenseValidatePayload(String assetId, String licenseKey, String serverIp) {
    return SmokeLicenseContract.buildLicenseValidatePayload(assetId, licenseKey, serverIp);
  }

  // Lightweight parser used by tests to validate the response contract shape.
  public static boolean isLicenseAllowed(String responseJson) {
    return SmokeLicenseContract.isLicenseAllowed(responseJson);
  }


  private final class SmokePluginTemplateStatusCommand extends AbstractCommand {
    private SmokePluginTemplateStatusCommand() {
      super("hdsmokestatus", "DevDocsSmoke template status command");
    setAllowsExtraArguments(true);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
      // Command response example using structured runtime state.
      String output =
          "[DevDocsSmoke] sender="
              + ctx.sender().getDisplayName()
              + ", heartbeatTicks="
              + heartbeatTicks.get()
              + ", setupEventSeen="
              + runtimeState.getOrDefault("setupEventSeen", "false");
      ctx.sendMessage(Message.raw(output));
      return CompletableFuture.completedFuture(null);
    }
  }
}
