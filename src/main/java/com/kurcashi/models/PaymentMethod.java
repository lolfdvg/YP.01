package com.kurcashi.models;

public class PaymentMethod {
    private int methodId;
    private String methodCode;
    private String methodName;
    private boolean isActive;
    private String description;

    public PaymentMethod() {}
    public PaymentMethod(int methodId, String methodCode, String methodName, boolean isActive, String description) {
        this.methodId = methodId;
        this.methodCode = methodCode;
        this.methodName = methodName;
        this.isActive = isActive;
        this.description = description;
    }

    public int getMethodId() { return methodId; }
    public void setMethodId(int methodId) { this.methodId = methodId; }
    public String getMethodCode() { return methodCode; }
    public void setMethodCode(String methodCode) { this.methodCode = methodCode; }
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() { return methodName; }
}