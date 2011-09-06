package uk.ac.ebi.ep.uniprot.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import uk.ac.ebi.ep.enzyme.model.ChemicalEntity;
import uk.ac.ebi.ep.enzyme.model.Disease;
import uk.ac.ebi.ep.enzyme.model.Enzyme;
import uk.ac.ebi.ep.enzyme.model.EnzymeModel;
import uk.ac.ebi.ep.enzyme.model.Molecule;
import uk.ac.ebi.ep.enzyme.model.Pathway;
import uk.ac.ebi.ep.enzyme.model.ReactionPathway;
import uk.ac.ebi.ep.enzyme.model.Sequence;
import uk.ac.ebi.ep.search.model.EnzymeAccession;
import uk.ac.ebi.ep.search.model.EnzymeSummary;
import uk.ac.ebi.ep.search.model.Species;
import uk.ac.ebi.kraken.interfaces.uniprot.DatabaseCrossReference;
import uk.ac.ebi.kraken.interfaces.uniprot.DatabaseType;
import uk.ac.ebi.kraken.interfaces.uniprot.Organism;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.interfaces.uniprot.comments.Comment;
import uk.ac.ebi.kraken.interfaces.uniprot.comments.CommentType;
import uk.ac.ebi.kraken.interfaces.uniprot.dbx.drugbank.DrugBank;
import uk.ac.ebi.kraken.interfaces.uniprot.dbx.reactome.Reactome;
import uk.ac.ebi.kraken.interfaces.uniprot.description.Name;
import uk.ac.ebi.kraken.model.uniprot.dbx.pdb.PdbImpl;
import uk.ac.ebi.kraken.uuw.services.remoting.Attribute;
import uk.ac.ebi.kraken.uuw.services.remoting.AttributeIterator;
import uk.ac.ebi.kraken.uuw.services.remoting.EntryRetrievalService;
import uk.ac.ebi.kraken.uuw.services.remoting.Query;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryBuilder;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryService;

/**
 *
 * @since   1.0
 * @version $LastChangedRevision$ <br/>
 *          $LastChangedDate$ <br/>
 *          $Author$
 * @author  $Author$
 */
public class UniprotCallable {

//********************************* VARIABLES ********************************//
   protected  static EntryRetrievalService entryRetrievalService = UniProtJAPI
           .factory.getEntryRetrievalService();
   protected  static UniProtQueryService queryService = UniProtJAPI
           .factory.getUniProtQueryService();

//******************************** CONSTRUCTORS ******************************//


//****************************** GETTER & SETTER *****************************//


//********************************** METHODS *********************************//

//******************************** INNER CLASS *******************************//

    public static class GetEntryCaller implements Callable<EnzymeSummary> {
        protected String accession;
        //protected boolean entryPage;
        public GetEntryCaller() {
        }

        public GetEntryCaller(String accession) {
            this.accession = accession;
        }



        public EnzymeSummary call() throws Exception {
            return getEnzymeCommonProperties();
        }

        public String getAccession() {
            return accession;
        }

        public void setAccession(String accession) {
            this.accession = accession;
        }

        /*
        public EnzymeSummary getEnzymeSummaryByAccession()  {
            //Retrieve UniProt entry by its accession number
            //UniProtEntry entry = (UniProtEntry) entryRetrievalService
               //     .getUniProtEntry(accession);
            return setEnzymeProperties();
        }
        */

        public List<Comment> getComments(CommentType commentType) {
            //StringBuffer sb = new StringBuffer("ognl:getComments(@uk.ac.ebi.kraken.interfaces.uniprot.comments.CommentType@");
            //sb.append(commentType.name());
            //sb.append(")");
            Object[] messageArguments = {commentType.name()};
            String ognlExp = Transformer.getMessageTemplate("commentTpl", messageArguments);
            List<Comment> comments = (List<Comment>) entryRetrievalService
                    .getUniProtAttribute(accession
                    , ognlExp);
            return comments;
        }


        public List<DatabaseCrossReference> getExternalDatabase(DatabaseType databaseType) {
            Object[] messageArguments = {databaseType.name()};
            String ognlExp = Transformer.getMessageTemplate("databaseTypeTpl", messageArguments);

            List<DatabaseCrossReference> refList = (List<DatabaseCrossReference>) entryRetrievalService
                    .getUniProtAttribute(accession, ognlExp);
            return refList;
        }
        
        public List<Molecule> getDrugBankMoleculeNames() {
            List<DatabaseCrossReference> refList = (List<DatabaseCrossReference>) getExternalDatabase(DatabaseType.DRUGBANK);
            List<Molecule> molecules = new ArrayList<Molecule>();
            if (refList.size() > 0) {
                for (DatabaseCrossReference ref : refList) {
                    DrugBank drugBank = (DrugBank) ref;
                    String dbAccession = drugBank.getDrugBankAccessionNumber().getValue();
                    if (dbAccession != null) {
                        String name = drugBank.getDrugBankDescription().getValue().trim();
                        Molecule molecule = new Molecule();
                        molecule.setName(name);
                        molecules.add(molecule);
                    }
                }
            }
            return molecules;
        }

        public String getFunction() {
            List<Comment> comments = getComments(CommentType.FUNCTION);
            return Transformer.getCommentText(comments);
        }

        public List<Disease> getDisease() {
            List<Comment> comments = getComments(CommentType.DISEASE);
            return Transformer.getDiseases(comments);
        }

        //TODO
        /*
        public ChemicalEntity getSmallMolecules() {
            ChemicalEntity chemicalEntity = new ChemicalEntity();
            List<Comment> comments = getComments(CommentType.ENZYME_REGULATION);
            String commentText = Transformer.getMoleculeComments(comments);
            List<Molecule> inhibitors = Transformer.parseTextForInhibitors(commentText);
            chemicalEntity.setInhibitors(inhibitors);

            List<Molecule> activators = Transformer.parseTextForActivators(commentText);
            chemicalEntity.setActivators(activators);


            List<Molecule> drugs = getDrugBankMoleculeNames();
            chemicalEntity.setDrugs(drugs);
            //Should this return a
            return chemicalEntity;
        }
         *
         */

    /**
     * Query Uniprot for the data that is shown in the Enzyme tab.
     * @return
     */
    public EnzymeSummary getEnzymeTabData() {
        //niProtEntry entry = (UniProtEntry) entryRetrievalService
           //     .getUniProtEntry(accession);
        //EnzymeModel enzymeModel =  (EnzymeModel)setEzymeResult(accession);
        //EnzymeModel enzymeModel =  (EnzymeModel)setEnzymeCommonProperties(accession);
        //EnzymeModel enzymeModel =  (EnzymeModel)setEnzymeProperties();
        EnzymeModel enzymeModel = (EnzymeModel) getEnzymeCommonProperties();
        List<Comment> functionCommentList = getComments(CommentType.FUNCTION);
        String function = Transformer.getCommentText(functionCommentList);
        enzymeModel.setFunction(function);
        List<Comment> diseaseCommentList = getComments(CommentType.DISEASE);
        List<Disease> diseases = Transformer.getDiseases(diseaseCommentList);
        enzymeModel.setDisease(diseases);

        List<Name> names = (List<Name>) getAttribute("proteinAlterNamestpl");
        enzymeModel.getSynonym().addAll(Transformer.getAltNames(names));
        enzymeModel.setPdbeaccession(this.getPdbeAccessions());

        int seqLength = (Integer)getAttribute("sequenceLengthTpl");
        int weight = (Integer)getAttribute("moleculeWeightTpl");
        String seqUrl = IUniprotAdapter.SEQUENCE_URL_BASE
                +this.accession
                +IUniprotAdapter.SEQUENCE_URL_SUFFIX;

        Sequence seq = new Sequence();
        seq.setSequence(String.valueOf(seqLength));
        seq.setWeight(String.valueOf(weight));
        seq.setSequenceurl(seqUrl);
        Enzyme enzyme = new Enzyme();
        enzyme.setSequence(seq);
        enzymeModel.setEnzyme(enzyme);
        return enzymeModel;
    }

    public EnzymeSummary getReactionPathwayByAccession() {
        EnzymeModel enzymeModel = (EnzymeModel)getEnzymeCommonProperties();
        ReactionPathway reactionpathway = this.getReactomePathways();
        enzymeModel.getReactionpathway().add(reactionpathway);
        return enzymeModel;
    }

    public EnzymeSummary getProteinStructureByAccession() {
        EnzymeModel enzymeModel = (EnzymeModel)getEnzymeCommonProperties();
        enzymeModel.getPdbeaccession().addAll(getPdbeAccessions());
        return enzymeModel;
    }
    public EnzymeSummary getSmallMoleculesByAccession() {
        EnzymeModel enzymeModel = (EnzymeModel)getEnzymeCommonProperties();
        //ReactionPathway reactionpathway = this.setReactomePathways(entry);
        //List<Molecule>  molecules = this.getDrugBankAccessions(entry);
        //chemicalEntity.setDrugs(molecules);
        ChemicalEntity chemicalEntity = new ChemicalEntity();
        List<Molecule>  molecules = getDrugBankMoleculeNames();
        chemicalEntity.setDrugs(molecules);
        
        //Inhibitors, activators
        List<Comment> comments = getComments(CommentType.ENZYME_REGULATION);
        String commentText = Transformer.getMoleculeComments(comments);
        String[] sentences = commentText.split("\\.");
        for (String sentence: sentences) {
            if (sentence.contains("Activated by") || sentence.contains("activated by")) {
                List<Molecule>  activators = Transformer.parseTextForActivators(sentence.trim());
                chemicalEntity.setActivators(activators);
            }
            if (sentence.contains("Inhibited by") || sentence.contains("inhibited by")) {
                List<Molecule>  inhibitors = Transformer.parseTextForInhibitors(sentence.trim());
                chemicalEntity.setInhibitors(inhibitors);
            }
        }

        enzymeModel.setMolecule(chemicalEntity);
        return enzymeModel;
    }

    public List<String> getPdbeAccessions() {
        List<DatabaseCrossReference> refList = (List<DatabaseCrossReference>) getExternalDatabase(DatabaseType.PDB);
        List<String> results = new ArrayList<String>();
        if (refList.size() > 0) {
            for (DatabaseCrossReference ref:refList) {
                PdbImpl pdbImpl = (PdbImpl)ref;
                String dbAccession = pdbImpl.getDbAccession();
                //PdbResolution resolution = ref.getPdbResolution();
                //String resolutionValue = resolution.getValue();
                if (dbAccession != null) {
                    //The url of pdbe images use lowercase
                    results.add(dbAccession.toLowerCase());
                }
            }
        }
        return results;
    }

   
    public ReactionPathway getReactomePathways() {
        List<DatabaseCrossReference> refList = (
                List<DatabaseCrossReference>) getExternalDatabase(DatabaseType.REACTOME);
        //List<String> results = new ArrayList<String>();
        ReactionPathway reactionpathway = new ReactionPathway();
        if (refList.size() > 0) {
            for (DatabaseCrossReference ref:refList) {
                Reactome reactome = (Reactome)ref;
                String dbAccession = reactome.getReactomeAccessionNumber().getValue();
                if (dbAccession != null) {
                    reactome.getReactomeDescription();
                    Pathway pathway = new Pathway();
                    pathway.setId(dbAccession);
                    pathway.setName(reactome.getReactomeDescription().getValue());
                    reactionpathway.getPathways().add(pathway);
                }
            }
        }
        return reactionpathway;
    }
/*
    public List<Molecule> getDrugBankAccessions() {
        List<DatabaseCrossReference> refList = (
                List<DatabaseCrossReference>) getExternalDatabase(DatabaseType.REACTOME);
        List<Molecule> molecules = new ArrayList<Molecule>();
        if (refList.size() > 0) {
            for (DatabaseCrossReference ref:refList) {
                DrugBank drugBank = (DrugBank)ref;
                String dbAccession = drugBank.getDrugBankAccessionNumber().getValue();
                if (dbAccession != null) {
                    String name = drugBank.getDrugBankDescription().getValue();
                    Molecule molecule = new Molecule();
                    molecule.getXrefs().add(drugBank.getDrugBankAccessionNumber().getValue());
                    molecules.add(molecule);
                }
            }
        }
        return molecules;
    }
*/
        public Object getAttribute(String ognlTplName) {
            String ognlExp = Transformer.getMessageTemplate(ognlTplName);
            Object attValue = entryRetrievalService.getUniProtAttribute(accession, ognlExp);
            return attValue;
        }

        /**
         * Query Uniprot for enzyme common attributes to be shown in the seacrch
         * results and the species header of the entry tabs. The attributes that
         * are set here include enzyme names, ec, species.
         * 
         * @return
         */
        public EnzymeSummary getEnzymeCommonProperties() {
            EnzymeModel enzymeSummary = new EnzymeModel();
            enzymeSummary.getUniprotaccessions().add(accession);
            String id = (String) getAttribute("idTpl");
            enzymeSummary.setUniprotid(id);
            List<Name> names = (List<Name>)getAttribute("proteinSubNameTpl");
            Name recName = (Name)getAttribute("proteinRecNameTpl");
            //ProteinDescription desc = (ProteinDescription) getAttribute("protienDescTpl");
            //ProteinDescription desc = (ProteinDescription) getAttribute("protienDescTpl");
            String name = null;
            if (names.size() > 0) {
                //List<Name> names = desc.getSubNames();
                name = Transformer.getFullName(names.get(0));
            } else {
                //Name recName = desc.getRecommendedName();
                name = Transformer.getFullName(recName);
            }

            enzymeSummary.setName(name);

            List<String> ecs = (List<String>)getAttribute("ecTpl");

            enzymeSummary.getEc().addAll(ecs);
            Organism organism = (Organism)getAttribute("speciesTpl");
            String speciesCommonName = organism.getCommonName().getValue();
            String scientificName = organism.getScientificName().getValue();
            Species species = new Species();
            species.setCommonname(speciesCommonName);
            species.setScientificname(scientificName);
            enzymeSummary.setSpecies(species);

            return enzymeSummary;
        }
    }
    /*
    public EnzymeSummary setEnzymeCommonProperties(UniProtEntry entry) {
        EnzymeModel enzymeSummary = new EnzymeModel();
        enzymeSummary.getUniprotaccessions().add(entry.getPrimaryUniProtAccession().getValue());
        enzymeSummary.setUniprotid(entry.getUniProtId().getValue());
        if (entry != null) {
            ProteinDescription desc = entry.getProteinDescription();
            String name = null;
            if (desc.hasSubNames()){
                 List<Name> names  = desc.getSubNames();
                name = Transformer.getFullName(names.get(0));
            }
            else {
                Name recName = desc.getRecommendedName();
                name = Transformer.getFullName(recName);
            }

            enzymeSummary.setName(name);

            enzymeSummary.getEc().addAll(desc.getEcNumbers());
            String speciesCommonName = entry.getOrganism().getCommonName().getValue();
            String scientificName = entry.getOrganism().getScientificName().getValue();
            Species species = new Species();
            species.setCommonname(speciesCommonName);
            species.setScientificname(scientificName);
            enzymeSummary.setSpecies(species);
        }
        return enzymeSummary;
    }
    */
        /*
    public EnzymeSummary setEnzymeProperties() {
        //EnzymeSummary enzymeSummary = new EnzymeSummary();
        EnzymeModel enzymeModel = (EnzymeModel) setEnzymeCommonProperties(accession);
        List<Comment> functionCommentList = getComments(CommentType.FUNCTION);
        String function = Transformer.getCommentText(functionCommentList);
            enzymeModel.setFunction(function);
            List<Comment> diseaseCommentList = getComments(CommentType.DISEASE);
            List<Disease> diseases = Transformer.getDiseases(diseaseCommentList);
            enzymeModel.setDisease(diseases);

            List<Name> names = (List<Name>) getAttribute("proteinAlterNamestpl");
            enzymeModel.getSynonym().addAll(Transformer.getAltNames(names));
            enzymeModel.setPdbeaccession(this.getPdbeAccessions());
        return enzymeModel;
    }

  }
*/




    //******************************** INNER CLASS *******************************//
    /**
     * A caller that implements the {@link Callable} interface that can be called
     * to perform concurrent queries to Uniprot API to get Uniprot entries grouped by species.
     */
    public static class QueryEntryByIdCaller implements Callable<EnzymeSummary> {    
        //protected UniProtEntry mainEntry;
        protected String defaultSpecies;
        protected Query uniprotQuery;

        public QueryEntryByIdCaller(String query, String defaultSpecies) {
            uniprotQuery = UniProtQueryBuilder.buildQuery(query);
            this.defaultSpecies = defaultSpecies;
        }


        public EnzymeSummary call() throws Exception {
            return queryforEnzymeSummaryEntry();
        }

        public EnzymeSummary queryforEnzymeSummaryEntry()  {
            List<EnzymeAccession> speciesList = getSpecies();
            EnzymeSummary enzymeSummary = null;
            if (speciesList.size() > 0) {
                EnzymeAccession topSpecies = speciesList.get(0);
                GetEntryCaller caller = new GetEntryCaller(
                topSpecies.getUniprotaccessions().get(0));
                //Retieve the main entry
                enzymeSummary = caller.getEnzymeCommonProperties();

                //Add related species
                if (speciesList.size() > 1) {
                    //speciesList.remove(topSpecies);
                    enzymeSummary.getRelatedspecies().addAll(speciesList);
                }
                
            }
            return enzymeSummary;
        }

        public EnzymeSummary queryforEnzymeSequenceEntry()  {
            List<EnzymeAccession> speciesList = getSpecies();
            EnzymeSummary enzymeSummary = null;
            if (speciesList.size() > 0) {
                EnzymeAccession topSpecies = speciesList.get(0);
                GetEntryCaller caller = new GetEntryCaller(
                topSpecies.getUniprotaccessions().get(0));
                //Retieve the main entry
                enzymeSummary = caller.getEnzymeTabData();
                //Add related species
                if (speciesList.size() > 1) {
                    //speciesList.remove(topSpecies);
                    enzymeSummary.getRelatedspecies().addAll(speciesList);
                }

            }
            return enzymeSummary;
        }

        public EnzymeSummary queryforEnzymePathwayEntry()  {
            List<EnzymeAccession> speciesList = getSpecies();
            EnzymeSummary enzymeSummary = null;
            if (speciesList.size() > 0) {
                EnzymeAccession topSpecies = speciesList.get(0);
                GetEntryCaller caller = new GetEntryCaller(
                topSpecies.getUniprotaccessions().get(0));
                //Retieve the main entry
                enzymeSummary = caller.getReactionPathwayByAccession();
                //Add related species
                if (speciesList.size() > 1) {
                    //The top species is not set in getEnzymePathwaysByAccession method
                    //speciesList.remove(topSpecies);
                    enzymeSummary.getRelatedspecies().addAll(speciesList);
                }

            }
            return enzymeSummary;
        }

        public List<EnzymeAccession> getSpecies() {
             AttributeIterator<UniProtEntry> attributes  = queryService
                     .getAttributes(uniprotQuery, "ognl:organism");
             List<EnzymeAccession> accSpeciesList = new ArrayList<EnzymeAccession>();
             for (Attribute att : attributes) {
                Organism organism = (Organism)att.getValue();
                String commonName = organism.getCommonName().getValue();
                String scientificName = organism.getScientificName().getValue();
                EnzymeAccession enzymeAccession = new EnzymeAccession();
                enzymeAccession.getUniprotaccessions().add(att.getAccession());
                Species species = new Species();
                species.setCommonname(commonName);
                species.setScientificname(scientificName);
                enzymeAccession.setSpecies(species);
                 //if (commonName.equalsIgnoreCase(this.defaultSpecies)) {
                if (scientificName.equalsIgnoreCase(this.defaultSpecies)) {
                     accSpeciesList.add(0,enzymeAccession);
                 } else {
                    accSpeciesList.add(enzymeAccession);
                 }
             }
             return accSpeciesList;

        }
  }
/*
        public List<UniProtEntry> separateEntries(EntryIterator<UniProtEntry> entries) {
            List<UniProtEntry> secEntries = new ArrayList<UniProtEntry>();
            boolean hasMainEntry = false;
            for (UniProtEntry uniProtEntry: entries) {
                String species = uniProtEntry.getOrganism().getCommonName().getValue();
                if (species.equalsIgnoreCase(this.defaultSpecies)) {
                    this.mainEntry = uniProtEntry;
                    hasMainEntry = true;
                } else {
                    secEntries.add(uniProtEntry);
                }

                String accesion = uniProtEntry.getPrimaryUniProtAccession().getValue();
            }
            if (!hasMainEntry) {
                this.mainEntry = secEntries.get(0);
                //secEntries.remove(this.mainEntry);
            }
            return secEntries;
        }
*/


//******************************** INNER CLASS *******************************//

    public static class GetSpeciesCaller implements Callable<Map<String,String>> {
        protected String query;

        public GetSpeciesCaller(String query) {
            this.query = query;
        }


        public Map<String,String> call() throws Exception {
            return getSpecies();
        }
        public Map<String,String> getSpecies() {
            Query uniprotQuery = UniProtQueryBuilder.buildQuery(query);
            return getSpecies(uniprotQuery);
        }
        public Map<String,String> getSpecies(Query uniprotQuery) {
             AttributeIterator<UniProtEntry> attributes  = queryService
                     .getAttributes(uniprotQuery, "ognl:organism");
             Map<String,String> speciesMap = new HashMap<String, String>();
             for (Attribute att : attributes) {
                Organism organism = (Organism)att.getValue();
                String commonName = organism.getCommonName().getValue();
                String scientificName = organism.getScientificName().getValue();
                speciesMap.put(scientificName, commonName);

             }
             return speciesMap;
        }

    }    
}
