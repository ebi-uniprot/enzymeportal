package uk.ac.ebi.ep.intenz.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import uk.ac.ebi.ep.enzyme.model.EcClass;
import uk.ac.ebi.ep.enzyme.model.EnzymeHierarchy;
import uk.ac.ebi.intenz.xml.jaxb.EcClassType;
import uk.ac.ebi.intenz.xml.jaxb.EcSubclassType;
import uk.ac.ebi.intenz.xml.jaxb.EcSubsubclassType;
import uk.ac.ebi.intenz.xml.jaxb.EntryType;
import uk.ac.ebi.intenz.xml.jaxb.EnzymeNameType;
import uk.ac.ebi.intenz.xml.jaxb.Intenz;
import uk.ac.ebi.intenz.xml.jaxb.Synonyms;
import uk.ac.ebi.intenz.xml.jaxb.XmlContentType;

/**
 *
 * @since   1.0
 * @version $LastChangedRevision$ <br/>
 *          $LastChangedDate$ <br/>
 *          $Author$
 * @author  $Author$
 */
public class IntenzCallable {

//********************************* VARIABLES ********************************//
    public static final String INTENZ_PACKAGE = "uk.ac.ebi.intenz.xml.jaxb";
    private static Logger log = Logger.getLogger(GetSynonymsCaller.class);

//******************************** CONSTRUCTORS ******************************//
//****************************** GETTER & SETTER *****************************//
//********************************** METHODS *********************************//
//******************************** INNER CLASS *******************************//
    public static class GetIntenzCaller implements Callable<Intenz> {

        protected String ecUrl;

        public GetIntenzCaller(String ecUrl) {
            this.ecUrl = ecUrl;
        }

        public GetIntenzCaller() {
        }

        public Intenz call() throws Exception {
            return getData();
        }

        public Intenz getData() {
            URL url = null;
            try {
                url = new URL(ecUrl);
            } catch (MalformedURLException ex) {
                log.error(IintenzAdapter.FAILED_MSG
                        + "ec url is invalid: " + this.ecUrl, ex);
            }

            URLConnection con = null;
            try {
                con = url.openConnection();
            } catch (IOException ex) {
                log.error(IintenzAdapter.FAILED_MSG + "Unable to connection to Intenz server!", ex);
            }

            InputStream is = null;
            try {
                is = con.getInputStream();
            } catch (IOException ex) {
                /*If there is an error of this type write in the log rather than
                 * throwing a exception so that the processing can continue.
                 */
                log.error(IintenzAdapter.FAILED_MSG + "Unable download the xml file from "
                        + this.ecUrl + "!", ex);
                /*
                throw new SynonymException(IintenzAdapter.FAILED_MSG + "Unable download the xml file from "
                +this.ecUrl +"!", ex);
                 * *
                 */
            }

            JAXBContext jaxbContext = null;
            try {
                jaxbContext = JAXBContext.newInstance(INTENZ_PACKAGE);
            } catch (JAXBException ex) {
                log.error(IintenzAdapter.FAILED_MSG + "Unable to find the package "
                        + INTENZ_PACKAGE + " to map the intenz xml file!", ex);
            }
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = jaxbContext.createUnmarshaller();
            } catch (JAXBException ex) {
                log.error(IintenzAdapter.FAILED_MSG + "Failed to create JAXB Context!", ex);
            }
            Intenz intenz = null;
            try {
                if (is != null) {
                    intenz = (Intenz) unmarshaller.unmarshal(is);
                }
            } catch (JAXBException ex) {
                log.error(IintenzAdapter.FAILED_MSG + "unmarshal Intenz xml file "
                        + "to the object model failed!", ex);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    log.warn("Failed to close inputstream of the intenz xml file! ");
                }
            }

            return intenz;
        }

        public EcClass setEnzymeName(XmlContentType contentType, String ecNumber) {
            EcClass ecClass = new EcClass();
            String name = null;
            if (contentType != null) {
                List<Object> nameObject = contentType.getContent();
                name = (String) nameObject.get(0);
            } else {
                name = ecNumber;
            }

            ecClass.setEc(ecNumber);
            ecClass.setName(name);
            return ecClass;
        }

        public EcClass setEnzymeName(List<Object> nameObject, String ecNumber) {
            EcClass ecClass = new EcClass();
            String name = (String) nameObject.get(0);
            ecClass.setEc(ecNumber);
            ecClass.setName(name);
            return ecClass;
        }

        public List<EcClass> getEcClass(Intenz intenz) {
            List<EcClass> ecClasseList = new ArrayList<EcClass>();
            EcClassType levelOne = intenz.getEcClass().get(0);
            String levelOneEc = levelOne.getEc1().toString();
            EcClass ecClass = setEnzymeName(levelOne.getName(), levelOneEc);
            ecClasseList.add(ecClass);

            EcSubclassType levelTwo = (EcSubclassType) levelOne.getEcSubclass().get(0);
            String levelTwoEc = levelOneEc +"." +levelTwo.getEc2().toString();
            EcClass ecClass2 = setEnzymeName(levelTwo.getName(), levelTwoEc);
            ecClasseList.add(ecClass2);

            EcSubsubclassType levelThree = (EcSubsubclassType) levelTwo.getEcSubSubclass().get(0);
            String levelThreeEc =  levelTwoEc+"."  +levelThree.getEc3().toString();
            EcClass ecClass3 = setEnzymeName(levelThree.getName(), levelThreeEc);
            ecClasseList.add(ecClass3);

            EntryType levelFour = (EntryType) levelThree.getEnzyme().get(0);
            String levelFourEc = levelThreeEc +"." +levelFour.getEc4().toString();
            EnzymeNameType enzymeNameType = (EnzymeNameType) levelFour.getAcceptedName().get(0);
            enzymeNameType.getContent();
            EcClass ecClass4 = setEnzymeName(
                    enzymeNameType.getContent(), levelFourEc);
            ecClasseList.add(ecClass4);

            return ecClasseList;
        }
    }

    public static class GetSynonymsCaller implements Callable<Set<String>> {

        protected GetIntenzCaller intenzCaller;

        public GetSynonymsCaller(String ecUrl) {
            intenzCaller = new GetIntenzCaller(ecUrl);
        }

        public GetSynonymsCaller() {
        }

        public Set<String> call() throws Exception {
            return getSynonyms();
        }

        public Set<String> getSynonyms() {
            Intenz intenz = intenzCaller.getData();
            Set<String> synonyms = new LinkedHashSet<String>();
            if (intenz != null) {
                synonyms.addAll(getSynonyms(intenz));
            }
            return synonyms;
        }

        public Set<String> getSynonyms(Intenz intenz) {
            Set<String> names = new LinkedHashSet<String>();
            Synonyms synonymsType =
                    intenz.getEcClass().get(0).getEcSubclass().get(0).getEcSubSubclass().get(0).getEnzyme().get(0).getSynonyms();
            if (synonymsType != null) {
                List<EnzymeNameType> synonyms = synonymsType.getSynonym();
                if (synonyms.size() > 0) {
                    for (EnzymeNameType synonym : synonyms) {
                        List<Object> objList = synonym.getContent();
                        String name = (String) objList.get(0);
                        names.add(name);
                    }

                }

            }
            return names;
        }
    }

    public static class GetEcHierarchyCaller implements Callable<EnzymeHierarchy> {

        protected GetIntenzCaller intenzCaller;

        public GetEcHierarchyCaller(String ecUrl) {
            intenzCaller = new GetIntenzCaller(ecUrl);
        }

        public GetEcHierarchyCaller() {
            intenzCaller = new GetIntenzCaller();
        }

        public EnzymeHierarchy call() throws Exception {
            Intenz intenz = intenzCaller.getData();
            return getEcHierarchy(intenz);
        }

        public EnzymeHierarchy getEcHierarchy(Intenz intenz) {
            EnzymeHierarchy enzymeHierarchy = new EnzymeHierarchy();
            List<EcClass> ecClassList = intenzCaller.getEcClass(intenz);
            enzymeHierarchy.setEcclass(ecClassList);
            return enzymeHierarchy;
        }
    }
}
