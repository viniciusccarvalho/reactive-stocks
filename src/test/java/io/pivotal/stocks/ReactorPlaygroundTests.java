package io.pivotal.stocks;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import io.pivotal.stocks.domain.Quote;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SchedulerGroup;
import reactor.core.subscriber.Subscribers;

/**
 * @author Vinicius Carvalho
 */
public class ReactorPlaygroundTests {

	private SchedulerGroup async = SchedulerGroup.async();
	private SchedulerGroup io = SchedulerGroup.io();

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


	@Test
	public void ioSchedulerTest() throws Exception {
		List<String> topSymbols = loadSymbols(5);
		Flux.interval(15).log("interval " + Instant.now().toEpochMilli()).flatMap(time -> {
			return Flux.fromIterable(topSymbols).flatMap(symbols -> {
				return Mono.fromCallable(() -> {return fetchQuotes(symbols);}).publishOn(io).flatMap(Flux::fromIterable).log("mono "+Instant.now().toEpochMilli());
			});
		}).subscribe(Subscribers.consumer(quote -> {System.out.println(quote);}));
		Thread.sleep(30000L);
	}


	private List<Quote> fetchQuotes(String delimitedSymbolString) throws Exception{

		List<Quote> quotes = new ArrayList<>();
		for(String s : delimitedSymbolString.split(",")){
			Quote quote = new Quote();
			quote.setSymbol(s);
			quotes.add(quote);
		}
		Thread.sleep((long)(2000+(Math.random()*2000)));

		return quotes;
	}






	private List<String> loadSymbols(int howMany) throws Exception{
		List<String> symbols = new ArrayList<>();
		Scanner scanner = new Scanner(ReactorPlaygroundTests.class.getClassLoader().getResourceAsStream("symbols.csv"));
		scanner.useDelimiter("\\n");
		while(scanner.hasNext() && symbols.size() < howMany){
			symbols.add(scanner.nextLine());
		}
		return symbols;
	}

}
