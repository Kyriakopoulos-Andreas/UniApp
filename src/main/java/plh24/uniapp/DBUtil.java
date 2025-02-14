package plh24.uniapp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;
import java.util.logging.*;

/**
 * Η κλάση {@code DBUtil} διαχειρίζεται τη σύνδεση και τις λειτουργίες στη βάση δεδομένων
 * Apache Derby. Η κλάση αυτή υλοποιεί το πρότυπο Singleton ώστε να υπάρχει μόνο ένα instance.
 * <p>
 * Οι βασικές λειτουργίες της κλάσης είναι:
 * <ul>
 *   <li>Φόρτωση ρυθμίσεων (π.χ. DB URL και διαδρομές σε αρχεία SQL) από το αρχείο
 *       {@code dbconfig.properties}.</li>
 *   <li>Δημιουργία και διαγραφή πινάκων και indexes με χρήση ξεχωριστών αρχείων SQL.</li>
 *   <li>Ελέγχος ύπαρξης πινάκων και indexes μέσω του {@code DatabaseMetaData}.</li>
 * </ul>
 * 
 * Σημείωση: Για τα indexes, όλα τα {@code CREATE INDEX} statements βρίσκονται σε ένα αρχείο,
 * και για κάθε εντολή γίνεται parsing για την εξαγωγή του ονόματος του index, ώστε να ελεγχθεί
 * αν υπάρχει ήδη πριν εκτελεστεί.
 */
public class DBUtil {

    /** Logger για την καταγραφή των συμβάντων */
    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

    // Ρυθμίσεις και διαδρομές αρχείων SQL, που φορτώνονται από το dbconfig.properties
    private static String DB_URL;
    private static String SQL_CREATE_UNIVERSITY_PATH;
    private static String SQL_CREATE_UNIVERSITYVIEW_PATH;
    private static String SQL_CREATE_INDEXES_PATH;

    // Singleton instance της κλάσης DBUtil
    private static final DBUtil INSTANCE = new DBUtil();

    /**
     * Ιδιωτικός constructor. Φορτώνει τις ρυθμίσεις και αρχικοποιεί τον Logger.
     */
    private DBUtil() {
        loadProperties();
        initializeLogger();
    }

    /**
     * Επιστρέφει το μοναδικό instance της κλάσης DBUtil.
     *
     * @return το instance του DBUtil.
     */
    public static DBUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Φορτώνει τις ρυθμίσεις από το αρχείο {@code dbconfig.properties}.
     * Ανατίθενται τιμές για το DB URL και για τις διαδρομές των αρχείων SQL που χρησιμοποιούνται για
     * τη δημιουργία πινάκων και indexes. Σε περίπτωση που κάποιο αρχείο δεν βρεθεί, χρησιμοποιούνται
     * default τιμές.
     */
    private void loadProperties() {
        Properties props = new Properties();

        try (InputStream in = Files.newInputStream(Paths.get("resources/dbconfig.properties"))) {
            props.load(in);
            LOGGER.info("ℹ️ Φορτώθηκε το dbconfig.properties");
        } catch (IOException e) {
            LOGGER.warning("⚠️ Δεν βρέθηκε/φορτώθηκε το dbconfig.properties - χρήση default τιμών.");
        }

        // Default τιμές
        String defaultDB = "jdbc:derby:UniDB;create=true";
        String defaultUni = "resources/sql/create_university.sql";
        String defaultUniView = "resources/sql/create_universityview.sql";
        String defaultIndexes = "resources/sql/create_indexes.sql";

        DB_URL = props.getProperty("db.url", defaultDB);
        SQL_CREATE_UNIVERSITY_PATH = props.getProperty("sql.create_university", defaultUni);
        SQL_CREATE_UNIVERSITYVIEW_PATH = props.getProperty("sql.create_universityview", defaultUniView);
        SQL_CREATE_INDEXES_PATH = props.getProperty("sql.create_indexes", defaultIndexes);

        LOGGER.info("ℹ️ ️️DB_URL = " + DB_URL);
        LOGGER.info("ℹ️ SQL_CREATE_UNIVERSITY_PATH = " + SQL_CREATE_UNIVERSITY_PATH);
        LOGGER.info("ℹ️ SQL_CREATE_UNIVERSITYVIEW_PATH = " + SQL_CREATE_UNIVERSITYVIEW_PATH);
        LOGGER.info("ℹ️ SQL_CREATE_INDEXES_PATH = " + SQL_CREATE_INDEXES_PATH);
    }

    /**
     * Αρχικοποιεί τον Logger ώστε να καταγράφει τα συμβάντα στο αρχείο
     * {@code logs/DBUtil.log} με τη χρήση του {@code SimpleFormatter}.
     */
    private void initializeLogger() {
        try {
            Files.createDirectories(Paths.get("logs"));

            // Αφαίρεση τυχόν υπαρχόντων handlers για αποφυγή διπλών καταγραφών.
            for (Handler h : LOGGER.getHandlers()) {
                LOGGER.removeHandler(h);
            }

            FileHandler fileHandler = new FileHandler("logs/DBUtil.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);

            LOGGER.setLevel(Level.ALL);
            LOGGER.setUseParentHandlers(false);

            LOGGER.info("📌 Έναρξη καταγραφής του Logger στο logs/DBUtil.log");
        } catch (IOException e) {
            System.err.println("Σφάλμα κατά την αρχικοποίηση του Logger: " + e.getMessage());
        }
    }

    /**
     * Επιστρέφει μια {@code Connection} στη βάση δεδομένων βάσει του {@code DB_URL}.
     *
     * @return {@code Connection} προς τη βάση δεδομένων.
     * @throws SQLException εάν η σύνδεση αποτύχει.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Ελέγχει και δημιουργεί τους πίνακες και τα indexes της βάσης δεδομένων,
     * αν δεν υπάρχουν ήδη.
     */
    public void initializeDatabase() {
        LOGGER.info("🟢 Έλεγχος & δημιουργία της Βάσης Δεδομένων (αν δεν υπάρχει)...");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            if (!tableExists("UNIVERSITY")) {
                createTablesAndIndexes(stmt);
                LOGGER.info("✅ Η Βάση Δεδομένων δημιουργήθηκε επιτυχώς!");
            } else {
                LOGGER.info("ℹ️ Η Βάση Δεδομένων υπάρχει ήδη.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Σφάλμα κατά την αρχικοποίηση της Βάσης Δεδομένων.", e);
        }
    }

    /**
     * Διαγράφει όλους τους πίνακες της βάσης δεδομένων και δημιουργεί ξανά τη δομή της.
     * <p>
     * Προσοχή: Όλα τα δεδομένα θα χαθούν.
     */
    public void resetDatabase() {
        LOGGER.warning("⚠️ Διαγραφή και επαναδημιουργία της Βάσης Δεδομένων...");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            dropTables(stmt);
            createTablesAndIndexes(stmt);
            LOGGER.info("🔄 Η Βάση Δεδομένων διαγράφηκε και επαναδημιουργήθηκε!");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Σφάλμα κατά την επαναδημιουργία της Βάσης Δεδομένων.", e);
        }
    }

     /**
     * Κάνει shutdown τη βάση δεδομένων.
     * <p>
     * Η Derby πετάει μια SQLException με SQLState "XJ015" ή "08006" όταν το shutdown είναι επιτυχές.
     */
    public void shutdownDatabase() {
        try {
            // Προσθέτουμε το shutdown=true για τερματισμό της βάσης.
            DriverManager.getConnection(DB_URL + ";shutdown=true");
            LOGGER.info("ℹ️ Η Βάση Δεδομένων έκλεισε επιτυχώς (χωρίς exception).");
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            if ("XJ015".equals(sqlState) || "08006".equals(sqlState)) {
                LOGGER.info("ℹ️ Η Βάση Δεδομένων έκλεισε επιτυχώς.");
            } else {
                LOGGER.log(Level.SEVERE, "❌ Σφάλμα κατά το κλείσιμο της Βάσης Δεδομένων.", e);
            }
        }
    }    
    
    /**
     * Διαγράφει τους πίνακες της βάσης δεδομένων.
     *
     * @param stmt το {@code Statement} για την εκτέλεση εντολών SQL.
     * @throws SQLException εάν αποτύχει η διαγραφή.
     */
    private void dropTables(Statement stmt) throws SQLException {
        String[] tables = {"UNIVERSITYVIEW", "UNIVERSITY"};
        for (String table : tables) {
            if (tableExists(table)) {
                stmt.executeUpdate("DROP TABLE " + table);
                LOGGER.warning("🗑️ Ο πίνακας " + table + " διαγράφηκε.");
            } else {
                LOGGER.info("ℹ️ Ο πίνακας " + table + " δεν υπάρχει, δεν απαιτείται διαγραφή.");
            }
        }
    }

    /**
     * Δημιουργεί τους πίνακες και τα indexes της βάσης δεδομένων διαβάζοντας
     * τα αντίστοιχα .sql αρχεία.
     *
     * @param stmt το {@code Statement} για την εκτέλεση εντολών SQL.
     * @throws SQLException εάν αποτύχει η δημιουργία.
     */
    private void createTablesAndIndexes(Statement stmt) throws SQLException {
        try {
            // Δημιουργία πίνακα UNIVERSITY
            if (!tableExists("UNIVERSITY")) {
                String sqlUni = loadSQLFromFile(SQL_CREATE_UNIVERSITY_PATH);
                    // Αφαίρεση τυχόν τερματικού χαρακτήρα ';' αν υπάρχει
                if (sqlUni.endsWith(";")) {
                    sqlUni = sqlUni.substring(0, sqlUni.length() - 1).trim();
                }            
                stmt.execute(sqlUni);
                LOGGER.info("✅ Ο πίνακας UNIVERSITY δημιουργήθηκε.");
            } else {
                LOGGER.info("ℹ️ Ο πίνακας UNIVERSITY υπάρχει ήδη.");
            }
          
            // Δημιουργία πίνακα UNIVERSITYVIEW
            if (!tableExists("UNIVERSITYVIEW")) {
                String sqlUniView = loadSQLFromFile(SQL_CREATE_UNIVERSITYVIEW_PATH);
                // Αφαίρεση τυχόν τερματικού χαρακτήρα ';' αν υπάρχει           
                if (sqlUniView.endsWith(";")) {
                    sqlUniView = sqlUniView.substring(0, sqlUniView.length() - 1).trim();
                }            
                stmt.execute(sqlUniView);
                LOGGER.info("✅ Ο πίνακας UNIVERSITYVIEW δημιουργήθηκε.");
            } else {
                LOGGER.info("ℹ️ Ο πίνακας UNIVERSITYVIEW υπάρχει ήδη.");
            }

            // Δημιουργία indexes από ένα αρχείο που περιέχει πολλαπλά CREATE INDEX statements.
            createIndexesFromFile(stmt, SQL_CREATE_INDEXES_PATH);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "❌ Αποτυχία φόρτωσης αρχείου .sql", e);
        }
    }

    /**
     * Ελέγχει αν υπάρχει ο πίνακας με το όνομα {@code tableName} στη βάση δεδομένων,
     * χρησιμοποιώντας το {@code DatabaseMetaData}.
     *
     * @param tableName το όνομα του πίνακα.
     * @return {@code true} αν ο πίνακας υπάρχει, {@code false} αλλιώς.
     */
    private boolean tableExists(String tableName) {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), new String[] {"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "⚠️ Σφάλμα ελέγχου ύπαρξης πίνακα: " + tableName, e);
            return false;
        }
    }

    /**
     * Ελέγχει αν υπάρχει ένα index με το όνομα {@code indexName} στον πίνακα UNIVERSITY.
     *
     * @param indexName το όνομα του index.
     * @return {@code true} αν υπάρχει το index, {@code false} αλλιώς.
     */
    private boolean indexExists(String indexName) {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            // Ελέγχουμε όλα τα indexes του πίνακα UNIVERSITY
            try (ResultSet rs = meta.getIndexInfo(null, null, "UNIVERSITY", false, false)) {
                while (rs.next()) {
                    String idxName = rs.getString("INDEX_NAME");
                    if (indexName.equalsIgnoreCase(idxName)) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "⚠️ Σφάλμα ελέγχου ύπαρξης index: " + indexName, e);
        }
        return false;
    }

    /**
     * Εκτελεί όλα τα {@code CREATE INDEX} statements από ένα μόνο αρχείο .sql.
     * Πριν από κάθε εκτέλεση γίνεται parsing για την εξαγωγή του ονόματος του index και
     * έλεγχος ύπαρξής του.
     *
     * @param stmt       το {@code Statement} για την εκτέλεση εντολών SQL.
     * @param sqlFilePath η διαδρομή προς το αρχείο .sql που περιέχει τα CREATE INDEX statements.
     * @throws IOException  εάν αποτύχει η ανάγνωση του αρχείου.
     * @throws SQLException εάν αποτύχει η εκτέλεση κάποιας εντολής.
     */
    private void createIndexesFromFile(Statement stmt, String sqlFilePath) throws IOException, SQLException {
        String fileContent = loadSQLFromFile(sqlFilePath);

        // Χωρίζουμε το αρχείο σε επιμέρους statements βάσει του χαρακτήρα ';'
        String[] statements = fileContent.split(";");
        for (String rawStmt : statements) {
            String sql = rawStmt.trim();
            if (!sql.isEmpty()) {
                // Αν η εντολή καταλήγει με ';', την αφαιρούμε (για να μην την παρερμηνεύσει ο Derby)
                if (sql.endsWith(";")) {
                    sql = sql.substring(0, sql.length() - 1).trim();
                }
                // Εξαγωγή του ονόματος του index από το statement, π.χ. "CREATE INDEX IDX_UNIVERSITY_NAME ON UNIVERSITY (NAME)"
                String indexName = parseIndexName(sql);
                if (indexName == null) {
                    LOGGER.warning("⚠️ Δεν βρέθηκε όνομα index στη δήλωση: " + sql);
                    continue;
                }

                // Ελέγχουμε αν το index υπάρχει ήδη
                if (!indexExists(indexName)) {
                    stmt.execute(sql);
                    LOGGER.info("✅ Δημιουργήθηκε index: " + indexName);
                } else {
                    LOGGER.info("ℹ️ Index '" + indexName + "' υπάρχει ήδη - παράλειψη.");
                }
            }
        }
    }

    /**
     * Αναλύει (parses) ένα {@code CREATE INDEX} statement για να εξαγάγει το όνομα του index.
     * Υποθέτει ότι η σύνταξη είναι πάντα της μορφής:
     * <pre>
     *     CREATE INDEX &lt;INDEX_NAME&gt; ON &lt;TABLE_NAME&gt; ...
     * </pre>
     *
     * @param createIndexSQL το SQL statement για τη δημιουργία του index.
     * @return το όνομα του index ή {@code null} αν δεν βρεθεί.
     */
    private String parseIndexName(String createIndexSQL) {
        String upper = createIndexSQL.toUpperCase();
        int idxCreate = upper.indexOf("CREATE INDEX ");
        if (idxCreate == -1) {
            return null; // Δεν βρέθηκε το "CREATE INDEX"
        }

        int idxOn = upper.indexOf(" ON ", idxCreate);
        if (idxOn == -1) {
            return null; // Δεν βρέθηκε το " ON "
        }

        // Εξαγωγή του ονόματος του index: αυτό που βρίσκεται ανάμεσα στο "CREATE INDEX " και πριν το " ON "
        String indexName = createIndexSQL.substring(idxCreate + 12, idxOn).trim();
        return indexName.isEmpty() ? null : indexName;
    }

    /**
     * Βοηθητική μέθοδος που διαβάζει ολόκληρο το περιεχόμενο ενός αρχείου (π.χ. .sql ή .txt)
     * με κωδικοποίηση UTF-8 και το επιστρέφει ως {@code String}.
     *
     * @param path η διαδρομή προς το αρχείο.
     * @return το περιεχόμενο του αρχείου ως {@code String}.
     * @throws IOException εάν αποτύχει η ανάγνωση του αρχείου.
     */
    private String loadSQLFromFile(String path) throws IOException {
        byte[] allBytes = Files.readAllBytes(Paths.get(path));
        return new String(allBytes, StandardCharsets.UTF_8);
    }
}
