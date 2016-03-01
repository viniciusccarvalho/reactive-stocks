package io.pivotal.stocks.services;

import java.util.List;

import io.pivotal.stocks.domain.Quote;

/**
 * @author Vinicius Carvalho
 */
public interface StockService {
	List<Quote> fetchQuotes(String delimitedSymbolString);

	List<Quote> fetchQuotes(List<String> symbols);
}
