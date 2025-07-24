package com.spookymvlder.formtools.datatypes;

import java.util.Objects;

//TODO - remove
/**
 * Class FormEntry is a data type encapsulating a field's value and the foreign key associated with that value.
 */
public class FormEntry {
    private String entryValue;
    private String foreignKey;

    /**
     * Constructor
     * @param formValue - value of form
     * @param foreignKey - id value is associated with. Necessary when more than one object is specified on the form, such
     *                   as a person having two vehicles.
     */
    public FormEntry(String formValue, String foreignKey) {
        this.entryValue = formValue;
        this.foreignKey = foreignKey;
    }

    /**
     * Getter for entry's value
     * @return - value as string
     */
    public String getEntryValue() {
        return entryValue;
    }

    /**
     * Getter for foreign key
     * @return as string
     */
    public String getForeignKey() {
        return foreignKey;
    }

    @Override
    public String toString() {
        return "finalproject.formintake.datatypes.FormEntry{" +
                "formValue='" + entryValue + '\'' +
                ", foreignKey='" + foreignKey + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FormEntry formEntry = (FormEntry) o;
        return Objects.equals(getEntryValue(), formEntry.getEntryValue()) && Objects.equals(getForeignKey(), formEntry.getForeignKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntryValue(), getForeignKey());
    }
}
