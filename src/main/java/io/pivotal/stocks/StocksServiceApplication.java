package io.pivotal.stocks;

import io.pivotal.stocks.domain.Quote;
import reactor.core.publisher.SchedulerGroup;
import reactor.core.publisher.TopicProcessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StocksServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StocksServiceApplication.class, args);
	}


	@Bean
	public SchedulerGroup io(){
		return SchedulerGroup.io();
	}

	@Bean
	public SchedulerGroup async(){
		return SchedulerGroup.async();
	}

	@Bean
	public TopicProcessor<Quote> quotesTopic(){
		return TopicProcessor.create();
	}


}
