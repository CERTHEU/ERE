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

public class ConcentrationCORule {
    KB kb;
    public ConcentrationCORule(KB kb) {this.kb= kb;}

    public boolean checkRule(int consentrationLvl, int periodperiodOfAverage) throws IOException{
        boolean check=false;
        String sparql = MyUtils.fileToString("sparql/chechCOConcentration.sparql");
        String query = Input.PREFIXES + sparql;
        System.err.println("ConcentrationCORule query: " + query);
        TupleQueryResult result = QueryUtils.evaluateSelectQuery(kb.getConnection(), query,
                new SimpleBinding[] {new SimpleBinding("co_limit", kb.factory.createLiteral(consentrationLvl)),
                        new SimpleBinding("co_duration", kb.factory.createLiteral(periodperiodOfAverage))});


        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            System.err.println("ConcentrationCO enabled");
            //IRI p1 = (IRI) bindingSet.getBinding("Activity").getValue();

            Value co_measurement = bindingSet.getBinding("co_val").getValue();
            Value frId = bindingSet.getBinding("frid").getValue();
            IRI fr = (IRI) bindingSet.getBinding("fr").getValue();
            Value deviceCOId = bindingSet.getBinding("device_co_id").getValue();
            IRI deviceCO = (IRI) bindingSet.getBinding("device_co").getValue();
            Value dateTimeCO = bindingSet.getBinding("co_time").getValue();

            if (bindingSet.getBinding("analysis_time") != null) {
                Value analysisTime = bindingSet.getBinding("analysis_time").getValue();
                System.err.println("CONCENTRATION_CO || Carbon Monoxide value: " + co_measurement.stringValue() +  " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_CO: " + deviceCO.stringValue()  + " || DEVICE_CO_ID: " +deviceCOId.stringValue()+ " || DATETIME CO: " + dateTimeCO.stringValue() +" || Analysis Time: " + analysisTime.stringValue());
            } else {
                System.err.println("CONCENTRATION_CO || Carbon Monoxide value: " + co_measurement.stringValue() + " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_CO: " + deviceCO.stringValue() + " || DEVICE_CO_ID: " + deviceCOId.stringValue() + " || DATETIME CO: " + dateTimeCO.stringValue());
                          }
                IRI analysisIRI = kb.factory.createIRI(Input.NAMESPACE, "Analysis_ConcentrationCO_" + fr.getLocalName());
                IRI concentrationCOIRI = kb.factory.createIRI(Input.NAMESPACE, "ConcentrationCO_" + fr.getLocalName());
                //IRI alert_iri = kb.factory.createIRI(Input.NAMESPACE, "Alert" + fr.getLocalName());
                String modification = ("PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
                        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
                        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
                        + "DELETE{\r\n"
                        + "            $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
                        + "        }\r\n"
                        + "        INSERT {\r\n"
                        + "            $analysis_iri a ing:Analysis.\r\n"
                        + "            $analysis_iri ing:hasTimeStamp $timestamp.\r\n"
                        + "            $analysis_iri ing:hasAnalysisType \"Expert Reasoning\".\r\n"
                        + "            $analysis_iri ing:detects $concentrationCO_iri. \r\n"
                        + "			   $analysis_iri ing:hasDataSource ?alert.\r\n"
                        + "            $analysis_iri ing:hasDataSource $device_iri_co. \r\n"
                        + "            $analysis_iri  ing:triggers $alert_iri. \r\n"
                        + "        \r\n"
                        + "            $concentrationCO_iri a ing:ConcentrationCO.\r\n"
                        + "            $concentrationCO_iri a ing:PhysiologicalCondition.\r\n"
                        + "            $fr_iri ing:hasPhysiologicalCondition $concentrationCO_iri. \r\n"
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
                    IRI alert_iri = kb.factory.createIRI(Input.NAMESPACE, uuidAsString);


                    //System.out.println(bindingSet.toString());
                    if (bindingSet.getBinding("analysis_time") == null) {

                        if (consentrationLvl == 50) {
                            sem.AlertGenerator("Alert", alert_iri.getLocalName(), "Gas Alert", "FR is  CO leakage area (50ppm)  30 min scope of action", "FR Health Status", "Immediate", "Low", split[1],"");

                        } else if (consentrationLvl == 100) {
                            System.out.println("consentrationLvl == 100");
                            sem.AlertGenerator("Alert", alert_iri.getLocalName(), "Gas Alert", "FR is in Medium CO leakage area (100) )20 min scope of action", "FR Health Status alert", "Immediate", "Medium", split[1],"");

                        } else if (consentrationLvl == 300)
                            sem.AlertGenerator("Alert", alert_iri.getLocalName(), "Gas Alert", "FR is in High CO leakage area (300) less than 3 min - wear protective equip", "FR Health Status alert", "Immediate", "Extreme", split[1],"");


                    }

                    sem.executeUpdate(kb.getConnection(), modification, new SimpleBinding("analysis_iri", analysisIRI),
                            new SimpleBinding("fr_iri", fr),
                            new SimpleBinding("device_iri_co", deviceCO),
                            new SimpleBinding("concentrationCO_iri", concentrationCOIRI),
                            new SimpleBinding("timestamp", timeLimit),
                            new SimpleBinding("alert_iri", kb.factory.createLiteral(uuidAsString)));
                    //new SimpleBinding("alert_iri", kb.factory.createLiteral("alert_" + fr.getLocalName())));


                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }



        }
        return  check;
    }

}
