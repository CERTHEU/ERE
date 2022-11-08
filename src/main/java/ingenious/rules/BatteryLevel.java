package ingenious.rules;

import ingenious.Input;
import ingenious.SemanticIntegration;
import ingenious.utils.MyUtils;
import ingenious.utils.QueryUtils;
import kb.KB;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

import java.io.IOException;
import java.util.UUID;

public class BatteryLevel {
    KB kb;

    public BatteryLevel(KB kb){this.kb=kb;}

    public void checkLevel()  throws IOException {
        String sparql2 = MyUtils.fileToString("sparql/checkBatteryLevel.sparql");
        String query = Input.PREFIXES + sparql2;
        TupleQueryResult result = QueryUtils.evaluateSelectQuery(kb.getConnection(), query);

        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            System.err.println("Battery lvl Low");
            Value frId = bindingSet.getBinding("frid").getValue();
            IRI fr = (IRI) bindingSet.getBinding("fr").getValue();
            String type = bindingSet.getBinding("type").getValue().stringValue();
            String id = bindingSet.getBinding("id").getValue().stringValue();
            SemanticIntegration sem = new SemanticIntegration();
            long timestamp;
            try {
                timestamp = sem.getCurrentDateTimeToEpochSeconds();
                String str = sem.getZonedDateTimeFromEpochSeconds(timestamp).toString();
                String frName = fr.getLocalName();
                String[] split = frName.split("_");
                UUID uuid = UUID.randomUUID();
                String uuidAsString = uuid.toString();
                String desc = type+" with id " + id + " has LOW Battery";

                sem.AlertGenerator("Alert", uuidAsString,"Battery Alert",desc,"Device Status Alert","Immediate", "Low", split[1],"");
                sem.executeUpdate(kb.getConnection(), MyUtils.fileToString("sparql/deleteBatteryLevel.sparql"));
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
