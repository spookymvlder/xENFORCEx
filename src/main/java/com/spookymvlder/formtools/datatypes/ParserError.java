package com.spookymvlder.formtools.datatypes;

import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.Objects;

public class ParserError extends ErrorEntry{
    private final String nodeName;
    private final String resolvedXPath;


    public ParserError(ErrorSource source, String message, ErrorSeverity severity, String nodeName, String resolvedXpath) {
        super(source, message, severity);
        this.nodeName = nodeName;
        this.resolvedXPath = resolvedXpath;
    }

    @Override
    public String getFormattedMessage() {
        return String.format("Node %s - %s: %s at %s", nodeName, severity, message, resolvedXPath);
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getResolvedXPath() {return resolvedXPath;}

    @Override
    public String toString() {
        return "ParserError{" +
                "nodeName='" + nodeName + '\'' +
                ", resolvedXPath='" + resolvedXPath + '\'' +
                ", source=" + source +
                ", message='" + message + '\'' +
                ", severity=" + severity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ParserError that = (ParserError) o;
        return Objects.equals(getNodeName(), that.getNodeName()) && Objects.equals(getResolvedXPath(), that.getResolvedXPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getNodeName(), getResolvedXPath());
    }
}
