package plh24.uniapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Κλάση για την επεξεργασία των στοιχείων ενός πανεπιστημίου.
 * Επιτρέπει την προβολή και επεξεργασία όλων των πεδίων εκτός από το ID και το isModified.
 * 
 * Αυτή η κλάση υλοποιεί το πρότυπο Singleton, έτσι ώστε να υπάρχει μόνο μία εμφάνιση του διαλόγου.
 */
public class EditUniversityDialog extends JDialog {
    private University university;
    private JTextField txtId, txtName, txtCountry, txtAlphaTwoCode, txtStateProvince, txtDomains, txtWebPages;
    private JTextField txtSchool, txtDepartment, txtDescription, txtContact, txtComments;
    private JCheckBox chkModified;
    private JButton btnSave, btnCancel;
    private Runnable onCloseCallback;

    // Το μοναδικό instance της κλάσης
    private static EditUniversityDialog instance;

    /**
     * Ιδιωτικός constructor ώστε να μην δημιουργούνται εξωτερικά νέα instances.
     *
     * @param parent Το γονικό παράθυρο του διαλόγου.
     */
    private EditUniversityDialog(Frame parent) {
        super(parent, "Επεξεργασία Πανεπιστημίου", true);
        // Χρησιμοποιούμε HIDE_ON_CLOSE ώστε το dialog να αποκρύπτεται όταν ο χρήστης το κλείνει.
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        initComponents();

        // Προσθέτουμε listener ώστε όταν το dialog "καταστρέφεται" (για παράδειγμα μέσω του "X") να μηδενίζεται το instance.
        addWindowListener(new WindowAdapter() {

            public void windowHidden(WindowEvent e) {
                // Μόνο όταν αποκρύπτεται το dialog, θέτουμε το instance σε null.
                instance = null;
            }
        });
    }

    /**
     * Επιστρέφει το μοναδικό instance της κλάσης EditUniversityDialog.
     * Αν δεν υπάρχει ή δεν είναι displayable, δημιουργείται νέο.
     *
     * @param parent Το γονικό παράθυρο του διαλόγου.
     * @return Το μοναδικό instance της κλάσης.
     */
    public static EditUniversityDialog getInstance(Frame parent) {
        if (instance == null || !instance.isDisplayable()) {
            instance = new EditUniversityDialog(parent);
        } else {
            instance.setLocationRelativeTo(parent);
        }
        return instance;
    }

    /**
     * Ανοίγει τον διάλογο για επεξεργασία του συγκεκριμένου πανεπιστημίου.
     * Αντιγράφει τα δεδομένα, ενημερώνει τα πεδία και εμφανίζει το παράθυρο.
     *
     * @param university     Το πανεπιστήμιο που θα επεξεργαστεί.
     * @param onCloseCallback Callback που καλείται όταν κλείνει ο διάλογος.
     */
    public void openDialog(University university, Runnable onCloseCallback) {
        this.university = university;
        this.onCloseCallback = onCloseCallback;
        populateFields();
        pack();
        setSize(600, 600);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel fieldsPanel = new JPanel(new GridLayout(13, 2, 10, 10));
        fieldsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Δημιουργία ετικετών
        JLabel lblId = new JLabel("Κωδικός (ID):");
        JLabel lblName = new JLabel("Όνομα:");
        JLabel lblCountry = new JLabel("Χώρα:");
        JLabel lblAlpha = new JLabel("Alpha Code:");
        JLabel lblState = new JLabel("Περιφέρεια:");
        JLabel lblDomains = new JLabel("Ιστότοποι:");
        JLabel lblWebPages = new JLabel("Ιστοσελίδες:");
        JLabel lblSchool = new JLabel("Σχολή:");
        JLabel lblDepartment = new JLabel("Τμήμα:");
        JLabel lblDescription = new JLabel("Περιγραφή:");
        JLabel lblContact = new JLabel("Επικοινωνία:");
        JLabel lblComments = new JLabel("Σχόλια:");
        JLabel lblModified = new JLabel("Ενημερώθηκε:");

        Dimension fieldSize = new Dimension(350, 30);
        txtId = new JTextField();
        txtId.setPreferredSize(fieldSize);
        txtId.setEnabled(false);
        txtName = new JTextField();
        txtName.setPreferredSize(fieldSize);
        txtCountry = new JTextField();
        txtCountry.setPreferredSize(fieldSize);
        txtAlphaTwoCode = new JTextField();
        txtAlphaTwoCode.setPreferredSize(fieldSize);
        txtStateProvince = new JTextField();
        txtStateProvince.setPreferredSize(fieldSize);
        txtDomains = new JTextField();
        txtDomains.setPreferredSize(fieldSize);
        txtWebPages = new JTextField();
        txtWebPages.setPreferredSize(fieldSize);
        txtSchool = new JTextField();
        txtSchool.setPreferredSize(fieldSize);
        txtDepartment = new JTextField();
        txtDepartment.setPreferredSize(fieldSize);
        txtDescription = new JTextField();
        txtDescription.setPreferredSize(fieldSize);
        txtContact = new JTextField();
        txtContact.setPreferredSize(fieldSize);
        txtComments = new JTextField();
        txtComments.setPreferredSize(fieldSize);
        chkModified = new JCheckBox();
        chkModified.setEnabled(false);

        fieldsPanel.add(lblId);          fieldsPanel.add(txtId);
        fieldsPanel.add(lblName);        fieldsPanel.add(txtName);
        fieldsPanel.add(lblCountry);     fieldsPanel.add(txtCountry);
        fieldsPanel.add(lblAlpha);       fieldsPanel.add(txtAlphaTwoCode);
        fieldsPanel.add(lblState);       fieldsPanel.add(txtStateProvince);
        fieldsPanel.add(lblDomains);     fieldsPanel.add(txtDomains);
        fieldsPanel.add(lblWebPages);    fieldsPanel.add(txtWebPages);
        fieldsPanel.add(lblSchool);      fieldsPanel.add(txtSchool);
        fieldsPanel.add(lblDepartment);  fieldsPanel.add(txtDepartment);
        fieldsPanel.add(lblDescription); fieldsPanel.add(txtDescription);
        fieldsPanel.add(lblContact);     fieldsPanel.add(txtContact);
        fieldsPanel.add(lblComments);    fieldsPanel.add(txtComments);
        fieldsPanel.add(lblModified);    fieldsPanel.add(chkModified);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Αποθήκευση");
        btnCancel = new JButton("Ακύρωση");
        buttonsPanel.add(btnSave);
        buttonsPanel.add(btnCancel);
        add(buttonsPanel, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> saveUniversity());
        btnCancel.addActionListener(e -> closeDialog());
    }

    private void populateFields() {
        txtId.setText(String.valueOf(university.getId()));
        txtName.setText(university.getName());
        txtCountry.setText(university.getCountry());
        txtAlphaTwoCode.setText(university.getAlphaTwoCode());
        txtStateProvince.setText(university.getStateProvince());
        txtDomains.setText(university.getDomains());
        txtWebPages.setText(university.getWebPages());
        txtSchool.setText(university.getSchool());
        txtDepartment.setText(university.getDepartment());
        txtDescription.setText(university.getDescription());
        txtContact.setText(university.getContact());
        txtComments.setText(university.getComments());
        chkModified.setSelected(university.isModified());
    }

    private void saveUniversity() {
        university.setName(txtName.getText().trim());
        university.setCountry(txtCountry.getText().trim());
        university.setAlphaTwoCode(txtAlphaTwoCode.getText().trim());
        university.setStateProvince(txtStateProvince.getText().trim());
        university.setDomains(txtDomains.getText().trim());
        university.setWebPages(txtWebPages.getText().trim());
        university.setSchool(txtSchool.getText().trim());
        university.setDepartment(txtDepartment.getText().trim());
        university.setDescription(txtDescription.getText().trim());
        university.setContact(txtContact.getText().trim());
        university.setComments(txtComments.getText().trim());
        university.setModified(true);

        try {
            UniversityDAO.getInstance().updateUniversityUser(university);
            JOptionPane.showMessageDialog(this, "Τα δεδομένα αποθηκεύτηκαν επιτυχώς!", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
            if (onCloseCallback != null) {
                onCloseCallback.run();
                onCloseCallback = null;
            }
            closeDialog();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Σφάλμα κατά την αποθήκευση: " + ex.getMessage(), "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void closeDialog() {
        setVisible(false);
        // Δεν καλούμε dispose() εδώ για να μην προκληθεί επανεμφάνιση λόγω modal behavior
        // Αν χρειάζεται πλήρης καταστροφή, μπορούμε να το κάνουμε εξωτερικά
        instance = null;
    }
}
