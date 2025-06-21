package com.spookymvlder.formtools.datatypes;

import java.util.List;

//TODO make abstract form piece and inherit.
public class FormInstance {
    private String longName;
    private List<FormSection> sections;
    private String metadata;

    public FormInstance(String longName, List<FormSection> sections) {
        this.longName = longName;
        this.sections = sections;
    }

    public String getLongName() {
        return longName;
    }

    public List<FormSection> getSections() {
        return sections;
    }

}
