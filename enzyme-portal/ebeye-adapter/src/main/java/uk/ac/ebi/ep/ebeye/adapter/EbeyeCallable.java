package uk.ac.ebi.ep.ebeye.adapter;

import java.util.concurrent.Callable;
import uk.ac.ebi.ebeye.param.ParamGetNumberOfResults;
import uk.ac.ebi.ebeye.param.ParamOfGetResults;
import uk.ac.ebi.ebeye.util.Transformer;
import uk.ac.ebi.webservices.ebeye.ArrayOfArrayOfString;
import uk.ac.ebi.webservices.ebeye.ArrayOfEntryReferences;
import uk.ac.ebi.webservices.ebeye.ArrayOfString;
import uk.ac.ebi.webservices.ebeye.EBISearchService;
import uk.ac.ebi.webservices.ebeye.EBISearchService_Service;

/**
 *
 * @since   1.0
 * @version $LastChangedRevision$ <br/>
 *          $LastChangedDate$ <br/>
 *          $Author$
 * @author  $Author$
 */
public class EbeyeCallable {

//********************************* VARIABLES ********************************//
    
    private static EBISearchService eBISearchService
            = new EBISearchService_Service().getEBISearchServiceHttpPort();


//******************************** CONSTRUCTORS ******************************//


//****************************** GETTER & SETTER *****************************//


//********************************** METHODS *********************************//
//******************************** INNER CLASS *******************************//

    public static class NumberOfResultsCaller
            implements Callable<Integer> {
        protected ParamGetNumberOfResults paramGetNumberOfResults;

        public NumberOfResultsCaller(
                ParamGetNumberOfResults paramGetNumberOfResults) {
            this.paramGetNumberOfResults = paramGetNumberOfResults;
        }

        public ParamGetNumberOfResults getParamGetNumberOfResults() {
            return paramGetNumberOfResults;
        }

        public void setParamGetNumberOfResults(
                ParamGetNumberOfResults paramGetNumberOfResults) {
            this.paramGetNumberOfResults = paramGetNumberOfResults;
        }

        public Integer getNumberOfResults() {
            int totalFound = eBISearchService.getNumberOfResults(
                    paramGetNumberOfResults.getDomain()
                    , paramGetNumberOfResults.getQuery());
            return totalFound;
        }

        public Integer call() throws Exception {
           return getNumberOfResults();
        }

    }

//******************************** INNER CLASS *******************************//
    public static class GetResultsCallable
            implements Callable<ArrayOfArrayOfString> {
        protected ParamOfGetResults param;
        protected int start;
        protected int size;

        public GetResultsCallable(ParamOfGetResults param, int start, int size) {
            this.param = param;
            this.start = start;
            this.size = size;
        }


        public ArrayOfArrayOfString call() throws Exception {
            return callGetResults();
        }

        public ArrayOfArrayOfString callGetResults() {
            ArrayOfString ebeyeFields = Transformer
                    .transformToArrayOfString(param.getFields());
            ArrayOfArrayOfString EbeyeResult = eBISearchService
                    .getResults(
                    param.getDomain()
                    ,param.getQuery()
                    ,ebeyeFields
                    ,start
                    ,size
                    );
            return EbeyeResult;
        }

    }

//getAllDomainsResults

//******************************** INNER CLASS *******************************//
//******************************** INNER CLASS *******************************//
    public static class GetEntriesCallable
            implements Callable<ArrayOfArrayOfString>  {
        protected String domain;
        protected ArrayOfString id;
        protected ArrayOfString fields;

        public GetEntriesCallable(String domain, ArrayOfString id, ArrayOfString fields) {
            this.domain = domain;
            this.id = id;
            this.fields = fields;
        }

        public ArrayOfArrayOfString call() throws Exception {
            return callGetEntries();
        }

        public ArrayOfArrayOfString callGetEntries() {
            ArrayOfArrayOfString EbeyeResult = eBISearchService
                    .getEntries(domain, id, fields);
            return EbeyeResult;
        }

    }

//******************************** INNER CLASS *******************************//
    public static class GetReferencedEntriesSet
            implements Callable<ArrayOfEntryReferences>  {
        protected String domain;
        protected ArrayOfString entries;
        protected String referencedDomain;
        protected ArrayOfString fields;

        public GetReferencedEntriesSet(String domain, ArrayOfString entries
                , String referencedDomain, ArrayOfString fields) {
            this.domain = domain;
            this.entries = entries;
            this.referencedDomain = referencedDomain;
            this.fields = fields;
        }



        public ArrayOfEntryReferences call() throws Exception {
            return callGetReferencedEntriesSet();
        }

        public ArrayOfEntryReferences callGetReferencedEntriesSet() {
            ArrayOfEntryReferences EbeyeResult = eBISearchService
                    .getReferencedEntriesSet(domain, entries, referencedDomain, fields);
            return EbeyeResult;
        }

    }



//******************************** INNER CLASS *******************************//

/*
    public static class GetAllResultsCallable
            implements Callable<List<ArrayOfArrayOfString>> {
        ParamOfGetAllResults paramOfGetAllResults;

        public GetAllResultsCallable(ParamOfGetAllResults paramOfGetAllResults) {
            this.paramOfGetAllResults = paramOfGetAllResults;
        }


        public List<ArrayOfArrayOfString> call() throws Exception {
            return getAllResults();
        }

    public List<ArrayOfArrayOfString> getAllResults()
                                        throws InterruptedException, ExecutionException {
        NumberOfResultsCaller caller =
                new EbeyeCallable.NumberOfResultsCaller(paramOfGetAllResults);

        int totalFound = caller.getNumberOfResults();
        List<ArrayOfArrayOfString> rawResults =
                this.getEbeyeResults(totalFound);
        return rawResults;
    }

    public List<ArrayOfArrayOfString> getEbeyeResults (int totalFound)
            throws InterruptedException, ExecutionException {
        String domain = paramOfGetAllResults.getDomain();
        String query = paramOfGetAllResults.getQuery();
        List<String> fields = paramOfGetAllResults.getFields();

        List<ArrayOfArrayOfString> resultList = new ArrayList<ArrayOfArrayOfString>();
        if (totalFound > 0) {
            ExecutorService pool = Executors.newCachedThreadPool();
            try {
                ArrayOfString ebeyeFields = Transformer.transformToArrayOfString(fields);
                int numberOfLoops = Calculator.calTotalPages(totalFound,
                        IEbeyeAdapter.EBEYE_RESULT_LIMIT);
                int lastLoopSize = Calculator.getLastPageResults(totalFound,
                        IEbeyeAdapter.EBEYE_RESULT_LIMIT);
                int currentLoop = 0;
                int start = 0;
                while (currentLoop < numberOfLoops) {
                    int size = IEbeyeAdapter.EBEYE_RESULT_LIMIT;
                    if (currentLoop == numberOfLoops-1) {
                        size = lastLoopSize;
                    }
                    Callable<ArrayOfArrayOfString> callable =
                            new GetResultsCallable(
                            domain, query, ebeyeFields, start,size);
                    Future<ArrayOfArrayOfString> future = pool.submit(callable);
                    ArrayOfArrayOfString rawResults = (ArrayOfArrayOfString)future.get();
                    resultList.add(rawResults);
                    start = start+size;
                    currentLoop++;
                }
            }
            finally {
                pool.shutdown();
            }

        }
        return resultList;
    }

    }
*/


}
