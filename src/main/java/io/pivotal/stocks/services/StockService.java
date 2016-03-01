package io.pivotal.stocks.services;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.pivotal.stocks.domain.Quote;
import io.pivotal.stocks.domain.YahooResponse;
import io.pivotal.stocks.repository.CompanyRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SchedulerGroup;
import reactor.core.publisher.TopicProcessor;
import reactor.core.subscriber.Subscribers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author Vinicius Carvalho
 */
@Service
public class StockService {

	private CompanyRepository companyRepository;

	private LoadingCache<String,List<String>> symbols;

	private RestTemplate restTemplate = new RestTemplate();

	private List<String> topSymbols;

	@Value("${quotes.endpoint.yahoo}")
	private String yahooEndpoint;

	@Value("${quotes.symbols.lines_to_load}")
	private int linesToLoad;

	@Autowired
	private GaugeService gaugeService;

	@Autowired
	private CounterService counterService;

	@Autowired
	private TopicProcessor<Quote> quotesTopic;

	@Autowired
	private SchedulerGroup io;

	@Autowired
	private SchedulerGroup async;



	public List<Quote> fetchQuotes(String delimitedSymbolString){
		List<Quote> quotes = null;
		Instant start = Instant.now();
		ResponseEntity<YahooResponse> response = restTemplate.getForEntity(yahooEndpoint,YahooResponse.class, Collections.singletonMap("symbol", delimitedSymbolString));
		Instant end = Instant.now();
		if(response.getStatusCode().is2xxSuccessful()){
			quotes = response.getBody().getQuery().getResults().getQuotes();
			if(quotes.isEmpty())
				System.out.println("Empty response from list: " + delimitedSymbolString);
		}else{
			throw new RuntimeException("Failed to connect to remote endpoint");
		}
		counterService.increment("yahoo.invocation");
		gaugeService.submit("yahoo.response_time",Duration.between(start,end).toMillis());
		return quotes;
	}
	private List<Quote> fakeQuotes(String delimitedSymbolString){
		String[] symbols = delimitedSymbolString.split(",");
		List<Quote> quotes = new ArrayList<>();
		for(String s : symbols){
			Quote quote = new Quote();
			quote.setSymbol(s);
			quotes.add(quote);
		}

		return quotes;
	}

	public List<Quote> fetchQuotes(List<String> symbols){
		return fetchQuotes(Joiner.on(",").join(symbols));
	}

	@Autowired
	public StockService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
		this.symbols = CacheBuilder.newBuilder().build(new CacheLoader<String, List<String>>() {
			public List<String> load(String s) throws Exception {
				return StreamSupport.stream(Spliterators.spliteratorUnknownSize(companyRepository.topSymbols().iterator(), Spliterator.ORDERED),false).map(company -> {return company.getSymbol();}).collect(Collectors.toList());
			}
		});
	}





	@PostConstruct
	public void setup() throws Exception {
		this.topSymbols = loadSymbols(linesToLoad);

		Flux.interval(30).log("interval " + Instant.now().toEpochMilli()).flatMap(time -> {
			return Flux.fromIterable(topSymbols).flatMap(symbols -> {
				return Mono.fromCallable(() -> {return fetchQuotes(symbols);}).publishOn(async).flatMap(Flux::fromIterable).log("mono "+Instant.now().toEpochMilli());
			});
		}).subscribe(Subscribers.consumer(quote -> {System.out.println(quote);}));

	}


	private List<String> loadSymbols(int howMany) throws Exception{
		List<String> symbols = new ArrayList<>();
		Scanner scanner = new Scanner(StockService.class.getClassLoader().getResourceAsStream("symbols.csv"));
		scanner.useDelimiter("\\n");
		while(scanner.hasNext() && symbols.size() < howMany){
			symbols.add(scanner.nextLine());
		}
		return symbols;
	}



	public List<String> getSymbols() {
		try {
			return symbols.get("all");
		}
		catch (ExecutionException e) {
			return null;
		}
	}

}
