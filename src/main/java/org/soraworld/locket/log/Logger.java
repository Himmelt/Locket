package org.soraworld.locket.log;

import org.slf4j.LoggerFactory;

public final class Logger {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger("Locket");

    public static void info(String msg) {
        logger.info(msg);
    }
}
