package ood.assignment.adminmodule;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Read-only payment history created by the Student module. */
public class PaymentRecord {
    private final String paymentId;
    private final String studentId;
    private final String classId;
    private final BigDecimal amount;
    private final LocalDate date;
    private final String status;

    public PaymentRecord(String paymentId, String studentId, String classId,
                         BigDecimal amount, LocalDate date, String status) {
        this.paymentId = clean(paymentId, "Payment ID");
        this.studentId = clean(studentId, "Student ID");
        this.classId = clean(classId, "Class ID");
        this.status = clean(status, "Status");
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must not be negative.");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount must have no more than two decimal places.");
        }
        if (date == null) throw new IllegalArgumentException("Payment date is required.");
        this.amount = amount;
        this.date = date;
    }

    private static String clean(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        String text = value.trim();
        if (text.contains("|") || text.contains("\r") || text.contains("\n")) {
            throw new IllegalArgumentException(field + " contains an invalid separator.");
        }
        return text;
    }

    public String getPaymentId() { return paymentId; }
    public String getStudentId() { return studentId; }
    public String getClassId() { return classId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getStatus() { return status; }
}
