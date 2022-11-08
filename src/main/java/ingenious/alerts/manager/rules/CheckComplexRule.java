package ingenious.alerts.manager.rules;

import java.io.IOException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.query.impl.SimpleBinding;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

import ingenious.Input;
import ingenious.SemanticIntegration;
import ingenious.utils.ConfigsLoader;
import ingenious.utils.MyUtils;
import ingenious.utils.QueryUtils;
import kb.KB;

public class CheckComplexRule {
	
	static ConfigsLoader configInstance;
	
	static {
		configInstance = ConfigsLoader.getInstance();
	}
	


	public void detectChanges() throws IOException {
		KB kb = new KB(configInstance.getGraphdb());
		
		String sparql = MyUtils.fileToString("sparql/cancelCompexRule.sparql");
		String query = Input.PREFIXES + sparql;
		//System.err.println("detectChanges query: " + query);
		//We should change the hr_limit to a constant. Remove the hardcoded 20
		TupleQueryResult result = QueryUtils.evaluateSelectQuery(kb.getConnection(), query,
				new SimpleBinding("hr_limit", kb.factory.createLiteral(20)));
		


		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			System.err.println("HAS RESULT");
			
			Value hr_measurement = bindingSet.getBinding("hr_val").getValue();
			Value frId = bindingSet.getBinding("frid").getValue();
			IRI fr = (IRI) bindingSet.getBinding("fr").getValue();
			IRI deviceHR = (IRI) bindingSet.getBinding("device_hr").getValue();
			Value dateTimeHR = bindingSet.getBinding("hr_time").getValue();
			Value alert_id = bindingSet.getBinding("alert_id").getValue();
			//Cancel the alert, we should save the alert iri in semantic integration
			new SemanticIntegration().AlertGenerator("Cancel", alert_id.stringValue(),"event","description","areaDesc","Expected", "Moderate", fr.getLocalName(),"");
			//Delete data from Cancel Complex Rule in graphdb
			String sparql2 = MyUtils.fileToString("sparql/deleteDataFromCancelCompexRule.sparql");
			String query2 = Input.PREFIXES + sparql2;
			System.out.println("Delete data from Complex rule... ");
			SemanticIntegration.executeUpdate(kb.getConnection(), query2, new SimpleBinding("hr_limit", kb.factory.createLiteral(20)));
	    	System.out.println("Done!");

		}
		

	}

	
}
