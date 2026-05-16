package dk.magnusjensen.tender.storage.sqlite;

import java.sql.Connection;

class Migrations {
    public static void migrate(Connection conn) {
            try (var stmt = conn.createStatement()) {
                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS schema_version (
                            id INTEGER NOT NULL CHECK (id = 1) PRIMARY KEY,
                            version INTEGER NOT NULL CHECK (version >= 0)
                        );
                        """);

                var schemaVersionResult = stmt.executeQuery("SELECT version FROM schema_version WHERE id = 1");
                int currentVersion = 0;
                if (schemaVersionResult.next()) {
                    currentVersion = schemaVersionResult.getInt("version");
                }

                if (currentVersion < 1) {
                    stmt.execute("""
                    CREATE TABLE tender_fallback_economy (
                        account_id VARCHAR(255) PRIMARY KEY,
                        balance INTEGER NOT NULL
                    );
                    """);

                    // Update schema version to 1
                    stmt.execute("""
                    INSERT INTO schema_version (id, version) VALUES (1, 1)
                        ON CONFLICT(id) DO UPDATE SET version = excluded.version;
                    """);
                }



            } catch (Exception e) {
                throw new RuntimeException("Failed to migrate database", e);
            }
    }
}
