package io.pivotal.stocks;

import io.pivotal.stocks.repository.CompanyRepository;
import io.pivotal.stocks.services.CompanyService;
import io.pivotal.stocks.services.StockServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.SchedulerGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = StocksServiceApplication.class)
@WebAppConfiguration
public class StocksServiceApplicationTests {

	@Autowired
	private CompanyService service;

	@Autowired
	private StockServiceImpl stockServiceImpl;

	@Autowired
	private CompanyRepository repository;

	@Value("${quotes.endpoint.yahoo}")
	private String endpoint;

	private SchedulerGroup io = SchedulerGroup.io();

	@Test
	public void contextLoads() throws Exception{

	}





}
