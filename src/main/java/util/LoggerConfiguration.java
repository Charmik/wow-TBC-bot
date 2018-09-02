package util;

import java.util.Iterator;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.slf4j.LoggerFactory;

/**
 * @author alexlovkov
 */
public class LoggerConfiguration {

    public void configure(String fraction) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (Logger logger : loggerContext.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                Appender<ILoggingEvent> appender = index.next();
                if (appender.getName().equals("FILE")) {
                    RollingFileAppender asyncAppender = (RollingFileAppender) appender;
                    asyncAppender.setFile("logs/" + fraction + "_bot.log");
                    asyncAppender.rollover();
                }
            }
        }
    }
}
