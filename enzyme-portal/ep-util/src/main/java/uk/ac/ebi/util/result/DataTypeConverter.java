package uk.ac.ebi.util.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
//import uk.ac.ebi.ebeye.ResultOfGetReferencedEntriesSet;
//import uk.ac.ebi.ebeye.ResultOfGetResultsIds;
import java.util.Map;
import java.util.Set;
import uk.ac.ebi.ep.config.Domain;
import uk.ac.ebi.ep.config.ResultField;
import uk.ac.ebi.ep.config.ResultFieldList;
import uk.ac.ebi.ep.config.SearchField;
import uk.ac.ebi.ep.search.model.Compound;

/**
 *
 * @since   1.0
 * @version $LastChangedRevision$ <br/>
 *          $LastChangedDate$ <br/>
 *          $Author$
 * @author  $Author$
 */
public class DataTypeConverter {


//********************************* VARIABLES ********************************//


//******************************** CONSTRUCTORS ******************************//


//****************************** GETTER & SETTER *****************************//


//********************************** METHODS *********************************//
/*
    public static String accessionToXLink(String url, String accession) {
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(accession);
        return sb.toString();
    }
*/
    public static List<String> accessionsToList (String[] arrayOfAccessions) {
        List<String> list = new ArrayList<String>();
        for (String accession:arrayOfAccessions) {
            list.add(accession);
        }
        return list;
    }
    /**
     * Retieve the ids of all domains in the {@link ResultOfGetResultsIds} list
     * @param resultList
     * @return
     */
    /*
    public static List<String> getResultsIds(
        List<ResultOfGetResultsIds> resultList) {
        Iterator it = resultList.iterator();
        List<String> resultsIds = new ArrayList<String>();
        while (it.hasNext()) {
            ResultOfGetResultsIds result =
            (ResultOfGetResultsIds)it.next();
            resultsIds.addAll(result.getResult().getString());
        }
        return resultsIds;
    }
   
    public static List<String> getResultsIdsFromDomain(
        List<ResultOfGetResultsIds> resultList, String domain) {
        Iterator it = resultList.iterator();
        List<String> uniprotIdList = new ArrayList<String>();
        while (it.hasNext()) {
            ResultOfGetResultsIds result =
            (ResultOfGetResultsIds)it.next();
            String resultDomain = result.getParamOfGetResultsIds()
                    .getResultOfGetNumberOfResults()
                    .getParamGetNumberOfResults()
                    .getDomain();
            if (resultDomain.equals(domain)) {
                uniprotIdList.addAll(result.getResult().getString());
            }
        }
        return uniprotIdList;
    }
    
    public static List<ResultOfGetResultsIds> excludeDomainFromResults(
        List<ResultOfGetResultsIds> resultList, String domain) {
        Iterator it = resultList.iterator();
        List<ResultOfGetResultsIds> uniprotIdList =
                        new ArrayList<ResultOfGetResultsIds>();
        while (it.hasNext()) {
            ResultOfGetResultsIds result =
            (ResultOfGetResultsIds)it.next();
            String resultDomain = result.getParamOfGetResultsIds()
                    .getResultOfGetNumberOfResults()
                    .getParamGetNumberOfResults()
                    .getDomain();
            if (!resultDomain.equals(domain)) {
                uniprotIdList.add(result);
            }
        }
        return uniprotIdList;
    }

    
    public static List<String> getXrefIdsFromDomain(
            List<ResultOfGetReferencedEntriesSet> xrefResults, String domain) {
         List<String> result = new ArrayList<String>();
        Iterator it = xrefResults.iterator();
        while(it.hasNext()) {
            ResultOfGetReferencedEntriesSet xrefResult =
                    (ResultOfGetReferencedEntriesSet)it.next();
            String resultDomain = xrefResult
                    .getResultOfGetResultsIds()
                    .getParamOfGetResultsIds()
                    .getResultOfGetNumberOfResults()
                    .getParamGetNumberOfResults()
                    .getDomain();
            if (resultDomain.equals(domain)) {
                result = xrefResult.getUniprotXRefList();
            }
        }
        return result;
    }
 */
    public static List<String> getConfigResultFields(Domain domain) {
        List<ResultField> fields = domain.getResultFieldList().getResultField();
        List<String> configFields = new ArrayList<String>();
        Iterator it = fields.iterator();
        while (it.hasNext()) {
            ResultField field = (ResultField)it.next();
            configFields.add(field.getId());
        }
        return configFields;
    }

    public static List<String> getConfigSearchFields(Domain domain) {
        List<SearchField> fields = domain.getSearchFieldList().getSearchField();
        List<String> configFields = new ArrayList<String>();
        Iterator it = fields.iterator();
        while (it.hasNext()) {
            SearchField field = (SearchField)it.next();
            configFields.add(field.getId());
        }
        return configFields;
    }

    public static List<Compound> mapToCompound(Map<String,String> compoundMap) {
         List<Compound> compoundList = new ArrayList<Compound>();
         Set<String> keys = compoundMap.keySet();
         for (String key:keys) {
             Compound compound = new Compound();
             compound.setId(key);
             compound.setName(compoundMap.get(key));
             compoundList.add(compound);
         }
         return compoundList;
    }
   
    public static ResultFieldList cloneResultFieldList(ResultFieldList originalObj) {
        ResultFieldList clonedObj = new ResultFieldList();
        Iterator clonedIt = clonedObj.getResultField().iterator();
        Iterator orgIt = originalObj.getResultField().iterator();
        while (orgIt.hasNext() && clonedIt.hasNext()) {
            ResultField clonedResultField = (ResultField)clonedIt.next();
            ResultField orgResultField = (ResultField)orgIt.next();
            clonedResultField.setId(orgResultField.getId());
            clonedResultField.setName(orgResultField.getName());
            clonedResultField.setResultvalue(orgResultField.getResultvalue());
            clonedObj.getResultField().add(clonedResultField);
        }
        return clonedObj;
    }

/*
    
    public static String StringOfAccessionsToXLinks(Constant.DOMAINS_ENUM domains,
                                                                    String accessions) {
        String accessionXLinks = null;
        switch (domains) {
            case unitprot:{
                accessionXLinks = uniprotAccessionsToXLinks(accessions);
                break;
            }
        }
        return accessionXLinks;
    }
    
    public static String uniprotAccessionsToXLinks(String accessions) {        
        ResourceBundle templateFile = ResourceBundle.getBundle("template");
        String[] arrayOfAccessions = accessions.split(" ");
        String url = templateFile.getString("uniprotUrl");
        StringBuffer sb = new StringBuffer();
        for (String accession:arrayOfAccessions) {
            Object[] messageArguments = {accession,url+accession,accession};
            MessageFormat formatter = new MessageFormat("");
            formatter.applyPattern(templateFile.getString("xlinkTemplate"));
            String xLink = formatter.format(messageArguments);
            sb.append(xLink);
            sb.append(" ");
        }
        return sb.toString();
        
    }
     * */

}
