package ingenious;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class Input 
{
	public static final String NAMESPACE = "http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#";

	
	//Resource Properties
	public static final IRI RESOURCEID = getIRI("hasResourceId");
	
	
	//FR Properties
	public static final IRI FRID = getIRI("hasFrId");
	public static final IRI STATUS = getIRI("hasStatus");
	public static final IRI EQUIPMENT = getIRI("wearsEquipment");
	public static final IRI VITALSIGN = getIRI("hasVitalSign");
	public static final IRI GENDER = getIRI("hasGender");
	public static final IRI NAME = getIRI("isNamed");
	public static final IRI AGE = getIRI("hasAge");
	
	public static final IRI TEAM = getIRI("belongsToTeam");
	public static final IRI ROLE = getIRI("roleInTeam");
	
	public static final IRI WEIGHT = getIRI("hasWeight");
	public static final IRI HEIGHT = getIRI("hasHeight");
	public static final IRI K9 = getIRI("handlesK9");
	
	public static final IRI TARGET = getIRI("isTargetOfAlert");
	
	//Vehicle Properties
	public static final IRI VEHICLEID = getIRI("hasVehicleId");
	
	//K9 Properties
	public static final IRI K9ID = getIRI("hasK9Id");
	
	//Equipment Properties
	public static final IRI ATTACHMENT = getIRI("isAttachedTo");
	public static final IRI MAKESMEASUREMENT = getIRI("makesMeasurement");
	
	//General Properties
	public static final IRI ORGANIZATION = getIRI("belongsToOrganization");
	public static final IRI TYPE = getIRI("hasType");
	public static final IRI TIMESTAMP = getIRI("hasTimestamp");
	public static final IRI EQUIPMENTID = getIRI("hasEquipmentId");
	
	
	//Header Properties
	public static final IRI VERSION = getIRI("hasVersion");
	public static final IRI SENT = getIRI("sentOn");
	public static final IRI SOURCE = getIRI("sensorSource");
	public static final IRI HEADERCRC = getIRI("headerCRC");
	public static final IRI PAYLOADCRC = getIRI("payloadCRC");
	
	//Measurement Properties
	public static final IRI OBSERVATIONID = getIRI("hasObservationId");
	public static final IRI SENSORID = getIRI("isMeasuredBy");
	public static final IRI OBSERVEDPROPERTY = getIRI("observedProperty");
	public static final IRI RESULT = getIRI("hasResult");
	public static final IRI LOCATION = getIRI("hasLocation");
	public static final IRI AVERAGE = getIRI("hasRollingAverage");
	
	//Rolling Average Properties
	public static final IRI VALUE = getIRI("hasValue");
	public static final IRI WINDOWSTART = getIRI("hasWindowStart");
	public static final IRI WINDOWEND = getIRI("hasWindowEnd");
	public static final IRI WINDOWDURATION = getIRI("hasWindowDuration");

	
	//Boots Alert Properties
	public static final IRI ALERTID = getIRI("hasAlertId");
	//public static final IRI TIMESTAMP = getIRI("hasTimestamp");
	public static final IRI ALERTSOURCE = getIRI("hasSource");
	public static final IRI EVENT = getIRI("showsEvent");
	public static final IRI ALERTEQUIPMENT = getIRI("madeByEquipment");
	
	
	 public static final String PREFIXES = "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
			 									+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
			 									+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n";
	

	private static IRI getIRI(String localName) 
	{
		return SimpleValueFactory.getInstance().createIRI(NAMESPACE, localName);
	}
}
