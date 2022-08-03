package com.axonivy.utils.persistence.test.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.transaction.TransactionRolledbackException;

import org.junit.jupiter.api.Test;

import com.axonivy.utils.persistence.dao.CascadeDelete;
import com.axonivy.utils.persistence.dao.CriteriaQueryGenericContext;
import com.axonivy.utils.persistence.dao.DeleteQueryContext;
import com.axonivy.utils.persistence.daos.ProducerDAO;
import com.axonivy.utils.persistence.entities.Producer;
import com.axonivy.utils.persistence.entities.Producer_;
import com.axonivy.utils.persistence.entities.Product;
import com.axonivy.utils.persistence.search.FindByExample;
import com.axonivy.utils.persistence.test.DemoTestBase;

import ch.ivyteam.ivy.environment.IvyTest;


@IvyTest
public class ProducerDAOTest extends DemoTestBase {
	private static ProducerDAO producerDAO = new ProducerDAO();

	public static Producer createFakeData() {
		Product product = new Product();
		product.setName("Iphone 6s");
		product.setPrice(1000);

		Product product1 = new Product();
		product.setName("Iphone 7s");
		product.setPrice(1300);

		Set<Product> products = new HashSet<>();
		products.add(product);
		products.add(product1);

		Producer producer = new Producer();
		producer.setName("axon");
		producer.setAddress("30 thang 4");
		producer.setEmail("axonactive@com.vn");
		producer.setProducts(products);

		producer = producerDAO.save(producer);

		return producer;
	}

	public static List<Producer> createFakeDatas() throws TransactionRolledbackException {
		Product product = new Product();
		product.setName("Iphone 6s");
		product.setPrice(1000);

		Product product1 = new Product();
		product.setName("samsung 7s");
		product.setPrice(1300);

		Product product2 = new Product();
		product.setName("Iphone 8s");
		product.setPrice(1000);

		Product product3 = new Product();
		product.setName("samsung 9");
		product.setPrice(1300);

		Set<Product> products = new HashSet<>();
		products.add(product);
		products.add(product1);

		Set<Product> products2 = new HashSet<>();
		products2.add(product2);
		products2.add(product3);

		Producer producer = new Producer();
		producer.setName("apple axon");
		producer.setAddress("30 thang 4");
		producer.setEmail("axonactive@com.vn");
		producer.setProducts(products);

		Producer producer1 = new Producer();
		producer1.setName("samsung axon");
		producer1.setAddress("30.4");
		producer1.setEmail("samsung@com.vn");
		producer1.setProducts(products2);

		List<Producer> producers = new ArrayList<>();
		producers.add(producer);
		producers.add(producer1);
		producers = producerDAO.saveAll(producers);

		return producers;
	}

	@Test
	public void testDeleteProducer() throws TransactionRolledbackException {
		Producer producer = new Producer();
		producer = producerDAO.save(createFakeData());
		producerDAO.evict(producer);
		producer = producerDAO.deleteCascade(producer, new CascadeDelete<Producer>() {
			@Override
			public void deleteChildren(Producer bean) {
			}
		});
		assertThat(producer.isDeleted()).as("Deleted producer").isTrue();
	}

	@Test
	public void testFindByCriteria() {
		producerDAO.save(createFakeData());
		try (CriteriaQueryGenericContext<Producer, Tuple> q = producerDAO.initializeQuery(Producer.class,
				Tuple.class)) {
			Expression<String> path1 = ProducerDAO.getExpression(null, q.r, Producer_.name);
			q.q.where(q.c.like(path1, "axon"));
			q.q.multiselect(path1);
			List<Tuple> tuples = producerDAO.findByCriteria(q);
			assertThat(tuples).as("Found exactly one entry").hasSize(1);
			assertThat(tuples.get(0).get(0)).as("Name of entry match").isEqualTo("axon");
		}
	}

	@Test
	public void testFirstFindByExample() {
		Producer producer = new Producer();
		producer = producerDAO.save(createFakeData());
		FindByExample<Producer> pr = FindByExample.getInstance(Producer.class);

		producer = producerDAO.findFirstByExample(pr);
		assertThat(producer).isNotNull();
	}

	@Test
	public void testFindByExampleWithParameter() throws TransactionRolledbackException {
		producerDAO.saveAll(createFakeDatas());
		FindByExample<Producer> example = FindByExample.getInstance(Producer.class);
		example.getE().setName("axon");
		List<Producer> producers = new ArrayList<>();
		producers = producerDAO.findByExample(example, Producer_.name);
		assertThat(producers).isNotEmpty();
	}

	@Test
	public void testFindByExampleWithParameterLike() throws TransactionRolledbackException {
		producerDAO.saveAll(createFakeDatas());
		FindByExample<Producer> example = FindByExample.getInstance(Producer.class);
		example.getE().setName("xon");
		List<Producer> producers = new ArrayList<>();
		producers = producerDAO.findByExample(example, Optional.of(Boolean.TRUE), Producer_.name);
		assertThat(producers).isNotEmpty();
	}

	@Test
	public void testFindByExampleWithParameterOrLike() throws TransactionRolledbackException {
		producerDAO.saveAll(createFakeDatas());
		FindByExample<Producer> example = FindByExample.getInstance(Producer.class);
		example.getE().setName("xon");
		List<Producer> producers = new ArrayList<>();
		producers = producerDAO.findByExample(example, Optional.of(Boolean.TRUE), Optional.of(Boolean.TRUE),
				Producer_.name);
		assertThat(producers).isNotEmpty();
	}

	@Test
	public void testFindByExampleWithParameterOrLikeIsAscending() throws TransactionRolledbackException {
		producerDAO.saveAll(createFakeDatas());
		FindByExample<Producer> example = FindByExample.getInstance(Producer.class);
		example.getE().setName("xon");
		List<Producer> producers = new ArrayList<>();
		Boolean[] booleans = { false, true };
		producers = producerDAO.findByExample(example, Optional.of(Boolean.TRUE), Optional.of(Boolean.TRUE), booleans,
				Producer_.name);
		assertThat(producers).isNotEmpty();
	}

	@Test
	public void testFindByExampleWithParameterOrLikeIsAscendingIndexNumber() throws TransactionRolledbackException {
		producerDAO.saveAll(createFakeDatas());
		FindByExample<Producer> example = FindByExample.getInstance(Producer.class);
		example.getE().setName("xon");
		List<Producer> producers = new ArrayList<>();
		Boolean[] booleans = { true, true };
		Integer fisrt = 0;
		Integer result = 1;
		producers = producerDAO.findByExample(example, Optional.of(Boolean.TRUE), Optional.of(Boolean.TRUE), booleans,
				fisrt, result, Producer_.name);
		assertThat(producers).as("Found exactly one entry").hasSize(1);
	}

	@Test
	public void testDeletePhysicallyRawByCriteria() {
		producerDAO.save(createFakeData());
		try (DeleteQueryContext<Producer> query = producerDAO.initializeDeleteQuery();) {
			Predicate wdaInPredicate = query.c.equal(query.r.get(Producer_.name), "axon");
			query.d.where(wdaInPredicate);
			Long result = producerDAO.deletePhysicallyRawByCriteria(query);
			assertThat(result).isGreaterThan(0);
		}
	}

	@Test
	public void testDeleteWithoutAuditing() {
		Producer producer = new Producer();
		producer = producerDAO.save(createFakeData());
		producer = producerDAO.deleteWithoutAuditing(producer);
		producer = producerDAO.find(producer);
		assertThat(producer).as("Deleted producer").isNull();
	}

	@Test
	public void testUndelete() {
		Producer producer = new Producer();
		producer = producerDAO.save(createFakeData());
		producer = producerDAO.undelete(producer);
		assertThat(producer.isDeleted()).as("Deleted producer").isFalse();
	}

	@Test
	public void testFindPermittedByEntityIds() throws TransactionRolledbackException {
		Collection<Producer> producers = new ArrayList<>();
		List<Producer> pros = createFakeDatas();
		producers = producerDAO.findByEntityIds(pros);
		assertThat(producers).isNotEmpty();
	}

	@Test
	public void testFindByIds() throws TransactionRolledbackException {
		Collection<Producer> producers = new ArrayList<>();
		List<Producer> pros = createFakeDatas();
		List<String> ids = pros.stream().map(e -> e.getId()).collect(Collectors.toList());
		producers = producerDAO.findByIds(ids);
		assertThat(producers).isNotEmpty();
	}

	@Test
	public void testSaveWithoutAuditing() {
		Producer producer = new Producer();
		producer = producerDAO.save(createFakeData());
		producer = producerDAO.saveWithoutAuditing(producer);
		assertThat(producer).isNotNull();
	}

}
