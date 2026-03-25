package ee.cyber.cdoc2;

import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class TestDbConfiguration {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.jdbcx.JdbcDataSource");
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS foo");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        createTestDBFromLiquibase(dataSource);

        return dataSource;
    }

    @SuppressWarnings("PMD.SystemPrintln")
    public void createTestDBFromLiquibase(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            dropAllObjects(conn);
            try (
                JdbcConnection jdbcConnection = new JdbcConnection(conn);
                Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
            ) {
                Map<String, Object> scopeObjects = Map.of(
                    Scope.Attr.database.name(), database,
                    Scope.Attr.resourceAccessor.name(), new ClassLoaderResourceAccessor()
                );
                Scope.child(scopeObjects, () -> {
                    CommandScope update = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                        .addArgumentValue(
                            DbUrlConnectionArgumentsCommandStep.DATABASE_ARG,
                            database
                        )
                        .addArgumentValue(
                            DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS,
                            new ChangeLogParameters(database)
                        )
                        .addArgumentValue(
                            UpdateCommandStep.CHANGELOG_FILE_ARG,
                            "db.changelog-master.yaml"
                        );
                    update.execute();
                });
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(ExceptionUtils.getStackTrace(e));

            throw new RuntimeException(e);
        }
    }

    private void dropAllObjects(Connection conn) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement("DROP ALL OBJECTS")) {
            preparedStatement.executeUpdate();
        }
    }
}
