package com.bence.projector.server.utils;

public class TimeSpentAtLine {
    private double timeSpent;
    private String className;
    private long lineNumber;

    public double getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(double timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void addTimeSpent(double time) {
        this.timeSpent += time;
    }

    @Override
    public String toString() {
        return timeSpent / 1000 + "\t" + className+ "\t" + lineNumber;
    }
}
