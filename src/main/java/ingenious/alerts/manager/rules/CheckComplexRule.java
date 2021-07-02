package ingenious.alerts.manager.rules;

import java.io.IOException;

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
		
		String sparql = MyUtils.fileToString("sparql/cancelComplexRule.sparql");
		String query = Input.PREFIXES + sparql;
		
		
		TupleQueryResult result = QueryUtils.evaluateSelectQuery(kb.getConnection(), query,
				new SimpleBinding("heatstrokeLimit", kb.factory.createLiteral(SemanticIntegration.heatStrokeLimitBT)));

		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			System.err.println("HAS RESULT");
		}

	}
}
