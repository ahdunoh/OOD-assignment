package ood.assignment.adminmodule;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Reader only: Administrator must never modify payment records. */
public class PaymentStore {
    public static final Path FILE = Paths.get(System.getProperty("sdams.data.dir", "."), "payments.txt");

    public List<PaymentRecord> readAll() throws IOException {
        List<PaymentRecord> payments = new ArrayList<>();
        Set<String> paymentIds = new HashSet<>();
        if (!Files.exists(FILE)) return payments;
        List<String> lines = Files.readAllLines(FILE, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] f = line.split("\\s*,\\s*", -1);
            if (f.length != 6) {
                throw new IOException(FILE + " line " + (i + 1)
                        + ": expected Payment ID, Username, Class ID, Amount, Date, Status");
            }
            try {
                PaymentRecord payment = new PaymentRecord(f[0], f[1], f[2],
                        new BigDecimal(f[3].trim()), LocalDate.parse(f[4].trim()), f[5]);
                if (!paymentIds.add(payment.getPaymentId().toLowerCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("Duplicate Payment ID.");
                }
                payments.add(payment);
            } catch (IllegalArgumentException | DateTimeParseException ex) {
                throw new IOException(FILE + " line " + (i + 1) + ": " + ex.getMessage(), ex);
            }
        }
        return payments;
    }
}
