package org.pgstyle.mcrcon_deprecated.console;

import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Console {
    
    private static final Logger logger = LogManager.getLogger("rcon");

    public static void put(ConsoleFormat format, String message, Object... args) {
        String formatted = format.apply(message);
        logger.info(formatted, args);
    }
}
