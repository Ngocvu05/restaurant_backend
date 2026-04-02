package com.management.restaurant.helper;

public class SagaTransaction {
    private final java.util.List<Runnable> compensations = new java.util.ArrayList<>();
    private boolean committed = false;

    public void addCompensation(Runnable compensation) {
        compensations.add(compensation);
    }

    public void commit() {
        committed = true;
        compensations.clear();
    }

    public void rollback() {
        if (!committed) {
            // Execute compensations in reverse order
            for (int i = compensations.size() - 1; i >= 0; i--) {
                try {
                    compensations.get(i).run();
                } catch (Exception e) {
                    // Log but continue with other compensations
                    System.err.println("Compensation failed: " + e.getMessage());
                }
            }
        }
        compensations.clear();
    }
}