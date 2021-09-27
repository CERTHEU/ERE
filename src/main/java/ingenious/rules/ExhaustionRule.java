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

public class ExhaustionRule {
	KB kb;
	
	public ExhaustionRule(KB kb) {
		this.kb = kb;
	}
	
	public void checkRule() throws IOException  {
		
		String sparql = MyUtils.fileToString("sparql/checkExhaustionRule.sparql");
		String query = Input.PREFIXES + sparql;
		System.err.println("ExhaustionRule query: " + query);
		TupleQueryResult result = QueryUtils.evaluateSelectQuery(kb.getConnection(), query,
				new SimpleBinding[] {new SimpleBinding("youngerHRLimit", kb.factory.createLiteral(IngeniousConsts.exhaustionYoungerLimitHR)), 
									new SimpleBinding("youngerAgeLimit", kb.factory.createLiteral(IngeniousConsts.exhaustionYoungerLimitAge)),
									new SimpleBinding("olderHRLimit", kb.factory.createLiteral(IngeniousConsts.exhaustionOlderLimitHR)),
									new SimpleBinding("olderAgeLimit", kb.factory.createLiteral(IngeniousConsts.exhaustionOlderLimitAge)),
									new SimpleBinding("hr_duration", kb.factory.createLiteral(IngeniousConsts.durationOfTwoMinutes)),
									new SimpleBinding("bt_duration", kb.factory.createLiteral(IngeniousConsts.durationOfTwoMinutes)),
									new SimpleBinding("bt_limit", kb.factory.createLiteral(IngeniousConsts.exhaustionTempLimit))});
		
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			System.err.println("EXHAUSTION enabled");
			//IRI p1 = (IRI) bindingSet.getBinding("Activity").getValue();			
			
			Value bt_measurement = bindingSet.getBinding("bt_val").getValue();
			Value hr_measurement = bindingSet.getBinding("hr_val").getValue();
			Value frId = bindingSet.getBinding("frid").getValue();
			IRI fr = (IRI) bindingSet.getBinding("fr").getValue();
			Value deviceHRId = bindingSet.getBinding("device_hr_id").getValue();
			Value deviceBTId = bindingSet.getBinding("device_bt_id").getValue();
			IRI deviceBT = (IRI) bindingSet.getBinding("device_bt").getValue();
			IRI deviceHR = (IRI) bindingSet.getBinding("device_hr").getValue();
			Value dateTimeBT = bindingSet.getBinding("bt_time").getValue();
			Value dateTimeHR = bindingSet.getBinding("hr_time").getValue();
			
			if (bindingSet.getBinding("analysis_time") != null) {
				Value analysisTime = bindingSet.getBinding("analysis_time").getValue();
				System.err.println("EXHAUSTION || Body temperature value: " + bt_measurement.stringValue() + " || Heart rate value: " + hr_measurement.stringValue() + " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_BT: " + deviceBT.stringValue() + " || DEVICE_HR: " + deviceHR.stringValue() + " || DEVICE_BT_ID: " +deviceBTId.stringValue() + " || DEVICE_HR_ID: " +deviceHRId.stringValue() + " || DATETIME BT: " + dateTimeBT.stringValue() + " || DATETIME HR: " + dateTimeHR.stringValue() + " || Analysis Time: " + analysisTime.stringValue());	
			 } else {
				System.err.println("EXHAUSTION || Body temperature value: " + bt_measurement.stringValue() + " || Heart rate value: " + hr_measurement.stringValue() + " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_BT: " + deviceBT.stringValue() + " || DEVICE_HR: " + deviceHR.stringValue() + " || DEVICE_BT_ID: " +deviceBTId.stringValue() + " || DEVICE_HR_ID: " +deviceHRId.stringValue() + " || DATETIME BT: " + dateTimeBT.stringValue() + " || DATETIME HR: " + dateTimeHR.stringValue());	
			 }
			IRI analysisIRI = kb.factory.createIRI(Input.NAMESPACE, "Analysis_Exhaustion_" + fr.getLocalName());
			IRI exhaustionIRI = kb.factory.createIRI(Input.NAMESPACE, "Exhaustion_" + fr.getLocalName());
	        
			String modification=("PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
								+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
								+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
								+ "DELETE{\r\n"
								+ "            $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
								+ "        }\r\n"
								+ "        INSERT {\r\n"
								+ "            $analysis_iri a ing:Analysis.\r\n"
								+ "            $analysis_iri ing:hasTimeStamp $timestamp.\r\n"
								+ "            $analysis_iri ing:hasAnalysisType \"Expert Reasoning\".\r\n"
								+ "            $analysis_iri ing:detects $exhaustion_iri. \r\n"
								+ "			   $analysis_iri ing:hasDataSource ?alert.\r\n"
							    + "            $analysis_iri ing:hasDataSource $device_iri_hr. \r\n"
				 			    + "            $analysis_iri ing:hasDataSource $device_iri_bt. \r\n"
								+ "            $analysis_iri  ing:triggers $alert_iri. \r\n"
								+ "        \r\n"
								+ "            $exhaustion_iri a ing:Exhaustion.\r\n"
								+ "            $exhaustion_iri a ing:PhysiologicalCondition.\r\n"
								+ "            $fr_iri ing:hasPhysiologicalCondition $exhaustion_iri. \r\n"
								+ "        }\r\n"
								+ "        WHERE{\r\n"
								+ "        OPTIONAL{\r\n"
								+ "                $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
								+ "            }\r\n"
								+ "        }"
					);
			
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
			        //float bodytemp = Float.parseFloat(hr_measurement.stringValue());
			        IRI alert_iri = kb.factory.createIRI(Input.NAMESPACE, uuidAsString);

			        if (bindingSet.getBinding("analysis_time") == null)
			        	sem.AlertGenerator("Alert", alert_iri.getLocalName(),"FR suffering from exhaustion","description","areaDesc","Immediate", "Extreme", split[1]);
			       
			        
			        
			        sem.executeUpdate(kb.getConnection(), modification, new SimpleBinding("analysis_iri", analysisIRI), 
			        					new SimpleBinding("fr_iri", fr),
			        					new SimpleBinding("device_iri_hr", deviceHR),
			        					new SimpleBinding("device_iri_bt", deviceBT),
			        					new SimpleBinding("exhaustion_iri", exhaustionIRI),
			        					new SimpleBinding("timestamp", timeLimit),
			        					new SimpleBinding("alert_iri", kb.factory.createLiteral(uuidAsString)));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        

		        
		}
		
	}


}
