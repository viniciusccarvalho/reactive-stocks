package io.pivotal.stocks.repository;

import java.util.Collection;

import io.pivotal.stocks.domain.Company;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

/**
 * @author Vinicius Carvalho
 */
@RepositoryRestResource(path = "companies", collectionResourceRel = "companies")
public interface CompanyRepository extends PagingAndSortingRepository<Company,String> {



	public Collection<Company> findByIndustryIn(@Param("industry") Collection<String> industries);

	public Collection<Company> findBySectorIn(@Param("sector") Collection<String> sectors);

	public Collection<Company> findBySymbolNotLike(@Param("symbol") String filter);

	@Query("select c from Company c order by c.marketCap desc")
	public Collection<Company> topSymbols();

	@Override
	@RestResource(exported = false)
	void delete(String id);

	@Override
	@RestResource(exported = false)
	void delete(Company entity);

	@Override
	@RestResource(exported = false)
	Company save(Company s);


}
