package soundcloud.server;

import java.util.concurrent.Callable;


public interface CancelableRunnable extends Runnable {

	Callable<Boolean> shutdown();

}
