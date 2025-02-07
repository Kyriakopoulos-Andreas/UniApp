package plh24.uniapp;

import java.util.Objects;

/**
 * Κλάση που αναπαριστά ένα πανεπιστήμιο από τη βάση δεδομένων.
 * 
 * Περιέχει πεδία για τα χαρακτηριστικά ενός πανεπιστημίου, με ασφαλείς getters και setters,
 * καθώς και μεθόδους equals, hashCode και toString για σωστή συμπεριφορά σε συλλογές.
 * 
 */
public class University {
    private int id;
    private String name;
    private String country;
    private String alphaTwoCode;
    private String stateProvince;
    private String domains;
    private String webPages;
    private String school;
    private String department;
    private String description;
    private String contact;
    private String comments;
    private boolean isModified;
    private int viewCount;

    /**
     * Προεπιλεγμένος constructor.
     */
    public University() {}

    /**
     * Constructor για εύκολη αρχικοποίηση όλων των πεδίων.
     *
     * @param id            το ID του πανεπιστημίου
     * @param name          το όνομα του πανεπιστημίου
     * @param country       η χώρα του πανεπιστημίου
     * @param alphaTwoCode  ο κωδικός χώρας
     * @param stateProvince η περιφέρεια ή πολιτεία
     * @param domains       οι ιστότοποι του πανεπιστημίου
     * @param webPages      οι ιστοσελίδες του πανεπιστημίου
     * @param school        το όνομα της σχολής (αν υπάρχει)
     * @param department    το όνομα του τμήματος (αν υπάρχει)
     * @param description   περιγραφή του πανεπιστημίου
     * @param contact       στοιχεία επικοινωνίας
     * @param comments      επιπλέον σχόλια
     * @param isModified    δείκτης αν έχει τροποποιηθεί τοπικά
     */
    public University(int id, String name, String country, String alphaTwoCode, String stateProvince,
                      String domains, String webPages, String school, String department,
                      String description, String contact, String comments, boolean isModified) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.alphaTwoCode = alphaTwoCode;
        this.stateProvince = stateProvince;
        this.domains = domains;
        this.webPages = webPages;
        this.school = school;
        this.department = department;
        this.description = description;
        this.contact = contact;
        this.comments = comments;
        this.isModified = isModified;
    }

    /**
     * Επιστρέφει το ID του πανεπιστημίου.
     *
     * @return το ID
     */
    public int getId() { 
        return id; 
    }

    /**
     * Θέτει το ID του πανεπιστημίου.
     *
     * @param id το νέο ID
     */
    public void setId(int id) { 
        this.id = id; 
    }

    /**
     * Επιστρέφει το όνομα του πανεπιστημίου, εξασφαλίζοντας ότι δεν επιστρέφεται null.
     *
     * @return το όνομα του πανεπιστημίου
     */
    public String getName() { 
        return safeString(name); 
    }

    /**
     * Θέτει το όνομα του πανεπιστημίου.
     *
     * @param name το νέο όνομα
     */
    public void setName(String name) { 
        this.name = name; 
    }

    /**
     * Επιστρέφει τη χώρα του πανεπιστημίου.
     *
     * @return τη χώρα
     */
    public String getCountry() { 
        return safeString(country); 
    }

    /**
     * Θέτει τη χώρα του πανεπιστημίου.
     *
     * @param country η νέα χώρα
     */
    public void setCountry(String country) { 
        this.country = country; 
    }

    /**
     * Επιστρέφει τον κωδικό χώρας (alphaTwoCode).
     *
     * @return τον κωδικό χώρας
     */
    public String getAlphaTwoCode() { 
        return safeString(alphaTwoCode); 
    }

    /**
     * Θέτει τον κωδικό χώρας.
     *
     * @param alphaTwoCode ο νέος κωδικός
     */
    public void setAlphaTwoCode(String alphaTwoCode) { 
        this.alphaTwoCode = alphaTwoCode; 
    }

    /**
     * Επιστρέφει την περιφέρεια ή πολιτεία του πανεπιστημίου.
     *
     * @return την περιφέρεια ή πολιτεία
     */
    public String getStateProvince() { 
        return safeString(stateProvince); 
    }

    /**
     * Θέτει την περιφέρεια ή πολιτεία.
     *
     * @param stateProvince η νέα περιφέρεια ή πολιτεία
     */
    public void setStateProvince(String stateProvince) { 
        this.stateProvince = stateProvince; 
    }

    /**
     * Επιστρέφει τους ιστότοπους του πανεπιστημίου.
     *
     * @return τους ιστότοπους
     */
    public String getDomains() { 
        return safeString(domains); 
    }

    /**
     * Θέτει τους ιστότοπους του πανεπιστημίου.
     *
     * @param domains οι νέοι ιστότοποι
     */
    public void setDomains(String domains) { 
        this.domains = domains; 
    }

    /**
     * Επιστρέφει τις ιστοσελίδες του πανεπιστημίου.
     *
     * @return τις ιστοσελίδες
     */
    public String getWebPages() { 
        return safeString(webPages); 
    }

    /**
     * Θέτει τις ιστοσελίδες του πανεπιστημίου.
     *
     * @param webPages οι νέες ιστοσελίδες
     */
    public void setWebPages(String webPages) { 
        this.webPages = webPages; 
    }

    /**
     * Επιστρέφει το όνομα της σχολής.
     *
     * @return το όνομα της σχολής
     */
    public String getSchool() { 
        return safeString(school); 
    }

    /**
     * Θέτει το όνομα της σχολής.
     *
     * @param school το νέο όνομα της σχολής
     */
    public void setSchool(String school) { 
        this.school = school; 
    }

    /**
     * Επιστρέφει το όνομα του τμήματος.
     *
     * @return το όνομα του τμήματος
     */
    public String getDepartment() { 
        return safeString(department); 
    }

    /**
     * Θέτει το όνομα του τμήματος.
     *
     * @param department το νέο όνομα του τμήματος
     */
    public void setDepartment(String department) { 
        this.department = department; 
    }

    /**
     * Επιστρέφει την περιγραφή του πανεπιστημίου.
     *
     * @return την περιγραφή
     */
    public String getDescription() { 
        return safeString(description); 
    }

    /**
     * Θέτει την περιγραφή του πανεπιστημίου.
     *
     * @param description η νέα περιγραφή
     */
    public void setDescription(String description) { 
        this.description = description; 
    }

    /**
     * Επιστρέφει τα στοιχεία επικοινωνίας του πανεπιστημίου.
     *
     * @return τα στοιχεία επικοινωνίας
     */
    public String getContact() { 
        return safeString(contact); 
    }

    /**
     * Θέτει τα στοιχεία επικοινωνίας του πανεπιστημίου.
     *
     * @param contact τα νέα στοιχεία επικοινωνίας
     */
    public void setContact(String contact) { 
        this.contact = contact; 
    }

    /**
     * Επιστρέφει τα σχόλια σχετικά με το πανεπιστήμιο.
     *
     * @return τα σχόλια
     */
    public String getComments() { 
        return safeString(comments); 
    }

    /**
     * Θέτει τα σχόλια σχετικά με το πανεπιστήμιο.
     *
     * @param comments τα νέα σχόλια
     */
    public void setComments(String comments) { 
        this.comments = comments; 
    }

    /**
     * Επιστρέφει την ένδειξη αν το πανεπιστήμιο έχει τροποποιηθεί τοπικά.
     *
     * @return true αν έχει τροποποιηθεί, αλλιώς false
     */
    public boolean isModified() { 
        return isModified; 
    }

    /**
     * Θέτει την ένδειξη τροποποίησης του πανεπιστημίου.
     *
     * @param isModified true αν έχει τροποποιηθεί, αλλιώς false
     */
    public void setModified(boolean isModified) { 
        this.isModified = isModified; 
    }

    /**
     * Επιστρέφει τον μετρητή προβολών του πανεπιστημίου.
     *
     * @return ο μετρητής προβολών
     */
    public int getViewCount() { 
        return viewCount; 
    }

    /**
     * Θέτει τον μετρητή προβολών του πανεπιστημίου.
     *
     * @param viewCount ο νέος μετρητής προβολών
     */
    public void setViewCount(int viewCount) { 
        this.viewCount = viewCount; 
    }

    /**
     * Επιστρέφει την είσοδο ως ασφαλές string για να αποφεύγονται null τιμές.
     *
     * @param value το string που θέλουμε να ελέγξουμε
     * @return το ίδιο string ή κενό string αν το value είναι null
     */
    private String safeString(String value) {
        return (value == null) ? "" : value;
    }

    /**
     * Επιστρέφει μια συμβολοσειρά που αναπαριστά το αντικείμενο University, χρήσιμο για debugging.
     *
     * @return η αναπαράσταση του University ως string.
     */
    @Override
    public String toString() {
        return "University{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", alphaTwoCode='" + alphaTwoCode + '\'' +
                ", stateProvince='" + stateProvince + '\'' +
                ", domains='" + domains + '\'' +
                ", webPages='" + webPages + '\'' +
                ", school='" + school + '\'' +
                ", department='" + department + '\'' +
                ", description='" + description + '\'' +
                ", contact='" + contact + '\'' +
                ", comments='" + comments + '\'' +
                ", isModified=" + isModified +
                ", viewCount=" + viewCount +
                '}';
    }

    /**
     * Ελέγχει αν δύο αντικείμενα University είναι όμοια με βάση όλα τα πεδία τους.
     *
     * @param o το αντικείμενο προς σύγκριση.
     * @return true αν είναι όμοια, αλλιώς false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        University that = (University) o;
        return id == that.id && isModified == that.isModified &&
                Objects.equals(name, that.name) &&
                Objects.equals(country, that.country) &&
                Objects.equals(alphaTwoCode, that.alphaTwoCode) &&
                Objects.equals(stateProvince, that.stateProvince) &&
                Objects.equals(domains, that.domains) &&
                Objects.equals(webPages, that.webPages) &&
                Objects.equals(school, that.school) &&
                Objects.equals(department, that.department) &&
                Objects.equals(description, that.description) &&
                Objects.equals(contact, that.contact) &&
                Objects.equals(comments, that.comments);
    }

    /**
     * Επιστρέφει το hash code του αντικειμένου, βασιζόμενο στα πεδία του.
     *
     * @return το hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, country, alphaTwoCode, stateProvince, domains, webPages, school,
                department, description, contact, comments, isModified);
    }
}
