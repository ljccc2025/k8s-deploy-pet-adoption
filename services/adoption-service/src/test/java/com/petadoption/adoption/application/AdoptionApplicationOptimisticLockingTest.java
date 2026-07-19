package com.petadoption.adoption.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.RollbackException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.TransactionSystemException;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:adoption_service_optimistic;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.schemas=adoption_schema",
    "spring.flyway.default-schema=adoption_schema",
    "spring.jpa.open-in-view=false",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.jpa.properties.hibernate.default_schema=adoption_schema",
    "spring.task.scheduling.enabled=false"
})
@Sql(statements = {
    "DELETE FROM adoption_schema.adoption_outbox_events",
    "DELETE FROM adoption_schema.adoption_applications"
})
class AdoptionApplicationOptimisticLockingTest {
  private static final UUID APPLICATION_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID PET_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final UUID USER_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
  private static final UUID REVIEWER_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

  @Autowired EntityManagerFactory entityManagerFactory;

  @Test
  void staleTransitionFailsWithOptimisticLocking() {
    persistSubmittedApplication();
    EntityManager first = entityManagerFactory.createEntityManager();
    EntityManager second = entityManagerFactory.createEntityManager();
    try {
      first.getTransaction().begin();
      second.getTransaction().begin();
      AdoptionApplication firstCopy = first.find(AdoptionApplication.class, APPLICATION_ID);
      AdoptionApplication secondCopy = second.find(AdoptionApplication.class, APPLICATION_ID);

      firstCopy.approve(REVIEWER_ID, LocalDateTime.now());
      first.getTransaction().commit();

      secondCopy.reject(REVIEWER_ID, "stale reject", LocalDateTime.now());
      assertThatThrownBy(() -> second.getTransaction().commit())
          .isInstanceOfAny(RollbackException.class, TransactionSystemException.class);
    } finally {
      close(first);
      close(second);
    }
  }

  private void persistSubmittedApplication() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      entityManager.persist(new AdoptionApplication(
          APPLICATION_ID,
          PET_ID,
          USER_ID,
          "reason",
          "experience",
          LocalDateTime.now()));
      entityManager.getTransaction().commit();
    } finally {
      close(entityManager);
    }
  }

  private static void close(EntityManager entityManager) {
    if (entityManager.getTransaction().isActive()) {
      entityManager.getTransaction().rollback();
    }
    entityManager.close();
  }
}
