package com.spookymvlder.formtools.utilities;

import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.datatypes.fields.*;

import java.util.List;
import java.util.Map;

public final class FormDebugPrinter {
    private FormDebugPrinter() {}

    private static final String INDENT = "  ";

    public static void printForms(List<FormInstance> instances) {
        if (instances == null || instances.isEmpty()) {
            System.out.println("[FormDebugPrinter] No form instances to print.");
            return;
        }
        for (int i = 0; i < instances.size(); i++) {
            printInstance(instances.get(i), i);
        }
    }

    private static void printInstance(FormInstance instance, int index) {
        System.out.println();
        System.out.println("=== Form Instance " + index + ": \"" + instance.getFormName() + "\" ===");
        printField(instance.getStartField(), "");
    }

    private static void printField(FormField field, String indent) {
        if (field == null) return;

        String tag;
        String valueStr;

        if (field instanceof CompositeField composite) {
            tag = "[COMPOSITE]";
            valueStr = formatComposite(composite.getValue());
        } else if (field instanceof SimpleField simple) {
            tag = "[SIMPLE]";
            FieldData data = simple.getValue();
            valueStr = data != null ? "\"" + data.getValue() + "\"" : "(null)";
        } else if (field instanceof ContainerField) {
            tag = "[CONTAINER]";
            valueStr = "";
        } else {
            tag = "[FIELD]";
            valueStr = "";
        }

        String name = field.getDisplayName() != null ? field.getDisplayName() : "(unnamed)";
        String path = field.getResolvedPath() != null ? "  →  " + field.getResolvedPath() : "";

        if (valueStr.isEmpty()) {
            System.out.println(indent + tag + " " + name + path);
        } else {
            System.out.println(indent + tag + " " + name + ": " + valueStr + path);
        }

        for (FormField child : field.getChildren()) {
            printField(child, indent + INDENT);
        }
    }

    private static String formatComposite(Map<String, FieldData> constituents) {
        if (constituents == null || constituents.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        constituents.forEach((key, data) -> {
            sb.append(key).append("=");
            sb.append(data != null ? "\"" + data.getValue() + "\"" : "null");
            sb.append(", ");
        });
        sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }
}