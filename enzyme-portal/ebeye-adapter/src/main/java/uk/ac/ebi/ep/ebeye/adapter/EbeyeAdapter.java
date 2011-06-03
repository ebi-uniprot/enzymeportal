package uk.ac.ebi.ep.ebeye.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;
import uk.ac.ebi.ebeye.param.ParamOfGetResults;
import uk.ac.ebi.ebeye.param.ParamOfResultSize;
import uk.ac.ebi.ebeye.util.Transformer;
import uk.ac.ebi.ep.ebeye.adapter.EbeyeCallable.GetResultsCallable;
import uk.ac.ebi.ep.ebeye.adapter.EbeyeCallable.NumberOfResultsCaller;
import uk.ac.ebi.ep.ebeye.result.jaxb.Result;
import uk.ac.ebi.ep.search.exception.MultiThreadingException;
import uk.ac.ebi.ep.search.result.Pagination;
import uk.ac.ebi.ep.util.query.LuceneQueryBuilder;
import uk.ac.ebi.webservices.ebeye.ArrayOfArrayOfString;
import uk.ac.ebi.webservices.ebeye.ArrayOfString;

/**
 *
 * @since   1.0
 * @version $LastChangedRevision$ <br/>
 *          $LastChangedDate$ <br/>
 *          $Author$
 * @author  $Author$
 */
public class EbeyeAdapter implements IEbeyeAdapter {

//********************************* VARIABLES ********************************//
//private static Logger log = Logger.getLogger(EbeyeAdapter.class);
  private static Logger log = Logger.getLogger(EbeyeAdapter.class);

//******************************** CONSTRUCTORS ******************************//


//****************************** GETTER & SETTER *****************************//


//********************************** METHODS *********************************//
    public List<Result> getAllResults(ParamOfGetResults paramOfGetAllResults){
         List<Result> resultList = new ArrayList<Result>();
         /*
        List<ArrayOfArrayOfString> rawResults = getAllEbeyeResults(paramOfGetAllResults);
        List<List<String>> transformedResults = Transformer.transformToList(rawResults);
        //Save the content to an object
        ResultFactory resultFactory = new ResultFactory(
                paramOfGetAllResults.getDomain(), paramOfGetAllResults.getFields());
        resultList = resultFactory.getResults(transformedResults);
        */
        return resultList;

    }

    public Map<String, List<Result>> getMultiDomainsResults(
            List<ParamOfGetResults> paramOfGetResultsList) throws MultiThreadingException  {        
        getNumberOfResults(paramOfGetResultsList);
        Map<String, List<Result>> allDomainsResults = new HashMap<String, List<Result>>();
        //getResultsByAccessions(paramOfGetResultsList)

        for (ParamOfGetResults param:paramOfGetResultsList )  {
                    List<String> domainFields = param.getFields();
                    String domain  = param.getDomain();

                    List<Callable<ArrayOfArrayOfString>> callableList
                             = prepareCallableCollection(param);


                     //List<ArrayOfArrayOfString> rawResults =
                        //      submitAll(paramOfGetResultsList);
                     //List<ArrayOfArrayOfString> rawResults = submitAll(callableList);
                     List<ArrayOfArrayOfString> rawResults = executeCallables(callableList);
                     List<Result> resultList =
                     transformRawResult(domain, domainFields, rawResults);
                     /*
                    List<List<String>> transformedResults
                            = Transformer.transformToList(rawResults);
                    //Save the content to an object

                    ResultFactory resultFactory = new ResultFactory(
                            domain, domainFields);
                    List<Result> resultList = resultFactory.getResults(transformedResults);
                      *
                      */
                    allDomainsResults.put(domain, resultList);
        }
        return allDomainsResults;
    }

    /*
    public Map<String, List<ParamOfGetResults>> getNumberOfResults(
            List<ParamOfGetAllResults> paramOfGetAllResultsList) throws MultiThreadingException {
        Map<String, List<ParamOfGetResults>> domainParams = new
                Hashtable<String, List<ParamOfGetResults>>();
        ExecutorService pool = Executors.newCachedThreadPool();        
        try {
            for (ParamOfGetAllResults param:paramOfGetAllResultsList) {
                Callable<Integer> callable = new NumberOfResultsCaller(param);
                Future<Integer> future = pool.submit(callable);
                Integer totalFound;
                try {
                    totalFound = future.get(IEbeyeAdapter.EBEYE_ONLINE_REQUEST_TIMEOUT, TimeUnit.SECONDS);

                if (totalFound > IEbeyeAdapter.EP_RESULTS_PER_DOIMAIN_LIMIT) {
                    totalFound = IEbeyeAdapter.EP_RESULTS_PER_DOIMAIN_LIMIT;
                }

                List<ParamOfGetResults> paramOfGetResultsList = null;
                if (totalFound > 0) {
                        paramOfGetResultsList = prepareGetResultsParams(totalFound, param);
                        domainParams.put(param.getDomain(), paramOfGetResultsList);
                }
                } catch (InterruptedException ex) {
                    throw  new MultiThreadingException(ex.getMessage(), ex);
                } catch (ExecutionException ex) {
                    throw  new MultiThreadingException(ex.getMessage(), ex);
                } catch (TimeoutException ex) {
                    throw  new MultiThreadingException(ex.getMessage(), ex);
                }
                      
            }            
        }
        finally {
            pool.shutdown();
        }

        return domainParams;

    }
*/
/*
    public Map<String, List<Result>> executeCallables(
            List<ParamOfGetResults> paramOfGetResultsList) throws MultiThreadingException {

        //Get the number of results and save it in the param
        getNumberOfResults(paramOfGetResultsList);
        Map<String, List<Result>> allDomainsResults = new HashMap<String, List<Result>>();
        //getResultsByAccessions(paramOfGetResultsList)
        
        for (ParamOfGetResults param:paramOfGetResultsList )  {
                    List<String> domainFields = param.getFields();
                    String domain  = param.getDomain();

                    List<Callable<ArrayOfArrayOfString>> callableList
                             = prepareCallableCollection(param);


                     //List<ArrayOfArrayOfString> rawResults =
                        //      submitAll(paramOfGetResultsList);
                     //List<ArrayOfArrayOfString> rawResults = submitAll(callableList);
                     List<ArrayOfArrayOfString> rawResults = executeCallables(callableList);
                    List<List<String>> transformedResults
                            = Transformer.transformToList(rawResults);
                    //Save the content to an object
                    ResultFactory resultFactory = new ResultFactory(
                            domain, domainFields);
                    List<Result> resultList = resultFactory.getResults(transformedResults);
                    allDomainsResults.put(domain, resultList);
        }
        return allDomainsResults;
    }
 * */

    /*
    public List<ArrayOfArrayOfString>  submitAll(
            List<ParamOfGetResults> paramOfGetResultsList) {
        ExecutorService pool = Executors.newCachedThreadPool();
        List<ArrayOfArrayOfString> resultList = new ArrayList<ArrayOfArrayOfString>();


        try {
            for (ParamOfGetResults param: paramOfGetResultsList) {
                ArrayOfString ebeyeFields = Transformer.transformToArrayOfString(
                        param.getFields());
                Callable<ArrayOfArrayOfString> callable =
                            new GetResultsCallable(
                            param.getDomain(),
                            param.getQuery(),
                            ebeyeFields,
                            param.getStart(),
                            param.getSize()
                            );
                Future<ArrayOfArrayOfString>future = pool.submit(callable);
                ArrayOfArrayOfString result = null;
                try {
                    result = (ArrayOfArrayOfString) future
                        .get(IEbeyeAdapter.EBEYE_ONLINE_REQUEST_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex.getCause());
            } catch (ExecutionException ex) {
                log.error(ex.getMessage(), ex.getCause());
            } catch (TimeoutException ex) {
                log.error(ex.getMessage(), ex.getCause());
            }
                resultList.add(result);
            }
        }
        finally {
            pool.shutdown();
        }
        return resultList;
    }
     *
     */

    public List<ArrayOfArrayOfString>  submitAll(
            List<Callable<ArrayOfArrayOfString>> callableList) throws MultiThreadingException{
        Iterator it = callableList.iterator();
        ExecutorService pool = null;
        List<ArrayOfArrayOfString> resultList = new ArrayList<ArrayOfArrayOfString>();
        try {
            pool = Executors.newCachedThreadPool();
        
            while (it.hasNext()) {
                Callable<ArrayOfArrayOfString> callable =
                        (Callable<ArrayOfArrayOfString>)it.next();
                Future<ArrayOfArrayOfString>future = pool.submit(callable);
                ArrayOfArrayOfString result = null;
                try {
                    result = (ArrayOfArrayOfString) future
                        .get(IEbeyeAdapter.EBEYE_ONLINE_REQUEST_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                throw  new MultiThreadingException(ex.getMessage(), ex);
            } catch (ExecutionException ex) {
                throw  new MultiThreadingException(ex.getMessage(), ex);
            } catch (TimeoutException ex) {
                throw  new MultiThreadingException(ex.getMessage(), ex);
            }
                resultList.add(result);
            }
        }
        finally {
            pool.shutdownNow();
        }
        return resultList;
    }
/*
    public List<ArrayOfArrayOfString> getResultFromThreads(
            List<Future<ArrayOfArrayOfString>> futureList )
            throws InterruptedException, ExecutionException, TimeoutException {
        List<ArrayOfArrayOfString> resultList = new ArrayList<ArrayOfArrayOfString>();
        Iterator it = futureList.iterator();
        while (it.hasNext()) {
            Future<ArrayOfArrayOfString> future
                    =(Future<ArrayOfArrayOfString>)it.next();
            ArrayOfArrayOfString resultLine = (ArrayOfArrayOfString)future.get(
                    IEbeyeAdapter.EBEYE_ONE_RECORD_TIMEOUT, TimeUnit.MILLISECONDS);
            resultList.add(resultLine);
        }
        return resultList;
    }
 * *
 */
/*
    public List<ArrayOfArrayOfString> getAllEbeyeResults(
            ParamOfGetAllResults paramOfGetAllResults){
        NumberOfResultsCaller caller =
                new EbeyeCallable.NumberOfResultsCaller(paramOfGetAllResults);

        int totalFound = caller.getNumberOfResults();
        List<ArrayOfArrayOfString> rawResults =
                this.getEbeyeResults(totalFound, paramOfGetAllResults);
        return rawResults;
    }

        public List<ArrayOfArrayOfString> getEbeyeResults (int totalFound
            , ParamOfGetAllResults paramOfGetAllResults){
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
                    ArrayOfArrayOfString rawResults = null;
                    try {
                        rawResults = (ArrayOfArrayOfString) future.get(
                                IEbeyeAdapter.EBEYE_ONLINE_REQUEST_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex.getCause());
            } catch (ExecutionException ex) {
                log.error(ex.getMessage(), ex.getCause());
            } catch (TimeoutException ex) {
                log.error(ex.getMessage(), ex.getCause());
            }
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
 * *
 */
        public List<Callable<ArrayOfArrayOfString>> prepareCallableCollection (
                List<ParamOfGetResults> paramList){
            List<Callable<ArrayOfArrayOfString>> callableList
                    = new ArrayList<Callable<ArrayOfArrayOfString>>();
            if (paramList.size() > 0) {
                Iterator it = paramList.iterator();
                while (it.hasNext()) {
                    ParamOfGetResults param = (ParamOfGetResults)it.next();
                    List<Callable<ArrayOfArrayOfString>> callables =
                                                    prepareCallableCollection(param);
                    if (callables.size()>0) {
                        callableList.addAll(callables);
                    }

                }
            }
            return callableList;
        }
        public List<Callable<ArrayOfArrayOfString>> prepareCallableCollection (
                ParamOfGetResults param){
            List<Callable<ArrayOfArrayOfString>> callableList
                    = new ArrayList<Callable<ArrayOfArrayOfString>>();
                    String domain = param.getDomain();
                    String query = param.getQuery();
                    List<String> fields = param.getFields();
                    int totalFound = param.getTotalFound();
                    int size = IEbeyeAdapter.EBEYE_RESULT_LIMIT;
                    //Work around to solve big result set issue
                    Pagination pagination = new Pagination(totalFound, IEbeyeAdapter.EBEYE_RESULT_LIMIT);
                    int nrOfQueries = pagination.calTotalPages();
                    int start = 0;

                    for (int i = 0; i < nrOfQueries; i++) {
                        if (i == nrOfQueries - 1 && (totalFound % IEbeyeAdapter.EBEYE_RESULT_LIMIT) > 0) {
                            size =pagination.getLastPageResults();
                        } 
                        ArrayOfString ebeyeFields = Transformer.transformToArrayOfString(fields);
                        Callable<ArrayOfArrayOfString> callable =
                                new GetResultsCallable(
                                domain, query, ebeyeFields, start,size);
                        callableList.add(callable);
                    }
            return callableList;
        }


/*
        public List<ParamOfGetResults> prepareGetResultsParams (
                ParamOfGetResults paramOfGetResults) {
        int totalFound = paramOfGetResults.getTotalFound();
        List<String> fields = paramOfGetResults.getFields();
        List<ParamOfGetResults> paramList = new ArrayList<ParamOfGetResults>();
        if (totalFound > 0) {
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
                ParamOfResultSize paramOfResultSize = new ParamOfResultSize(start, size);
                paramOfGetResults.getResultSizeList().add(paramOfResultSize);
                start = start+size;
                currentLoop++;
            }
        }
        return paramList;
    }
*/

    public List<ParamOfGetResults> prepareQueriesForAccessions(String domain
            , List<String> fields, List<String> uniprotXrefAccs) {
        List<ParamOfGetResults> params = new ArrayList<ParamOfGetResults>();
        if (uniprotXrefAccs.size() > 0) {
            List<String> uniprotXrefAccList = new ArrayList<String>();
            uniprotXrefAccList.addAll(uniprotXrefAccs);
            int endIndex = 0;
            int total = uniprotXrefAccs.size();
            //Work around to solve big result set issue
            if (total > IEbeyeAdapter.EP_UNIPROT_XREF_RESULT_LIMIT) {
                total = IEbeyeAdapter.EP_UNIPROT_XREF_RESULT_LIMIT;
            }
            Pagination pagination = new Pagination(total, IEbeyeAdapter.EBEYE_RESULT_LIMIT);
            int nrOfQueries = pagination.calTotalPages();
            int start = 0;
            for (int i = 0; i < nrOfQueries; i++) {
                if (i == nrOfQueries - 1 && (total % IEbeyeAdapter.EBEYE_RESULT_LIMIT) > 0) {
                    endIndex = endIndex + pagination.getLastPageResults();
                } else {
                    endIndex = endIndex + IEbeyeAdapter.EBEYE_RESULT_LIMIT;
                }
                String query = LuceneQueryBuilder.createUniprotQueryForEnzyme(uniprotXrefAccList.subList(start, endIndex));
                ParamOfGetResults paramOfGetAllResults =
                        new ParamOfGetResults(domain, query, fields);
                params.add(paramOfGetAllResults);
                start = endIndex;
            }

        }
        return params;
    }

    public List<Result> getResultsByAccessions(String domain
            , List<String> accessions) throws MultiThreadingException {
        List<String> fields = new ArrayList<String>();
        IEbeyeAdapter.FieldsOfGetResults.getFields();
        fields.add(IEbeyeAdapter.FieldsOfGetResults.id.name());
        fields.add(IEbeyeAdapter.FieldsOfGetResults.acc.name());
        List<ParamOfGetResults> params = prepareQueriesForAccessions(domain, fields, accessions);
        //Get and save
        getNumberOfResults(params);

        List<Callable<ArrayOfArrayOfString>> callables = prepareCallableCollection(params);

        List<ArrayOfArrayOfString> ebeyeResultList = executeCallables(callables);
        List<Result> resultList = transformRawResult(domain, fields, ebeyeResultList);
        return resultList;
    }

    public List<Result> transformRawResult(String domain, List<String> fields
            , List<ArrayOfArrayOfString> ebeyeResultList) {
            List<List<String>> transformedResults
                    = Transformer.transformToList(ebeyeResultList);
            //Save the content to an object
            ResultFactory resultFactory = new ResultFactory(
                    domain, fields);
            List<Result> resultList = resultFactory.getResults(transformedResults);
            return resultList;
    }

    public List<ArrayOfArrayOfString> executeCallables(
            List<Callable<ArrayOfArrayOfString>> callables) throws MultiThreadingException {
        List<ArrayOfArrayOfString> ebeyeResultList = new ArrayList<ArrayOfArrayOfString>();
           ExecutorService pool = Executors.newCachedThreadPool();
        int counter = 0;
        try {
            for (Callable<ArrayOfArrayOfString> callable:callables) {
                Future<ArrayOfArrayOfString> future  = pool.submit(callable);
                ArrayOfArrayOfString rawResults = null;
                try {
                    rawResults = (ArrayOfArrayOfString) future
                            .get(IEbeyeAdapter.EBEYE_ONLINE_REQUEST_TIMEOUT, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    throw  new MultiThreadingException(ex.getMessage(), ex);
                } catch (ExecutionException ex) {
                    throw  new MultiThreadingException(ex.getMessage(), ex);
                } catch (TimeoutException ex) {
                    throw  new MultiThreadingException(ex.getMessage(), ex);
                }
                ebeyeResultList.add(rawResults);
                counter++;
                if (counter >  IEbeyeAdapter.EP_THREADS_LIMIT)
                    break;
            }
        }
        finally {
            pool.shutdown();
        }

        return ebeyeResultList;


    }
    public List<ParamOfGetResults> getNumberOfResults(
            List<ParamOfGetResults> paramOfGetResults) throws MultiThreadingException {
        List<ParamOfGetResults> params = new ArrayList<ParamOfGetResults>();
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            for (ParamOfGetResults param:paramOfGetResults) {
                Callable<Integer> callable = new NumberOfResultsCaller(param);
                Future<Integer> future = pool.submit(callable);
                try {
                    int totalFound = future.get(IEbeyeAdapter.EBEYE_ONLINE_REQUEST_TIMEOUT, TimeUnit.SECONDS);
                    param.setTotalFound(totalFound);
                } catch (InterruptedException ex) {
                    throw  new MultiThreadingException(ex.getMessage(), ex);
                } catch (ExecutionException ex) {
                    throw  new MultiThreadingException(ex.getMessage(), ex);
                } catch (TimeoutException ex) {
                    throw  new MultiThreadingException(ex.getMessage(), ex);
                }

            }
        }
        finally {
            pool.shutdown();
        }

        return params;

    }
}
