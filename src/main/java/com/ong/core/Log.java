package com.ong.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Custom java.util.logger wrapper class which logs all messages in file "ObviouslyNotDiscord.log". All methods are
 * executed only if variable DEBUG = true.
 */
public final class Log {
    private static final boolean DEBUG = true;
    private static Logger logger;

    /**
     * Initializes and sets the logger format to "[LEVEL] [DATE(yyyy-MM-dd)] [TIME(HH:MM:SS)]: [MESSAGE]"
     */
    public static void init() {
        if (DEBUG) {
            logger = Logger.getLogger("");
            System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$s] %1$tF %1$tT: %5$s%6$s%n");

            FileHandler fh = null;
            try {
                fh = new FileHandler("./ObviouslyNotDiscord.log", true);
            } catch (Exception e) {
                Log.error("Could not create file handler for Log class",e);
            }
            logger.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter();
            if (fh != null) {
                fh.setFormatter(formatter);
            }
        }
    }

    /**
     * Retrieves the name of the parent calling method and adds it to the message.
     * @param message
     */
    public static void info(String message) {
        if (DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
            logger.info(stackTraceElement.getClassName() + "/" + stackTraceElement.getMethodName() + ": " + message);
        }
    }

    /**
     * Retrieves the name of the parent calling method and adds it to the message.
     * @param message
     */
    public static void warning(String message) {
        if (DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
            logger.warning(stackTraceElement.getClassName() + "/" + stackTraceElement.getMethodName() + ": " + message);
        }
    }

    /**
     * Retrieves the name of the parent calling method and adds it to the Exception stack trace.
     * @param e
     */
    public static void error(Exception e) {
        if (DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
            String sStackTrace = "";
            if (e != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                sStackTrace = sw.toString();
            }
            logger.severe(stackTraceElement.getClassName() + "/" + stackTraceElement.getMethodName() + ": " + '\n' + sStackTrace);
        }
    }

    /**
     * Retrieves the name of the parent calling method and adds it to the message.
     * @param message
     */
    public static void error(String message) {
        if (DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
            logger.severe(stackTraceElement.getClassName() + "/" + stackTraceElement.getMethodName() + ": " + message);
        }
    }

    /**
     * Retrieves the name of the parent calling method and adds it to the message and the Exception stack trace.
     * @param message
     * @param e
     */
    public static void error(String message, Exception e) {
        if (DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
            String sStackTrace = "";
            if (e != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                sStackTrace = sw.toString();
            }
            logger.severe(stackTraceElement.getClassName() + "/" + stackTraceElement.getMethodName() + ": " + message + '\n' + sStackTrace);
        }
    }
}
