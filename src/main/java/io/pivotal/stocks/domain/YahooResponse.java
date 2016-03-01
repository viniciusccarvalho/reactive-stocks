package io.pivotal.stocks.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Vinicius Carvalho
 */
public class YahooResponse {

	private Query query;

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public static class Query {
		private Integer count;
		private Results results;

		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}

		public Results getResults() {
			return results;
		}

		public void setResults(Results results) {
			this.results = results;
		}
	}

	public static class Results {
		@JsonProperty("quote")
		private List<Quote> quotes;

		public List<Quote> getQuotes() {
			return quotes;
		}

		public void setQuotes(List<Quote> quotes) {
			this.quotes = quotes;
		}
	}
}


