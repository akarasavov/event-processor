package soundcloud.util;

import rx.Subscription;

/**
 * @author akt.
 */
public class RxUtil {

	public static void unsubscribe(Subscription subscription) {
		if (subscription != null && !subscription.isUnsubscribed()) {
			subscription.unsubscribe();
		}
	}
}
