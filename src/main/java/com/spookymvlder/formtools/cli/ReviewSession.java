package com.spookymvlder.formtools.cli;

import com.spookymvlder.formtools.datatypes.FormInstance;

import java.util.List;

/**
 * Holds at most one reviewed-but-not-yet-committed form. Deliberately singular: the pipeline is
 * single-file today, and starting a second review while one is pending should be an explicit
 * decision (commit or discard first) rather than silently overwriting what's held.
 */
public class ReviewSession {
    private List<FormInstance> forms;
    private Integer logId;

    public boolean hasPending() {
        return forms != null;
    }

    public void hold(List<FormInstance> forms, Integer logId) {
        this.forms = forms;
        this.logId = logId;
    }

    public void clear() {
        this.forms = null;
        this.logId = null;
    }

    public List<FormInstance> getForms() {
        return forms;
    }

    public Integer getLogId() {
        return logId;
    }
}
