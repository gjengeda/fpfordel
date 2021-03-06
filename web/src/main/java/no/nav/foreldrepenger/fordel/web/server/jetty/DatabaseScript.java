package no.nav.foreldrepenger.fordel.web.server.jetty;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseScript {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseScript.class);

    private final DataSource dataSource;
    private final boolean cleanOnException;
    private final String locations;

    public DatabaseScript(DataSource dataSource, boolean cleanOnException, String locations) {
        this.dataSource = dataSource;
        this.cleanOnException = cleanOnException;
        this.locations = locations;
    }

    public void migrate() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations(locations);
        flyway.setBaselineOnMigrate(true);

        try {
            flyway.migrate();
        } catch (FlywayException e) {
            // prøv en gang til
            if (cleanOnException) {
                LOG.warn("Failed migration. Cleaning and retrying", e);
                flyway.clean();
                flyway.migrate();
            } else {
                throw e;
            }
        }

    }
}
