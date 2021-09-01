package org.pgstyle.mcrcon.log;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pgstyle.mcrcon.control.command.CommandEntry;

public final class Log {

    public static final Log out;

    private static final Map<String, Log> LOG_POOL;
    
    private static final Logger STDOUT_LOGGER;
    private static final Logger MASTER_LOGGER;
    private static final Logger COMMAND_LOGGER;
    private static final Logger PROMPT_LOGGER;

    private static final DateTimeFormatter DATETIME_FORMAT;

    static {
        LOG_POOL = new HashMap<>();
        out = Log.getLog(null);
        STDOUT_LOGGER  = LogManager.getLogger();
        MASTER_LOGGER  = LogManager.getLogger("master");
        COMMAND_LOGGER = LogManager.getLogger("command");
        PROMPT_LOGGER  = LogManager.getLogger("prompt");
        DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ");
    }

    public static Log getLog(String uci) {
        return Log.LOG_POOL.getOrDefault(uci, new Log(uci));
    }

    private static void debug(String component, String string, Object... args) {
        if (Log.STDOUT_LOGGER.isDebugEnabled()) {
            Log.STDOUT_LOGGER.debug(Log.stdout(null, component, string), args);
        }
        if (!Objects.isNull(component) && Log.MASTER_LOGGER.isDebugEnabled()) {
            Log.MASTER_LOGGER.debug(Log.master(component, string), args);
        }
    }

    private static void info(String component, String string, Object... args) {
        if (Log.STDOUT_LOGGER.isInfoEnabled()) {
            Log.STDOUT_LOGGER.info(Log.stdout(null, component, string), args);
        }
        if (!Objects.isNull(component) && Log.MASTER_LOGGER.isInfoEnabled()) {
            Log.MASTER_LOGGER.info(Log.master(component, string), args);
        }
    }

    private static void warn(String component, String string, Object... args) {
        if (Log.STDOUT_LOGGER.isWarnEnabled()) {
            Log.STDOUT_LOGGER.warn(Log.stdout("W", component, string), args);
        }
        if (!Objects.isNull(component) && Log.MASTER_LOGGER.isWarnEnabled()) {
            Log.MASTER_LOGGER.warn(Log.master(component, string), args);
        }
    }

    private static void error(String component, String string, Object... args) {
        if (Log.STDOUT_LOGGER.isErrorEnabled()) {
            Log.STDOUT_LOGGER.error(Log.stdout("E", component, string), args);
        }
        if (!Objects.isNull(component) && Log.MASTER_LOGGER.isErrorEnabled()) {
            Log.MASTER_LOGGER.error(Log.master(component, string), args);
        }
    }

    private static void fatal(String component, String string, Object... args) {
        if (Log.STDOUT_LOGGER.isFatalEnabled()) {
            Log.STDOUT_LOGGER.fatal(Log.stdout("F", component, string), args);
        }
        if (!Objects.isNull(component) && Log.MASTER_LOGGER.isFatalEnabled()) {
            Log.MASTER_LOGGER.fatal(Log.master(component, string), args);
        }
    }

    public static void prompt(String message, Object... args) {
        if (Log.PROMPT_LOGGER.isInfoEnabled()) {
            Log.PROMPT_LOGGER.info(message, args);
        }
    }

    public static void command(CommandEntry command) {
        Objects.requireNonNull(command, "command == null");
        Objects.requireNonNull(command.getStartTime(), "command#start == null");
        Objects.requireNonNull(command.getEndTime(), "command#end == null");
        if (Log.COMMAND_LOGGER.isInfoEnabled()) {
            Log.COMMAND_LOGGER.info("{}", command.getText());
            Log.COMMAND_LOGGER.info("  from {} to {}",
                                        Log.DATETIME_FORMAT.format(command.getStartTime()),
                                        Log.DATETIME_FORMAT.format(command.getEndTime()));
            if (!Objects.isNull(command.getArgs())) {
                Log.COMMAND_LOGGER.info("  with arguments: {}", command.getArgs());
            }
            if (!Objects.isNull(command.getResult())) {
                Log.COMMAND_LOGGER.info("  with result: {}", command.getResult());
            }
        }
    }

    private static String master(String component, String string) {
        StringBuilder builder = new StringBuilder("[");
        if (!Objects.isNull(component)) {
            builder.append(component);
        }
        else {
            builder.append("stdout");
        }
        return builder.append("] ").append(string).toString();
    }

    private static String stdout(String level, String component, String string) {
        StringBuilder builder = new StringBuilder();
        if (!Objects.isNull(component)) {
            builder.append('#').append(component);
            if (!Objects.isNull(level)) {
                builder.append(":").append(level);
            }
            builder.append("> ");
        }
        return builder.append(string).toString();
    }

    private Log(String uci) {
        this.uci = uci;
        Log.LOG_POOL.put(uci, this);
    }

    private String uci;


    public void debug(String string, Object... args) {
        Log.debug(this.uci, string, args);
    }

    public void info(String string, Object... args) {
        Log.info(this.uci, string, args);
    }

    public void warn(String string, Object... args) {
        Log.warn(this.uci, string, args);
    }

    public void error(String string, Object... args) {
        Log.error(this.uci, string, args);
    }

    public void fatal(String string, Object... args) {
        Log.fatal(this.uci, string, args);
    }

    public static void main(String[] args) {
        CommandEntry command = new CommandEntry("exit", null);
        command.setResult("connection terminated");
        command.setEndTime();
        Log.command(command);
        Log.fatal("rcon", "fatal");
        Log.error("rcon", "error");
        Log.warn("rcon", "warn");
        Log.info("rcon", "normal");
    }
    
}
