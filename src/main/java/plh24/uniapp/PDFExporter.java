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
 * Η κλάση είναι αποσυνδεδεμένη από το UI και διαθέτει μία μέθοδο που δέχεται μια λίστα αντικειμένων {@link University}
 * και δημιουργεί ένα PDF που περιέχει έναν τίτλο και έναν πίνακα στατιστικών (ID, Όνομα Πανεπιστημίου, Χώρα, Προβολές).
 * Για την ορθή απόδοση των ελληνικών χαρακτήρων χρησιμοποιείται η γραμματοσειρά που βρίσκεται στο relative path
 * {@value DEFAULT_FONT_RELATIVE_PATH}. Το όνομα του PDF δημιουργείται δυναμικά, π.χ. "Stats_2025-03-05-11-46-46.pdf".
 * </p>
 *
 */
public class PDFExporter {

    // Logger για καταγραφή μηνυμάτων και σφαλμάτων για την κλάση PDFExporter.
    private static final Logger LOGGER = Logger.getLogger(PDFExporter.class.getName());

    /**
     * Static initializer block της κλάσης.
     * <p>
     * Αυτό το block εκτελείται μία φορά, κατά την πρώτη φόρτωση της κλάσης από το JVM.
     * Καλεί τη μέθοδο {@code initializeLogger()} για να ρυθμίσει τον logger,
     * ώστε όλα τα logs να καταγράφονται στο αρχείο "logs/PDFExporter.log".
     * </p>
     */
    static {
        initializeLogger();
    }
    
    /**
     * Αρχικοποιεί τον logger ώστε να γράφει τα logs σε εξωτερικό αρχείο.
     * <p>
     * Δημιουργεί τον φάκελο "logs" (αν δεν υπάρχει), αφαιρεί τους υπάρχοντες handlers,
     * και προσθέτει έναν FileHandler που γράφει στο αρχείο "logs/PDFExporter.log" σε λειτουργία append.
     * Ορίζει επίσης το επίπεδο καταγραφής σε ALL και απενεργοποιεί τους parent handlers.
     * </p>
     */
    private static void initializeLogger() {
        try {
            // Δημιουργία του φακέλου "logs", εάν δεν υπάρχει.
            Files.createDirectories(Paths.get("logs"));
            // Αφαίρεση τυχόν υπάρχοντων handlers για αποφυγή διπλών καταγραφών.
            for (Handler h : LOGGER.getHandlers()) {
                LOGGER.removeHandler(h);
            }
            // Δημιουργία FileHandler που γράφει στο "logs/PDFExporter.log" σε λειτουργία append.
            FileHandler fileHandler = new FileHandler("logs/PDFExporter.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            
            // Ρύθμιση επιπέδου καταγραφής και απενεργοποίηση των parent handlers.
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
     * Εξάγει τα στατιστικά των πανεπιστημίων σε αρχείο PDF.
     *
     * <p>
     * Αυτή η μέθοδος δέχεται μια λίστα αντικειμένων {@link University} και δημιουργεί ένα PDF με όνομα
     * που βασίζεται στην τρέχουσα ημερομηνία και ώρα (π.χ. "Stats_2025-03-05-11-46-46.pdf"). Το PDF περιέχει έναν τίτλο
     * και έναν πίνακα στατιστικών, ενώ χρησιμοποιεί τη γραμματοσειρά που βρίσκεται στο {@value DEFAULT_FONT_RELATIVE_PATH}
     * για την ορθή απόδοση των ελληνικών χαρακτήρων.
     * </p>
     *
     * @param popularUniversities η λίστα των πανεπιστημίων προς εξαγωγή στο PDF.
     * @return {@code true} αν το PDF δημιουργήθηκε επιτυχώς, {@code false} αν η λίστα είναι null ή κενή.
     * @throws Exception σε περίπτωση σφάλματος κατά την δημιουργία του PDF.
     */
    public static boolean exportStatisticsToPDF(List<University> popularUniversities) throws Exception {
        // Έλεγχος αν η λίστα είναι null ή κενή.
        if (popularUniversities == null || popularUniversities.isEmpty()) {
            LOGGER.log(Level.INFO, "ℹ️ Δεν υπάρχουν διαθέσιμα στατιστικά για εξαγωγή.");
            return false;
        }
        
        // Δημιουργία δυναμικού ονόματος αρχείου με βάση την τρέχουσα ημερομηνία και ώρα.
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String timestamp = LocalDateTime.now().format(dtf);
        String dynamicFilename = "Stats_" + timestamp + ".pdf";
        
        // Χρήση try-with-resources για αυτόματη διαχείριση πόρων (PdfWriter, PdfDocument, Document).
        try (PdfWriter writer = new PdfWriter(dynamicFilename);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Φόρτωση της γραμματοσειράς από το relative path.
            PdfFont font = PdfFontFactory.createFont(DEFAULT_FONT_RELATIVE_PATH, PdfEncodings.IDENTITY_H, true);
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
            LOGGER.log(Level.INFO, "✅ Το αρχείο {0} δημιουργήθηκε με επιτυχία.", dynamicFilename);
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
     * Κάθε κελί μορφοποιείται με την κατάλληλη στοίχιση (κεντρική ή αριστερή, ανάλογα με το περιεχόμενο).
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
