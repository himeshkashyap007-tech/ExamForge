package com.examforge.model;

public class BlueprintConstraint {
    private int constraintId;
    private int blueprintId;
    private ConstraintCategory category;
    private ConstraintBasis basis;
    private String target;
    private double requiredValue;
    private double tolerance;
    private ConstraintPriority priority;

    public BlueprintConstraint() {
        this.priority = ConstraintPriority.HARD;
        this.tolerance = 0;
    }

    public BlueprintConstraint(ConstraintCategory category, ConstraintBasis basis,
                               String target, double requiredValue, double tolerance,
                               ConstraintPriority priority) {
        this.category = category;
        this.basis = basis;
        this.target = target;
        this.requiredValue = requiredValue;
        this.tolerance = tolerance;
        this.priority = priority;
    }

    public int getConstraintId() { return constraintId; }
    public void setConstraintId(int constraintId) { this.constraintId = constraintId; }
    public int getBlueprintId() { return blueprintId; }
    public void setBlueprintId(int blueprintId) { this.blueprintId = blueprintId; }
    public ConstraintCategory getCategory() { return category; }
    public void setCategory(ConstraintCategory category) { this.category = category; }
    public ConstraintBasis getBasis() { return basis; }
    public void setBasis(ConstraintBasis basis) { this.basis = basis; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public double getRequiredValue() { return requiredValue; }
    public void setRequiredValue(double requiredValue) { this.requiredValue = requiredValue; }
    public double getTolerance() { return tolerance; }
    public void setTolerance(double tolerance) { this.tolerance = tolerance; }
    public ConstraintPriority getPriority() { return priority; }
    public void setPriority(ConstraintPriority priority) { this.priority = priority; }

    @Override
    public String toString() {
        return category + " " + basis + " " + target + " = " + requiredValue +
               " (tol=" + tolerance + ", " + priority + ")";
    }
}
