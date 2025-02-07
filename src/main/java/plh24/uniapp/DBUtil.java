package plh24.uniapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.*;

/**
 * Κλάση διαχείρισης της βάσης δεδομένων Apache Derby.
 * 
 * Περιλαμβάνει τη σύνδεση με τη βάση, τη δημιουργία/διαγραφή πινάκων, 
 * και ένα μενού για διαχείριση από τον χρήστη.
 * 
 * Αυτή η κλάση υλοποιεί το πρότυπο Singleton ώστε να υπάρχει μόνο ένα instance.
 * 
 */
public class DBUtil {
    
    /** Logger για καταγραφή γεγονότων */
    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

    /** URL σύνδεσης με τη βάση δεδομένων Apache Derby */
    private static final String DB_URL = "jdbc:derby:UniDB;create=true";

    /** SQL εντολές για δημιουργία πινάκων και indexes */
    private static final String CREATE_UNIVERSITY_TABLE = "CREATE TABLE UNIVERSITY (" +
            "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
            "NAME VARCHAR(255) NOT NULL, " +
            "COUNTRY VARCHAR(100) NOT NULL, " +
            "ALPHATWOCODE VARCHAR(2), " +
            "STATEPROVINCE VARCHAR(100), " +
            "DOMAINS VARCHAR(255), " +
            "WEBPAGES VARCHAR(255), " +
            "SCHOOL VARCHAR(255), " +
            "DEPARTMENT VARCHAR(255), " +
            "DESCRIPTION VARCHAR(1024), " +
            "CONTACT VARCHAR(255), " +
            "COMMENTS VARCHAR(2048), " +
            "ISMODIFIED BOOLEAN DEFAULT FALSE, " +
            "PRIMARY KEY (ID))";

    private static final String CREATE_UNIVERSITY_VIEW_TABLE = "CREATE TABLE UNIVERSITYVIEW (" +
            "UNIVERSITYID INTEGER NOT NULL, " +
            "VIEWCOUNT INTEGER DEFAULT 0, " +
            "PRIMARY KEY (UNIVERSITYID), " +
            "FOREIGN KEY (UNIVERSITYID) REFERENCES UNIVERSITY (ID) ON DELETE CASCADE)";

    private static final String CREATE_INDEX_NAME = 
            "CREATE INDEX IDX_UNIVERSITY_NAME ON UNIVERSITY (NAME)";
    private static final String CREATE_INDEX_COUNTRY = 
            "CREATE INDEX IDX_UNIVERSITY_COUNTRY ON UNIVERSITY (COUNTRY)";
    private static final String CREATE_INDEX_NAME_COUNTRY = 
            "CREATE INDEX IDX_UNIVERSITY_NAME_COUNTRY ON UNIVERSITY (NAME, COUNTRY)";

    // Μοναδικό instance της κλάσης (eager initialization)
    private static final DBUtil INSTANCE = new DBUtil();

    // Ιδιωτικός constructor για να μην δημιουργηθούν άλλα instances
    private DBUtil() {
        initializeLogger();
    }

    /**
     * Επιστρέφει το μοναδικό instance της κλάσης DBUtil.
     *
     * @return το instance του DBUtil
     */
    public static DBUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Αρχικοποιεί τον Logger και τον ρυθμίζει να γράφει στο αρχείο logs/DBUtil.log.
     */
    public void initializeLogger() {
        try {
            Files.createDirectories(Paths.get("logs"));
            FileHandler fileHandler = new FileHandler("logs/DBUtil.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            for (Handler handler : LOGGER.getHandlers()) {
                LOGGER.removeHandler(handler);
            }
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
            LOGGER.setUseParentHandlers(false);
            LOGGER.info("📌 Έναρξη καταγραφής του Logger στο logs/DBUtil.log");
        } catch (IOException e) {
            System.err.println("⚠️ Σφάλμα κατά την αρχικοποίηση του Logger: " + e.getMessage());
        }
    }

    /**
     * Επιστρέφει σύνδεση με τη βάση δεδομένων.
     *
     * @return Connection προς τη βάση δεδομένων.
     * @throws SQLException αν η σύνδεση αποτύχει.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Δημιουργεί τη βάση δεδομένων (δηλαδή τους πίνακες και τα indexes) αν δεν υπάρχουν ήδη.
     */
    public void initializeDatabase() {
        LOGGER.info("🟢 Έλεγχος και δημιουργία βάσης δεδομένων...");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            if (!tableExists("UNIVERSITY")) {
                createTables(stmt);
                LOGGER.info("✅ Η βάση δεδομένων δημιουργήθηκε επιτυχώς!");
            } else {
                LOGGER.info("ℹ️ Η βάση δεδομένων υπάρχει ήδη.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Σφάλμα κατά την αρχικοποίηση της βάσης", e);
        }
    }

    /**
     * Ελέγχει αν υπάρχει ένας πίνακας στη βάση δεδομένων.
     *
     * @param tableName Το όνομα του πίνακα
     * @return true αν ο πίνακας υπάρχει, false αν δεν υπάρχει.
     */
    private boolean tableExists(String tableName) {
        String sql = "SELECT 1 FROM SYS.SYSTABLES WHERE TABLENAME = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "⚠️ Σφάλμα κατά τον έλεγχο ύπαρξης του πίνακα " + tableName, e);
            return false;
        }
    }

    /**
     * Διαγράφει όλους τους πίνακες και δημιουργεί ξανά τη δομή της βάσης δεδομένων.
     * 
     * Προσοχή: Όλα τα δεδομένα θα χαθούν.
     */
    public void resetDatabase() {
        LOGGER.warning("⚠️ Διαγραφή και επανεκκίνηση της βάσης δεδομένων...");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            dropTables(stmt);
            createTables(stmt);
            LOGGER.info("🔄 Η βάση δεδομένων διαγράφηκε και ξαναδημιουργήθηκε!");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Σφάλμα κατά την επανεκκίνηση της βάσης", e);
        }
    }

    /**
     * Διαγράφει τους πίνακες από τη βάση δεδομένων.
     *
     * @param stmt Το Statement για εκτέλεση εντολών SQL.
     * @throws SQLException αν αποτύχει η διαγραφή.
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
     * Δημιουργεί τους πίνακες και τα indexes της βάσης δεδομένων.
     *
     * @param stmt Το Statement για εκτέλεση εντολών SQL.
     * @throws SQLException αν αποτύχει η δημιουργία.
     */
    private void createTables(Statement stmt) throws SQLException {
        if (!tableExists("UNIVERSITY")) {
            stmt.execute(CREATE_UNIVERSITY_TABLE);
            LOGGER.info("✅ Ο πίνακας UNIVERSITY δημιουργήθηκε.");
        } else {
            LOGGER.info("ℹ️ Ο πίνακας UNIVERSITY υπάρχει ήδη.");
        }

        if (!tableExists("UNIVERSITYVIEW")) {
            stmt.execute(CREATE_UNIVERSITY_VIEW_TABLE);
            LOGGER.info("✅ Ο πίνακας UNIVERSITYVIEW δημιουργήθηκε.");
        } else {
            LOGGER.info("ℹ️ Ο πίνακας UNIVERSITYVIEW υπάρχει ήδη.");
        }

        if (!indexExists("IDX_UNIVERSITY_NAME")) {
            stmt.execute(CREATE_INDEX_NAME);
            LOGGER.info("✅ Δημιουργήθηκε Index: IDX_UNIVERSITY_NAME.");
        }

        if (!indexExists("IDX_UNIVERSITY_COUNTRY")) {
            stmt.execute(CREATE_INDEX_COUNTRY);
            LOGGER.info("✅ Δημιουργήθηκε Index: IDX_UNIVERSITY_COUNTRY.");
        }

        if (!indexExists("IDX_UNIVERSITY_NAME_COUNTRY")) {
            stmt.execute(CREATE_INDEX_NAME_COUNTRY);
            LOGGER.info("✅ Δημιουργήθηκε Index: IDX_UNIVERSITY_NAME_COUNTRY.");
        }
    }

    /**
     * Ελέγχει αν υπάρχει ένα Index στη βάση δεδομένων.
     *
     * @param indexName Το όνομα του Index.
     * @return true αν υπάρχει, false αν δεν υπάρχει.
     */
    private boolean indexExists(String indexName) {
        String sql = "SELECT 1 FROM SYS.SYSCONGLOMERATES WHERE CONGLOMERATENAME = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, indexName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "⚠️ Σφάλμα κατά τον έλεγχο ύπαρξης Index: " + indexName, e);
            return false;
        }
    }

    /**
     * Εκτελεί το μενού επιλογών για τη διαχείριση της βάσης δεδομένων.
     *
     * @param args Δεν απαιτούνται παράμετροι.
     */
    public static void main(String[] args) {
        // Αν και έχουμε το singleton, εδώ καλούμε getInstance() για να χρησιμοποιήσουμε τις μεθόδους του DBUtil
        DBUtil.getInstance().initializeLogger(); // Διπλή κλήση για ασφαλή ρύθμιση
        System.setProperty("file.encoding", "UTF-8");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Διαχείριση Βάσης Δεδομένων =====");
            System.out.println("1. Δημιουργία βάσης (αν δεν υπάρχει)");
            System.out.println("2. Επαναφορά βάσης (διαγραφή και εκ νέου δημιουργία)");
            System.out.println("3. Έξοδος");
            System.out.print("➡️ Επιλέξτε 1-3: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Μη έγκυρη επιλογή. Παρακαλώ εισάγετε αριθμό.");
                continue;
            }

            switch (choice) {
                case 1:
                    DBUtil.getInstance().initializeDatabase();
                    System.out.println("✅ Η βάση δεδομένων δημιουργήθηκε (αν δεν υπήρχε).");
                    break;
                case 2:
                    System.out.print("⚠️ Είστε σίγουρος ότι θέλετε να διαγράψετε και να επανεκκινήσετε τη βάση; (Ν/Ο): ");
                    String confirmation = scanner.nextLine().trim().toLowerCase();
                    if (confirmation.equals("Ν")) {
                        DBUtil.getInstance().resetDatabase();
                        System.out.println("🔄 Η βάση διαγράφηκε και δημιουργήθηκε ξανά.");
                    } else {
                        System.out.println("✅ Η βάση δεδομένων **ΔΕΝ** διαγράφηκε.");
                    }
                    break;
                case 3:
                    System.out.println("👋 Έξοδος από τη διαχείριση βάσης.");
                    scanner.close();
                    return;
                default:
                    System.out.println("⚠️ Μη έγκυρη επιλογή. Δοκιμάστε ξανά.");
            }
        }
    }
}
