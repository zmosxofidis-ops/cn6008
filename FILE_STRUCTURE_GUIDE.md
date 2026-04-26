# Εκτενής Οδηγός Αρχιτεκτονικής και Δομής Αρχείων (Technical Documentation)

## 1. Εισαγωγή στη Δομή του Project
Το **SmartCity App** ακολουθεί την πρότυπη δομή ενός Android Studio Project, βελτιστοποιημένη για την πλατφόρμα Android 14 (API 34). Η οργάνωση των αρχείων έχει γίνει με τέτοιο τρόπο ώστε να διασφαλίζεται ο διαχωρισμός των ευθυνών (Separation of Concerns), επιτρέποντας την εύκολη συντήρηση και την μελλοντική επέκταση του κώδικα.

---

## 2. Java Source Code: Η Λογική της Εφαρμογής
Η καρδιά της εφαρμογής βρίσκεται στον κατάλογο `app/src/main/java/com/example/smartcityapp/`.

### **2.1. MainActivity.java (The Orchestrator)**
Η κλάση αυτή αποτελεί τον κεντρικό ελεγκτή. Δεν περιορίζεται μόνο στην εμφάνιση του UI, αλλά διαχειρίζεται το πλήρες lifecycle της εφαρμογής.
*   **Διαχείριση Κατάστασης (State Management):** Χρησιμοποιεί μεταβλητές για την παρακολούθηση του τρέχοντος layout (Home, History, Report Form) και διασφαλίζει ότι η πλοήγηση με το κουμπί "Back" είναι ομαλή.
*   **Runtime Permissions:** Υλοποιεί το σύγχρονο μοντέλο αδειών του Android χρησιμοποιώντας `ActivityResultLauncher`. Ζητά πρόσβαση στην κάμερα και την τοποθεσία μόνο όταν είναι απαραίτητο (Just-in-Time).
*   **Integration με GPS:** Συνεργάζεται με το `FusedLocationProviderClient` για τη λήψη συντεταγμένων υψηλής ακρίβειας.
*   **Image Processing:** Περιλαμβάνει τη μέθοδο `processAndSaveImage`, η οποία εκτελεί downsampling στα bitmaps για την αποφυγή memory leaks.

### **2.2. DatabaseHelper.java (The Data Layer)**
Υλοποιεί την τοπική αποθήκευση μέσω SQLite. 
*   **Schema Design:** Ορίζει έναν πίνακα `reports` με αυστηρούς τύπους δεδομένων (INTEGER, TEXT, REAL).
*   **Thread Safety:** Η κλάση έχει σχεδιαστεί για να καλείται από background threads, αποφεύγοντας το μπλοκάρισμα του Main UI Thread (Network/Database on Main Thread Exception).
*   **Migration Path:** Περιλαμβάνει τη μέθοδο `onUpgrade` για την ασφαλή μετάβαση σε νεότερες εκδόσεις της βάσης χωρίς την απώλεια των δεδομένων του χρήστη.

### **2.3. ReportAdapter.java (The View Binder)**
Είναι υπεύθυνος για την αποδοτική εμφάνιση μεγάλων λιστών δεδομένων.
*   **ViewHolder Pattern:** Χρησιμοποιεί το πρότυπο ViewHolder για να ελαχιστοποιήσει τις κλήσεις `findViewById`, βελτιώνοντας την απόδοση του scrolling.
*   **Dynamic Styling:** Περιέχει λογική που αναλύει το κείμενο της κατηγορίας και εφαρμόζει δυναμικά χρώματα στα `GradientDrawable` backgrounds.

### **2.4. models/CityReport.java (The Data Model)**
Ένα καθαρό POJO (Plain Old Java Object) που αναπαριστά την οντότητα "Αναφορά". Περιέχει getters και setters για την ασφαλή πρόσβαση στα δεδομένα.

---

## 3. Layouts και Γραφικό Περιβάλλον (Resource Layer)
Τα αρχεία XML στον κατάλογο `res/layout/` καθορίζουν την εμπειρία του χρήστη.

### **3.1. activity_main.xml (Main Hub)**
*   **View Switching:** Χρησιμοποιεί πολλαπλά `LinearLayout` (containers) που γίνονται Visible/Gone, προσφέροντας μια εμπειρία Single-Activity Architecture.
*   **Material Design 3:** Χρησιμοποιεί `MaterialCardView` για τα κουμπιά, δίνοντας αίσθηση βάθους και σκιές που ανταποκρίνονται στο άγγιγμα.

### **3.2. dialog_report.xml (Input Interface)**
*   **UX Design:** Τα πεδία είναι τοποθετημένα σε `ScrollView` για να είναι προσβάσιμα ακόμα και σε συσκευές με μικρή οθόνη ή όταν εμφανίζεται το πληκτρολόγιο.
*   **Validation Support:** Χρησιμοποιεί `TextInputLayout` για να παρέχει οπτική ανατροφοδότηση στον χρήστη.

### **3.3. item_report.xml (List Blueprint)**
*   **Visual Hierarchy:** Ο σχεδιασμός δίνει έμφαση στον τίτλο και την κατηγορία, ενώ οι δευτερεύουσες πληροφορίες (συντεταγμένες) εμφανίζονται με μικρότερη γραμματοσειρά και χαμηλότερη αντίθεση.

---

## 4. Πόροι και Ρυθμίσεις (Values & Metadata)
### **4.1. colors.xml & themes.xml**
*   **Theming Strategy:** Η εφαρμογή χρησιμοποιεί ένα προσαρμοσμένο "Night Mode" θέμα. Οι παλέτες χρωμάτων ακολουθούν τις προδιαγραφές προσβασιμότητας (WCAG), διασφαλίζοντας ότι το κείμενο είναι ευανάγνωστο για όλους.
*   **Color Mapping:** Ορίστηκαν "Aliases" για να γεφυρωθεί το κενό μεταξύ των παραδοσιακών ονομάτων χρωμάτων και των Material 3 attributes.

### **4.2. AndroidManifest.xml**
*   **Hardware Declarations:** Δηλώνει την απαίτηση για κάμερα και GPS, επιτρέποντας στο Google Play Store να φιλτράρει τις συμβατές συσκευές.
*   **Security Configuration:** Ορίζει τον `FileProvider` με ειδικές διαδρομές (`res/xml/file_paths.xml`), διασφαλίζοντας ότι η εφαρμογή δεν έχει πρόσβαση σε αρχεία εκτός του δικού της "sandbox".

---

## 5. Build System (Gradle Configuration)
Το αρχείο `app/build.gradle.kts` καθορίζει τις εξαρτήσεις:
*   **Material Components:** Παρέχει τα UI στοιχεία (Buttons, Cards, Dialogs).
*   **Play Services Location:** Επιτρέπει την επικοινωνία με τους αισθητήρες τοποθεσίας.
*   **Annotation Processor:** Βοηθά στη βελτιστοποίηση του κώδικα κατά τη μεταγλώττιση.

---

## 6. Διαδικασία Ανάπτυξης και Συντήρησης
Ο οδηγός αυτός χρησιμεύει ως σημείο αναφοράς για κάθε νέο προγραμματιστή που θα ασχοληθεί με το project. Η αυστηρή τήρηση της ονοματολογίας (π.χ. `tv_` για TextViews, `btn_` για Buttons) και η οργάνωση των resources διασφαλίζουν ότι το SmartCity App μπορεί να μεγαλώσει χωρίς να γίνει χαοτικό.
