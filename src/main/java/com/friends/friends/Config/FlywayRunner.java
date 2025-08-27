package com.friends.friends.Config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// Tato třída se hodí pro prvotní vývoj aplikace společne s hibernate create-drop.
// Při každém restartu vždy aplikuje všechny migrace po vytvoření tabulek
// Při vývoji tak vytvoří již implementované funkce a jiné potřebné věci.
// :)
@Configuration
public class FlywayRunner implements ApplicationListener<ApplicationReadyEvent> {

    private final DataSource dataSource;

    public FlywayRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public Flyway flyway() {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .cleanDisabled(false)
                .validateOnMigrate(false)
                .load();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Smaž tabulku historie, pokud existuje
            stmt.execute("DROP TABLE IF EXISTS flyway_schema_history CASCADE");
            System.out.println("✅ flyway_schema_history dropped.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to drop flyway_schema_history", e);
        }

        // Spusť všechny migrace znovu
        Flyway flyway = flyway();
        flyway.migrate();
        System.out.println("✅ Flyway migrations applied.");
    }
}