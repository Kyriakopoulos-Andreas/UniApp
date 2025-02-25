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
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Η κλάση {@code PDFExporter} παρέχει λειτουργικότητα για την εξαγωγή
 * στατιστικών πανεπιστημίων σε αρχείο PDF, χωρίς εξάρτηση από το UI.
 */
public class PDFExporter {

    private static final Logger LOGGER = Logger.getLogger(PDFExporter.class.getName());
    private static final String DEFAULT_OUTPUT_FILENAME = "statistics.pdf";
    private static final String DEFAULT_FONT_RELATIVE_PATH = "resources/fonts/FreeSans.ttf";

    /**
     * Εξάγει τα στατιστικά πανεπιστημίων σε αρχείο PDF χρησιμοποιώντας τις προεπιλεγμένες ρυθμίσεις.
     *
     * @param popularUniversities η λίστα των πανεπιστημίων προς εξαγωγή στο PDF.
     * @return {@code true} αν η εξαγωγή ήταν επιτυχής, {@code false} διαφορετικά.
     * @throws Exception σε περίπτωση σφάλματος κατά τη δημιουργία του PDF.
     */
    public static boolean exportStatisticsToPDF(List<University> popularUniversities) throws Exception {
        return exportStatisticsToPDF(popularUniversities, DEFAULT_OUTPUT_FILENAME, DEFAULT_FONT_RELATIVE_PATH);
    }

    /**
     * Εξάγει τα στατιστικά πανεπιστημίων σε αρχείο PDF με παραμετροποιήσιμα settings.
     *
     * @param popularUniversities η λίστα των πανεπιστημίων προς εξαγωγή στο PDF.
     * @param outputFilename      το όνομα του αρχείου εξόδου.
     * @param fontPath            το relative path της γραμματοσειράς.
     * @return {@code true} αν η εξαγωγή ήταν επιτυχής, {@code false} διαφορετικά.
     * @throws Exception σε περίπτωση σφάλματος κατά τη δημιουργία του PDF.
     */
    public static boolean exportStatisticsToPDF(List<University> popularUniversities, String outputFilename, String fontPath) throws Exception {
        if (popularUniversities == null || popularUniversities.isEmpty()) {
            LOGGER.log(Level.INFO, "Δεν υπάρχουν διαθέσιμα στατιστικά για εξαγωγή.");
            return false;
        }

        try (PdfWriter writer = new PdfWriter(outputFilename);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Φόρτωση γραμματοσειράς μέσω relative path
            PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, true);
            document.setFont(font);

            // Προσθήκη τίτλου στο PDF
            Paragraph title = new Paragraph("Στατιστικά Δημοφιλέστερων Αναζητήσεων")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Δημιουργία πίνακα με τα στατιστικά
            Table table = createTable(popularUniversities);
            document.add(table);

            LOGGER.log(Level.INFO, "Το αρχείο {0} δημιουργήθηκε με επιτυχία!", outputFilename);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Σφάλμα κατά τη δημιουργία του PDF", e);
            throw e;  // Εναλλακτικά μπορείς να επιστρέψεις false
        }
    }

    /**
     * Δημιουργεί έναν πίνακα με τα στατιστικά πανεπιστημίων.
     *
     * @param popularUniversities η λίστα των πανεπιστημίων προς εξαγωγή στο PDF.
     * @return ο διαμορφωμένος {@link Table} που περιέχει τις επικεφαλίδες και τα δεδομένα.
     */
    private static Table createTable(List<University> popularUniversities) {
        float[] columnWidths = {1, 4, 3, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth();

        addHeaderCell(table, "ID");
        addHeaderCell(table, "Όνομα Πανεπιστημίου");
        addHeaderCell(table, "Χώρα");
        addHeaderCell(table, "Προβολές");

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
     * @param table το {@link Table} στο οποίο θα προστεθεί το κελί.
     * @param text  το κείμενο της επικεφαλίδας.
     */
    private static void addHeaderCell(Table table, String text) {
        table.addHeaderCell(new Cell().add(new Paragraph(text))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
    }
}
