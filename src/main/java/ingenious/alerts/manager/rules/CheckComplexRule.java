package ingenious.alerts.manager.rules;

import java.io.IOException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

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
			//Cancel the alert, we should save the alert iri in semantic integration
			//AlertGenerator("Cancel", heatstrokeIRI.getLocalName(),"event","description","areaDesc","Expected", "Moderate", fr.getLocalName());
		}

	}
}
