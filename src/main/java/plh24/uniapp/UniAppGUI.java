package plh24.uniapp;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;

/**
 *
 * @author Στέλιος
 */
public class UniAppGUI extends JFrame {
    private JTable universityTable;
    private DefaultTableModel tableModel;
    private JButton searchButton, refreshButton, loadButton;
    private JComboBox<String> countryComboBox;
    private JTextField txtSearchName;
    private JProgressBar progressBar;

    /**
     *
     */
    public UniAppGUI() {
        setTitle("UniApp - Πανεπιστήμια");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 🔹 Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.SOUTH);

        // 🔹 Πάνελ αναζήτησης
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Όνομα:"));
        txtSearchName = new JTextField(15);
        searchPanel.add(txtSearchName);

        searchPanel.add(new JLabel("Χώρα:"));
        countryComboBox = new JComboBox<>();
        countryComboBox.addItem("Όλες οι χώρες"); // ✅ Default επιλογή
        searchPanel.add(countryComboBox);

        searchButton = new JButton("Αναζήτηση");
        searchPanel.add(searchButton);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // 🔹 Πίνακας εμφάνισης δεδομένων
        tableModel = new DefaultTableModel(new Object[]{"ID", "Όνομα", "Χώρα", "Code", "Ιστοσελίδα", "Περιφέρεια"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        universityTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(universityTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 🔹 Ρύθμιση για εμφάνιση υπερσυνδέσμου στις ιστοσελίδες
        universityTable.getColumnModel().getColumn(4).setCellRenderer(new HyperlinkRenderer());

        // 🔹 Χρωματισμός τροποποιημένων εγγραφών
        universityTable.setDefaultRenderer(Object.class, new TableColorRenderer());

        // 🔹 Πάνελ με κουμπιά
        JPanel buttonsPanel = new JPanel();
        refreshButton = new JButton("Ενημέρωση από Web Service");
        loadButton = new JButton("Φόρτωση από ΒΔ");
        JButton statsButton = new JButton("Στατιστικά");
        JButton exportPdfButton = new JButton("Εξαγωγή σε PDF");
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(loadButton);
        buttonsPanel.add(statsButton);
        buttonsPanel.add(exportPdfButton);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 🔹 Action Listeners
        refreshButton.addActionListener(e -> refreshDataFromWebService());
        loadButton.addActionListener(e -> loadAllUniversities());
        searchButton.addActionListener(e -> searchUniversities());
        statsButton.addActionListener(e -> showStatistics());
        exportPdfButton.addActionListener(e -> exportStatisticsToPDF());

        // 🔹 Διπλό κλικ για Edit
        universityTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedUniversity();
                }
            }
        });

        // 🔹 Φόρτωση χωρών στο ComboBox
        loadCountries();
    }

    /**
     * Εμφανίζει τα στατιστικά των πιο δημοφιλών πανεπιστημίων.
     */
    private void showStatistics() {
        UniversityDAO dao = UniversityDAO.getInstance();
        List<University> popularUniversities = dao.getPopularUniversities();

        if (popularUniversities.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Δεν υπάρχουν διαθέσιμα στατιστικά προβολών.");
            return;
        }

        String[] columnNames = {"ID", "Όνομα Πανεπιστημίου", "Χώρα", "Προβολές"};
        DefaultTableModel statsModel = new DefaultTableModel(columnNames, 0);

        for (University uni : popularUniversities) {
            statsModel.addRow(new Object[]{uni.getId(), uni.getName(), uni.getCountry(), uni.getViewCount()});
        }

        JTable statsTable = new JTable(statsModel);
        JOptionPane.showMessageDialog(this, new JScrollPane(statsTable), "Στατιστικά Πανεπιστημίων", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportStatisticsToPDF() {
        UniversityDAO dao = UniversityDAO.getInstance();
        List<University> popularUniversities = dao.getPopularUniversities();

        if (popularUniversities.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Δεν υπάρχουν διαθέσιμα στατιστικά για εκτύπωση.");
            return;
        }

        try {
            PdfWriter writer = new PdfWriter("statistics.pdf");
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Στατιστικά Πανεπιστημίων"));

            Table table = new Table(4);
            table.addCell("ID");
            table.addCell("Όνομα Πανεπιστημίου");
            table.addCell("Χώρα");
            table.addCell("Προβολές");

            for (University uni : popularUniversities) {
                table.addCell(String.valueOf(uni.getId()));
                table.addCell(uni.getName());
                table.addCell(uni.getCountry());
                table.addCell(String.valueOf(uni.getViewCount()));
            }

            document.add(table);
            document.close();
            JOptionPane.showMessageDialog(this, "Το αρχείο statistics.pdf δημιουργήθηκε με επιτυχία!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Σφάλμα κατά τη δημιουργία του PDF.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshDataFromWebService() {
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String url = "https://raw.githubusercontent.com/Hipo/university-domains-list/master/world_universities_and_domains.json";
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();
                    int code = response.code();

                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        Gson gson = new Gson();
                        UniversityJSON[] uniArray = gson.fromJson(json, UniversityJSON[].class);

                        if (uniArray == null || uniArray.length == 0) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                    UniAppGUI.this, "Δεν βρέθηκαν δεδομένα από το Web Service."));
                            return null;
                        }

                        UniversityDAO dao = UniversityDAO.getInstance();
                        int insertedCount = 0;

                        for (int i = 0; i < uniArray.length; i++) {
                            UniversityJSON uj = uniArray[i];
                            University uni = new University();
                            uni.setName(uj.getName());
                            uni.setCountry(uj.getCountry());
                            uni.setAlphaTwoCode(uj.getAlphaTwoCode());
                            uni.setStateProvince(uj.getStateProvince());
                            uni.setDomains((uj.getDomains() != null) ? String.join(",", uj.getDomains()) : null);
                            uni.setWebPages((uj.getWebPages() != null) ? String.join(",", uj.getWebPages()) : null);
                            uni.setSchool(null);
                            uni.setDepartment(null);
                            uni.setDescription(null);
                            uni.setContact(null);
                            uni.setComments(null);
                            uni.setModified(false);

                            if (dao.upsertUniversity(uni)) { // Ελέγχουμε αν εισήχθη νέα εγγραφή
                                insertedCount++;
                            }

                            publish((i + 1) * 100 / uniArray.length); // Ενημέρωση progress bar
                        }

                        final int finalInsertedCount = insertedCount;
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(UniAppGUI.this,
                                    "Ενημέρωση δεδομένων ολοκληρώθηκε!\nΣυνολικές εγγραφές που προστέθηκαν: " + finalInsertedCount);
                            loadAllUniversities();
                        });

                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(UniAppGUI.this,
                                "Αποτυχία ανάκτησης δεδομένων. Κωδικός: " + code));
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(UniAppGUI.this,
                            "Σφάλμα κατά τη λήψη δεδομένων: " + ex.getMessage()));
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
            }

            @Override
            protected void done() {
                progressBar.setValue(100);
                progressBar.setVisible(false);
            }
        };

        progressBar.setValue(0);
        progressBar.setVisible(true);
        worker.execute();
    }

    private void recordView(int universityId) {
        UniversityDAO dao = UniversityDAO.getInstance();
        dao.increaseViewCount(universityId);
    }

    private void loadAllUniversities() {
        UniversityDAO dao = UniversityDAO.getInstance();
        List<University> universities = dao.getAllUniversities();
        updateTable(universities);
    }

    private void searchUniversities() {
        String name = txtSearchName.getText().trim();
        String country = countryComboBox.getSelectedItem().toString();
        if (country.equals("Όλες οι χώρες")) {
            country = "";
        }
        UniversityDAO dao = UniversityDAO.getInstance();
        List<University> universities = dao.searchUniversities(name, country);
        updateTable(universities);
    }

    private void updateTable(List<University> universities) {
        tableModel.setRowCount(0);
        for (University uni : universities) {
            Object[] row = new Object[]{
                    uni.getId(),
                    uni.getName(),
                    uni.getCountry(),
                    uni.getAlphaTwoCode(),
                    uni.getWebPages(),
                    uni.getStateProvince()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Ανανεώνει τα πανεπιστήμια χρησιμοποιώντας τα τρέχοντα φίλτρα αναζήτησης.
     */
    private void refreshFilteredData() {
        String name = txtSearchName.getText().trim();
        String country = (String) countryComboBox.getSelectedItem();
        if (country.equals("Όλες οι χώρες")) {
            country = "";
        }
        UniversityDAO dao = UniversityDAO.getInstance();
        List<University> universities = dao.searchUniversities(name, country);
        updateTable(universities);
    }

    /**
     * Επεξεργασία του επιλεγμένου πανεπιστημίου.
     */
    private void editSelectedUniversity() {
        int selectedRow = universityTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Επιλέξτε ένα πανεπιστήμιο για επεξεργασία.");
            return;
        }

        int uniId = (int) tableModel.getValueAt(selectedRow, 0);
        UniversityDAO dao = UniversityDAO.getInstance();

        // Αυξάνουμε το view count πριν ανοίξει το EditUniversityDialog
        dao.increaseViewCount(uniId);

        University selectedUni = dao.getUniversityById(uniId);
        if (selectedUni != null) {
            EditUniversityDialog editDialog = EditUniversityDialog.getInstance(this);
            editDialog.openDialog(selectedUni, this::refreshFilteredData);
            //editDialog.setVisible(true);
            searchUniversities();
        }
    }

    private void loadCountries() {
        UniversityDAO dao = UniversityDAO.getInstance();
        List<String> countries = dao.getAllCountries();
        for (String country : countries) {
            countryComboBox.addItem(country);
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UniAppGUI gui = new UniAppGUI();
            gui.setVisible(true);
        });
    }

    // 🔹 Κλάση για υπερσυνδέσμους στον πίνακα
    class HyperlinkRenderer extends DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            if (value != null) {
                setText("<html><a href='" + value + "'>" + value + "</a></html>");
            } else {
                setText("");
            }
        }
    }

    // 🔹 Κλάση για JSON mapping

    /**
     *
     */
    public static class UniversityJSON {
        private String name;
        private String country;

        @SerializedName("alpha_two_code")
        private String alphaTwoCode;

        @SerializedName("state-province")
        private String stateProvince;

        private java.util.List<String> domains;

        @SerializedName("web_pages")
        private java.util.List<String> webPages;

        /**
         *
         * @return
         */
        public String getName() { return name; }

        /**
         *
         * @return
         */
        public String getCountry() { return country; }

        /**
         *
         * @return
         */
        public String getAlphaTwoCode() { return alphaTwoCode; }

        /**
         *
         * @return
         */
        public String getStateProvince() { return stateProvince; }

        /**
         *
         * @return
         */
        public java.util.List<String> getDomains() { return domains; }

        /**
         *
         * @return
         */
        public java.util.List<String> getWebPages() { return webPages; }
    }

    // 🔹 Κλάση για χρωματισμό γραμμών αν έχει τροποποιηθεί
    class TableColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // Αν η στήλη isModified είναι στη θέση 11
            if (column == 11) {
                boolean modified = false;
                if (value instanceof Boolean) {
                    modified = (Boolean) value;
                } else if (value instanceof String) {
                    modified = Boolean.parseBoolean((String) value);
                }
                cell.setBackground(modified ? Color.GREEN : Color.WHITE);
            } else {
                cell.setBackground(Color.WHITE);
            }
            return cell;
        }
    }
}
