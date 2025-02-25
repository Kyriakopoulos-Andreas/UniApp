package plh24.uniapp;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.*;
import java.nio.file.*;
import java.io.IOException;

/**
 * Η κλάση {@code PDFExporter} παρέχει λειτουργικότητα για την εξαγωγή στατιστικών πανεπιστημίων σε αρχείο PDF.
 *
 * <p>
 * Αυτή η κλάση είναι αποσυνδεδεμένη από οποιοδήποτε συγκεκριμένο περιβάλλον χρήστη (UI) και παρέχει στατικές
 * μεθόδους οι οποίες δέχονται μια λίστα αντικειμένων {@link University} και παράγουν ένα αρχείο PDF που περιέχει
 * τα στατιστικά. Το παραγόμενο PDF περιλαμβάνει έναν τίτλο και έναν πίνακα με στήλες για το ID, το Όνομα Πανεπιστημίου,
 * τη Χώρα και τις Προβολές.
 * </p>
 *
 * <p>
 * Για να αποδοθούν σωστά οι ελληνικοί χαρακτήρες, φορτώνεται μια γραμματοσειρά που υποστηρίζει τους ελληνικούς χαρακτήρες,
 * χρησιμοποιώντας ένα relative path. Βεβαιωθείτε ότι το αρχείο γραμματοσειράς (π.χ. FreeSans.ttf) βρίσκεται στο
 * "resources/fonts/FreeSans.ttf" σε σχέση με το working directory της εφαρμογής.
 * </p>
 *
 * <p>
 * <strong>Παράδειγμα Χρήσης:</strong>
 * <pre>
 *    List&lt;University&gt; popularUniversities = UniversityDAO.getInstance().getPopularUniversities();
 *    try {
 *         boolean success = PDFExporter.exportStatisticsToPDF(popularUniversities);
 *         if (success) {
 *              // Ενημέρωση για την επιτυχή δημιουργία του PDF
 *         }
 *    } catch (Exception ex) {
 *         // Διαχείριση της εξαίρεσης κατάλληλα
 *    }
 * </pre>
 * </p>
 */
public class PDFExporter {

    // Logger για καταγραφή μηνυμάτων και σφαλμάτων για την κλάση PDFExporter.
    private static final Logger LOGGER = Logger.getLogger(PDFExporter.class.getName());
    
    // Static initializer για τη ρύθμιση του logger ώστε να γράφει σε αρχείο "logs/PDFExporter.log".
    static {
        try {
            // Δημιουργία του φακέλου "logs" εάν δεν υπάρχει.
            Files.createDirectories(Paths.get("logs"));
            // Αφαίρεση τυχόν υπάρχοντων handlers για αποφυγή διπλών καταγραφών.
            for (Handler h : LOGGER.getHandlers()) {
                LOGGER.removeHandler(h);
            }
            // Δημιουργία FileHandler που γράφει στο αρχείο "logs/PDFExporter.log" σε λειτουργία append.
            FileHandler fileHandler = new FileHandler("logs/PDFExporter.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            
            // Ρύθμιση επιπέδου καταγραφής και απενεργοποίηση του parent handler.
            LOGGER.setLevel(Level.ALL);
            LOGGER.setUseParentHandlers(false);
            
            LOGGER.info("📌 Έναρξη καταγραφής του Logger στο logs/PDFExporter.log");
        } catch (IOException e) {
            System.err.println("❌ Σφάλμα κατά την αρχικοποίηση του Logger για PDFExporter: " + e.getMessage());
        }
    }
    
    // Προεπιλεγμένο relative path προς το αρχείο γραμματοσειράς που υποστηρίζει τους ελληνικούς χαρακτήρες.
    private static final String DEFAULT_FONT_RELATIVE_PATH = "resources/fonts/FreeSans.ttf";

    /**
     * Εξάγει τα στατιστικά των πανεπιστημίων σε αρχείο PDF χρησιμοποιώντας δυναμική ονομασία αρχείου.
     *
     * <p>
     * Αυτή η μέθοδος δέχεται μια λίστα αντικειμένων {@link University} και δημιουργεί ένα PDF με όνομα
     * που βασίζεται στην τρέχουσα ημερομηνία και ώρα, π.χ. "Stats_2024-03-10-11-46-46.pdf". Το PDF περιέχει έναν τίτλο
     * και έναν πίνακα στατιστικών, ενώ χρησιμοποιεί τη γραμματοσειρά που βρίσκεται στο {@value DEFAULT_FONT_RELATIVE_PATH}
     * για να εξασφαλίσει την ορθή απόδοση των ελληνικών χαρακτήρων.
     * </p>
     *
     * @param popularUniversities η λίστα των πανεπιστημίων προς εξαγωγή στο PDF.
     * @return {@code true} αν το PDF δημιουργήθηκε επιτυχώς, {@code false} αν η λίστα είναι κενή ή null.
     * @throws Exception σε περίπτωση σφάλματος κατά την δημιουργία του PDF.
     */
    public static boolean exportStatisticsToPDF(List<University> popularUniversities) throws Exception {
        // Έλεγχος εάν η λίστα είναι null ή κενή.
        if (popularUniversities == null || popularUniversities.isEmpty()) {
            LOGGER.log(Level.INFO, "ℹ️ Δεν υπάρχουν διαθέσιμα στατιστικά για εξαγωγή.");
            return false;
        }
        
        // Δημιουργία δυναμικού ονόματος αρχείου με βάση την τρέχουσα ημερομηνία και ώρα.
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String timestamp = LocalDateTime.now().format(dtf);
        String dynamicFilename = "Stats_" + timestamp + ".pdf";
        
        // Καλεί την υπερφορτωμένη μέθοδο με το δυναμικό όνομα αρχείου.
        return exportStatisticsToPDF(popularUniversities, dynamicFilename, DEFAULT_FONT_RELATIVE_PATH);
    }

    /**
     * Εξάγει τα στατιστικά των πανεπιστημίων σε αρχείο PDF με παραμετροποιήσιμες ρυθμίσεις.
     *
     * <p>
     * Αυτή η μέθοδος δημιουργεί ένα PDF που περιέχει έναν τίτλο και έναν πίνακα με τα στατιστικά των πανεπιστημίων.
     * Ο πίνακας περιλαμβάνει στήλες για το ID, το Όνομα Πανεπιστημίου, τη Χώρα και τις Προβολές. Η μέθοδος χρησιμοποιεί
     * το relative path που παρέχεται για τη φόρτωση της γραμματοσειράς, προκειμένου να αποδοθούν σωστά οι ελληνικοί χαρακτήρες.
     * </p>
     *
     * @param popularUniversities η λίστα των πανεπιστημίων προς εξαγωγή στο PDF.
     * @param outputFilename      το όνομα του αρχείου PDF εξόδου.
     * @param fontPath            το relative path προς το αρχείο γραμματοσειράς (π.χ. "resources/fonts/FreeSans.ttf").
     * @return {@code true} αν το PDF δημιουργήθηκε επιτυχώς, {@code false} αν η λίστα είναι κενή ή null.
     * @throws Exception σε περίπτωση σφάλματος κατά την δημιουργία του PDF.
     */
    public static boolean exportStatisticsToPDF(List<University> popularUniversities, String outputFilename, String fontPath) throws Exception {
        // Έλεγχος εάν η λίστα είναι null ή κενή.
        if (popularUniversities == null || popularUniversities.isEmpty()) {
            LOGGER.log(Level.INFO, "ℹ Δεν υπάρχουν διαθέσιμα στατιστικά για εξαγωγή.");
            return false;
        }

        // Χρήση try-with-resources για αυτόματη διαχείριση πόρων (PdfWriter, PdfDocument, Document).
        try (PdfWriter writer = new PdfWriter(outputFilename);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Φόρτωση της γραμματοσειράς από το relative path.
            PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, true);
            document.setFont(font);

            // Δημιουργία ενός τίτλου για το PDF με κατάλληλη μορφοποίηση.
            Paragraph title = new Paragraph("Στατιστικά Δημοφιλέστερων Αναζητήσεων")
                    .setFontSize(18)                           // Ορισμός μεγέθους γραμματοσειράς.
                    .setBold()                                 // Έντονη γραφή.
                    .setTextAlignment(TextAlignment.CENTER)    // Κεντρική στοίχιση.
                    .setMarginBottom(20);                      // Προσθήκη περιθωρίου στο κάτω μέρος.
            document.add(title);

            // Δημιουργία του πίνακα που θα περιέχει τα στατιστικά.
            Table table = createTable(popularUniversities);
            document.add(table);

            // Καταγραφή μηνύματος επιτυχίας.
            LOGGER.log(Level.INFO, "✅ Το αρχείο {0} δημιουργήθηκε με επιτυχία.", outputFilename);
            return true;
        } catch (Exception e) {
            // Καταγραφή του σφάλματος και ρίψη της εξαίρεσης για περαιτέρω διαχείριση από τον caller.
            LOGGER.log(Level.SEVERE, "❌ Σφάλμα κατά τη δημιουργία του PDF", e);
            throw e;
        }
    }

    /**
     * Δημιουργεί έναν πίνακα που περιέχει τα στατιστικά των πανεπιστημίων.
     *
     * <p>
     * Ο πίνακας περιλαμβάνει στήλες για το ID, το Όνομα Πανεπιστημίου, τη Χώρα και τις Προβολές.
     * Κάθε κελί του πίνακα μορφοποιείται με την κατάλληλη στοίχιση (κεντρική ή αριστερή, ανάλογα με το περιεχόμενο).
     * </p>
     *
     * @param popularUniversities η λίστα των πανεπιστημίων που θα συμπεριληφθούν στον πίνακα.
     * @return ένα {@link Table} αντικείμενο που περιέχει τις επικεφαλίδες και τις γραμμές δεδομένων.
     */
    private static Table createTable(List<University> popularUniversities) {
        // Ορισμός αναλογιών πλάτους στηλών: ID, Όνομα Πανεπιστημίου, Χώρα, Προβολές.
        float[] columnWidths = {1, 4, 3, 2};
        // Δημιουργία του πίνακα με τις καθορισμένες αναλογίες και χρήση όλου του διαθέσιμου πλάτους.
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth();

        // Προσθήκη των κεφαλίδων για κάθε στήλη.
        addHeaderCell(table, "ID");
        addHeaderCell(table, "Όνομα Πανεπιστημίου");
        addHeaderCell(table, "Χώρα");
        addHeaderCell(table, "Προβολές");

        // Προσθήκη των γραμμών δεδομένων για κάθε πανεπιστήμιο.
        for (University uni : popularUniversities) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(uni.getId())))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(uni.getName()))
                    .setTextAlignment(TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(uni.getCountry()))
                    .setTextAlignment(TextAlignment.LEFT));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(uni.getViewCount())))
                    .setTextAlignment(TextAlignment.CENTER));
        }
        // Επιστροφή του πλήρως διαμορφωμένου πίνακα.
        return table;
    }

    /**
     * Προσθέτει ένα κελί επικεφαλίδας στον πίνακα με προκαθορισμένη μορφοποίηση.
     *
     * <p>
     * Το κελί επικεφαλίδας έχει ελαφρύ γκρι φόντο, έντονη γραφή και το κείμενο του κεντρικά στοιχισμένο.
     * </p>
     *
     * @param table ο πίνακας στον οποίο θα προστεθεί το κελί.
     * @param text  το κείμενο της επικεφαλίδας.
     */
    private static void addHeaderCell(Table table, String text) {
        table.addHeaderCell(new Cell().add(new Paragraph(text))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
    }
}
