package alexandregerault.mcrp;

import org.apache.logging.log4j.Logger;

public class Log4JLogger implements ILogger {
    private final Logger logger;

    public Log4JLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }
}
