package com.examforge.validator;

import com.examforge.model.ValidationCheckStatus;

public class ValidationCheckResult {
    private String constraintName;
    private String expectedValue;
    private String actualValue;
    private double difference;
    private ValidationCheckStatus status;
    private String message;

    public ValidationCheckResult() {}

    public ValidationCheckResult(String constraintName, String expectedValue, String actualValue,
                                  double difference, ValidationCheckStatus status, String message) {
        this.constraintName = constraintName;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
        this.difference = difference;
        this.status = status;
        this.message = message;
    }

    public static ValidationCheckResult pass(String name, String expected, String actual) {
        return new ValidationCheckResult(name, expected, actual, 0, ValidationCheckStatus.PASS,
            name + " check passed");
    }

    public static ValidationCheckResult fail(String name, String expected, String actual, String msg) {
        double diff = 0;
        try {
            diff = Double.parseDouble(actual) - Double.parseDouble(expected);
        } catch (NumberFormatException ignored) {}
        return new ValidationCheckResult(name, expected, actual, diff, ValidationCheckStatus.FAIL, msg);
    }

    public String getConstraintName() { return constraintName; }
    public void setConstraintName(String constraintName) { this.constraintName = constraintName; }
    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }
    public String getActualValue() { return actualValue; }
    public void setActualValue(String actualValue) { this.actualValue = actualValue; }
    public double getDifference() { return difference; }
    public void setDifference(double difference) { this.difference = difference; }
    public ValidationCheckStatus getStatus() { return status; }
    public void setStatus(ValidationCheckStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
