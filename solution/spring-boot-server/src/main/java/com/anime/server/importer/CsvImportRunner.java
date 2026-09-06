package com.anime.server.importer;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import static com.anime.server.importer.CsvValueParser.*;

/**
 * Imports the static CSV files into PostgreSQL when the application is
 * started with the {@code --import-data} argument:
 *
 * <pre>./gradlew bootRun --args='--import-data'</pre>
 *
 * Column mappings follow the real dataset headers (e.g. {@code mal_id},
 * {@code character_mal_id}, {@code person_mal_id}); a few fallback
 * names are kept for robustness.
 *
 * Design notes:
 * <ul>
 *   <li>Inserts use {@link JdbcTemplate#batchUpdate} with batches of
 *       {@value #BATCH_SIZE} rows: JPA {@code saveAll} cannot batch
 *       IDENTITY-generated keys, JDBC batching is ~10x faster here.</li>
 *   <li>Malformed rows are skipped and counted, never fatal.</li>
 *   <li>Each table is truncated before import so the command is
 *       idempotent and can be re-run safely.</li>
 * </ul>
 */
@Component
public class CsvImportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CsvImportRunner.class);
    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbc;

    @Value("${app.data.directory:../../data}")
    private String dataDirectory;

    public CsvImportRunner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        if (!Arrays.asList(args).contains("--import-data")) {
            log.info("CSV import skipped (start with --import-data to import).");
            return;
        }

        Path dir = Paths.get(dataDirectory);
        if (!Files.isDirectory(dir)) {
            log.error("Data directory not found: {}", dir.toAbsolutePath());
            return;
        }

        long start = System.currentTimeMillis();
        log.info("=== CSV import started from {} ===", dir.toAbsolutePath());

        // details.csv: mal_id,title,title_japanese,url,image_url,type,status,score,
        // scored_by,start_date,end_date,synopsis,rank,popularity,members,favorites,
        // genres,studios,themes,demographics,source,rating,episodes,season,year,...
        importFile(dir, "details.csv", "anime_details",
                "INSERT INTO anime_details (anime_id, title, title_japanese, type, source, episodes, " +
                        "status, start_date, end_date, season, air_year, rating, score, scored_by, anime_rank, " +
                        "popularity, members, favorites, synopsis, genres, themes, demographics, studios, " +
                        "producers, image_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                        "ON CONFLICT (anime_id) DO NOTHING",
                (h) -> (row) -> {
                    Long id = parseLong(value(row, h, "mal_id", "anime_id", "id"));
                    if (id == null) return null;
                    return new Object[]{
                            id,
                            value(row, h, "title", "name"),
                            value(row, h, "title_japanese"),
                            value(row, h, "type"),
                            value(row, h, "source"),
                            parseInt(value(row, h, "episodes")),
                            value(row, h, "status"),
                            value(row, h, "start_date"),
                            value(row, h, "end_date"),
                            value(row, h, "season"),
                            parseInt(value(row, h, "year")),
                            value(row, h, "rating"),
                            parseDouble(value(row, h, "score")),
                            parseInt(value(row, h, "scored_by")),
                            parseInt(value(row, h, "rank")),
                            parseInt(value(row, h, "popularity")),
                            parseInt(value(row, h, "members")),
                            parseInt(value(row, h, "favorites")),
                            value(row, h, "synopsis"),
                            value(row, h, "genres"),
                            value(row, h, "themes"),
                            value(row, h, "demographics"),
                            value(row, h, "studios"),
                            value(row, h, "producers"),
                            value(row, h, "image_url")
                    };
                });

        // characters.csv: character_mal_id,url,name,name_kanji,image,favorites,about
        importFile(dir, "characters.csv", "characters",
                "INSERT INTO characters (character_id, name, name_kanji, favorites, about, image_url) " +
                        "VALUES (?,?,?,?,?,?) ON CONFLICT (character_id) DO NOTHING",
                (h) -> (row) -> {
                    Long id = parseLong(value(row, h, "character_mal_id", "mal_id", "id"));
                    if (id == null) return null;
                    return new Object[]{
                            id,
                            value(row, h, "name"),
                            value(row, h, "name_kanji"),
                            parseInt(value(row, h, "favorites")),
                            value(row, h, "about"),
                            value(row, h, "image", "image_url")
                    };
                });

        // character_anime_works.csv: anime_mal_id,character_mal_id,character_name,role
        importFile(dir, "character_anime_works.csv", "character_anime_works",
                "INSERT INTO character_anime_works (character_id, anime_id, role) VALUES (?,?,?)",
                (h) -> (row) -> {
                    Long cid = parseLong(value(row, h, "character_mal_id", "character_id"));
                    Long aid = parseLong(value(row, h, "anime_mal_id", "anime_id"));
                    if (cid == null || aid == null) return null;
                    return new Object[]{cid, aid, value(row, h, "role")};
                });

        // character_nicknames.csv: character_mal_id,nickname
        importFile(dir, "character_nicknames.csv", "character_nicknames",
                "INSERT INTO character_nicknames (character_id, nickname) VALUES (?,?)",
                (h) -> (row) -> {
                    Long cid = parseLong(value(row, h, "character_mal_id", "character_id"));
                    String nick = value(row, h, "nickname");
                    if (cid == null || nick == null) return null;
                    return new Object[]{cid, nick};
                });

        // person_details.csv: person_mal_id,url,website_url,image_url,name,
        // given_name,family_name,birthday,favorites,relevant_location
        importFile(dir, "person_details.csv", "person_details",
                "INSERT INTO person_details (person_id, name, given_name, family_name, birthday, " +
                        "favorites, location, website_url, image_url) VALUES (?,?,?,?,?,?,?,?,?) " +
                        "ON CONFLICT (person_id) DO NOTHING",
                (h) -> (row) -> {
                    Long id = parseLong(value(row, h, "person_mal_id", "mal_id", "id"));
                    if (id == null) return null;
                    return new Object[]{
                            id,
                            value(row, h, "name"),
                            value(row, h, "given_name"),
                            value(row, h, "family_name"),
                            value(row, h, "birthday"),
                            parseInt(value(row, h, "favorites")),
                            value(row, h, "relevant_location", "location"),
                            value(row, h, "website_url"),
                            value(row, h, "image_url")
                    };
                });

        // person_anime_works.csv: person_mal_id,position,anime_mal_id
        importFile(dir, "person_anime_works.csv", "person_anime_works",
                "INSERT INTO person_anime_works (person_id, anime_id, work_position) VALUES (?,?,?)",
                (h) -> (row) -> {
                    Long pid = parseLong(value(row, h, "person_mal_id", "person_id"));
                    Long aid = parseLong(value(row, h, "anime_mal_id", "anime_id"));
                    if (pid == null || aid == null) return null;
                    return new Object[]{pid, aid, value(row, h, "position", "role")};
                });

        // person_voice_works.csv: person_mal_id,role,anime_mal_id,character_mal_id,language
        importFile(dir, "person_voice_works.csv", "person_voice_works",
                "INSERT INTO person_voice_works (person_id, anime_id, character_id, role, language) " +
                        "VALUES (?,?,?,?,?)",
                (h) -> (row) -> {
                    Long pid = parseLong(value(row, h, "person_mal_id", "person_id"));
                    if (pid == null) return null;
                    return new Object[]{
                            pid,
                            parseLong(value(row, h, "anime_mal_id", "anime_id")),
                            parseLong(value(row, h, "character_mal_id", "character_id")),
                            value(row, h, "role"),
                            value(row, h, "language")
                    };
                });

        // person_alternate_names.csv: person_mal_id,alt_name
        importFile(dir, "person_alternate_names.csv", "person_alternate_names",
                "INSERT INTO person_alternate_names (person_id, alternate_name) VALUES (?,?)",
                (h) -> (row) -> {
                    Long pid = parseLong(value(row, h, "person_mal_id", "person_id"));
                    String name = value(row, h, "alt_name", "alternate_name", "name");
                    if (pid == null || name == null) return null;
                    return new Object[]{pid, name};
                });

        log.info("=== CSV import finished in {}s ===", (System.currentTimeMillis() - start) / 1000);
    }

    /**
     * Generic streaming import of one CSV file into one table.
     *
     * @param dir           data directory
     * @param fileName      CSV file name
     * @param table         destination table (truncated first)
     * @param insertSql     parameterised INSERT statement
     * @param mapperFactory given the header map, returns a row mapper
     *                      producing the INSERT arguments (null = skip row)
     */
    private void importFile(Path dir, String fileName, String table, String insertSql,
                            Function<Map<String, Integer>, Function<String[], Object[]>> mapperFactory) {
        Path file = dir.resolve(fileName);
        if (!Files.exists(file)) {
            log.warn("[{}] file not found, skipped", fileName);
            return;
        }

        log.info("[{}] importing into {}...", fileName, table);
        jdbc.execute("TRUNCATE TABLE " + table);

        long imported = 0;
        long skipped = 0;

        // UTF-8 esplicito: il charset di default su Windows (Cp1252)
        // corromperebbe i caratteri giapponesi del dataset.
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(file.toFile(), StandardCharsets.UTF_8)).build()) {
            String[] headerRow = reader.readNext();
            if (headerRow == null) {
                log.warn("[{}] empty file", fileName);
                return;
            }
            Map<String, Integer> headers = new HashMap<>();
            for (int i = 0; i < headerRow.length; i++) {
                headers.put(normaliseHeader(headerRow[i]), i);
            }
            Function<String[], Object[]> mapper = mapperFactory.apply(headers);

            List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
            String[] row;
            while ((row = reader.readNext()) != null) {
                Object[] params;
                try {
                    params = mapper.apply(row);
                } catch (Exception e) {
                    params = null;
                }
                if (params == null) {
                    skipped++;
                    continue;
                }
                batch.add(params);
                if (batch.size() == BATCH_SIZE) {
                    imported += flushBatch(insertSql, batch, fileName);
                }
            }
            if (!batch.isEmpty()) {
                imported += flushBatch(insertSql, batch, fileName);
            }
            log.info("[{}] done: {} rows imported, {} skipped", fileName, imported, skipped);
        } catch (Exception e) {
            log.error("[{}] import failed: {}", fileName, e.getMessage());
        }
    }

    /**
     * Executes one batch, tolerating failures: a failing batch is
     * logged and dropped without aborting the rest of the file.
     *
     * @param insertSql parameterised INSERT statement
     * @param batch     rows to insert (cleared before returning)
     * @param fileName  source file, for logging
     * @return number of rows handed to the database
     */
    private int flushBatch(String insertSql, List<Object[]> batch, String fileName) {
        int size = batch.size();
        try {
            jdbc.batchUpdate(insertSql, batch);
            return size;
        } catch (Exception e) {
            log.warn("[{}] batch of {} rows failed and was skipped: {}",
                    fileName, size, e.getMessage());
            return 0;
        } finally {
            batch.clear();
        }
    }
}
