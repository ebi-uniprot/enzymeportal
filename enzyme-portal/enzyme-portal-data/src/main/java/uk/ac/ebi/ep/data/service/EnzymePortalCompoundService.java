/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ep.data.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ep.data.domain.EnzymePortalCompound;
import uk.ac.ebi.ep.data.repositories.EnzymePortalCompoundRepository;
import uk.ac.ebi.ep.data.repositories.EnzymeSummaryRepository;
import uk.ac.ebi.ep.data.repositories.ReactionRepository;
import uk.ac.ebi.ep.data.repositories.UniprotEntryRepository;

/**
 *
 * @author joseph
 */
@Transactional
@Service
public class EnzymePortalCompoundService {

    @Autowired
    private EnzymePortalCompoundRepository repository;
    @Autowired
    private UniprotEntryRepository uniprotEntryRepository;
    
    @Autowired
    private EnzymeSummaryRepository enzymeSummaryRepository;
    
    @Autowired
    private ReactionRepository reactionRepository;

    @Transactional
    public EnzymePortalCompound addCompound(EnzymePortalCompound c) {
        EnzymePortalCompound compound = repository.saveAndFlush(c);
        return compound;
    }

    @Transactional
    public List<EnzymePortalCompound> addCompound(List<EnzymePortalCompound> compounds) {
        List<EnzymePortalCompound> portalCompounds = repository.save(compounds);
        return portalCompounds;
    }

    @Transactional
    public void parseAndLoadChEMBLCompounds(String file) throws Exception {
        ChemblSaxParser parser = new ChemblSaxParser(repository, uniprotEntryRepository);
        parser.parse(file);

    }

    @Transactional
    public void parseIntenzAndLoadCompoundsAndReactions(String file) throws Exception {
        IntenzSaxParser parser = new IntenzSaxParser(repository,enzymeSummaryRepository,reactionRepository);
        parser.parse(file);

    }
    
    
        @Transactional
    public void loadChEBICompounds()  {
          
        ChEBICompounds chebi = new ChEBICompounds(enzymeSummaryRepository, repository);
      
       chebi.computeAndLoadChEBICompounds();

    }

}
