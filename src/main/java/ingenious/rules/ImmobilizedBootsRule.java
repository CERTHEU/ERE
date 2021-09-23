package ingenious.rules;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

import ingenious.IngeniousConsts;
import ingenious.Input;
import ingenious.SemanticIntegration;
import ingenious.utils.MyUtils;
import ingenious.utils.QueryUtils;
import kb.KB;

public class ImmobilizedBootsRule {
	KB kb;
	
	public ImmobilizedBootsRule(KB kb) {
		this.kb = kb;
	}
	
	public void checkRule() throws IOException  {
		
		String sparq2 = MyUtils.fileToString("sparql/checkImmobilizedRule.sparql");
		String query = Input.PREFIXES + sparq2;
		TupleQueryResult result = QueryUtils.evaluateSelectQuery(kb.getConnection(), query);
		
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			
		 	Value frId = bindingSet.getBinding("frid").getValue();
		 	IRI fr = (IRI) bindingSet.getBinding("fr").getValue();
		 	IRI alert = (IRI) bindingSet.getBinding("alert").getValue();
		 	
		 	boolean isImmobilized = Boolean.parseBoolean(bindingSet.getBinding("immobilized").getValue().stringValue());
		 	boolean hasHeavyLoad = Boolean.parseBoolean(bindingSet.getBinding("heavyload").getValue().stringValue());
		 	
		 	
		 	String state = "";
		 	
		 	String alertMsg = null;
		 	if (isImmobilized) {
		 		state = state.concat("Immobilized ");
		 		System.err.println("FR is immobilized");
		 		alertMsg = "FR is immobilized";
			}
		 	if (hasHeavyLoad) {
		 		state = state.concat("Has heavy load ");
		 		System.err.println("FR has heavy load");
		 		alertMsg = "FR has heavy load";
		 	}
		 	
		 	
			if (bindingSet.getBinding("analysis_time") != null) {
				Value analysisTime = bindingSet.getBinding("analysis_time").getValue();
				System.out.println(state + " || FR: " + fr.getLocalName() + " || FR_ID: " + frId.toString() + " || Analysis Time: " + analysisTime.stringValue() + "\n Boots Alert ID: " + alert.stringValue());		
			 } else {
				 System.out.println(state + " || FR: " + fr.getLocalName() + " || FR_ID: " + frId.toString()  + "\n Boots Alert ID: " + alert.stringValue());
			 }
			IRI analysisIRI = kb.factory.createIRI(Input.NAMESPACE, "Analysis_Immobilized_" + fr.getLocalName());
			IRI immobilizedIRI = kb.factory.createIRI(Input.NAMESPACE, "Immobilized_" + fr.getLocalName());
	        
			String modification="PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
								+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
								+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
								+ "DELETE{\r\n"
								+ "            $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
								+ "        }\r\n"
								+ "        INSERT {\r\n"
								+ "            $analysis_iri a ing:Analysis.\r\n"
								+ "            $analysis_iri ing:hasTimeStamp $timestamp.\r\n"
								+ "            $analysis_iri ing:hasAnalysisType \"Expert Reasoning\".\r\n"
								+ "            $analysis_iri ing:detects $immobilized_iri. \r\n"
								+ "			   $analysis_iri ing:hasDataSource ?alert.\r\n"
								+ "            $analysis_iri ing:triggers $alert_iri. \r\n"
								+ "        \r\n";
								if (isImmobilized)
									modification += "            $immobilized_iri a ing:Immobilized.\r\n";
								if (hasHeavyLoad)
									modification += "            $immobilized_iri a ing:HeavyLoad.\r\n";
								modification += "            $immobilized_iri a ing:PhysiologicalCondition.\r\n"
								+ "            $fr_iri ing:hasPhysiologicalCondition $immobilized_iri. \r\n"
								+ "        }\r\n"
								+ "        WHERE{\r\n"
								+ "        OPTIONAL{\r\n"
								+ "                $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
								+ "            }\r\n"
								+ "        }";
			
				SemanticIntegration sem = new SemanticIntegration();
		        long timestamp;
				try {
					timestamp = sem.getCurrentDateTimeToEpochSeconds();
					String str = sem.getZonedDateTimeFromEpochSeconds(timestamp).toString();
			        System.out.println("Time of Analysis: " + str);
			        
			        Literal timeLimit = kb.factory.createLiteral(str, XSD.DATETIME);
			        String frName = fr.getLocalName(); 
			        String[] split = frName.split("_");
			        UUID uuid = UUID.randomUUID();
			        String uuidAsString = uuid.toString();
			        IRI alert_iri = kb.factory.createIRI(Input.NAMESPACE, uuidAsString);

			        if (bindingSet.getBinding("analysis_time") == null)
			        	sem.AlertGenerator("Alert", alert_iri.getLocalName(),alertMsg,"description","areaDesc","Immediate", "Extreme", split[1]);
			       
			        
			        
			        sem.executeUpdate(kb.getConnection(), modification, new SimpleBinding("analysis_iri", analysisIRI), 
			        					new SimpleBinding("fr_iri", fr),
			        					new SimpleBinding("immobilized_iri", immobilizedIRI),
			        					new SimpleBinding("timestamp", timeLimit),
			        					new SimpleBinding("alert_iri", kb.factory.createLiteral(uuidAsString)));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        

		        
		}
		
	}


}
