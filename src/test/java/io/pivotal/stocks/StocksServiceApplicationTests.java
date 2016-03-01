package io.pivotal.stocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.base.Joiner;
import io.pivotal.stocks.domain.Quote;
import io.pivotal.stocks.domain.YahooResponse;
import io.pivotal.stocks.repository.CompanyRepository;
import io.pivotal.stocks.services.CompanyService;
import io.pivotal.stocks.services.StockService;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SchedulerGroup;
import reactor.core.subscriber.Subscribers;
import reactor.rx.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = StocksServiceApplication.class)
@WebAppConfiguration
public class StocksServiceApplicationTests {

	@Autowired
	private CompanyService service;

	@Autowired
	private StockService stockService;

	@Autowired
	private CompanyRepository repository;

	@Value("${quotes.endpoint.yahoo}")
	private String endpoint;

	private SchedulerGroup io = SchedulerGroup.io();

	@Test
	public void contextLoads() throws Exception{
		List<String> topFive = loadSymbols().subList(5,10);
		int total = 0;
		for(String s : topFive){
			total += s.split(",").length;
		}
		final CountDownLatch latch = new CountDownLatch(total);

		Flux.fromIterable(topFive).flatMap(symbols -> {
			return Mono.fromCallable(() -> {return  stockService.fetchQuotes(symbols);}).publishOn(io).flatMap(o -> {return Flux.fromIterable(o);});
		}).doOnError(throwable -> {throwable.printStackTrace();}).subscribe(Subscribers.consumer(quote -> {
			latch.countDown();
		}));

		latch.await();

	}


	private List<String> loadSymbols() throws Exception{
		List<String> symbols = new ArrayList<>();
		Scanner scanner = new Scanner(StocksServiceApplicationTests.class.getClassLoader().getResourceAsStream("symbols.csv"));
		scanner.useDelimiter("\\n");
		while(scanner.hasNext()){
			symbols.add(scanner.nextLine());
		}
		return symbols;
	}


}
