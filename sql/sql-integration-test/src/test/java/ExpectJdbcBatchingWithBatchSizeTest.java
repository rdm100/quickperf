import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.ExpectJdbcBatching;

import javax.persistence.Query;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.config.HibernateConfigBuilder.anHibernateConfig;

public class ExpectJdbcBatchingWithBatchSizeTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeAMultipleOfRowsToInsert extends SqlTestBase {

        private static final int BATCH_SIZE = 30;

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(BATCH_SIZE)
                    .build();
        }

        @Test
        @ExpectJdbcBatching(batchSize = 30)
        public void execute_insert_queries_in_batch_mode_with_batchSize_a_multiple_of_rows_to_insert() {

            executeInATransaction(entityManager -> {
                for (int i = 0; i < 60; i++) {
                    Book newBook = new Book();
                    newBook.setTitle("new book");
                    if (i % BATCH_SIZE == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                    entityManager.persist(newBook);
                }
            });

        }

    }

    @Test public void
    should_pass_with_batch_size_a_multiple_of_rows_to_insert() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeAMultipleOfRowsToInsert.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeNOTAMultipleOfRowsToInsert extends SqlTestBase {

        private static final int BATCH_SIZE = 30;

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(BATCH_SIZE)
                    .build();
        }

        @Test
        @ExpectJdbcBatching(batchSize = BATCH_SIZE)
        public void execute_inserts_queries_in_batch_mode_with_batch_size_NOT_a_multiple_of_rows_to_insert() {

            executeInATransaction(entityManager -> {
                int numberOfElementsToInsertNotAMultipleOfBatchSize = 70;
                for (int i = 0; i < numberOfElementsToInsertNotAMultipleOfBatchSize; i++) {
                    Book newBook = new Book();
                    newBook.setTitle("new book");
                    if (i % BATCH_SIZE == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                    entityManager.persist(newBook);
                }
            });

        }

    }

    @Test public void
    should_pass_if_last_batch_execution_has_a_batch_size_different_from_the_expected_batch_size() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeNOTAMultipleOfRowsToInsert.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsertInNewJvm extends SqlTestBase {

        @Test
        @ExpectJdbcBatching(batchSize = 30)
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        public void execute_two_insert_queries_not_batched_in_new_Jvm() {

            executeInATransaction(entityManager -> {
                String firstInsertQueryAsString = "INSERT INTO Book (id, title) VALUES (1200, 'Book title')";
                Query firstInsertQuery = entityManager.createNativeQuery(firstInsertQueryAsString);
                firstInsertQuery.executeUpdate();

                String secondInsertQueryAsString = "INSERT INTO Book (id, title) VALUES (1300, 'Book title')";
                Query secondInsertQuery = entityManager.createNativeQuery(secondInsertQueryAsString);
                secondInsertQuery.executeUpdate();
            });

        }

    }

    @Test public void
    should_fail_with_two_insert_queries_not_batched_in_new_jvm() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsertInNewJvm.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(1);
        assertThat(printableResult.toString()).contains("a performance property is not respected");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedExpectJdbcBatchingInNewJvm extends SqlTestBase {

        private static final int BATCH_SIZE = 30;

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(BATCH_SIZE)
                    .build();
        }

        @Test
        @ExpectJdbcBatching(batchSize = BATCH_SIZE)
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        public void execute_inserts_queries_in_batch_mode() {

            executeInATransaction(entityManager -> {
                for (int i = 0; i < 1_000; i++) {
                    Book newBook = new Book();
                    newBook.setTitle("new book");
                    if (i % BATCH_SIZE == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                    entityManager.persist(newBook);
                }
            });

        }
    }

    @Test public void
    should_pass_with_batched_insert_queries_in_new_jvm() {

        Class<?> testClass = AClassHavingAMethodAnnotatedExpectJdbcBatchingInNewJvm.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsert extends SqlTestBase {

        @Test
        @ExpectJdbcBatching(batchSize = 30)
        public void execute_two_insert_query_not_batched() {

            executeInATransaction(entityManager -> {
                String firstInsertQueryAsString = "INSERT INTO Book (id,title) VALUES (1200, 'Book title')";
                Query firstInsertQuery = entityManager.createNativeQuery(firstInsertQueryAsString);
                firstInsertQuery.executeUpdate();

                String secondInsertQueryAsString = "INSERT INTO Book (id,title) VALUES (1300, 'Book title')";
                Query secondInsertQuery = entityManager.createNativeQuery(secondInsertQueryAsString);
                secondInsertQuery.executeUpdate();

            });

        }

    }

    @Test public void
    should_fail_with_two_insert_queries_not_batched() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsert.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(1);
        assertThat(printableResult.toString())
                .contains("[PERF] Expected batch size <30> but is <0>.");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndWithBatchSizeDifferentFromTheExpectedOne extends SqlTestBase {

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(20)
                    .build();
        }

        @Test
        @ExpectJdbcBatching(batchSize = 30)
        public void execute_insert_queries_in_batch_mode_with_batchSize_a_multiple_of_rows_to_insert() {

            executeInATransaction(entityManager -> {
                for (int i = 0; i < 60; i++) {
                    Book newBook = new Book();
                    newBook.setTitle("new book");
                    if (i % 20 == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                    entityManager.persist(newBook);
                }
            });

        }
    }

    @Test public void
    should_fail_if_batch_size_is_not_as_expected() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndWithBatchSizeDifferentFromTheExpectedOne.class;

        PrintableResult testResult = PrintableResult.testResult(testClass);

        assertThat(testResult.failureCount()).isEqualTo(1);
        assertThat(testResult.toString()).contains("Expected batch size <30> but is <20>");
    }

}
