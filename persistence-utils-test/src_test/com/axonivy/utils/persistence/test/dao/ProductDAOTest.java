package com.axonivy.utils.persistence.test.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.transaction.TransactionRolledbackException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.axonivy.utils.persistence.dao.CriteriaQueryGenericContext;
import com.axonivy.utils.persistence.dao.QuerySettings;
import com.axonivy.utils.persistence.dao.markers.AuditableMarker;
import com.axonivy.utils.persistence.daos.ProductDAO;
import com.axonivy.utils.persistence.entities.Product;
import com.axonivy.utils.persistence.entities.Product_;
import com.axonivy.utils.persistence.enums.ProductSearchField;
import com.axonivy.utils.persistence.search.SearchFilter;
import com.axonivy.utils.persistence.test.DemoTestBase;

import ch.ivyteam.ivy.environment.IvyTest;


@IvyTest
public class ProductDAOTest extends DemoTestBase {

	private static ProductDAO productDAO = new ProductDAO();

	@Test
	public void testSaveProduct() {
		Product product = productDAO.save(this.getProduct());
		Product copy = productDAO.findById(product.getId());

		assertThat(copy.getId()).as("Id of entry match").isEqualTo(product.getId());
		productDAO.delete(product);
	}

	@Test
	public void testSaveProductWithEmptyData() {
		Product product = new Product();
		product = productDAO.save(product);
		Product copy = productDAO.findById(product.getId());

		assertThat(copy.getId()).as("Id of entry match").isEqualTo(product.getId());
		productDAO.delete(product);
	}

	@Test
	public void testSaveProductWithNullData() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			Product product = null;
			product = productDAO.save(product);
		}, "NullPointerException was expected");
	}

	@Test
	public void testSaveProducts() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		int expect = products.size();
		int actual = productDAO.findAll().size();

		assertThat(actual).isEqualTo(expect);
		productDAO.deleteAll(products);
	}

	@Test
	public void testSaveProductsWithEmptyData() throws TransactionRolledbackException {
		List<Product> products = new ArrayList<>();
		products = productDAO.saveAll(products);

		int expect = products.size();
		int actual = productDAO.findAll().size();
		productDAO.deleteAll(products);

		assertThat(actual).isEqualTo(expect);
	}

	@Test
	public void testSaveProductsWithNullData() {
		Assertions.assertThrows(TransactionRolledbackException.class, () -> {
			List<Product> products = null;
			products = productDAO.saveAll(products);
		}, "TransactionRolledbackException was expected");
	}

	// find
	@Test
	public void testFindProduct() {
		Product product = productDAO.save(this.getProduct());
		Product copy = productDAO.find(product);

		assertThat(copy.getId()).as("Id of entry match").isEqualTo(product.getId());
		product = productDAO.delete(product);
	}

	@Test
	public void testFindProductWithNullData() {
		Product product = productDAO.find(null);
		assertThat(product).isNull();
	}

	@Test
	public void testFindProductWithEmptyData() {
		Product product = productDAO.find(new Product());
		assertThat(product).isNull();
	}

	@Test
	public void testFindAllProductWithNullData() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		int expect = products.size();
		int actual = productDAO.findAll(null).size();

		assertThat(actual).isEqualTo(expect);
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindAllProductWithEmptyData() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		int expect = products.size();
		int actual = productDAO.findAll(new QuerySettings<>()).size();
		assertThat(actual).isEqualTo(expect);
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindAllProductWithMarkerAll() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		List<Product> productsFindAll = productDAO
				.findAll(new QuerySettings<Product>().withMarkers(AuditableMarker.ALL));
		int actual = productsFindAll.size();
		assertThat(actual).isGreaterThan(0);
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindAllProductWithMarkerDelete() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		productDAO.deleteAll(products);
		List<Product> productsFindAll = productDAO
				.findAll(new QuerySettings<Product>().withMarkers(AuditableMarker.DELETED));
		int actual = productsFindAll.size();
		assertThat(actual).isGreaterThan(0);
	}

	@Test
	public void testFindAllProductWithMarkerActive() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		List<Product> productsFindAll = productDAO
				.findAll(new QuerySettings<Product>().withMarkers(AuditableMarker.ACTIVE));
		int actual = productsFindAll.size();
		assertThat(actual).isGreaterThan(0);
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindProductByIdInternal() {
		Product product = productDAO.save(this.getProduct());
		Product copy = productDAO.findById(product.getId());

		assertThat(copy.getId()).as("Id of entry match").isEqualTo(product.getId());
		productDAO.delete(product);
	}

	//Delete	
	@Test
	public void testDeleteProduct() {
		Product product = new Product();
		product.setName("Iphone 7s");
		product.setPrice(1300);
		product = productDAO.save(product);

		assertThat(productDAO.findById(product.getId())).as("Created product").isNotNull();
		product = productDAO.delete(product);
		assertThat(product.isDeleted()).as("Deleted product").isTrue();
	}

	@Test
	public void testDeleteProductWithEmptyData() {
		Product product = new Product();
		product = productDAO.save(product);
		assertThat(productDAO.findById(product.getId())).as("Created product").isNotNull();

		product = productDAO.delete(product);
		assertThat(product.isDeleted()).as("Deleted product").isTrue();
	}

	@Test
	public void testDeleteProductWithNullData() {
		Product actual = productDAO.delete(null);
		assertThat(actual).isNull();
	}

	// deleteAll	
	@Test
	public void testDeleteAllProduct() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		productDAO.deleteAll(products);
		int actual = productDAO.findAll().size();
		assertThat(actual).isEqualTo(0);
	}

	@Test
	public void testDeleteAllProductWithEmptyData() throws TransactionRolledbackException {
		List<Product> products = new ArrayList<>();
		products = productDAO.saveAll(products);
		productDAO.deleteAll(products);
		int actual = productDAO.findAll().size();
		assertThat(actual).isEqualTo(0);
		productDAO.deleteAll(products);
	}

	@Test
	public void testDeleteAllProductWithNullData() {
		productDAO.deleteAll(null);
	}

	//findBySearchFilter
	@Test
	public void testSearchFilterProduct() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter);

		assertThat(result).isNotEmpty();
		productDAO.deleteAll(products);
	}

	@Test
	public void testSearchFilterProductWithNullData() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			productDAO.findBySearchFilter(null);
		}, "NullPointerException was expected");
	}

	@Test
	public void testSearchFilterProductWithSpecificData() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT, "%Samsung");

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter);

		assertThat(result).isNotEmpty();
		productDAO.deleteAll(products);
	}

	// findBySearchFilter(searchFilter, querySettings)

	@Test
	public void testFindProductBySearchFilterWithQuerySettings() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter,
				new QuerySettings<Product>().withFirstResult(0).withMaxResults(2));

		assertThat(result).isNotEmpty();
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindEmptyProductWithQuerySettings() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter,
				new QuerySettings<Product>().withFirstResult(6).withMaxResults(5));

		productDAO.deleteAll(products);
		assertThat(result).isEmpty();
	}

	@Test
	public void testFindEmptyProductWithNullQuerySettings() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter, null);

		assertThat(result).isNotEmpty();
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindProductWithQuerySettings() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter, new QuerySettings<Product>().withFirstResult(0)
				.withMaxResults(2).withMarkers(AuditableMarker.ACTIVE).withOrderAttributes(Product_.price));

		assertThat(result).isNotEmpty();
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindEmptyProductWithEmptyQuerySettings() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter, new QuerySettings<Product>());

		assertThat(result).isNotEmpty();
		productDAO.deleteAll(products);
	}

	// countBySearchFilter

	@Test
	public void testCountBySearchFilter() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT, "%");

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter);
		Long count = productDAO.countBySearchFilter(searchFilter);

		assertThat(result).isNotNull();
		assertThat(result).as("Compare size to count").hasSize(count.intValue());
		productDAO.deleteAll(products);
	}

	@Test
	public void testCountBySearchFilterProductWithEmptyData() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter();
		Long actual = productDAO.countBySearchFilter(searchFilter);

		assertThat(products).hasSize(actual.intValue());
		productDAO.deleteAll(products);
	}

	@Test
	public void testCountBySearchFilterProductWithNullData() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			productDAO.countBySearchFilter(null);
		}, "NullPointerException was expected");
	}

	// FindByCriteria
	@Test
	public void testFindByCriteria() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		try (CriteriaQueryGenericContext<Product, Tuple> q = productDAO.initializeQuery(Product.class, Tuple.class)) {

			Expression<String> path = ProductDAO.getExpression(null, q.r, Product_.name);
			q.q.where(q.c.like(path, "%Samsung%"));
			q.q.multiselect(path);
			List<Tuple> tuples = productDAO.findByCriteria(q);

			assertThat(tuples).isNotEmpty();
		}

		productDAO.deleteAll(products);
	}

	@Test
	public void testCountByCriteria() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		try (CriteriaQueryGenericContext<Product, Tuple> q = productDAO.initializeQuery(Product.class, Tuple.class);
				CriteriaQueryGenericContext<Product, Long> countQueryContext = productDAO.initializeQuery(Product.class,
						Long.class);) {

			Expression<String> path = ProductDAO.getExpression(null, q.r, Product_.name);
			q.q.where(q.c.like(path, "%Samsung%"));
			q.q.multiselect(path);
			List<Tuple> tuples = productDAO.findByCriteria(q);

			countQueryContext.where(q.q.getRestriction());
			Long count = productDAO.countByCriteria(countQueryContext);

			assertThat(tuples).isNotNull();
			assertThat(tuples).as("Compare size to count").hasSize(count.intValue());
		}
		productDAO.deleteAll(products);
	}

	@Test
	public void testDeleteWithoutAuditing() throws TransactionRolledbackException {
		Product product = new Product();
		product.setName("Iphone 78s");
		product.setPrice(2300);

		product = productDAO.save(product);
		productDAO.evict(product);
		product = productDAO.deleteWithoutAuditing(product);
		assertThat(product.isDeleted()).as("Delete without auditing ").isTrue();

		productDAO.beginSession();
	}

	private List<Product> getProducts() {
		List<Product> products = new ArrayList<>();

		Product product1 = new Product();
		product1.setName("Iphone 7s");
		product1.setPrice(1300);

		Product product2 = new Product();
		product2.setName("Iphone 6s");
		product2.setPrice(1000);

		Product product3 = new Product();
		product3.setName("Samsung 6 plus");
		product3.setPrice(500);

		Product product4 = new Product();
		product3.setName("Samsung 8 plus");
		product3.setPrice(2000);

		products.add(product1);
		products.add(product2);
		products.add(product3);
		products.add(product4);

		return products;
	}

	private Product getProduct() {
		Product product = new Product();
		product.setName("Iphone 7s");
		product.setPrice(1300);

		return product;
	}

}
