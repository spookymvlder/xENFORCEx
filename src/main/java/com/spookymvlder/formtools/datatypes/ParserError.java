package com.spookymvlder.formtools.datatypes;

import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.Objects;

public class ParserError extends ErrorEntry{
    private final String nodeName;


    public ParserError(ErrorSource source, String message, ErrorSeverity severity, String nodeName) {
        super(source, message, severity);
        this.nodeName = nodeName;

    }

    @Override
    public String getFormattedMessage() {
        return String.format("Node %s - %s: %s", nodeName, severity, message);
    }

    public String getNodeName() {
        return nodeName;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ParserError that = (ParserError) o;
        return Objects.equals(getNodeName(), that.getNodeName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getNodeName());
    }
}
