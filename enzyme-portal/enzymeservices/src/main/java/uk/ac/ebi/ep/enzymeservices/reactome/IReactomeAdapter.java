package uk.ac.ebi.ep.enzymeservices.reactome;

import java.util.List;
import uk.ac.ebi.ep.data.enzyme.model.EnzymeModel;
import uk.ac.ebi.ep.data.enzyme.model.Pathway;


/**
 *
 * @since   1.0
 * @version $LastChangedRevision$ <br/>
 *          $LastChangedDate$ <br/>
 *          $Author$
 * @author  $Author$
 */
public interface IReactomeAdapter {

	public void setConfig(ReactomeConfig config);
	
    public String getReactionDescription(String reactomeAccession)
	throws ReactomeServiceException;

    public String getPathwayDescription(String reactomeAccession)
    throws ReactomeServiceException;

    /** Gets descriptions for any reactions catalised by the enzyme
     * and adds them to the model.
     * @param enzymeModel an enzyme model.
     * @return the same model updated with reaction descriptions.
     * @throws ReactomeServiceException
     */
    public EnzymeModel addReactionDescriptions(EnzymeModel enzymeModel)
    throws ReactomeServiceException;

    public List<Pathway> getPathways(List<String> stableIds)
    throws ReactomeServiceException;
}
