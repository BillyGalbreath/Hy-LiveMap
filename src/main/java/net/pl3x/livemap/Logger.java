package net.pl3x.livemap;

import com.hypixel.hytale.logger.HytaleLogger;

import java.util.logging.Level;

public final class Logger {
    public static HytaleLogger logger() {
        return LiveMap.instance().getLogger();
    }

    public static void info(String msg) {
        logger().at(Level.INFO).log(msg);
    }

    public static void warning(String msg) {
        logger().at(Level.WARNING).log(msg);
    }

    public static void warning(String msg, Object... obj) {
        logger().at(Level.WARNING).log(msg, obj);
    }

    public static void severe(String msg) {
        logger().at(Level.SEVERE).log(msg);
    }

    public static void severe(String msg, Object... obj) {
        logger().at(Level.SEVERE).log(msg, obj);
    }
}
