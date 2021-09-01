package org.pgstyle.mcrcon.control.command;

import java.time.OffsetDateTime;

public final class CommandEntry {

    private OffsetDateTime start;
    private OffsetDateTime end;
    private String text;
    private String args;
    private String result;

    public CommandEntry(String command, String args) {
        this.text = command;
        this.args = args;
        this.start = OffsetDateTime.now();
    }

    public String getArgs() {
        return this.args;
    }

    public String getText() {
        return this.text;
    }

    public OffsetDateTime getEndTime() {
        return this.end;
    }

    public String getResult() {
        return this.result;
    }

    public OffsetDateTime getStartTime() {
        return this.start;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setEndTime() {
        this.end = OffsetDateTime.now();
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(this.getText()).append(System.lineSeparator());
        string.append("  start:  ").append(this.getStartTime()).append(System.lineSeparator());
        string.append("  end:    ").append(this.getEndTime()).append(System.lineSeparator());
        string.append("  args:   ").append(this.getArgs()).append(System.lineSeparator());
        string.append("  result: ").append(this.getResult());
        return string.toString();
    }

}
