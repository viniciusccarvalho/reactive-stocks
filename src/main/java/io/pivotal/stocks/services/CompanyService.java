package io.pivotal.stocks.services;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import io.pivotal.stocks.domain.Company;
import io.pivotal.stocks.repository.CompanyRepository;
import org.apache.catalina.connector.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Vinicius Carvalho
 */
@Service
@RestController
public class CompanyService {

	@Autowired
	private CompanyRepository repository;

	private RestTemplate template = new RestTemplate();

	@Value("${quotes.endpoint.nasdaq}")
	private String remoteEndpoint;

	private ReentrantLock lock = new ReentrantLock();




	@RequestMapping(value = "/load", method = RequestMethod.POST)
	public ResponseEntity<String> loadDatabase(){
		Long start = System.currentTimeMillis();
		LinkedList<Company> companies = new LinkedList<>();
		try {
			lock.lock();

			if(repository.count() <= 0){

				ResponseEntity<String> response = template.getForEntity(remoteEndpoint, String.class);
				Scanner scanner = new Scanner(response.getBody());
				scanner.useDelimiter("\\n");
				scanner.nextLine();
				while(scanner.hasNextLine()){
					String line = scanner.nextLine();
					Company c = parseLine(line);
					companies.add(c);
				}
				repository.save(companies);
			}
		}
		catch (Exception e){
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.SERVICE_UNAVAILABLE);
		}
		finally {
			lock.unlock();
		}
		Long end = System.currentTimeMillis();
		return  new ResponseEntity<String>(String.format("Imported %d results in %d ms",companies.size(),end-start), HttpStatus.CREATED);
	}

	private Company parseLine(String line) {
		Company company = new Company();

		String[] contents = line.replaceAll("\\\"","").split(",");
		company.setSymbol(contents[0]);
		company.setName(contents[1]);
		String marketCap = contents[3];
		if(marketCap.startsWith("$")){
			Double value = Double.valueOf(marketCap.substring(1,marketCap.length()-1));
			String unity = marketCap.substring(marketCap.length()-1,marketCap.length());
			if("M".equalsIgnoreCase(unity)){
				company.setMarketCap(value*1000000);
			}else if("B".equalsIgnoreCase(unity)){
				company.setMarketCap(value*1000000000);
			}
		}
		try{
			company.setIpoYear(Integer.parseInt(contents[4]));
		}catch (NumberFormatException nfe){}
		company.setSector(contents[5]);
		company.setIndustry(contents[6]);
		company.setQuoteEndpoint(contents[7]);
		return company;
	}
}
