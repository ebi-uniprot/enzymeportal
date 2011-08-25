package uk.ac.ebi.ep.adapter.literature;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import uk.ac.ebi.biobabel.citations.DataSource;
import uk.ac.ebi.cdb.webservice.Journal;
import uk.ac.ebi.cdb.webservice.JournalIssue;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.AgricolaId;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.Author;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.Book;
import uk.ac.ebi.kraken.interfaces.uniprot.Citation;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.ElectronicArticle;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.HasAgricolaId;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.HasAuthors;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.HasJournalName;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.HasPages;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.HasPubMedId;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.HasPublicationDate;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.HasTitle;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.HasVolume;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.JournalArticle;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.Page;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.Patent;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.PubMedId;
import uk.ac.ebi.kraken.interfaces.uniprot.citations.Volume;
import uk.ac.ebi.kraken.uuw.services.remoting.Attribute;
import uk.ac.ebi.kraken.uuw.services.remoting.AttributeIterator;
import uk.ac.ebi.kraken.uuw.services.remoting.Query;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryBuilder;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryService;

/**
 * Caller to retrieve citations from UniProt using the UniProtJAPI.
 * @author rafa
 *
 */
public class UniprotJapiLiteratureCaller
implements Callable<Collection<uk.ac.ebi.cdb.webservice.Citation>> {

	private String uniprotId;

	/**
	 * The constructor.
	 * @param uniprotId The UniProt ID to get citations from.
	 */
	public UniprotJapiLiteratureCaller(String uniprotId){
		this.uniprotId = uniprotId;
	}
	
	public Collection<uk.ac.ebi.cdb.webservice.Citation> call()
	throws Exception {
		// CiteXplore citations:
		HashSet<uk.ac.ebi.cdb.webservice.Citation> cxCits = null;
		UniProtQueryService uniProtQueryService =
				UniProtJAPI.factory.getUniProtQueryService();
		Query query = UniProtQueryBuilder.buildIDListQuery(
				Arrays.asList(new String[]{ uniprotId }));
		AttributeIterator<UniProtEntry> it =
				uniProtQueryService.getAttributes(query, "ognl:citations");
		if (it.hasNext()) cxCits =
				new HashSet<uk.ac.ebi.cdb.webservice.Citation>();
		for (Attribute att: it){
			// UniProt citations:
			@SuppressWarnings("unchecked")
			List<Citation> upCits = (List<Citation>) att.getValue();
			for (Citation upCit :upCits){
				uk.ac.ebi.cdb.webservice.Citation cxCit =
						new uk.ac.ebi.cdb.webservice.Citation();
				JournalIssue issue = new JournalIssue();
				Journal journal = new Journal();
				issue.setJournal(journal);
				cxCit.setJournalIssue(issue);
				loadCoreMetadata(upCit, cxCit);
				loadXrefs(upCit, cxCit);
				// Special bits:
				switch (upCit.getCitationType()){
				case BOOK:
					Book book = (Book) upCit;
					load(cxCit, book);
					break;
				case PATENT:
					Patent patent = (Patent) upCit;
					load(cxCit, patent);
					break;
				/* Ignoring other cases */
				}
				
				cxCits.add(cxCit);
			}
		}
		return cxCits;
	}

	/**
	 * Loads core metadata - title, abstract, authors, year - from a UniProt
	 * citation into a CiteXplore citation.
	 * @param upCit
	 * @param cxCit
	 */
	private void loadCoreMetadata(Citation upCit,
			uk.ac.ebi.cdb.webservice.Citation cxCit) {
		// Title:
		if (upCit instanceof HasTitle){
			cxCit.setTitle(((HasTitle) upCit).getTitle().getValue());
		}
		// Abstract:
		/* No info from old Citation objects (wait for citationNew package)
		List<CitationSummary> upAbstracts = upCit.getCitationSummary();
		if (upAbstracts != null && !upAbstracts.isEmpty()){
			cxCit.setAbstractText(upAbstracts.get(0).getValue());
		}
		*/
		// Authors:
		if (upCit instanceof HasAuthors){
			for (Author upAuthor: ((HasAuthors) upCit).getAuthors()){
				uk.ac.ebi.cdb.webservice.Author cxAuthor =
						new uk.ac.ebi.cdb.webservice.Author();
				cxAuthor.setFullName(upAuthor.getValue());
				cxCit.getAuthorCollection().add(cxAuthor);
			}
		}
		// Year:
		if (upCit instanceof HasPublicationDate){
			cxCit.getJournalIssue().setYearOfPublication(Short.valueOf(
					((HasPublicationDate) upCit).getPublicationDate().getValue()));
		}
		// Volume:
		if (upCit instanceof HasVolume){
			Volume volume = ((HasVolume) upCit).getVolume();
			if (volume != null){
				cxCit.getJournalIssue().setVolume(volume.getValue());
			}
		}
		// Journal name:
		if (upCit instanceof HasJournalName){
			cxCit.getJournalIssue().getJournal().setTitle(
					((HasJournalName) upCit).getJournalName().getValue());
		}
		// Pages:
		if (upCit instanceof HasPages){
			setPages(cxCit, ((HasPages) upCit).getFirstPage(),
					((HasPages) upCit).getLastPage());
		}
	}

	/**
	 * Loads metadata from a UniProt Book object
	 * into a CiteXplore Citation object.
	 * @param cxCit The CiteXplore Citation to load metadata into.
	 * @param book The UniProt object to get metadata from.
	 */
	private void load(uk.ac.ebi.cdb.webservice.Citation cxCit, Book book) {
		// Book name:
		cxCit.getJournalIssue().getJournal()
				.setTitle(book.getBookName().getValue());
	}

	/**
	 * Loads metadata from a UniProt ElectronicArticle object
	 * into a CiteXplore Citation object.
	 * @param cxCit The CiteXplore Citation to load metadata into.
	 * @param eArticle The UniProt object to get metadata from.
	 */
	private void load(uk.ac.ebi.cdb.webservice.Citation cxCit,
			ElectronicArticle eArticle) {
	}

	/**
	 * Loads metadata from a UniProt JournalArticle object
	 * into a CiteXplore Citation object.
	 * @param cxCit The CiteXplore Citation to load metadata into.
	 * @param jArticle The UniProt object to get metadata from.
	 */
	private void load(uk.ac.ebi.cdb.webservice.Citation cxCit,
			JournalArticle jArticle) {
	}

	/**
	 * Loads metadata from a UniProt Patent object
	 * into a CiteXplore Citation object.
	 * @param cxCit The CiteXplore Citation to load metadata into.
	 * @param patent The UniProt object to get metadata from.
	 */
	private void load(uk.ac.ebi.cdb.webservice.Citation cxCit, Patent patent) {
		cxCit.getPatentDetails().setPatentDetailId(
				Integer.valueOf(patent.getPatentNumber().getValue()));
	}

	private void setPages(uk.ac.ebi.cdb.webservice.Citation cxCit, Page firstPage,
			Page lastPage) {
		if (firstPage != null){
			StringBuilder pages = new StringBuilder(firstPage.getValue());
			if (lastPage != null){
				pages.append('-').append(lastPage.getValue());
			}
			cxCit.setPageInfo(pages.toString());
		}
	}

	/**
	 * Loads bibliography database xrefs from a UniProt citation into
	 * a CiteXplore citation. Currently, only PubMed and Agricola are
	 * supported.
	 * @param upCit
	 * @param cxCit
	 */
	private void loadXrefs(Citation upCit,
			uk.ac.ebi.cdb.webservice.Citation cxCit) {
		if (upCit instanceof HasPubMedId){
			PubMedId pubmedId = ((HasPubMedId) upCit).getPubMedId();
			if (pubmedId != null){
				cxCit.setDataSource(DataSource.MED.name());
				cxCit.setExternalId(pubmedId.getValue());
			}
		} else if (upCit instanceof HasAgricolaId){
			AgricolaId agricolaId = ((HasAgricolaId) upCit).getAgricolaId();
			if (agricolaId != null){
				cxCit.setDataSource(DataSource.AGR.name());
				cxCit.setExternalId(agricolaId.getValue());
			}
		}
	}

}
