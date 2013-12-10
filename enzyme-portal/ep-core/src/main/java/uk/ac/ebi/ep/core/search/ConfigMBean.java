package uk.ac.ebi.ep.core.search;

import uk.ac.ebi.ep.core.search.IEnzymeFinder.UniprotImplementation;
import uk.ac.ebi.ep.core.search.IEnzymeFinder.UniprotSource;

public interface ConfigMBean {

	public abstract void setMaxPages(int maxPages);

	public abstract int getMaxPages();

	public abstract void setResultsPerPage(int resultsPerPage);

	public abstract int getResultsPerPage();

	/**
	 * Sets the resource used to retrieve UniProt entries.
	 * @param retrieverUniprotSource see {@link UniprotSource}
	 */
	public abstract void setRetrieverUniprotSource(String retrieverUniprotSource);

	public abstract String getRetrieverUniprotSource();

	/**
	 * Sets the resource used to search UniProt entries.
	 * @param finderUniprotSource see {@link UniprotSource}
	 */
	public abstract void setFinderUniprotSource(String finderUniprotSource);

	public abstract String getFinderUniprotSource();
	
	/**
	 * Sets the implementation used to search/retrieve UniProt entries.
	 * @param imp see {@link UniprotImplementation}
	 */
	public abstract void setUniprotImplementation(String imp);
	
	public abstract String getUniprotImplementation();
	
	/**
	 * Sets the size of the cache for search results.
	 * @param size
	 */
	public abstract void setSearchCacheSize(int size);
	
	public abstract int getSearchCacheSize();

	public abstract void setMmDatasource(String mmDatasource);

	public abstract String getMmDatasource();

    int getMaxMoleculesPerGroup();

    /**
     * Sets the maximum number of molecules to be retrieved per group
     * (inhibitors, activators, cofactors, drugs or bioactive). This is set for
     * performance reasons.
     * @param maxMoleculesPerGroup the maximum number of molecules to be
     *      retrieved.
     */
    void setMaxMoleculesPerGroup(int maxMoleculesPerGroup);

    public abstract String getStructureSearchUrl();

    /**
     * Sets the URL called for the chemical structure search.
     * @param structureSearchUrl the structure search URL, excluding the
     *      question mark and the query string.
     * @since 1.0.22
     */
    public abstract void setStructureSearchUrl(String structureSearchUrl);

    public abstract String getStructureSearchParams();

    /**
     * Sets the parameters sent to the chemical structure search URL.
     * @param structureSearchParams the parameters, as a query String (without
     *      question mark).
     * @since 1.0.22
     */
    public abstract void setStructureSearchParams(String structureSearchParams);

}
