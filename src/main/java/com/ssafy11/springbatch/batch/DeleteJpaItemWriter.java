package com.ssafy11.springbatch.batch;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.util.Assert;

@Slf4j
public class DeleteJpaItemWriter<T> extends JpaItemWriter<T> {

	private EntityManagerFactory entityManagerFactory;

	private boolean usePersist = false;

	private boolean clearPersistenceContext = true;

	public DeleteJpaItemWriter(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(entityManagerFactory != null, "An EntityManagerFactory is required");
	}

	@Override
	public void write(Chunk<? extends T> items) {
		EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
		if (entityManager == null) {
			throw new DataAccessResourceFailureException("Unable to obtain a transactional EntityManager");
		}
		doWrite(entityManager, items);
		entityManager.flush();
		if (this.clearPersistenceContext) {
			entityManager.clear();
		}
	}

	@Override
	protected void doWrite(EntityManager entityManager, Chunk<? extends T> items) {
		if (!items.isEmpty()) {
			long removedFromContextCount = 0;
			for (T item : items) {
				if (entityManager.contains(item)) {
					entityManager.remove(item);
					removedFromContextCount++;
				} else {
					T managedItem = entityManager.merge(item);
					entityManager.remove(managedItem);
					removedFromContextCount++;
				}
			}
			log.info("삭제된 엔티티 갯수 : {}", removedFromContextCount);
		}
	}
}
