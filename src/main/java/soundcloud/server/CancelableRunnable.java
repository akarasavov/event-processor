package soundcloud.server;

import java.util.concurrent.Callable;

/**
 * @author akt.
 */
public interface CancelableRunnable extends Runnable {

	Callable<Boolean> shutdown();

}
