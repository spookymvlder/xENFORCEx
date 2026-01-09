package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ParserSettings;

import java.util.*;

public class TEMPSETTINGS {

    private static final ImportSettings settings = ImportSettings.getInstance();

    public static void setTestSettings() {


        settings.setXmlFilePath("src/main/resources/local-only/condensedSampleDriver.xml");
        settings.setXsdFilePath("src/main/resources/local-only/sampleDriverSchema.xsd");
        settings.setSpecFilePath("src/main/resources/Documentation/field_property_template_driver_long.csv");
        settings.setFormRoot("prospectiveDrivers");
        settings.setFormRootIndex(0);

        ParserSettings parserSettings = ParserSettings.getInstance();
        parserSettings.setForeignKey("foreignKey");
        parserSettings.setFormStart("application");

        HashMap<String, List<String>> sectionToFields = new HashMap<>();
        sectionToFields.put("driverInfoHeader", Arrays.asList("driverFirstName", "driverLastName", "driverDob"));
        sectionToFields.put("licenseHeader", Arrays.asList("licenseId", "licenseFirstName", "licenseLastName", "licenseIssueDate", "licenseExpiryDate",
                "addressStreet", "addressTown", "addressRegion", "addressPost", "addressCountry", "licenseDob", "licenseRegion"));
        sectionToFields.put("driverHistoryHeader", Arrays.asList("violationDate", "violationCode", "violationType",
                "violationRegion", "violationVehicle", "violationLastName", "violationFirstName"));
        sectionToFields.put("vehicleHeader", Arrays.asList("vehicleMake", "vehicleModel", "vehicleYear", "vehicleColor", "ownerFirstName", "ownerLastName", "vehicleId", "vehicleIdRegion", "coveredVehicle", "policyFirstName",
                "policyLastName", "policyExpiryDate", "coveredRegion"));
        sectionToFields.put("additionalDriversHeader", Arrays.asList("additionalFirstName", "additionalLastName"));
        sectionToFields.put("vehicleIncidentHeader", Arrays.asList("vehicleIncidentRegion", "vehicleIncidentDate", "vehicleIncidentCode",
                "vehicleIncidentFirstName", "vehicleIncidentLastName", "vehicleIncidentVehicle"));
        sectionToFields.put("vehicleViolationHeader", Arrays.asList("violationDate", "violationCode", "violationType",
                "violationFirstName",  "violationLastName", "violationVehicle", "violationRegion"));
        parserSettings.setFormSectionToFields(sectionToFields);

        HashMap<String, String> formFieldToPath = new HashMap<>();
        formFieldToPath.put("driverFirstName", "descendant::name/given");
        formFieldToPath.put("driverLastName", "descendant::name/family");
        formFieldToPath.put("driverDob", "descendant::birthDate");

        formFieldToPath.put("licenseId", "descendant::licenseIdentifier/@id");
        formFieldToPath.put("licenseRegion", "descendant::licenseIdentifier/registeredRegion");
        formFieldToPath.put("licenseFirstName", "descendant::name/given");
        formFieldToPath.put("licenseLastName", "descendant::name/family");
        formFieldToPath.put("licenseIssueDate", "descendant::issueDate");
        formFieldToPath.put("licenseExpiryDate", "descendant::expirationDate");
        formFieldToPath.put("licenseDob", "descendant::birthDate");

        formFieldToPath.put("addressStreet", "descendant::address/street");
        formFieldToPath.put("addressTown", "descendant::address/town");
        formFieldToPath.put("addressRegion", "descendant::address/region");
        formFieldToPath.put("addressPost", "descendant::address/post");
        formFieldToPath.put("addressCountry", "descendant::country/code/@code");

        formFieldToPath.put("vehicleMake", "descendant::vehicleInfo/make");
        formFieldToPath.put("vehicleModel", "descendant::vehicleInfo/model");
        formFieldToPath.put("vehicleYear", "descendant::vehicleInfo/year");
        formFieldToPath.put("vehicleColor", "descendant::vehicleInfo/color");
        formFieldToPath.put("vehicleId", "descendant::vehicleInfo/vehicleIdentifier/@id");
        formFieldToPath.put("vehicleIdRegion", "descendant::vehicleInfo/vehicleIdentifier/registeredRegion");
        formFieldToPath.put("ownerFirstName", "descendant::owner/name/given");
        formFieldToPath.put("ownerLastName", "descendant::owner/name/family");

        formFieldToPath.put("policyFirstName", "descendant::policyHolder/name/given");
        formFieldToPath.put("policyLastName", "descendant::policyHolder/name/family");
        formFieldToPath.put("additionalFirstName", "descendant::name/given");
        formFieldToPath.put("additionalLastName", "descendant::name/family");
        formFieldToPath.put("policyExpiryDate", "descendant::insurance/expirationDate");
        formFieldToPath.put("coveredVehicle", "descendant::coverage/vehicleIdentifier/@id");
        formFieldToPath.put("coveredRegion", "descendant::coverage/vehicleIdentifier/registeredRegion");

        formFieldToPath.put("violationDate", "descendant::effectiveDate");
        formFieldToPath.put("violationCode", "descendant::code/@code");
        formFieldToPath.put("violationType", "descendant::code/@type");
        formFieldToPath.put("violationFirstName", "descendant::name/given");
        formFieldToPath.put("violationLastName", "descendant::name/family");
        formFieldToPath.put("violationVehicle", "descendant::vehicleIdentifier/@id");
        formFieldToPath.put("violationRegion", "descendant::vehicleIdentifier/registeredRegion");

        formFieldToPath.put("vehicleIncidentVehicle", "vehicleIdentifier/@id");
        formFieldToPath.put("vehicleIncidentRegion", "vehicleIdentifier/registeredRegion");
        formFieldToPath.put("vehicleIncidentDate", "effectiveDate");
        formFieldToPath.put("vehicleIncidentCode", "code/@code");
        formFieldToPath.put("vehicleIncidentFirstName", "descendant::name/given");
        formFieldToPath.put("vehicleIncidentLastName", "descendant::name/family");

        formFieldToPath.put("vehicleHeader", "vehicle");
        formFieldToPath.put("driverHistoryHeader", "descendant::driverHistory/violation");
        formFieldToPath.put("additionalDriversHeader", "descendant::additionalDrivers/driver");
        formFieldToPath.put("driverInfoHeader", "descendant::driverInfo");
        formFieldToPath.put("licenseHeader", "descendant::license");
        formFieldToPath.put("vehicleViolationHeader", "descendant::vehicleHistory/violation");
        formFieldToPath.put("vehicleIncidentHeader", "descendant::vehicleHistory/incident");
        formFieldToPath.put("driver", "descendant::driver");
        formFieldToPath.put("assets", "descendant::assets");
        parserSettings.setFormFieldToPath(formFieldToPath);

        parserSettings.setTopLevelSections(new ArrayList<>(Arrays.asList("driver", "assets")));

        settings.setTestIgnoreXSD(true);

        //TODO deprecate
        Map<String, String> sectionNameToPath = new HashMap<>();
        sectionNameToPath.put("driver", "descendant::driver");
        sectionNameToPath.put("assets", "descendant::assets");
        parserSettings.setSectionNameToPath(sectionNameToPath);

        Map<String, List<String>> subsections = new HashMap<>();
        subsections.put("driver", Arrays.asList("driverInfoHeader", "licenseHeader", "driverHistoryHeader"));
        subsections.put("assets", Arrays.asList("vehicleHeader", "additionalDriversHeader", "vehicleIncidentHeader", "vehicleViolationHeader"));
        parserSettings.setSubsections(subsections);
    }
}
