package io.pivotal.stocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SchedulerGroup;
import reactor.core.subscriber.Subscribers;

/**
 * @author Vinicius Carvalho
 */
public class ReactorPlaygroundTests {

	private SchedulerGroup async = SchedulerGroup.async();

	@Test
	public void intervalTest() throws Exception{

		final List<String> stocks = Arrays.asList("EMC","VMW","MSFT","AAPL");
		CountDownLatch latch = new CountDownLatch(20);
		Flux.interval(1).flatMap(time -> Flux.fromIterable(stocks)).dispatchOn(async).subscribe(Subscribers.consumer(stock -> {
			System.out.println(stock);
			latch.countDown();
		}));

		latch.await();

	}

}
