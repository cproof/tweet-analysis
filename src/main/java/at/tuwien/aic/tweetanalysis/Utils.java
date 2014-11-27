package at.tuwien.aic.tweetanalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p/>
 * Date: 27.11.2014
 * Time: 14:24
 *
 * @author Stefan Victora
 */
public final class Utils {

    public static final Logger log = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    /**
     * Copied from the java {@link java.util.concurrent.ExecutorService} tutorial. Safe Shutdown for an pool.
     *
     * @param pool ExecutorService to shut down
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being canceled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    log.warn("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
