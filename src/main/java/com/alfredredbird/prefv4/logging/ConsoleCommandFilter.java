package com.alfredredbird.prefv4.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Suppresses the vanilla "&lt;player&gt; issued server command: /pref ..."
 * console/log line for a configurable set of command labels. The command
 * itself still runs completely normally through Bukkit's dispatcher - this
 * only touches what gets printed to console and written to the log file.
 *
 * Vanilla prints that line from the network thread before Bukkit's
 * PlayerCommandPreprocessEvent even fires, so there's no supported plugin
 * API hook to stop it. Filtering it out at the root Log4j2 logger is the
 * standard approach (the same trick vanish plugins use to keep /vanish out
 * of logs).
 */
public class ConsoleCommandFilter extends AbstractFilter {

    private final Set<String> hiddenLabels = ConcurrentHashMap.newKeySet();
    private boolean registered = false;

    public void hide(String commandLabel) {
        hiddenLabels.add(commandLabel.toLowerCase(Locale.ROOT));
    }

    public void unhide(String commandLabel) {
        hiddenLabels.remove(commandLabel.toLowerCase(Locale.ROOT));
    }

    @Override
    public Result filter(LogEvent event) {
        return shouldHide(event.getMessage()) ? Result.DENY : Result.NEUTRAL;
    }

    private boolean shouldHide(Message message) {
        if (message == null || hiddenLabels.isEmpty()) return false;
        String text = message.getFormattedMessage();
        if (text == null) return false;

        final String marker = "issued server command: /";
        int idx = text.indexOf(marker);
        if (idx < 0) return false;

        String afterSlash = text.substring(idx + marker.length());
        String label = afterSlash.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        return hiddenLabels.contains(label);
    }

    public void register() {
        if (registered) return;
        LoggerConfig rootConfig = rootLoggerConfig();
        rootConfig.addFilter(this);
        refresh();
        registered = true;
    }

    public void unregister() {
        if (!registered) return;
        LoggerConfig rootConfig = rootLoggerConfig();
        rootConfig.removeFilter(this);
        refresh();
        registered = false;
    }

    private LoggerConfig rootLoggerConfig() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        return config.getRootLogger();
    }

    private void refresh() {
        ((LoggerContext) LogManager.getContext(false)).updateLoggers();
    }
}
