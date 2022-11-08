package ingenious.rules;

import ingenious.Input;
import ingenious.SemanticIntegration;
import ingenious.utils.MyUtils;
import ingenious.utils.QueryUtils;
import kb.KB;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

import java.io.IOException;
import java.util.UUID;

public class ConcentrationAMRule {
    KB kb;
    public ConcentrationAMRule (KB kb) {this.kb= kb;}
    public boolean checkRule(int consentrationLvl, int periodperiodOfAverage) throws IOException {
        boolean check= false;
        String sparql = MyUtils.fileToString("sparql/checkConcentrationNH3Rule.sparql");
        String query = Input.PREFIXES + sparql;
        System.err.println("ConcentrationAMRule query: " + query);
        TupleQueryResult result = QueryUtils.evaluateSelectQuery(kb.getConnection(), query,
                new SimpleBinding[] {new SimpleBinding("am_limit", kb.factory.createLiteral(consentrationLvl)),
                        new SimpleBinding("am_duration", kb.factory.createLiteral(periodperiodOfAverage))});


        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            System.err.println("ConcentrationAM enabled");
            //IRI p1 = (IRI) bindingSet.getBinding("Activity").getValue();

            Value am_measurement = bindingSet.getBinding("am_val").getValue();
            Value frId = bindingSet.getBinding("frid").getValue();
            IRI fr = (IRI) bindingSet.getBinding("fr").getValue();
            Value deviceAMId = bindingSet.getBinding("device_am_id").getValue();
            IRI deviceAM = (IRI) bindingSet.getBinding("device_am").getValue();
            Value dateTimeAM = bindingSet.getBinding("am_time").getValue();

            if (bindingSet.getBinding("analysis_time") != null) {
                Value analysisTime = bindingSet.getBinding("analysis_time").getValue();
                System.err.println("CONCENTRATION_AM || Ammonia value: " + am_measurement.stringValue() +  " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_AM: " + deviceAM.stringValue()  + " || DEVICE_AM_ID: " +deviceAMId.stringValue()+ " || DATETIME AM: " + dateTimeAM.stringValue() +" || Analysis Time: " + analysisTime.stringValue());
            } else {
                System.err.println("CONCENTRATION_AM || Ammonia value: " + am_measurement.stringValue() +  " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_AM: " + deviceAM.stringValue()  + " || DEVICE_AM_ID: " +deviceAMId.stringValue()+ " || DATETIME AM: " + dateTimeAM.stringValue());
            }
            IRI analysisIRI = kb.factory.createIRI(Input.NAMESPACE, "Analysis_ConcentrationAM_" + fr.getLocalName());
            IRI ConcentrationAmIRI = kb.factory.createIRI(Input.NAMESPACE, "ConcentrationAM_" + fr.getLocalName());

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
                    + "            $analysis_iri ing:detects $concentrationAm_iri. \r\n"
                    + "			   $analysis_iri ing:hasDataSource ?alert.\r\n"
                    + "            $analysis_iri ing:hasDataSource $device_iri_am. \r\n"
                    + "            $analysis_iri  ing:triggers $alert_iri. \r\n"
                    + "        \r\n"
                    + "            $concentrationAm_iri a ing:ConcentrationAM.\r\n"
                    + "            $concentrationAm_iri a ing:PhysiologicalCondition.\r\n"
                    + "            $fr_iri ing:hasPhysiologicalCondition $concentrationAm_iri. \r\n"
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


                if (bindingSet.getBinding("analysis_time") == null) {
                    if (consentrationLvl == 35) {
                        sem.AlertGenerator("Alert", alert_iri.getLocalName(), "Gas Alert", "FR is  Ammonia leakage area (35 ppm) 15 min scope of action", "FR Health Status alert", "Immediate", "Medium", split[1],"");

                    } else if (consentrationLvl == 300) {
                        sem.AlertGenerator("Alert", alert_iri.getLocalName(), "Gas Alert", "FR is in High Ammonia leakage area (300) less than 3 min - wear protective equip", "FR Health Status alert", "Immediate", "Extreme", split[1],"");

                    }

                }

                sem.executeUpdate(kb.getConnection(), modification, new SimpleBinding("analysis_iri", analysisIRI),
                        new SimpleBinding("fr_iri", fr),
                        new SimpleBinding("device_iri_am", deviceAM),
                        new SimpleBinding("concentrationAm_iri", ConcentrationAmIRI),
                        new SimpleBinding("timestamp", timeLimit),
                        new SimpleBinding("alert_iri", kb.factory.createLiteral(uuidAsString)));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }



        }
        return check;
    }
}
