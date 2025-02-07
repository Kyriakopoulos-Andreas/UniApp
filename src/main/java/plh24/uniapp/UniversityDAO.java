package plh24.uniapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * DAO (Data Access Object) για τη διαχείριση των δεδομένων των Πανεπιστημίων.
 * 
 * Παρέχει μεθόδους για εισαγωγή, ενημέρωση, αναζήτηση και στατιστικά για τα πανεπιστήμια στη βάση δεδομένων Derby.
 * Χρησιμοποιεί try-with-resources για σωστή διαχείριση των πόρων και το Logger για καταγραφή συμβάντων.
 * Εφαρμόζει το πρότυπο Singleton ώστε να υπάρχει μόνο ένα instance.
 * 
 */
public class UniversityDAO {

    /** Logger για την κλάση UniversityDAO. */
    private static final Logger LOGGER = Logger.getLogger(UniversityDAO.class.getName());

    /** Το μοναδικό instance της κλάσης (Singleton, eager initialization). */
    private static final UniversityDAO INSTANCE = new UniversityDAO();

    // Static block για αρχικοποίηση του Logger κατά τη φόρτωση της κλάσης.
    static {
        initializeLogger();
    }

    /**
     * Ιδιωτικός constructor ώστε να μην δημιουργούνται εξωτερικά νέα instances.
     */
    private UniversityDAO() {
        // Προαιρετικές αρχικοποιήσεις εάν χρειαστούν.
    }

    /**
     * Επιστρέφει το μοναδικό instance της κλάσης UniversityDAO.
     *
     * @return το instance του UniversityDAO.
     */
    public static UniversityDAO getInstance() {
        return INSTANCE;
    }

    /**
     * Αρχικοποιεί τον Logger και τον ρυθμίζει να γράφει στο αρχείο logs/UniversityDAO.log.
     */
    public static void initializeLogger() {
        try {
            Files.createDirectories(Paths.get("logs"));
            FileHandler fileHandler = new FileHandler("logs/UniversityDAO.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            // Αφαίρεση τυχόν προηγούμενων Handlers
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
     * Αναζητά ένα πανεπιστήμιο βάσει του ονόματος και της χώρας.
     *
     * @param name    το όνομα του πανεπιστημίου.
     * @param country η χώρα του πανεπιστημίου.
     * @return το αντικείμενο University αν βρεθεί, αλλιώς {@code null}.
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
            LOGGER.log(Level.SEVERE, "❌️ Σφάλμα κατά την ανάκτηση του πανεπιστημίου με όνομα '" + name +
                    "' και χώρα '" + country + "'", e);
        }
        return uni;
    }

    /**
     * Εισάγει ένα νέο πανεπιστήμιο στη βάση δεδομένων.
     *
     * @param uni το αντικείμενο University που θα εισαχθεί.
     * @return ✅ {@code true} αν η εισαγωγή ήταν επιτυχής, αλλιώς ⚠❌ {@code false}.
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
     *
     * @param uni το αντικείμενο University που θα ενημερωθεί.
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
    * Αν το πανεπιστήμιο δεν υπάρχει, πραγματοποιείται εισαγωγή (INSERT).
    * Εάν υπάρχει και δεν έχει τροποποιηθεί τοπικά, πραγματοποιείται ενημέρωση (UPDATE).
    *
    * @param uni το αντικείμενο University που θα εισαχθεί ή θα ενημερωθεί.
    * @return ✅ {@code true} αν έγινε εισαγωγή νέας εγγραφής, 🔄 ή ⚠ {@code false} αν έγινε ενημέρωση ή καμία αλλαγή.
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
    * Αυξάνει τον μετρητή προβολών για το πανεπιστήμιο με το δοσμένο ID.
    *
    * @param universityId το αναγνωριστικό του πανεπιστημίου.
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
    * Επιστρέφει μια λίστα με τα πιο δημοφιλή πανεπιστήμια βάσει του αριθμού προβολών.
    *
    * @return λίστα με τα πανεπιστήμια ταξινομημένα κατά φθίνουσα σειρά προβολών.
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
    * Επιστρέφει όλα τα πανεπιστήμια από τη βάση δεδομένων.
    *
    * @return λίστα με όλα τα πανεπιστήμια.
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
    * Επιστρέφει τη λίστα όλων των χωρών που υπάρχουν στη βάση δεδομένων.
    *
    * @return λίστα με μοναδικές χώρες ταξινομημένες αλφαβητικά. Χρήση σε ComboBox.
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
    * Εξάγει ένα αντικείμενο University από το ResultSet.
    *
    * @param rs το ResultSet που περιέχει τα δεδομένα του πανεπιστημίου.
    * @return το αντικείμενο University.
    * @throws SQLException ⚠️ αν παρουσιαστεί πρόβλημα κατά την ανάκτηση των δεδομένων.
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
     * Αναζητά ένα πανεπιστήμιο βάσει του ID.
     *
     * @param id το ID του πανεπιστημίου.
     * @return το αντικείμενο University αν βρεθεί, αλλιώς {@code null}.
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
     *
     * @param name    το όνομα (ή μέρος) του πανεπιστημίου.
     * @param country η χώρα (ή μέρος) του πανεπιστημίου.
     * @return λίστα με τα πανεπιστήμια που ταιριάζουν στα κριτήρια αναζήτησης.
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

        // Εκτέλεση της SQL εντολής χρησιμοποιώντας try-with-resources.
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
     * Βοηθητική μέθοδος για την εκτέλεση μιας SQL εντολής με παραμέτρους και την ενημέρωση δεδομένων.
     *
     * @param sql       η SQL εντολή για την ενημέρωση.
     * @param uni       το αντικείμενο University που περιέχει τα δεδομένα.
     * @param userEdit  {@code true} αν πρόκειται για ενημέρωση από τον χρήστη, {@code false} για κανονική ενημέρωση.
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
