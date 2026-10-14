package com.spookymvlder.formtools.cli;

import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ProcessingOverrides;
import com.spookymvlder.formtools.utilities.ErrorSeverity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Loads/saves a {@link CliConfig} as a flat Java Properties file (no new dependency — every setting
 * here is a scalar, so there's nothing a richer format like JSON would buy today), and moves values
 * between that snapshot and the live settings singletons.
 */
public class ConfigFileManager {

    public static final String DEFAULT_CONFIG_PATH = "xenforcex.properties";

    private ConfigFileManager() {
    }

    public static CliConfig load(String path) throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(path)) {
            props.load(in);
        }
        CliConfig config = new CliConfig();
        config.setXmlFilePath(props.getProperty("xmlFilePath"));
        config.setXsdFilePath(props.getProperty("xsdFilePath"));
        config.setSpecFilePath(props.getProperty("specFilePath"));
        config.setRootId(props.getProperty("rootId"));
        config.setOutputDirectory(props.getProperty("outputDirectory"));
        config.setSkipXsdValidation(Boolean.parseBoolean(props.getProperty("skipXsdValidation", "false")));
        config.setVerbose(Boolean.parseBoolean(props.getProperty("verbose", "false")));
        config.setDateFormat(props.getProperty("dateFormat", "yyyy-MM-dd"));
        String thresholdRaw = props.getProperty("errorThreshold", "FATAL").trim().toUpperCase();
        try {
            config.setErrorThreshold(ErrorSeverity.valueOf(thresholdRaw));
        } catch (IllegalArgumentException e) {
            throw new IOException("Config file has an invalid errorThreshold value: " + thresholdRaw, e);
        }
        return config;
    }

    public static void save(CliConfig config, String path) throws IOException {
        Properties props = new Properties();
        putIfNotNull(props, "xmlFilePath", config.getXmlFilePath());
        putIfNotNull(props, "xsdFilePath", config.getXsdFilePath());
        putIfNotNull(props, "specFilePath", config.getSpecFilePath());
        putIfNotNull(props, "rootId", config.getRootId());
        putIfNotNull(props, "outputDirectory", config.getOutputDirectory());
        props.setProperty("skipXsdValidation", String.valueOf(config.isSkipXsdValidation()));
        props.setProperty("verbose", String.valueOf(config.isVerbose()));
        props.setProperty("dateFormat", config.getDateFormat());
        props.setProperty("errorThreshold", config.getErrorThreshold().name());
        try (OutputStream out = new FileOutputStream(path)) {
            props.store(out, "xENFORCEx settings");
        }
    }

    private static void putIfNotNull(Properties props, String key, String value) {
        if (value != null) {
            props.setProperty(key, value);
        }
    }

    /** Builds a config snapshot from whatever the runtime singletons currently hold. */
    public static CliConfig snapshotFromRuntime() {
        ImportSettings importSettings = ImportSettings.getInstance();
        CliConfig config = new CliConfig();
        config.setXmlFilePath(importSettings.getXmlFilePath());
        config.setXsdFilePath(importSettings.getXsdFilePath());
        config.setSpecFilePath(importSettings.getSpecFilePath());
        config.setRootId(importSettings.getRootId());
        config.setOutputDirectory(importSettings.getOutputDirectory());
        config.setSkipXsdValidation(importSettings.getSkipXsdValidation());
        config.setVerbose(importSettings.isVerbose());
        config.setDateFormat(ProcessingOverrides.getInstance().getDateFormat());
        config.setErrorThreshold(ErrorSettings.getInstance().getErrorThreshold());
        return config;
    }

    /** Pushes a config snapshot into the runtime singletons — the moment "working" becomes "active". */
    public static void applyToRuntime(CliConfig config) {
        ImportSettings importSettings = ImportSettings.getInstance();
        importSettings.setXmlFilePath(config.getXmlFilePath());
        importSettings.setXsdFilePath(config.getXsdFilePath());
        importSettings.setSpecFilePath(config.getSpecFilePath());
        importSettings.setRootId(config.getRootId());
        importSettings.setOutputDirectory(config.getOutputDirectory());
        importSettings.setSkipXsdValidation(config.isSkipXsdValidation());
        importSettings.setVerbose(config.isVerbose());
        ProcessingOverrides.getInstance().setDateFormat(config.getDateFormat());
        ProcessingOverrides.getInstance().setConverters();
        ErrorSettings.getInstance().updateErrorThreshold(config.getErrorThreshold());
    }
}
