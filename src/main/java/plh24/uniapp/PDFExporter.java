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
import javax.swing.JOptionPane;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PDFExporter {

    /**
     * Εξάγει τα στατιστικά των πιο δημοφιλών πανεπιστημίων σε αρχείο PDF.
     */
    public static void exportStatisticsToPDF() {
        UniversityDAO dao = UniversityDAO.getInstance();
        List<University> popularUniversities = dao.getPopularUniversities();

        if (popularUniversities.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Δεν υπάρχουν διαθέσιμα στατιστικά για εκτύπωση.");
            return;
        }

        try {
            PdfWriter writer = new PdfWriter("statistics.pdf");
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Φόρτωση γραμματοσειράς που υποστηρίζει ελληνικούς χαρακτήρες χρησιμοποιώντας το path που δούλεψε προηγουμένως
            PdfFont font = PdfFontFactory.createFont("resources/fonts/FreeSans.ttf", PdfEncodings.IDENTITY_H, true);
            document.setFont(font);

            // Προσθήκη τίτλου
            Paragraph title = new Paragraph("Στατιστικά Δημοφιλέστερων Αναζητήσεων")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Ορισμός πίνακα με στήλες
            float[] columnWidths = {1, 4, 3, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .useAllAvailableWidth();

            // Προσθήκη επικεφαλίδων στον πίνακα
            table.addHeaderCell(new Cell().add(new Paragraph("ID"))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Όνομα Πανεπιστημίου"))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Χώρα"))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Προβολές"))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            // Προσθήκη δεδομένων του πίνακα
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

            document.add(table);
            document.close();

            JOptionPane.showMessageDialog(null, "Το αρχείο statistics.pdf δημιουργήθηκε με επιτυχία!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Σφάλμα κατά τη δημιουργία του PDF: " + e.getMessage(),
                    "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }
}

