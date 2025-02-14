package plh24.uniapp;

import java.util.Scanner;

/**
 * Κλάση για τη διαχείριση της βάσης μέσω μενού στην κονσόλα.
 */
public class DBMgmt {

    /**
    * Εκτελεί το μενού επιλογών για τη διαχείριση της βάσης δεδομένων.
    *
    * @param args Δεν απαιτούνται παράμετροι.
    */
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");

        // Δημιουργούμε ένα Scanner για το input του χρήστη
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
                System.out.println("Μη έγκυρη επιλογή. Παρακαλώ εισάγετε αριθμό.");
                continue;
            }

            switch (choice) {
                case 1:
                    DBUtil.getInstance().initializeDatabase();
                    System.out.println("Η βάση δεδομένων δημιουργήθηκε.");
                    break;

                case 2:
                    System.out.print("Είστε σίγουρος ότι θέλετε να διαγράψετε και να επανεκκινήσετε τη βάση; (ν/ο): ");
                    String confirmation = scanner.nextLine().trim();
                    if (confirmation.equalsIgnoreCase("ν") || confirmation.equalsIgnoreCase("ναι")) {
                        DBUtil.getInstance().resetDatabase();
                        System.out.println("Η βάση διαγράφηκε και δημιουργήθηκε ξανά.");
                    } else {
                        System.out.println("Η βάση δεδομένων **ΔΕΝ** διαγράφηκε.");
                    }
                    break;

                case 3:
                    DBUtil.getInstance().shutdownDatabase();
                    System.out.println("Έξοδος από τη διαχείριση βάσης.");
                    scanner.close();
                    return;

                default:
                    System.out.println("Μη έγκυρη επιλογή. Δοκιμάστε ξανά.");
            }
        }
    }
}
