package plh24.uniapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * <h2>DAO για τη Διαχείριση Πανεπιστημίων (UniversityDAO)</h2>
 * 
 * <p>
 * Αυτή η κλάση είναι ένα Data Access Object (DAO) που έχει σχεδιαστεί για να παρέχει μεθόδους
 * για την εισαγωγή, ενημέρωση, ανάκτηση, αναζήτηση και στατιστική ανάλυση δεδομένων για τα
 * πανεπιστήμια που αποθηκεύονται σε μια βάση δεδομένων Apache Derby.
 * 
 * 
 * <p>
 * Χαρακτηριστικά:
 * <ul>
 *   <li>Χρήση try-with-resources για την αυτόματη απελευθέρωση των πόρων (συνδέσεις, statements, result sets).</li>
 *   <li>Χρήση του Logger για την καταγραφή σημαντικών συμβάντων, σφαλμάτων και πληροφοριών.</li>
 *   <li>Εφαρμογή του προτύπου Singleton ώστε να υπάρχει μόνο ένα instance της κλάσης.</li>
 * </ul>
 * 
 */
public class UniversityDAO {

    /** Logger για την καταγραφή συμβάντων της κλάσης UniversityDAO. */
    private static final Logger LOGGER = Logger.getLogger(UniversityDAO.class.getName());

    /** Το μοναδικό instance της κλάσης (Singleton, eager initialization). */
    private static final UniversityDAO INSTANCE = new UniversityDAO();

    // Static block για την αρχικοποίηση του Logger κατά τη φόρτωση της κλάσης.
    static {
        initializeLogger();
    }

    /**
     * Ο ιδιωτικός constructor αποτρέπει τη δημιουργία επιπλέον instances της κλάσης.
     */
    private UniversityDAO() {
        // Μπορούν να τοποθετηθούν επιπλέον αρχικοποιήσεις εάν χρειαστεί.
    }

    /**
     * Επιστρέφει το μοναδικό instance της κλάσης UniversityDAO.
     *
     * @return Το instance του UniversityDAO.
     */
    public static UniversityDAO getInstance() {
        return INSTANCE;
    }

    /**
     * Αρχικοποιεί τον Logger και τον ρυθμίζει να γράφει τα μηνύματα στο αρχείο 
     * <code>logs/UniversityDAO.log</code> χρησιμοποιώντας τον {@code SimpleFormatter}.
     * <p>
     * Αν υπάρχουν ήδη προηγούμενοι handlers, αυτοί αφαιρούνται για να αποφευχθούν διπλές καταγραφές.
     * </p>
     */
    public static void initializeLogger() {
        try {
            Files.createDirectories(Paths.get("logs"));
            FileHandler fileHandler = new FileHandler("logs/UniversityDAO.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            // Αφαίρεση όλων των προηγούμενων handlers.
            for (Handler handler : LOGGER.getHandlers()) {
                LOGGER.removeHandler(handler);
            }
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
            LOGGER.setUseParentHandlers(false);
            LOGGER.info("📌 Έναρξη καταγραφής του Logger στο logs/UniversityDAO.log");
        } catch (IOException e) {
            System.err.println("❌️ Σφάλμα κατά την αρχικοποίηση του Logger: " + e.getMessage());
        }
    }

    /**
     * Αναζητά ένα πανεπιστήμιο στη βάση δεδομένων βάσει του ονόματος και της χώρας.
     * <p>
     * Εκτελεί ένα SELECT ερώτημα με χρήση παραμέτρων για αποφυγή SQL injection.
     * </p>
     *
     * @param name    Το όνομα του πανεπιστημίου.
     * @param country Η χώρα στην οποία βρίσκεται το πανεπιστήμιο.
     * @return Το αντικείμενο <code>University</code> αν βρεθεί, αλλιώς <code>null</code>.
     */
    public University getUniversityByNameAndCountry(String name, String country) {
        String sql = "SELECT * FROM UNIVERSITY WHERE NAME = ? AND COUNTRY = ?";
        University uni = null;
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, country);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    uni = extractUniversity(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την ανάκτηση του πανεπιστημίου με όνομα '" 
                    + name + "' και χώρα '" + country + "'", e);
        }
        return uni;
    }

    /**
     * Εισάγει ένα νέο πανεπιστήμιο στη βάση δεδομένων.
     * <p>
     * Εκτελεί ένα INSERT ερώτημα με χρήση παραμέτρων και επιστρέφει <code>true</code> αν η εισαγωγή
     * ήταν επιτυχής.
     * </p>
     *
     * @param uni Το αντικείμενο <code>University</code> που θα εισαχθεί.
     * @return <code>true</code> αν η εισαγωγή ήταν επιτυχής, αλλιώς <code>false</code>.
     */
    public boolean insertUniversity(University uni) {
        String sql = "INSERT INTO UNIVERSITY (NAME, COUNTRY, ALPHATWOCODE, STATEPROVINCE, DOMAINS, WEBPAGES, " +
                     "SCHOOL, DEPARTMENT, DESCRIPTION, CONTACT, COMMENTS, ISMODIFIED) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, uni.getName());
            ps.setString(2, uni.getCountry());
            ps.setString(3, uni.getAlphaTwoCode());
            ps.setString(4, uni.getStateProvince());
            ps.setString(5, (uni.getDomains() != null) ? uni.getDomains() : "");
            ps.setString(6, (uni.getWebPages() != null) ? uni.getWebPages() : "");
            ps.setString(7, (uni.getSchool() != null) ? uni.getSchool() : "");
            ps.setString(8, (uni.getDepartment() != null) ? uni.getDepartment() : "");
            ps.setString(9, (uni.getDescription() != null) ? uni.getDescription() : "");
            ps.setString(10, (uni.getContact() != null) ? uni.getContact() : "");
            ps.setString(11, (uni.getComments() != null) ? uni.getComments() : "");
            ps.setBoolean(12, uni.isModified());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LOGGER.log(Level.INFO, "✅ Το πανεπιστήμιο προστέθηκε επιτυχώς: {0}", uni.getName());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "⚠️ Δεν προστέθηκε το πανεπιστήμιο: {0}", uni.getName());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την εισαγωγή του πανεπιστημίου: " + uni.getName(), e);
            return false;
        }
    }

    /**
     * Ενημερώνει τα δεδομένα ενός πανεπιστημίου που έχει τροποποιηθεί από τον χρήστη.
     * <p>
     * Εκτελεί ένα UPDATE ερώτημα για το συγκεκριμένο πανεπιστήμιο βάσει του ID του.
     * </p>
     *
     * @param uni Το αντικείμενο <code>University</code> με τις νέες τιμές.
     */
    public void updateUniversityUser(University uni) {
        String sql = "UPDATE UNIVERSITY SET NAME=?, COUNTRY=?, ALPHATWOCODE=?, STATEPROVINCE=?, DOMAINS=?, " +
                     "WEBPAGES=?, SCHOOL=?, DEPARTMENT=?, DESCRIPTION=?, CONTACT=?, COMMENTS=?, ISMODIFIED=? " +
                     "WHERE ID=?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uni.getName());
            ps.setString(2, uni.getCountry());
            ps.setString(3, uni.getAlphaTwoCode());
            ps.setString(4, uni.getStateProvince());
            ps.setString(5, uni.getDomains());
            ps.setString(6, uni.getWebPages());
            ps.setString(7, uni.getSchool());
            ps.setString(8, uni.getDepartment());
            ps.setString(9, uni.getDescription());
            ps.setString(10, uni.getContact());
            ps.setString(11, uni.getComments());
            ps.setBoolean(12, uni.isModified());
            ps.setInt(13, uni.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LOGGER.log(Level.INFO, "✅ Το πανεπιστήμιο ενημερώθηκε: {0}", uni.getName());
            } else {
                LOGGER.log(Level.WARNING, "⚠️ Δεν πραγματοποιήθηκαν αλλαγές: {0}", uni.getName());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την ενημέρωση του πανεπιστημίου: " + uni.getName(), e);
        }
    }

    /**
     * Ελέγχει αν το πανεπιστήμιο υπάρχει ήδη στη βάση δεδομένων με βάση το όνομα και τη χώρα.
     * <p>
     * Αν δεν υπάρχει, πραγματοποιείται εισαγωγή (INSERT). Αν υπάρχει και δεν έχει τροποποιηθεί τοπικά,
     * πραγματοποιείται ενημέρωση (UPDATE). Εάν υπάρχει και έχει τροποποιηθεί τοπικά, επιστρέφεται false.
     * </p>
     *
     * @param uni Το αντικείμενο <code>University</code> που θα εισαχθεί ή θα ενημερωθεί.
     * @return <code>true</code> αν έγινε εισαγωγή νέας εγγραφής, <code>false</code> σε περίπτωση ενημέρωσης ή αν
     *         δεν πραγματοποιήθηκαν αλλαγές.
     */
    public boolean upsertUniversity(University uni) {
        University existing = getUniversityByNameAndCountry(uni.getName(), uni.getCountry());
        if (existing == null) {
            boolean inserted = insertUniversity(uni);
            if (inserted) {
                LOGGER.log(Level.INFO, "✅ Νέο πανεπιστήμιο εισήχθη: {0}", uni.getName());
            }
            return inserted;
        } else if (!existing.isModified()) {
            uni.setId(existing.getId());
            updateUniversityUser(uni);
            LOGGER.log(Level.INFO, "🔄 Το υπάρχον πανεπιστήμιο ενημερώθηκε: {0}", uni.getName());
            return false;
        } else {
            LOGGER.log(Level.WARNING, "⚠️ Το πανεπιστήμιο υπάρχει ήδη και έχει τροποποιηθεί τοπικά: {0}", uni.getName());
            return false;
        }
    }

    /**
     * Αυξάνει τον μετρητή προβολών για το πανεπιστήμιο με το δοσμένο αναγνωριστικό.
     * <p>
     * Ελέγχει πρώτα εάν υπάρχει εγγραφή για το συγκεκριμένο πανεπιστήμιο στον πίνακα
     * {@code UNIVERSITYVIEW}. Αν υπάρχει, ενημερώνει τον μετρητή (increment). Αν δεν υπάρχει,
     * εισάγει μια νέα εγγραφή με αρχικό μετρητή 1.
     * </p>
     *
     * @param universityId Το αναγνωριστικό του πανεπιστημίου.
     */
    public void increaseViewCount(int universityId) {
        String checkSql = "SELECT VIEWCOUNT FROM UNIVERSITYVIEW WHERE UNIVERSITYID = ?";
        String updateSql = "UPDATE UNIVERSITYVIEW SET VIEWCOUNT = VIEWCOUNT + 1 WHERE UNIVERSITYID = ?";
        String insertSql = "INSERT INTO UNIVERSITYVIEW (UNIVERSITYID, VIEWCOUNT) VALUES (?, 1)";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, universityId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, universityId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, universityId);
                        insertStmt.executeUpdate();
                    }
                }
            }
            LOGGER.log(Level.INFO, "👁️ Ο μετρητής προβολών αυξήθηκε για το πανεπιστήμιο με ID: {0}", universityId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την αύξηση του μετρητή προβολών για το πανεπιστήμιο με ID: " 
                    + universityId, e);
        }
    }

    /**
     * Επιστρέφει μια λίστα με τα πιο δημοφιλή πανεπιστήμια, ταξινομημένα κατά φθίνουσα σειρά
     * με βάση τον αριθμό προβολών.
     * <p>
     * Συνδυάζει δεδομένα από τους πίνακες {@code UNIVERSITY} και {@code UNIVERSITYVIEW}.
     * </p>
     *
     * @return Λίστα αντικειμένων <code>University</code> με τα πιο δημοφιλή πανεπιστήμια.
     */
    public List<University> getPopularUniversities() {
        List<University> popularList = new ArrayList<>();
        String sql = "SELECT U.ID, U.NAME, U.COUNTRY, S.VIEWCOUNT " +
                     "FROM UNIVERSITY U " +
                     "JOIN UNIVERSITYVIEW S ON U.ID = S.UNIVERSITYID " +
                     "ORDER BY S.VIEWCOUNT DESC";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                University uni = new University();
                uni.setId(rs.getInt("ID"));
                uni.setName(rs.getString("NAME"));
                uni.setCountry(rs.getString("COUNTRY"));
                uni.setViewCount(rs.getInt("VIEWCOUNT"));
                popularList.add(uni);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την ανάκτηση των πιο δημοφιλών πανεπιστημίων", e);
        }
        return popularList;
    }

    /**
     * Επιστρέφει μια λίστα με όλα τα πανεπιστήμια που υπάρχουν στη βάση δεδομένων.
     *
     * @return Λίστα αντικειμένων <code>University</code> που αναπαριστούν όλα τα πανεπιστήμια.
     */
    public List<University> getAllUniversities() {
        List<University> list = new ArrayList<>();
        String sql = "SELECT * FROM UNIVERSITY";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                University uni = new University();
                uni.setId(rs.getInt("ID"));
                uni.setName(rs.getString("NAME"));
                uni.setCountry(rs.getString("COUNTRY"));
                uni.setAlphaTwoCode(rs.getString("ALPHATWOCODE"));
                uni.setStateProvince(rs.getString("STATEPROVINCE"));
                uni.setDomains(rs.getString("DOMAINS"));
                uni.setWebPages(rs.getString("WEBPAGES"));
                uni.setSchool(rs.getString("SCHOOL"));
                uni.setDepartment(rs.getString("DEPARTMENT"));
                uni.setDescription(rs.getString("DESCRIPTION"));
                uni.setContact(rs.getString("CONTACT"));
                uni.setComments(rs.getString("COMMENTS"));
                uni.setModified(rs.getBoolean("ISMODIFIED"));
                list.add(uni);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την ανάκτηση όλων των πανεπιστημίων", e);
        }
        return list;
    }

    /**
     * Επιστρέφει μια λίστα με όλες τις χώρες που υπάρχουν στα δεδομένα των πανεπιστημίων,
     * ταξινομημένες αλφαβητικά. Η λίστα χρησιμοποιείται για παραδείγματα, όπως σε ComboBox.
     *
     * @return Λίστα <code>String</code> με μοναδικές χώρες.
     */
    public List<String> getAllCountries() {
        List<String> countryList = new ArrayList<>();
        String sql = "SELECT DISTINCT COUNTRY FROM UNIVERSITY ORDER BY COUNTRY";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                countryList.add(rs.getString("COUNTRY"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την ανάκτηση όλων των χωρών", e);
        }
        return countryList;
    }

    /**
     * Εξάγει και επιστρέφει ένα αντικείμενο <code>University</code> από το δεδομένο {@code ResultSet}.
     *
     * @param rs Το {@code ResultSet} που περιέχει τα δεδομένα του πανεπιστημίου.
     * @return Το αντικείμενο <code>University</code> που προκύπτει από τα δεδομένα.
     * @throws SQLException Εάν παρουσιαστεί σφάλμα κατά την ανάγνωση των δεδομένων.
     */
    private University extractUniversity(ResultSet rs) throws SQLException {
        return new University(
                rs.getInt("ID"),
                rs.getString("NAME"),
                rs.getString("COUNTRY"),
                rs.getString("ALPHATWOCODE"),
                rs.getString("STATEPROVINCE"),
                rs.getString("DOMAINS"),
                rs.getString("WEBPAGES"),
                rs.getString("SCHOOL"),
                rs.getString("DEPARTMENT"),
                rs.getString("DESCRIPTION"),
                rs.getString("CONTACT"),
                rs.getString("COMMENTS"),
                rs.getBoolean("ISMODIFIED")
        );
    }

    /**
     * Αναζητά ένα πανεπιστήμιο στη βάση δεδομένων βάσει του αναγνωριστικού του.
     *
     * @param id Το αναγνωριστικό του πανεπιστημίου.
     * @return Το αντικείμενο <code>University</code> αν βρεθεί, αλλιώς <code>null</code>.
     */
    public University getUniversityById(int id) {
        University uni = null;
        String sql = "SELECT * FROM UNIVERSITY WHERE ID = ?";
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    uni = extractUniversity(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την ανάκτηση του πανεπιστημίου με ID: " + id, e);
        }
        return uni;
    }

    /**
     * Αναζητά πανεπιστήμια χρησιμοποιώντας το LIKE για μερική αντιστοιχία στο όνομα και/ή στη χώρα.
     * <p>
     * Εάν τα κριτήρια είναι κενά, επιστρέφει όλα τα πανεπιστήμια.
     * </p>
     *
     * @param name    Το όνομα (ή μέρος του ονόματος) του πανεπιστημίου.
     * @param country Η χώρα (ή μέρος της χώρας) του πανεπιστημίου.
     * @return Λίστα με αντικείμενα <code>University</code> που ταιριάζουν στα κριτήρια αναζήτησης.
     */
    public List<University> searchUniversities(String name, String country) {
        List<University> list = new ArrayList<>();

        // Εάν και τα δύο κριτήρια είναι κενά, επιστρέφονται όλα τα πανεπιστήμια.
        if (name.isEmpty() && country.isEmpty()) {
            return getAllUniversities();
        }

        // Δημιουργία της SQL εντολής με δυναμική σύνθεση παραμέτρων.
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM UNIVERSITY WHERE ");
        List<String> params = new ArrayList<>();

        if (!name.isEmpty()) {
            sqlBuilder.append("NAME LIKE ?");
            params.add("%" + name + "%");
        }
        if (!country.isEmpty()) {
            if (!params.isEmpty()) {
                sqlBuilder.append(" AND ");
            }
            sqlBuilder.append("COUNTRY LIKE ?");
            params.add("%" + country + "%");
        }

        String sql = sqlBuilder.toString();

        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Ορισμός των παραμέτρων της SQL εντολής.
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }

            // Εκτέλεση του query και μετατροπή των αποτελεσμάτων σε αντικείμενα University.
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractUniversity(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την αναζήτηση πανεπιστημίων με όνομα: " 
                    + name + " και χώρα: " + country, e);
        }
        return list;
    }

    /**
     * Βοηθητική μέθοδος για την εκτέλεση μιας SQL εντολής ενημέρωσης δεδομένων με παραμέτρους.
     * <p>
     * Αυτή η μέθοδος χρησιμοποιείται για την κοινή λογική ενημέρωσης, ώστε να μην επαναλαμβάνεται ο κώδικας.
     * </p>
     *
     * @param sql      Η SQL εντολή ενημέρωσης.
     * @param uni      Το αντικείμενο <code>University</code> που περιέχει τα δεδομένα για την ενημέρωση.
     * @param userEdit <code>true</code> αν η ενημέρωση προέρχεται από επεξεργασία χρήστη, <code>false</code> διαφορετικά.
     */
    private void executeUpdate(String sql, University uni, boolean userEdit) {
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uni.getName());
            ps.setString(2, uni.getCountry());
            ps.setString(3, uni.getAlphaTwoCode());
            ps.setString(4, uni.getStateProvince());
            ps.setString(5, uni.getDomains());
            ps.setString(6, uni.getWebPages());
            ps.setString(7, uni.getSchool());
            ps.setString(8, uni.getDepartment());
            ps.setString(9, uni.getDescription());
            ps.setString(10, uni.getContact());
            ps.setString(11, uni.getComments());
            ps.setBoolean(12, userEdit);
            ps.setInt(13, uni.getId());

            ps.executeUpdate();

            LOGGER.log(Level.FINE, "✅ Εκτελέστηκε SQL: {0} με παραμέτρους: Όνομα={1}, Χώρα={2}, ID={3}, Τροποποιήθηκε={4}",
                    new Object[]{sql, uni.getName(), uni.getCountry(), uni.getId(), uni.isModified()});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την εκτέλεση ενημέρωσης με SQL: " + sql, e);
        }
    }
}
