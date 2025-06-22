package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.settings.ImportSettings;

import com.spookymvlder.formtools.settings.ParserSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ImportXml {

    private static final ImportSettings settings = ImportSettings.getInstance();

    // Temp main for testing.
    public static void main(String[] args) {
        settings.setXmlFilePath("src/main/resources/local-only/sampleDriver.xml");
        settings.setXsdFilePath("src/main/resources/local-only/sampleDriverSchema.xsd");
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

        formFieldToPath.put("vehicleHeader", "descendant::assets/vehicle");
        formFieldToPath.put("driverHistoryHeader", "descendant::driverHistory/violation");
        formFieldToPath.put("additionalDriversHeader", "descendant::additionalDrivers/driver");
        formFieldToPath.put("driverInfoHeader", "descendant::driverInfo");
        formFieldToPath.put("licenseHeader", "descendant::license");
        formFieldToPath.put("vehicleViolationHeader", "descendant::vehicleHistory/violation");
        formFieldToPath.put("vehicleIncidentHeader", "descendant::vehicleHistory/incident");
        parserSettings.setFormFieldToPath(formFieldToPath);

        parserSettings.setTopLevelSections(new ArrayList<>(Arrays.asList("driver", "assets")));


        Map<String, String> sectionNameToPath = new HashMap<>();
        sectionNameToPath.put("driver", "descendant::driver");
        sectionNameToPath.put("assets", "descendant::assets");
        parserSettings.setSectionNameToPath(sectionNameToPath);

        Map<String, List<String>> subsections = new HashMap<>();
        subsections.put("driver", Arrays.asList("driverInfoHeader", "licenseHeader", "driverHistoryHeader"));
        subsections.put("assets", Arrays.asList("licenseHeader", "vehicleHeader", "additionalDriversHeader", "vehicleIncidentHeader", "vehicleViolationHeader"));
        parserSettings.setSubsections(subsections);

        try{
        Document doc = importXML();
        ParseDoc.parseDoc(doc);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }



    public static Document importXML() throws Exception {
        //TODO put in specification version ID for xml so can validate that against our xpaths saved. Also put in xml id and store that temporarily so we don't double parse.
        File xmlFile = new File(settings.getXmlFilePath());
        File xsdFile = new File(settings.getXsdFilePath());
        validateXML(xmlFile, xsdFile);
        return getDocument(xmlFile);
    }



    private static Boolean validateXmlSettings() {
        //TODO come back to when internet and see why not working.

        if(!settings.getXmlFilePath().toLowerCase().endsWith(".xml")) {
            return false;
        }
        return settings.getFormRoot() != null;
    }

    private static Boolean validateXsdSettings() {
        if(!Paths.get(settings.getXsdFilePath()).endsWith(".xsd")) {
            return false;
        }
        return validateXmlSettings();
    }



    public static Document getDocument(File xmlFile) throws SAXException, IOException, ParserConfigurationException {
        if (!validateXmlSettings()){
            throw new InvalidPropertiesFormatException("Invalid XML settings.");
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        dbFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        //TODO ensure comments are stripped out too
        stripEmptyElements(doc.getElementsByTagName(settings.getFormRoot()).item(settings.getFormRootIndex()));
        return doc;
    }

    public static void validateXML(File xmlFile, File xsdFile) throws IOException, SAXException, ParserConfigurationException {
        //validateXML(xmlFile, xsdFile); Probably an error
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(xsdFile);
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xmlFile));
    }


    //https://stackoverflow.com/questions/11703635/strip-whitespace-and-newlines-from-xml-in-java
    private static void stripEmptyElements(Node node){
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().isEmpty()) {
                    child.getParentNode().removeChild(child);
                    i--;
                }
            }
            stripEmptyElements(child);
        }
    }



}