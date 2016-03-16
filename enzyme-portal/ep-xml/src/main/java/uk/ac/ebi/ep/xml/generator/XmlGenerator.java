/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ep.xml.generator;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ep.data.domain.UniprotEntry;
import uk.ac.ebi.ep.data.service.EnzymePortalService;
import uk.ac.ebi.ep.data.service.EnzymePortalXmlService;
import uk.ac.ebi.ep.xml.model.Database;
import uk.ac.ebi.ep.xml.service.XmlService;
import uk.ac.ebi.ep.xml.util.Preconditions;
import uk.ac.ebi.ep.xml.validator.EnzymePortalXmlValidator;

/**
 *
 * @author Joseph <joseph@ebi.ac.uk>
 */
public abstract class XmlGenerator extends XmlTransformer implements XmlService {

    @Autowired
    protected String ebeyeXSDs;

    protected EnzymePortalService enzymePortalService;
    protected EnzymePortalXmlService enzymePortalXmlService;

    protected static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XmlGenerator.class);

    public XmlGenerator(final EnzymePortalService enzymePortalService, final EnzymePortalXmlService xmlService) {
        this.enzymePortalService = enzymePortalService;
        this.enzymePortalXmlService = xmlService;
    }

    /**
     * generate the XML in the default location declared in the config files
     *
     * @throws JAXBException
     */
    public abstract void generateXmL() throws JAXBException;

    /**
     * uses default directories & XSDs provided by the implementing class.
     * implementing class must provide XML file location and XSD files to
     * validate against
     */
    public abstract void validateXML();

    /**
     * uses default XSDs provided to validate the generated XML file
     *
     * @param xmlFile xml dir/file
     * @return true if validated otherwise false
     */
    public boolean validateXML(String xmlFile) {
        Preconditions.checkArgument(ebeyeXSDs == null, "XSD file to be validated against cannot be null. Please ensure that ep-xml-config.properties is in the classpath.");
        Preconditions.checkArgument(xmlFile == null, "At least an XML File must be provided for XML validation to proceed.");
        String[] xsdFiles = ebeyeXSDs.split(",");

        return EnzymePortalXmlValidator.validateXml(xmlFile, xsdFiles);

    }

    protected void writeXml(Database database, String xmlFileLocation) throws JAXBException {
        // create JAXB context and instantiate marshaller
        JAXBContext context = JAXBContext.newInstance(Database.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        Path path = Paths.get(xmlFileLocation);
        try {
            Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            // Write to File
            m.marshal(database, writer);
            //m.marshal(database, new File(enzymeCentricXmlDir));
            //m.marshal(database, System.out);
            logger.info("Done writing XML to this Dir :" + xmlFileLocation);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    //use stream once proven working with latest spring data release
    // see issues https://jira.spring.io/browse/DATAJPA-742
    private void usingSpringDataStream(String ec) {
        try (Stream<List<UniprotEntry>> streamEntries = enzymePortalService.findStreamedSwissprotEnzymesByEc(ec)) {
            //collect stream
            List<UniprotEntry> flatEntries
                    = streamEntries.flatMap(List::stream)
                    .collect(Collectors.toList());

            // System.out.println("num items  found " + flatEntries.size());
            //save instead of printing
            try (Stream<List<UniprotEntry>> streamEntries1 = enzymePortalService.findStreamedSwissprotEnzymesByEc(ec)) {
                streamEntries1
                        .flatMap(l -> l.stream())
                        .forEach(x -> System.out.println("entry " + x));

            }

            try (Stream<UniprotEntry> streamEntries2 = enzymePortalService.streamEnzymes()) {
                streamEntries2
                        //.flatMap(l -> l.stream())
                        .forEach(x -> System.out.println("entry " + x));

            }

        }
    }
}
