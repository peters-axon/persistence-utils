package com.axonivy.utils.persistence.test.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.transaction.TransactionRolledbackException;

import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axonivy.utils.persistence.dao.CriteriaQueryGenericContext;
import com.axonivy.utils.persistence.dao.QuerySettings;
import com.axonivy.utils.persistence.dao.markers.AuditableMarker;
import com.axonivy.utils.persistence.daos.ProductDAO;
import com.axonivy.utils.persistence.entities.Product;
import com.axonivy.utils.persistence.entities.Product_;
import com.axonivy.utils.persistence.enums.ProductSearchField;
import com.axonivy.utils.persistence.search.SearchFilter;
import com.axonivy.utils.persistence.test.DemoTestBase;


@RunWith(PowerMockRunner.class)
public class ProductDAOTest extends DemoTestBase {

	private static ProductDAO productDAO = new ProductDAO();

	@Test
	public void testSaveProduct() {

		Product product = productDAO.save(this.getProduct());
		Product copy = productDAO.findById(product.getId());

		assertEquals(product.getId(), copy.getId());
		productDAO.delete(product);

	}

	@Test
	public void testSaveProductWithEmptyData() {
		Product product = new Product();
		product = productDAO.save(product);
		Product copy = productDAO.findById(product.getId());

		assertEquals(product.getId(), copy.getId());
		productDAO.delete(product);
	}

	@Test(expected = NullPointerException.class)
	public void testSaveProductWithNullData() {
		Product product = null;
		product = productDAO.save(product);
	}

	@Test
	public void testSaveProducts() {
		try {
			List<Product> products = productDAO.saveAll(this.getProducts());
			int expect = products.size();
			int actual = productDAO.findAll().size();

			assertEquals(expect, actual);
			productDAO.deleteAll(products);
		} catch (NullPointerException e) {
			assertThat(e.getMessage(), is(IsNull.nullValue()));
		} catch (TransactionRolledbackException e) {
			assertThat(e.getMessage(), is(IsNull.nullValue()));
		}
	}

	@Test
	public void testSaveProductsWithEmptyData() {
		try {
			List<Product> products = new ArrayList<>();
			products = productDAO.saveAll(products);

			int expect = products.size();
			int actual = productDAO.findAll().size();
			productDAO.deleteAll(products);

			assertEquals(expect, actual);
		} catch (NullPointerException e) {
			assertThat(e.getMessage(), is(IsNull.nullValue()));
		} catch (TransactionRolledbackException e) {
			assertThat(e.getMessage(), is(IsNull.nullValue()));
		}
	}

	@Test
	public void testSaveProductsWithNullData() {
		List<Product> products = null;
		try {
			products = productDAO.saveAll(products);
		} catch (NullPointerException e) {
			System.out.println("NullPointerException: " + e.getMessage());
			assertThat(e.getMessage(), is(IsNull.nullValue()));

		} catch (TransactionRolledbackException e) {
			System.out.println("TransactionRolledbackException: " + e.getMessage());
			assertThat(e.getMessage(), is(IsNull.nullValue()));
		}
	}

	// find
	@Test
	public void testFindProduct() {

		Product product = productDAO.save(this.getProduct());
		Product copy = productDAO.find(product);

		assertEquals(product.getId(), copy.getId());
		product = productDAO.delete(product);

	}

	@Test
	public void testFindProductWithNullData() {
		Product product = productDAO.find(null);
		assertThat(product, is(IsNull.nullValue()));
	}

	@Test
	public void testFindProductWithEmptyData() {
		Product product = productDAO.find(new Product());
		assertThat(product, is(IsNull.nullValue()));
	}

	@Test
	public void testFindAllProductWithNullData() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		int expect = products.size();
		int actual = productDAO.findAll(null).size();

		assertEquals(expect, actual);
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindAllProductWithEmptyData() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		int expect = products.size();
		int actual = productDAO.findAll(new QuerySettings<>()).size();
		assertEquals(expect, actual);
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindAllProductWithMarkerAll() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		List<Product> productsFindAll = productDAO
				.findAll(new QuerySettings<Product>().withMarkers(AuditableMarker.ALL));
		int actual = productsFindAll.size();
		assertTrue(actual > 0);
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindAllProductWithMarkerDelete() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		productDAO.deleteAll(products);
		List<Product> productsFindAll = productDAO
				.findAll(new QuerySettings<Product>().withMarkers(AuditableMarker.DELETED));
		int actual = productsFindAll.size();
		assertTrue(actual > 0);
	}

	@Test
	public void testFindAllProductWithMarkerActive() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		List<Product> productsFindAll = productDAO
				.findAll(new QuerySettings<Product>().withMarkers(AuditableMarker.ACTIVE));
		int actual = productsFindAll.size();
		assertTrue(actual > 0);
		productDAO.deleteAll(products);
	}

	@Test
	public void testFindProductByIdInternal() {

		Product product = productDAO.save(this.getProduct());
		Product copy = productDAO.findById(product.getId());

		assertEquals(product.getId(), copy.getId());
		productDAO.delete(product);

	}

	//Delete	
	@Test
	public void testDeleteProduct() {
		Product product = new Product();
		product.setName("Iphone 7s");
		product.setPrice(1300);
		product = productDAO.save(product);

		assertNotNull("Created product", productDAO.findById(product.getId()));
		product = productDAO.delete(product);
		assertTrue("Deleted document", product.isDeleted());
	}

	@Test
	public void testDeleteProductWithEmptyData() {
		Product product = new Product();
		product = productDAO.save(product);
		assertNotNull("Created product", productDAO.findById(product.getId()));

		product = productDAO.delete(product);
		assertTrue("Deleted document", product.isDeleted());
	}

	@Test
	public void testDeleteProductWithNullData() {
		Product actual = productDAO.delete(null);
		assertThat(actual, is(IsNull.nullValue()));
	}

	// deleteAll	
	@Test
	public void testDeleteAllProduct() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());
		productDAO.deleteAll(products);
		int actual = productDAO.findAll().size();
		assertEquals(0, actual);
	}

	@Test
	public void testDeleteAllProductWithEmptyData() {
		try {

			List<Product> products = new ArrayList<>();
			products = productDAO.saveAll(products);
			productDAO.deleteAll(products);
			int actual = productDAO.findAll().size();
			assertEquals(0, actual);
			productDAO.deleteAll(products);
		} catch (NullPointerException e) {
			assertThat(e.getMessage(), is(IsNull.nullValue()));
		} catch (TransactionRolledbackException e) {
			assertThat(e.getMessage(), is(IsNull.nullValue()));
		}
	}

	@Test
	public void testDeleteAllProductWithNullData() {
		try {
			productDAO.deleteAll(null);
		} catch (NullPointerException e) {
			assertThat(e.getMessage(), is(IsNull.nullValue()));
		}
	}

	//findBySearchFilter
	@Test
	public void testSearchFilterProduct() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter);

		assertTrue(result.size() > 0);
		productDAO.deleteAll(products);

	}

	@Test
	public void testSearchFilterProductWithNullData() {
		try {
			productDAO.findBySearchFilter(null);
		} catch (NullPointerException e) {
			assertThat(e.getMessage(), is(IsNull.nullValue()));
		}
	}

	@Test
	public void testSearchFilterProductWithSpecificData() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT, "%Samsung");

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter);

		assertTrue(result.size() > 0);
		productDAO.deleteAll(products);

	}

	// findBySearchFilter(searchFilter, querySettings)

	@Test
	public void testFindProductBySearchFilterWithQuerySettings() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter,
				new QuerySettings<Product>().withFirstResult(0).withMaxResults(2));

		assertTrue(result.size() > 0);
		productDAO.deleteAll(products);

	}

	@Test
	public void testFindEmptyProductWithQuerySettings() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter,
				new QuerySettings<Product>().withFirstResult(6).withMaxResults(5));

		productDAO.deleteAll(products);
		assertTrue(result.size() == 0);

	}

	@Test
	public void testFindEmptyProductWithNullQuerySettings() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter, null);

		assertTrue(result.size() > 0);
		productDAO.deleteAll(products);

	}

	@Test
	public void testFindProductWithQuerySettings() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter, new QuerySettings<Product>().withFirstResult(0)
				.withMaxResults(2).withMarkers(AuditableMarker.ACTIVE).withOrderAttributes(Product_.price));

		assertTrue(result.size() > 0);
		productDAO.deleteAll(products);

	}

	@Test
	public void testFindEmptyProductWithEmptyQuerySettings() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT);

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter, new QuerySettings<Product>());

		assertTrue(result.size() > 0);
		productDAO.deleteAll(products);

	}

	// countBySearchFilter

	@Test
	public void testCountBySearchFilter() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter().add(ProductSearchField.FILTER_NAME_PRODUCT, "%");

		List<Tuple> result = productDAO.findBySearchFilter(searchFilter);
		Long count = productDAO.countBySearchFilter(searchFilter);

		assertNotNull(result);
		assertEquals("compare size to count", result.size(), count.longValue());
		productDAO.deleteAll(products);

	}

	@Test
	public void testCountBySearchFilterProductWithEmptyData() throws TransactionRolledbackException {
		List<Product> products = productDAO.saveAll(this.getProducts());

		SearchFilter searchFilter = new SearchFilter();
		long actual = productDAO.countBySearchFilter(searchFilter);

		assertEquals(products.size(), actual);
		productDAO.deleteAll(products);
	}

	@Test(expected = NullPointerException.class)
	public void testCountBySearchFilterProductWithNullData() {
		productDAO.countBySearchFilter(null);
	}


	// FindByCriteria
	@Test
	public void testFindByCriteria() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());
		try (CriteriaQueryGenericContext<Product, Tuple> q = productDAO.initializeQuery(Product.class, Tuple.class)) {

			Expression<String> path = productDAO.getExpression(null, q.r, Product_.name);
			q.q.where(q.c.like(path, "%Samsung%"));
			q.q.multiselect(path);
			List<Tuple> tuples = productDAO.findByCriteria(q);

			assertTrue(tuples.size() > 0);
		}

		productDAO.deleteAll(products);

	}

	@Test
	public void testCountByCriteria() throws TransactionRolledbackException {

		List<Product> products = productDAO.saveAll(this.getProducts());
		try (CriteriaQueryGenericContext<Product, Tuple> q = productDAO.initializeQuery(Product.class, Tuple.class);
				CriteriaQueryGenericContext<Product, Long> countQueryContext = productDAO.initializeQuery(Product.class,
						Long.class);) {

			Expression<String> path = productDAO.getExpression(null, q.r, Product_.name);
			q.q.where(q.c.like(path, "%Samsung%"));
			q.q.multiselect(path);
			List<Tuple> tuples = productDAO.findByCriteria(q);

			countQueryContext.where(q.q.getRestriction());
			Long count = productDAO.countByCriteria(countQueryContext);

			assertNotNull(tuples);
			assertEquals("compare size to count", tuples.size(), count.longValue());
		}
		productDAO.deleteAll(products);
	}

	//  countBySearchFilter(searchFilter, querySettings)

	//	@Test
	//	public void testCountProductBySearchFilterWithQuerySettings() throws TransactionRolledbackException {
	//
	//		List<Product> products = productDAO.saveAll(this.getProducts());
	//
	//		SearchFilter searchFilter = new SearchFilter().add(IvyPersistanceSearchField.FILTER_NAME_PRODUCT);
	//
	//		long actual = productDAO.countBySearchFilter(searchFilter,
	//				new QuerySettings<Product>().withFirstResult(0).withMaxResults(2));
	//
	//		List<Tuple> result = productDAO.findBySearchFilter(searchFilter,
	//				new QuerySettings<Product>().withFirstResult(0).withMaxResults(2));
	//
	//		productDAO.deleteAll(products);
	//		assertEquals(result.size(), actual);
	//
	//	}

	@Test
	public void testDeleteWithoutAuditing() throws TransactionRolledbackException {

		Product product = new Product();
		product.setName("Iphone 78s");
		product.setPrice(2300);

		product = productDAO.save(product);
		productDAO.evict(product);
		product = productDAO.deleteWithoutAuditing(product);
		assertTrue("Delete without auditing ", product.isDeleted());

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
