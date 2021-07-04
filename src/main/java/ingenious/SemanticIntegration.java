package ingenious;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;


import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.query.impl.MutableTupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleBinding;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import ingenious.utils.ConfigsLoader;
import ingenious.utils.QueryUtils;



public class SemanticIntegration {
	
	static ConfigsLoader configInstance;
	
	static {
		configInstance = ConfigsLoader.getInstance();
		configInstance.loadProperties();
	}
	
	public static int durationOfFiveMinutes = 5;
	public static int durationOfOneMinute = 1;
	
	public static float heatStrokeLimitBT = 40;
	
	public static float dehydrationLimitHR = 140;
	public static float dehydrationLimitBT = 38;
	
	JsonArray storage;
	private RepositoryConnection connection;
	// Connection Constructor
	public SemanticIntegration(RepositoryConnection connection)
	{
		this.connection = connection;
	}
	
	//Method to load ontology
	public void loadOntology() throws RDFParseException, RepositoryException, IOException
	{
		connection.add(SemanticIntegration.class.getResourceAsStream("/exampleProject.owl"), "urn:base", RDFFormat.TURTLE);
	}
	
	//Method to clear the KB and load ontology
	public void clearKBAndLoadOntology() throws RDFParseException, RepositoryException, IOException
	{
		connection.clear();
		//loadOntology();`
	}
	
	//Method to load the Resource Information from File, locally
	public void loadResourceMapFromFile() throws IOException, URISyntaxException 
	{
		JsonReader reader = new JsonReader(new FileReader(configInstance.getFilepath() + "ResourceMap.json"));
		reader.setLenient(true);
		JsonElement element = new JsonParser().parse(reader);
		//System.out.println(element.toString());
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace("ing", Input.NAMESPACE);
		
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		JsonObject object = element.getAsJsonObject();
		JsonObject object2 = element.getAsJsonObject();
		//JsonObject object3 = element.getAsJsonObject();
		//object = object.getAsJsonObject("RESOURCE");
		
		IRI resourceIRI = factory.createIRI(Input.NAMESPACE, "Resource");
		builder.subject(resourceIRI).add(RDF.TYPE, "ing:Resource");
		builder.subject(resourceIRI).add(Input.RESOURCEID, factory.createLiteral(object.get("ID").getAsString()));
		builder.subject(resourceIRI).add(Input.TIMESTAMP, factory.createLiteral(object.get("TIMESTAMP").getAsString(), XSD.DATETIME));
		storage = object.getAsJsonArray("RESOURCE");
		
		JsonArray arrayOfResources = storage;
		//System.out.println("test "+ arrayOfResources.size());
		
		for (int i=0;i< arrayOfResources.size(); i++)
		{
			
			object=arrayOfResources.get(i).getAsJsonObject();
			//System.out.println(object.get("ID").getAsString());
			if (object.get("TYPE").getAsString().equals("FR"))
			{
				String ID = object.get("ID").getAsString();
				IRI frIRI = factory.createIRI(Input.NAMESPACE, "FR_" +ID);
				IRI hrIRI = factory.createIRI(Input.NAMESPACE, "HR_" + ID);
				IRI btIRI = factory.createIRI(Input.NAMESPACE, "BT_" + ID);
				IRI boIRI = factory.createIRI(Input.NAMESPACE, "BO_" + ID);
				builder.subject(hrIRI).add(RDF.TYPE, "ing:VitalSign");
				builder.subject(hrIRI).add(RDF.TYPE, "ing:HeartRate");
				builder.subject(boIRI).add(RDF.TYPE, "ing:VitalSign");
				builder.subject(boIRI).add(RDF.TYPE, "ing:BloodOxygen");
				builder.subject(btIRI).add(RDF.TYPE, "ing:VitalSign");
				builder.subject(btIRI).add(RDF.TYPE, "ing:BodyTemperature");
				builder.subject(frIRI).add(Input.VITALSIGN, hrIRI);
				builder.subject(frIRI).add(Input.VITALSIGN, btIRI);
				builder.subject(frIRI).add(Input.VITALSIGN, boIRI);
				
				builder.subject(frIRI).add(RDF.TYPE, "ing:FR");
				builder.subject(frIRI).add(Input.FRID, factory.createLiteral(ID));
				
				builder.subject(frIRI).add(Input.NAME, factory.createLiteral(object.get("NAME").getAsString()));
				builder.subject(frIRI).add(Input.AGE, factory.createLiteral(object.get("AGE").getAsString()));
				builder.subject(frIRI).add(Input.GENDER, factory.createLiteral(object.get("GENDER").getAsString()));
				builder.subject(frIRI).add(Input.ORGANIZATION, factory.createLiteral(object.get("ORGANIZATION").getAsString()));
				builder.subject(frIRI).add(Input.TYPE, factory.createLiteral(object.get("TYPE").getAsString()));
				builder.subject(frIRI).add(Input.STATUS, factory.createLiteral(object.get("STATUS").getAsString()));
				
				if (object.get("TEAM_ID")!=null) {
					builder.subject(frIRI).add(Input.TEAM, factory.createLiteral(object.get("TEAM_ID").getAsString()));}
				if (object.get("ROLE_IN_TEAM")!=null) {
					builder.subject(frIRI).add(Input.ROLE, factory.createLiteral(object.get("ROLE_IN_TEAM").getAsString()));}
				if (object.get("WEIGHT")!=null) {
					builder.subject(frIRI).add(Input.WEIGHT, factory.createLiteral(object.get("WEIGHT").getAsString()));}
				if (object.get("HEIGHT")!=null) {
					builder.subject(frIRI).add(Input.HEIGHT, factory.createLiteral(object.get("HEIGHT").getAsString()));}
				
				
				
				
				
			}
			
			else if (object.get("TYPE").getAsString().equals("K9"))
			{
				String ID = object.get("ID").getAsString();
				IRI k9IRI = factory.createIRI(Input.NAMESPACE, ID);
				
				builder.subject(k9IRI).add(RDF.TYPE, "ing:K9");
				builder.subject(k9IRI).add(Input.K9ID, factory.createLiteral(ID));
				builder.subject(k9IRI).add(Input.ORGANIZATION, factory.createLiteral(object.get("ORGANIZATION").getAsString()));
				builder.subject(k9IRI).add(Input.TYPE, factory.createLiteral(object.get("TYPE").getAsString()));
				builder.subject(k9IRI).add(Input.STATUS, factory.createLiteral(object.get("STATUS").getAsString()));
				if (object.get("WEIGHT")!=null) {
					builder.subject(k9IRI).add(Input.WEIGHT, factory.createLiteral(object.get("WEIGHT").getAsString()));}
				if (object.get("HEIGHT")!=null) {
					builder.subject(k9IRI).add(Input.HEIGHT, factory.createLiteral(object.get("HEIGHT").getAsString()));}
				
			}
			
			else if (object.get("TYPE").getAsString().equals("VEHICLE"))
			{
				String ID = object.get("ID").getAsString();
				IRI vehicleIRI = factory.createIRI(Input.NAMESPACE, "Vehicle_" + ID);
				
				builder.subject(vehicleIRI).add(RDF.TYPE, "ing:Vehicle");
				builder.subject(vehicleIRI).add(Input.VEHICLEID, factory.createLiteral(ID));
				builder.subject(vehicleIRI).add(Input.ORGANIZATION, factory.createLiteral(object.get("ORGANIZATION").getAsString()));
				builder.subject(vehicleIRI).add(Input.TYPE, factory.createLiteral(object.get("TYPE").getAsString()));
				builder.subject(vehicleIRI).add(Input.STATUS, factory.createLiteral(object.get("STATUS").getAsString()));
			}
			
			
			else if (object.get("TYPE").getAsString().equals("K9 VEST") || object.get("TYPE").getAsString().equals("BOOTS") || object.get("TYPE").getAsString().equals("UNIFORM") || object.get("TYPE").getAsString().equals("HELMET"))
			{
				IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + object.get("ID").getAsString());
				builder.subject(equipmentIRI).add(RDF.TYPE, "ing:PersonalEquipment");
				builder.subject(equipmentIRI).add(Input.EQUIPMENTID, factory.createLiteral(object.get("ID").getAsString()));
				builder.subject(equipmentIRI).add(Input.ORGANIZATION, factory.createLiteral(object.get("ORGANIZATION").getAsString()));
				builder.subject(equipmentIRI).add(Input.TYPE, factory.createLiteral(object.get("TYPE").getAsString()));
				
			}
			
		}
		//Loop to equip equipment to FRs and K9s
		for (int i=0;i< arrayOfResources.size(); i++)
		{
			
			object=arrayOfResources.get(i).getAsJsonObject();
			for (int j=0; j < arrayOfResources.size(); j++)
			{
				object2=arrayOfResources.get(j).getAsJsonObject();
				
				if (object.get("TYPE").getAsString().equals("FR"))
				{
					if ((object2.get("ATTACHED_TO")!=null) && (object2.get("TYPE").getAsString().equals("BOOTS") || object2.get("TYPE").getAsString().equals("UNIFORM") || object2.get("TYPE").getAsString().equals("HELMET")))
					{
						if (object.get("ID").getAsString().equals(object2.get("ATTACHED_TO").getAsString()))
						{
							String ID = object.get("ID").getAsString();
							IRI frIRI = factory.createIRI(Input.NAMESPACE, "FR_" +ID);
							IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + object2.get("ID").getAsString());
							builder.subject(frIRI).add(Input.EQUIPMENT, equipmentIRI);
							builder.subject(equipmentIRI).add(Input.ATTACHMENT, frIRI);
						}
					}
					if ((object2.get("ATTACHED_TO")!=null) && (object2.get("TYPE").getAsString().equals("K9")))
					{
						if (object.get("ID").getAsString().equals(object2.get("ATTACHED_TO").getAsString()))
						{
							String ID = object.get("ID").getAsString();
							IRI frIRI = factory.createIRI(Input.NAMESPACE, "FR_" +ID);
							IRI k9IRI = factory.createIRI(Input.NAMESPACE, object2.get("ID").getAsString());
							builder.subject(frIRI).add(Input.K9, k9IRI);
							builder.subject(k9IRI).add(Input.ATTACHMENT, frIRI);
						}
					}
				}
				
				if (object.get("TYPE").getAsString().equals("K9"))
				{
					if ((object2.get("ATTACHED_TO")!=null) && (object2.get("TYPE").getAsString().equals("K9 VEST")))
					{
						if (object.get("ID").getAsString().equals(object2.get("ATTACHED_TO").getAsString()))
						{
							String ID = object.get("ID").getAsString();
							IRI k9IRI = factory.createIRI(Input.NAMESPACE, ID);
							IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + object2.get("ID").getAsString());
							builder.subject(k9IRI).add(Input.EQUIPMENT, equipmentIRI);
							builder.subject(equipmentIRI).add(Input.ATTACHMENT, k9IRI);
						}
					}
				}
			}
		}		
		Model model = builder.build();
		
		File outputFile = new File(configInstance.getFilepath() + "resourceMapOutput.owl");
		FileOutputStream out = new FileOutputStream(outputFile);
		try
		{
			Rio.write(model, out, RDFFormat.TURTLE);
		}
		finally
		{
			out.close();
		}
		
		connection.add(model);
		
	}
	
	//Method to load the Resource Information from kafka stream (String)
	public void loadResourceMapFromStream(String stream) throws IOException, URISyntaxException 
	{
		//JsonReader reader = new JsonReader(new FileReader("C:\\Users\\Savvas\\Documents\\GitHub\\Ingenious\\ING_ER\\ResourceMap.json"));
		//reader.setLenient(true);
		
		JsonElement element = new JsonParser().parse(stream);
		System.out.println(element.toString());
		
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace("ing", Input.NAMESPACE);
		
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		JsonObject object = element.getAsJsonObject();
		JsonObject object2 = element.getAsJsonObject();
		//JsonObject object3 = element.getAsJsonObject();
		//object = object.getAsJsonObject("RESOURCE");
		
		IRI resourceIRI = factory.createIRI(Input.NAMESPACE, "Resource");
		builder.subject(resourceIRI).add(RDF.TYPE, "ing:Resource");
		builder.subject(resourceIRI).add(Input.RESOURCEID, factory.createLiteral(object.get("ID").getAsString()));
		builder.subject(resourceIRI).add(Input.TIMESTAMP, factory.createLiteral(object.get("TIMESTAMP").getAsString(), XSD.DATETIME));
		storage = object.getAsJsonArray("RESOURCE");
		
		JsonArray arrayOfResources = storage;
		//System.out.println("test "+ arrayOfResources.size());
		
		for (int i=0;i< arrayOfResources.size(); i++)
		{
			
			object=arrayOfResources.get(i).getAsJsonObject();
			//System.out.println(object.get("ID").getAsString());
			if (object.get("TYPE").getAsString().equals("FR"))
			{
				String ID = object.get("ID").getAsString();
				IRI frIRI = factory.createIRI(Input.NAMESPACE, "FR_" +ID);
				IRI hrIRI = factory.createIRI(Input.NAMESPACE, "HR_" + ID);
				IRI btIRI = factory.createIRI(Input.NAMESPACE, "BT_" + ID);
				IRI boIRI = factory.createIRI(Input.NAMESPACE, "BO_" + ID);
				builder.subject(hrIRI).add(RDF.TYPE, "ing:VitalSign");
				builder.subject(hrIRI).add(RDF.TYPE, "ing:HeartRate");
				builder.subject(boIRI).add(RDF.TYPE, "ing:VitalSign");
				builder.subject(boIRI).add(RDF.TYPE, "ing:BloodOxygen");
				builder.subject(btIRI).add(RDF.TYPE, "ing:VitalSign");
				builder.subject(btIRI).add(RDF.TYPE, "ing:BodyTemperature");
				builder.subject(frIRI).add(Input.VITALSIGN, hrIRI);
				builder.subject(frIRI).add(Input.VITALSIGN, btIRI);
				builder.subject(frIRI).add(Input.VITALSIGN, boIRI);
				
				builder.subject(frIRI).add(RDF.TYPE, "ing:FR");
				builder.subject(frIRI).add(Input.FRID, factory.createLiteral(ID));
				
				builder.subject(frIRI).add(Input.NAME, factory.createLiteral(object.get("NAME").getAsString()));
				builder.subject(frIRI).add(Input.AGE, factory.createLiteral(object.get("AGE").getAsString()));
				builder.subject(frIRI).add(Input.GENDER, factory.createLiteral(object.get("GENDER").getAsString()));
				builder.subject(frIRI).add(Input.ORGANIZATION, factory.createLiteral(object.get("ORGANIZATION").getAsString()));
				builder.subject(frIRI).add(Input.TYPE, factory.createLiteral(object.get("TYPE").getAsString()));
				builder.subject(frIRI).add(Input.STATUS, factory.createLiteral(object.get("STATUS").getAsString()));
				
				if (object.get("TEAM_ID")!=null) {
					builder.subject(frIRI).add(Input.TEAM, factory.createLiteral(object.get("TEAM_ID").getAsString()));}
				if (object.get("ROLE_IN_TEAM")!=null) {
					builder.subject(frIRI).add(Input.ROLE, factory.createLiteral(object.get("ROLE_IN_TEAM").getAsString()));}
				if (object.get("WEIGHT")!=null) {
					builder.subject(frIRI).add(Input.WEIGHT, factory.createLiteral(object.get("WEIGHT").getAsString()));}
				if (object.get("HEIGHT")!=null) {
					builder.subject(frIRI).add(Input.HEIGHT, factory.createLiteral(object.get("HEIGHT").getAsString()));}
				
				
				
				
				
			}
			
			else if (object.get("TYPE").getAsString().equals("K9"))
			{
				String ID = object.get("ID").getAsString();
				IRI k9IRI = factory.createIRI(Input.NAMESPACE, ID);
				
				builder.subject(k9IRI).add(RDF.TYPE, "ing:K9");
				builder.subject(k9IRI).add(Input.K9ID, factory.createLiteral(ID));
				builder.subject(k9IRI).add(Input.ORGANIZATION, factory.createLiteral(object.get("ORGANIZATION").getAsString()));
				builder.subject(k9IRI).add(Input.TYPE, factory.createLiteral(object.get("TYPE").getAsString()));
				builder.subject(k9IRI).add(Input.STATUS, factory.createLiteral(object.get("STATUS").getAsString()));
				if (object.get("WEIGHT")!=null) {
					builder.subject(k9IRI).add(Input.WEIGHT, factory.createLiteral(object.get("WEIGHT").getAsString()));}
				if (object.get("HEIGHT")!=null) {
					builder.subject(k9IRI).add(Input.HEIGHT, factory.createLiteral(object.get("HEIGHT").getAsString()));}
				if (object.get("ATTACHED_TO")!=null) {
					builder.subject(k9IRI).add(Input.ATTACHMENT, factory.createLiteral(object.get("ATTACHED_TO").getAsString()));}
			}
			
			else if (object.get("TYPE").getAsString().equals("VEHICLE"))
			{
				String ID = object.get("ID").getAsString();
				IRI vehicleIRI = factory.createIRI(Input.NAMESPACE, "Vehicle_" + ID);
				
				builder.subject(vehicleIRI).add(RDF.TYPE, "ing:Vehicle");
				builder.subject(vehicleIRI).add(Input.VEHICLEID, factory.createLiteral(ID));
				builder.subject(vehicleIRI).add(Input.ORGANIZATION, factory.createLiteral(object.get("ORGANIZATION").getAsString()));
				builder.subject(vehicleIRI).add(Input.TYPE, factory.createLiteral(object.get("TYPE").getAsString()));
				builder.subject(vehicleIRI).add(Input.STATUS, factory.createLiteral(object.get("STATUS").getAsString()));
			}
			
			
			else if (object.get("TYPE").getAsString().equals("K9 VEST") || object.get("TYPE").getAsString().equals("BOOTS") || object.get("TYPE").getAsString().equals("UNIFORM") || object.get("TYPE").getAsString().equals("HELMET"))
			{
				IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + object.get("ID").getAsString());
				builder.subject(equipmentIRI).add(RDF.TYPE, "ing:PersonalEquipment");
				builder.subject(equipmentIRI).add(Input.EQUIPMENTID, factory.createLiteral(object.get("ID").getAsString()));
				builder.subject(equipmentIRI).add(Input.ORGANIZATION, factory.createLiteral(object.get("ORGANIZATION").getAsString()));
				builder.subject(equipmentIRI).add(Input.TYPE, factory.createLiteral(object.get("TYPE").getAsString()));
				if (object.get("ATTACHED_TO")!=null) {
				builder.subject(equipmentIRI).add(Input.ATTACHMENT, factory.createLiteral(object.get("ATTACHED_TO").getAsString()));}
			}
			
		}
		
		for (int i=0;i< arrayOfResources.size(); i++)
		{
			
			object=arrayOfResources.get(i).getAsJsonObject();
			for (int j=0; j < arrayOfResources.size(); j++)
			{
				object2=arrayOfResources.get(j).getAsJsonObject();
				
				if (object.get("TYPE").getAsString().equals("FR"))
				{
					if ((object2.get("ATTACHED_TO")!=null) && (object2.get("TYPE").getAsString().equals("BOOTS") || object2.get("TYPE").getAsString().equals("UNIFORM") || object2.get("TYPE").getAsString().equals("HELMET")))
					{
						if (object.get("ID").getAsString().equals(object2.get("ATTACHED_TO").getAsString()))
						{
							String ID = object.get("ID").getAsString();
							IRI frIRI = factory.createIRI(Input.NAMESPACE, "FR_" +ID);
							IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + object2.get("ID").getAsString());
							builder.subject(frIRI).add(Input.EQUIPMENT, equipmentIRI);
						}
					}
					if ((object2.get("ATTACHED_TO")!=null) && (object2.get("TYPE").getAsString().equals("K9")))
					{
						if (object.get("ID").getAsString().equals(object2.get("ATTACHED_TO").getAsString()))
						{
							String ID = object.get("ID").getAsString();
							IRI frIRI = factory.createIRI(Input.NAMESPACE, "FR_" +ID);
							IRI k9IRI = factory.createIRI(Input.NAMESPACE, object2.get("ID").getAsString());
							builder.subject(frIRI).add(Input.K9, k9IRI);
						}
					}
				}
				
				if (object.get("TYPE").getAsString().equals("K9"))
				{
					if ((object2.get("ATTACHED_TO")!=null) && (object2.get("TYPE").getAsString().equals("K9 VEST")))
					{
						if (object.get("ID").getAsString().equals(object2.get("ATTACHED_TO").getAsString()))
						{
							String ID = object.get("ID").getAsString();
							IRI k9IRI = factory.createIRI(Input.NAMESPACE, ID);
							IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + object2.get("ID").getAsString());
							builder.subject(k9IRI).add(Input.EQUIPMENT, equipmentIRI);
						}
					}
				}
			}
		}		
		Model model = builder.build();
		
		File outputFile = new File(configInstance.getFilepath() + "resourceMapOutput.owl");
		FileOutputStream out = new FileOutputStream(outputFile);
		try
		{
			Rio.write(model, out, RDFFormat.TURTLE);
		}
		finally
		{
			out.close();
		}
		
		connection.add(model);
		
	}
	
	//Method to load Measurements from File, locally
	public void loadMeasurementsFromFile() throws IOException, URISyntaxException 
		{
			JsonReader reader = new JsonReader(new FileReader(configInstance.getFilepath() + "Measurements.json"));
			//System.out.println(reader.toString());
			
			reader.setLenient(true);
			JsonElement element = new JsonParser().parse(reader);
			//System.out.println(element.getAsString());
			ModelBuilder builder = new ModelBuilder();
			builder.setNamespace("ing", Input.NAMESPACE);
			//System.out.println(element.isJsonNull());
			ValueFactory factory = SimpleValueFactory.getInstance();
			
			JsonObject object = element.getAsJsonObject();
			//System.out.println(object.get("version").getAsString());
			JsonObject object2 = element.getAsJsonObject();
			JsonObject object3 = element.getAsJsonObject();
			//object = object.getAsJsonObject("RESOURCE");
			
			IRI headerIRI = factory.createIRI(Input.NAMESPACE, "header");
			object=object.getAsJsonObject("header");
			//System.out.println(object.get("version").getAsString());
			builder.subject(headerIRI).add(RDF.TYPE, "ing:header");
			builder.subject(headerIRI).add(Input.VERSION, factory.createLiteral(object.get("version").getAsString()));
			builder.subject(headerIRI).add(Input.SENT, factory.createLiteral(object.get("sent").getAsString()));
			//builder.subject(headerIRI).add(RDF.TYPE, "ing:header").add(Input.SOURCE, factory.createLiteral(object.get("source").getAsString()));
			for (int i=0;i< storage.size(); i++)
			{
				JsonObject tempObject=storage.get(i).getAsJsonObject();
				if (object.get("source").getAsString().equals(tempObject.get("ID").getAsString()))
						{
							IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + tempObject.get("ID").getAsString());
							builder.subject(headerIRI).add(Input.SOURCE, equipmentIRI);
						}
			}
			builder.subject(headerIRI).add(Input.HEADERCRC, factory.createLiteral(object.get("headerCRC").getAsString()));
			builder.subject(headerIRI).add(Input.PAYLOADCRC, factory.createLiteral(object.get("payloadCRC").getAsString()));
			//object=object.getAsJsonObject("payload");
			JsonArray arrayOfMeasurements = object2.getAsJsonArray("payload");
			//System.out.println("test "+ arrayOfResources.size());
			
			for (int i=0;i< arrayOfMeasurements.size(); i++)
			{
				
				object2=arrayOfMeasurements.get(i).getAsJsonObject();
				IRI measurementIRI = factory.createIRI(Input.NAMESPACE, "Measurement_" + object2.get("observationId").getAsString());
				builder.subject(measurementIRI).add(RDF.TYPE, "ing:Measurement").add(Input.OBSERVATIONID, factory.createLiteral(object2.get("observationId").getAsString()));
				//builder.subject(measurementIRI).add(RDF.TYPE, "ing:Measurement").add(Input.SENSORID, factory.createLiteral(object2.get("sensorId").getAsString()));
				for (int j=0;j< storage.size(); j++)
				{
					JsonObject tempObject=storage.get(j).getAsJsonObject();
					if (object2.get("sensorId").getAsString().equals(tempObject.get("ID").getAsString()))
							{
								IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + tempObject.get("ID").getAsString());
								//builder.subject(measurementIRI).add(Input.SENSORID, equipmentIRI);
								builder.subject(equipmentIRI).add(Input.MAKESMEASUREMENT, measurementIRI);
								
								if (object2.get("observedProperty").getAsString().equals("heart beat"))
								{
									String modification=(
											"PREFIX ing: <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
											+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
												
											+"INSERT {\r\n"
											    +"?measurementIRI ing:isMeasurementOf ?measured_property.\r\n"
											+"}\r\n"
											+"WHERE{\r\n"
											    +"?FR ing:wearsEquipment ?equipmentIRI.\r\n"
											    +"?FR ing:hasVitalSign ?measured_property.\r\n"	
											    +"?measured_property rdf:type ?propClass.\r\n"
											+"}\r\n"
											   );
									
									IRI prop_class = factory.createIRI(Input.NAMESPACE,"HeartRate");
									
								
									//System.out.println(prop_class);
		
									
	//								
									executeUpdate(connection, modification, new SimpleBinding("measurementIRI", measurementIRI),  new SimpleBinding("equipmentIRI", equipmentIRI), new SimpleBinding("propClass", prop_class));
								}
								else if (object2.get("observedProperty").getAsString().equals("temperature"))
								{
									
									
									
									String modification=(
											"PREFIX ing: <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
											+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
												
											+"INSERT{"
											    +"$measurementIRI ing:isMeasurementOf ?measured_property.\r\n"
											+"}"
											+"WHERE{"
											    +"?FR ing:wearsEquipment ?equipmentIRI.\r\n"
											    +"?FR ing:hasVitalSign ?measured_property.\r\n"	
											    +"?measured_property rdf:type ?propClass.\r\n"
											+"}"
											   );
									IRI prop_class = factory.createIRI(Input.NAMESPACE,"BodyTemperature");
									executeUpdate(connection, modification, new SimpleBinding("measurementIRI", measurementIRI),  new SimpleBinding("equipmentIRI", equipmentIRI), new SimpleBinding("propClass", prop_class));
								}
								else if (object2.get("observedProperty").getAsString().equals("oxygen levels"))
								{
									
									
								
									String modification=(
											"PREFIX ing: <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
											+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
												
											+"INSERT {"
											    +"$measurementIRI ing:isMeasurementOf ?measured_property.\r\n"
											+"}\r\n"
											+"WHERE{\r\n"
											    +"?FR ing:wearsEquipment ?equipmentIRI.\r\n"
											    +"?FR ing:hasVitalSign ?measured_property.\r\n"	
											    +"?measured_property rdf:type ?propClass.\r\n"
											+"}\r\n"
											   );
									IRI prop_class = factory.createIRI(Input.NAMESPACE,"BloodOxygen");
									executeUpdate(connection, modification, new SimpleBinding("measurementIRI", measurementIRI),  new SimpleBinding("equipmentIRI", equipmentIRI), new SimpleBinding("propClass", prop_class));
								}
							}
				} 
				//builder.subject(measurementIRI).add(Input.OBSERVEDPROPERTY, factory.createLiteral(object2.get("observedProperty").getAsString()));
				
				
				
				builder.subject(measurementIRI).add(Input.TIMESTAMP, factory.createLiteral(object2.get("time").getAsString(), XSD.DATETIME));
				
				builder.subject(measurementIRI).add(Input.RESULT, factory.createLiteral(object2.get("result").getAsString(), XSD.FLOAT));
				builder.subject(measurementIRI).add(Input.LOCATION, factory.createLiteral(object2.get("location").getAsString()));
				//location won't remain a String, testing.
			}
			
			Model model = builder.build();
			
			File outputFile = new File(configInstance.getFilepath() + "measurementOutput.owl");
			FileOutputStream out = new FileOutputStream(outputFile);
			try
			{
				Rio.write(model, out, RDFFormat.TURTLE);
			}
			finally
			{
				out.close();
			}
			
			connection.add(model);
			
		}
	
	//Method to load Measurements from Kafka stream (String)
	public void loadMeasurementsFromStream(String stream) throws IOException, URISyntaxException 
	{
		//JsonReader reader = new JsonReader(new FileReader("C:\\Users\\Savvas\\Documents\\GitHub\\Ingenious\\ING_ER\\Measurements.json"));
		//System.out.println(reader.toString());
		
		//reader.setLenient(true);
		JsonElement element = new JsonParser().parse(stream);
		//System.out.println(element.getAsString());
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace("ing", Input.NAMESPACE);
		//System.out.println(element.isJsonNull());
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		JsonObject object = element.getAsJsonObject();
		//System.out.println(object.get("version").getAsString());
		JsonObject object2 = element.getAsJsonObject();
		JsonObject object3 = element.getAsJsonObject();
		//object = object.getAsJsonObject("RESOURCE");
		
		IRI headerIRI = factory.createIRI(Input.NAMESPACE, "header");
		object=object.getAsJsonObject("header");
		//System.out.println(object.get("version").getAsString());
		builder.subject(headerIRI).add(RDF.TYPE, "ing:header");
		builder.subject(headerIRI).add(Input.VERSION, factory.createLiteral(object.get("version").getAsString()));
		builder.subject(headerIRI).add(Input.SENT, factory.createLiteral(object.get("sent").getAsString()));
		//builder.subject(headerIRI).add(RDF.TYPE, "ing:header").add(Input.SOURCE, factory.createLiteral(object.get("source").getAsString()));
		for (int i=0;i< storage.size(); i++)
		{
			JsonObject tempObject=storage.get(i).getAsJsonObject();
			if (object.get("source").getAsString().equals(tempObject.get("ID").getAsString()))
					{
						IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + tempObject.get("ID").getAsString());
						builder.subject(headerIRI).add(Input.SOURCE, equipmentIRI);
					}
		}
		builder.subject(headerIRI).add(Input.HEADERCRC, factory.createLiteral(object.get("headerCRC").getAsString()));
		builder.subject(headerIRI).add(Input.PAYLOADCRC, factory.createLiteral(object.get("payloadCRC").getAsString()));
		//object=object.getAsJsonObject("payload");
		JsonArray arrayOfMeasurements = object2.getAsJsonArray("payload");
		//System.out.println("test "+ arrayOfResources.size());
		
		for (int i=0;i< arrayOfMeasurements.size(); i++)
		{
			
			object2=arrayOfMeasurements.get(i).getAsJsonObject();
			IRI measurementIRI = factory.createIRI(Input.NAMESPACE, "Measurement_" + object2.get("observationId").getAsString());
			builder.subject(measurementIRI).add(RDF.TYPE, "ing:Measurement").add(Input.OBSERVATIONID, factory.createLiteral(object2.get("observationId").getAsString()));
			//builder.subject(measurementIRI).add(RDF.TYPE, "ing:Measurement").add(Input.SENSORID, factory.createLiteral(object2.get("sensorId").getAsString()));
			for (int j=0;j< storage.size(); j++)
			{
				JsonObject tempObject=storage.get(j).getAsJsonObject();
				if (object2.get("sensorId").getAsString().equals(tempObject.get("ID").getAsString()))
						{
							IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + tempObject.get("ID").getAsString());
							//builder.subject(measurementIRI).add(Input.SENSORID, equipmentIRI);
							builder.subject(equipmentIRI).add(Input.MAKESMEASUREMENT, measurementIRI);
							
							if (object2.get("observedProperty").getAsString().equals("heart beat"))
							{
								
								
								
								String modification=(
										"PREFIX ing: <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>"
										+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
											
										+"INSERT{"
										    +"?measurementIRI ing:isMeasurementOf ?measured_property."
										+"}"
										+"WHERE{"
										    +"?FR ing:wearsEquipment ?equipmentIRI."
										    +"?FR ing:hasVitalSign ?measured_property."	
										    +"?measured_property rdf:type ?propClass."
										+"}"
										   );
								
								IRI prop_class = factory.createIRI(Input.NAMESPACE,"HeartRate");
								
							
								//System.out.println(prop_class);
	
								
//								
								executeUpdate(connection, modification, new SimpleBinding("measurementIRI", measurementIRI),  new SimpleBinding("equipmentIRI", equipmentIRI), new SimpleBinding("propClass", prop_class));
							}
							else if (object2.get("observedProperty").getAsString().equals("temperature"))
							{
								
								
								
								String modification=(
										"PREFIX ing: <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>"
										+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
											
										+"INSERT{"
										    +"$measurementIRI ing:isMeasurementOf ?measured_property."
										+"}"
										+"WHERE{"
										    +"?FR ing:wearsEquipment ?equipmentIRI."
										    +"?FR ing:hasVitalSign ?measured_property."	
										    +"?measured_property rdf:type ?propClass."
										+"}"
										   );
								IRI prop_class = factory.createIRI(Input.NAMESPACE,"BodyTemperature");
								executeUpdate(connection, modification, new SimpleBinding("measurementIRI", measurementIRI),  new SimpleBinding("equipmentIRI", equipmentIRI), new SimpleBinding("propClass", prop_class));
							}
							else if (object2.get("observedProperty").getAsString().equals("oxygen levels"))
							{
								
								
								
								String modification=(
										"PREFIX ing: <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>"
										+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
										+"INSERT{"
										    +"$measurementIRI ing:isMeasurementOf ?measured_property."
										+"}"
										+"WHERE{"
										    +"?FR ing:wearsEquipment ?equipmentIRI."
										    +"?FR ing:hasVitalSign ?measured_property."	
										    +"?measured_property rdf:type ?propClass."
										+"}"
										   );
								IRI prop_class = factory.createIRI(Input.NAMESPACE,"BloodOxygen");
								executeUpdate(connection, modification, new SimpleBinding("measurementIRI", measurementIRI),  new SimpleBinding("equipmentIRI", equipmentIRI), new SimpleBinding("propClass", prop_class));
							}
						}
			} 
			//builder.subject(measurementIRI).add(Input.OBSERVEDPROPERTY, factory.createLiteral(object2.get("observedProperty").getAsString()));
			
			
			
			builder.subject(measurementIRI).add(Input.TIMESTAMP, factory.createLiteral(object2.get("time").getAsString(), XSD.DATETIME));
			
			builder.subject(measurementIRI).add(Input.RESULT, factory.createLiteral(object2.get("result").getAsString(), XSD.FLOAT));
			builder.subject(measurementIRI).add(Input.LOCATION, factory.createLiteral(object2.get("location").getAsString()));
			//location won't remain a String, testing.
		}
		
		Model model = builder.build();
		
		File outputFile = new File(configInstance.getFilepath() + "measurementOutput.owl");
		FileOutputStream out = new FileOutputStream(outputFile);
		try
		{
			Rio.write(model, out, RDFFormat.TURTLE);
		}
		finally
		{
			out.close();
		}
		
		connection.add(model);
		
	}
	
	//Method to load Boots alerts from File, locally
	public void loadBootsAlertFromFile() throws IOException, URISyntaxException 
	{
		JsonReader reader = new JsonReader(new FileReader(configInstance.getFilepath() + "BootsAlert.json"));
		reader.setLenient(true);
		JsonElement element = new JsonParser().parse(reader);
		//System.out.println(element.toString());
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace("ing", Input.NAMESPACE);
		
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		JsonObject serialObject = element.getAsJsonObject();
		JsonObject arrayObject = element.getAsJsonObject();
		//JsonObject object3 = element.getAsJsonObject();
		
		
		
	
		JsonArray arrayOfAlert = serialObject.getAsJsonArray("info");
		//System.out.println(storage);
		//System.out.println("test "+ arrayOfResources.size());
		
	
			
			//object=arrayOfResources.get(i).getAsJsonObject();
			String ID = serialObject.get("identifier").getAsString();
			IRI bootsAlertIRI = factory.createIRI(Input.NAMESPACE, "BootsAlert_" + ID);
			builder.subject(bootsAlertIRI).add(RDF.TYPE, "ing:BootsAlert");
			builder.subject(bootsAlertIRI).add(Input.ALERTID, factory.createLiteral(ID));
			//builder.subject(bootsAlertIRI).add(Input.ALERTSOURCE, factory.createLiteral(object.get("source").getAsString()));
			for (int i=0;i< arrayOfAlert.size(); i++) {
				arrayObject=arrayOfAlert.get(i).getAsJsonObject();
				builder.subject(bootsAlertIRI).add(Input.EVENT, factory.createLiteral(arrayObject.get("event").getAsString()));
				JsonArray arrayOfCode = serialObject.getAsJsonArray("code");
				for (int j=0;j< arrayOfCode.size(); j++) {
					String arrayObject2=arrayOfCode.get(j).getAsString();
					builder.subject(bootsAlertIRI).add(Input.ALERTEQUIPMENT, factory.createLiteral(arrayObject2));
					IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + arrayOfCode.get(j).getAsString());
					builder.subject(equipmentIRI).add(Input.MAKESMEASUREMENT, bootsAlertIRI);
				}
				
			}
			
			for (int i=0;i< storage.size(); i++){
				JsonObject frObject = storage.get(i).getAsJsonObject();
				//for (int j=0; j<arrayOfResources.size(); j++){
					//object2=arrayOfResources.get(j).getAsJsonObject();
					if (frObject.get("TYPE").getAsString().equals("FR")) {
						//System.out.println(serialObject);
						
						if (frObject.get("ID").getAsString().equals(serialObject.get("source").getAsString())) {
							String tempID = frObject.get("ID").getAsString();
							IRI frIRI = factory.createIRI(Input.NAMESPACE, "FR_" +tempID);
							
							IRI alertIRI = factory.createIRI(Input.NAMESPACE, "BootsAlert_" + serialObject.get("identifier").getAsString());
							builder.subject(frIRI).add(Input.TARGET, alertIRI);
							builder.subject(alertIRI).add(Input.ALERTSOURCE, frIRI);
						}
					}
					
				}
		Model model = builder.build();
		
		File outputFile = new File(configInstance.getFilepath() + "bootsAlertOutput.owl");
		FileOutputStream out = new FileOutputStream(outputFile);
		try
		{
			Rio.write(model, out, RDFFormat.RDFXML);
		}
		finally
		{
			out.close();
		}
		
		connection.add(model);}
		
	//}
	
	//Method to load Measurements from Kafka stream (String)
	public void loadBootsAlertFromStream(String stream) throws IOException, URISyntaxException 
	{
		JsonReader reader = new JsonReader(new FileReader(configInstance.getFilepath() + "BootsAlert.json"));
		reader.setLenient(true);
		JsonElement element = new JsonParser().parse(stream);
		//System.out.println(element.toString());
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace("ing", Input.NAMESPACE);
		
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		JsonObject serialObject = element.getAsJsonObject();
		JsonObject arrayObject = element.getAsJsonObject();
		//JsonObject object3 = element.getAsJsonObject();
		
		
		
	
		JsonArray arrayOfAlert = serialObject.getAsJsonArray("info");
		//System.out.println(storage);
		//System.out.println("test "+ arrayOfResources.size());
		
	
			
			//object=arrayOfResources.get(i).getAsJsonObject();
			String ID = serialObject.get("identifier").getAsString();
			IRI bootsAlertIRI = factory.createIRI(Input.NAMESPACE, "BootsAlert_" + ID);
			builder.subject(bootsAlertIRI).add(RDF.TYPE, "ing:BootsAlert");
			builder.subject(bootsAlertIRI).add(Input.ALERTID, factory.createLiteral(ID));
			//builder.subject(bootsAlertIRI).add(Input.ALERTSOURCE, factory.createLiteral(object.get("source").getAsString()));
			for (int i=0;i< arrayOfAlert.size(); i++) {
				arrayObject=arrayOfAlert.get(i).getAsJsonObject();
				builder.subject(bootsAlertIRI).add(Input.EVENT, factory.createLiteral(arrayObject.get("event").getAsString()));
				JsonArray arrayOfCode = serialObject.getAsJsonArray("code");
				for (int j=0;j< arrayOfCode.size(); j++) {
					String arrayObject2=arrayOfCode.get(j).getAsString();
					builder.subject(bootsAlertIRI).add(Input.ALERTEQUIPMENT, factory.createLiteral(arrayObject2));
					IRI equipmentIRI = factory.createIRI(Input.NAMESPACE, "Equipment_" + arrayOfCode.get(j).getAsString());
					builder.subject(equipmentIRI).add(Input.MAKESMEASUREMENT, bootsAlertIRI);
				}
				
			}
			
			for (int i=0;i< storage.size(); i++){
				JsonObject frObject = storage.get(i).getAsJsonObject();
				//for (int j=0; j<arrayOfResources.size(); j++){
					//object2=arrayOfResources.get(j).getAsJsonObject();
					if (frObject.get("TYPE").getAsString().equals("FR")) {
						//System.out.println(serialObject);
						
						if (frObject.get("ID").getAsString().equals(serialObject.get("source").getAsString())) {
							String tempID = frObject.get("ID").getAsString();
							IRI frIRI = factory.createIRI(Input.NAMESPACE, "FR_" +tempID);
							
							IRI alertIRI = factory.createIRI(Input.NAMESPACE, "BootsAlert_" + serialObject.get("identifier").getAsString());
							builder.subject(frIRI).add(Input.TARGET, alertIRI);
							builder.subject(alertIRI).add(Input.ALERTSOURCE, frIRI);
						}
					}
					
				}
		Model model = builder.build();
		
		File outputFile = new File(configInstance.getFilepath() + "bootsAlertOutput.owl");
		FileOutputStream out = new FileOutputStream(outputFile);
		try
		{
			Rio.write(model, out, RDFFormat.RDFXML);
		}
		finally
		{
			out.close();
		}
		
		connection.add(model);}
		
	//}

	//Execute update of KB
	public void executeUpdate(RepositoryConnection repositoryConnection, String update, Binding... bindings)
            throws MalformedQueryException, RepositoryException, UpdateExecutionException {
        Update preparedUpdate = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, update);
        // Setting any potential bindings (query parameters)
        for (Binding b : bindings) {
            preparedUpdate.setBinding(b.getName(), b.getValue());
        }
        preparedUpdate.execute();
    }
	
	//Get current DateTime in Epoch Seconds (long)
	public long getCurrentDateTimeToEpochSeconds ()
	{
		OffsetDateTime odt = OffsetDateTime.now();
		long timestamp = odt.toEpochSecond();
		return timestamp;
	}
	
	//Get a DateTime in String format, and convert it to Epoch Seconds (long)
	public long getDateTimeToEpochSecondsFromString (String dateTime)
	{
		OffsetDateTime odt = OffsetDateTime.parse(dateTime);
		System.out.println(odt);
		long timestamp = odt.toEpochSecond();
		System.out.println(timestamp);
		return timestamp;
	}
	
	//Get Epoch Seconds (long) and convert to OffsetDateTime format
	public OffsetDateTime getDateTimeFromEpochSeconds(long epochSeconds)
	{
		//OffsetDateTime odt = OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochSeconds*1000), ZoneOffset.UTC);
		OffsetDateTime odt2 = OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochSeconds*1000), ZoneId.systemDefault());
		return odt2;
		
	}
	
	//Get Epoch Seconds (long) and convert to ZonedDateTime format
	public ZonedDateTime getZonedDateTimeFromEpochSeconds(long epochSeconds)
	{
		ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochSeconds*1000), ZoneId.systemDefault());
		OffsetDateTime odt = OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochSeconds*1000), ZoneOffset.UTC);
		return zdt;
		
	}
	
	//Get values from KB, for a certain property type (e.g., "temperature") for a certain average period
	public TupleQueryResult getValues(String propertyType, int periodOfAverage) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        
        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI prop_class = factory.createIRI(Input.NAMESPACE, propertyType);
        
        long timestamp = getCurrentDateTimeToEpochSeconds() - periodOfAverage*60;
        String str = getDateTimeFromEpochSeconds(timestamp).toString();
        //System.out.println(str);

        Literal timeLimit = factory.createLiteral(str, XSD.DATETIME);
       
        System.out.println("timeLimit: " + timeLimit.toString());
        //System.out.println(timeLimit.getDatatype());
        
        
        
        TupleQueryResult result = QueryUtils.evaluateSelectQuery2(connection,
        		
        		     "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
        			+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        			+"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
        			+"SELECT ?value ?time ?property\r\n"
					//+"FROM <http://www.ontotext.com/implicit>\r\n" 
					//+"FROM <http://www.ontotext.com/explicit>\r\n"
        			+"WHERE{\r\n"
        			   +" ?measurement a ing:Measurement.\r\n"
        			    +"?measurement ing:hasTimestamp ?time.\r\n"
        			   + "?measurement ing:hasResult ?value.\r\n"
        			    +"?measurement ing:isMeasurementOf ?property.\r\n"
        			    +"?property a ?propClass.\r\n"
        			    +"?fr ing:hasVitalSign ?property.\r\n"
        			    +"FILTER(?time >= ?time_limit)\r\n"
        			+"}", new SimpleBinding("propClass", prop_class), new SimpleBinding("time_limit", timeLimit)
        			    );
        //System.out.println(result.hasNext());
        while (result.hasNext()) {
        	
            
            BindingSet bindingSet = result.next();
            //IRI p1 = (IRI) bindingSet.getBinding("Activity").getValue();
            Value measurement = bindingSet.getBinding("value").getValue();
           
            Value dateTime = bindingSet.getBinding("time").getValue();
            IRI property = (IRI) bindingSet.getBinding("property").getValue();
            //Value p1 = bindingSet.getBinding("property").getValue();

            System.out.println(" || Value: " + measurement.stringValue() + " || Time: " + dateTime.stringValue() + " || Property: " + property.toString());
           
        }
        // Once we are done with a particular result we need to close it
        //result.close();
        return result;
    }
	
	//Calculate Rolling Average for a certain property type and average period
	public void calculateRollingAverage(String propertyType, int periodOfAverage) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        System.out.println("calculateRollingAverage");
        TupleQueryResult result = getValues(propertyType, periodOfAverage);
        int add=0;
        float what=0;
        String str, str2, measuredProperty = null;
        
        List<Long> dateTimesInEpochSeconds= new ArrayList<>();
        
        long currentDateTime;
        long minDateTime=0;
        long maxDateTime=1;
        IRI property=null;
        //System.out.println(result.hasNext());
        //We make the TupleQueryResult mutable, so that it can be processed again.
        ((MutableTupleQueryResult) result).beforeFirst();
        System.out.println(result.hasNext());
        OffsetDateTime minDate = null;
        OffsetDateTime maxDate = null;
        Long min=(long) 0;
        while (result.hasNext()) {
        	
            add=add+1;
            BindingSet bindingSet = result.next();
            //IRI p1 = (IRI) bindingSet.getBinding("Activity").getValue();
            Value measurement = bindingSet.getBinding("value").getValue();
            str=measurement.stringValue();
            what = what + Float.parseFloat(str);
            
            //System.out.println(add + " check " + what);
            Value dateTime = bindingSet.getBinding("time").getValue();
            str2=dateTime.stringValue();
          
            dateTimesInEpochSeconds.add(getDateTimeToEpochSecondsFromString(str2));
            
            property = (IRI) bindingSet.getBinding("property").getValue();
            measuredProperty=property.stringValue();
            //Value p1 = bindingSet.getBinding("property").getValue();

            System.out.println(" || Value: " + measurement.stringValue() + " || Time: " + dateTime.stringValue() + " || Property: " + property.getLocalName());
            System.out.println(dateTimesInEpochSeconds);
            Long max = dateTimesInEpochSeconds.stream().mapToLong(v -> v).max().orElseThrow(NoSuchElementException::new);
            System.out.println(max);
            maxDateTime = max;
            min = dateTimesInEpochSeconds.stream().mapToLong(v -> v).min().orElseThrow(NoSuchElementException::new);
            System.out.println(min);
            minDateTime = min;
            minDate = getDateTimeFromEpochSeconds(min);
            System.out.println(minDate);
            maxDate = getDateTimeFromEpochSeconds(max);
            System.out.println(maxDate);
        }
        float mean;
        mean = what / add;
        if (minDate !=null) {
        	System.out.println("Mean: " + (what/add));
        }
        //Gia na treksoume locally, PREPEI na afairoume tis grammes 1085 kai 1092 kai 1093
        //To +60 mpainei gia na yparxei kapoio normalization stous xronous tou Rolling Average. Poly pithanon na thelei allagh. To periodOfAverage genika thelei optimization.
      //  if ((min !=0) && (min < getCurrentDateTimeToEpochSeconds() - periodOfAverage*60 + 60)) {
        	System.out.println(min);
        	if (minDate != null)
        		updateRollingAverage(property, mean, minDate.toString(), maxDate.toString(), periodOfAverage);
        	else
        		System.out.println("No rolling average was produced, therefore reasoning cannot proceed.");
           
      //  }
      //  else 
       // 	System.out.println("Waiting for more data.");
        result.close();
    }
	
	//Update Rolling Average
	public void updateRollingAverage(IRI property, float RollingAverage, String startDateTime, String endDateTime, int periodOfAverage)
	{
		ValueFactory factory = SimpleValueFactory.getInstance();
        //IRI prop_class = factory.createIRI(Input.NAMESPACE,propertyType);
        Literal start = factory.createLiteral(startDateTime, XSD.DATETIME);
        Literal end = factory.createLiteral(endDateTime, XSD.DATETIME);
        Literal RA = factory.createLiteral(RollingAverage);
        Literal period = factory.createLiteral(periodOfAverage);
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        IRI avg_iri = factory.createIRI(Input.NAMESPACE, uuidAsString);
		String modification=(
				"PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
	        			+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
	        			+"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
				+"DELETE{\n"
					+"?old_ra ?p ?o.\n"
				    +"?s ?p2 ?old_ra.\n"
				+"}\n"
				+"INSERT{\n"
				    +"$avg_iri rdf:type ing:RollingAverage.\n"
				    +"$avg_iri ing:hasValue $new_val.\n"
				    +"$avg_iri ing:hasWindowStart $start_time.\n"
				    +"$avg_iri ing:hasWindowEnd $end_time.\n"
				    +"$avg_iri ing:hasWindowDuration $period.\n"
				    +"?measured_property ing:hasRollingAverage ?avg_iri.\n"
				+"}\n"
				+"WHERE{\n"
				    +"OPTIONAL{\n"
				        +"$measured_property ing:hasRollingAverage ?old_ra.\n"
				        +"?old_ra ?p ?o.\n"
				    	+"?s ?p2 ?old_ra.\n"
				    +"}\n"  
				+"}\n"   
				   );
		
		executeUpdate(connection, modification, new SimpleBinding("period", period), new SimpleBinding("avg_iri", avg_iri), new SimpleBinding("new_val", RA),  new SimpleBinding("start_time", start), new SimpleBinding("end_time", end), new SimpleBinding("measured_property", property));
	}
	
//Dummy Reasoning Rule, not used in any demonstration whatsoever	
public void getAndInsertOxygen(float oxygenLimit, int periodOfAverage) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        
        ValueFactory factory = SimpleValueFactory.getInstance();
        
        
       

        Literal period = factory.createLiteral(periodOfAverage);
        Literal oxyLimit = factory.createLiteral(oxygenLimit);
      
        
        TupleQueryResult result = QueryUtils.evaluateSelectQuery2(connection,
        		
        		     "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
        			+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        			+"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
        			//+"PREFIX : <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
        			+ "SELECT DISTINCT ?value ?frid ?fr ?device_id ?device ?time ?analysis_time\r\n"
        			+ "where { \r\n"
        			+ "    ?avg a ing:RollingAverage.\r\n"
        			+ "    ?avg ing:hasWindowEnd ?time.\r\n"
        			+ "    ?avg ing:hasWindowDuration ?time_limit.\r\n"
        			+ "    ?property ing:hasRollingAverage ?avg.  \r\n"
        			+ "    ?property a ing:BloodOxygen. \r\n"
        			+ "    \r\n"
        			+ "    ?device ing:makesMeasurement ?m.\r\n"
        			+ "    ?m ing:isMeasurementOf ?property.\r\n"
        			+ "    ?device ing:hasEquipmentId ?device_id. \r\n"
        			+ "    \r\n"
        			+ "    ?fr ing:hasVitalSign ?property.\r\n"
        			+ "    ?fr ing:hasFrId ?frid. \r\n"
        			+ "    ?avg ing:hasValue ?value.\r\n"
        			+ "    \r\n"
        			+ "    \r\n"
        			+ "    OPTIONAL{\r\n"
        			+ "        ?analysis a ing:Analysis. \r\n"
        			+ "        ?analysis ing:detects ?heatstroke.\r\n"
        			+ "        ?heatstroke a ing:Heatstroke. \r\n"
        			+ "        ?fr ing:hasPhysiologicalCondition ?heatstroke.\r\n"
        			+ "        ?analysis ing:hasTimeStamp ?analysis_time. \r\n"
        			+ "    }\r\n"
        			+ "    FILTER (?value>?oxyLimit)\r\n"
        			+ "}"
        			    , new SimpleBinding("oxyLimit", oxyLimit), new SimpleBinding("time_limit", period)
        			    );
        //System.out.println(result.hasNext());
        
        while (result.hasNext()) {
        	
            
            BindingSet bindingSet = result.next();
            //IRI p1 = (IRI) bindingSet.getBinding("Activity").getValue();
            Value measurement = bindingSet.getBinding("value").getValue();
            Value frId = bindingSet.getBinding("frid").getValue();
            IRI fr = (IRI) bindingSet.getBinding("fr").getValue();
            Value deviceId = bindingSet.getBinding("device_id").getValue();
            IRI device = (IRI) bindingSet.getBinding("device").getValue();
            Value dateTime = bindingSet.getBinding("time").getValue();
            if (bindingSet.getBinding("analysis_time")!=null) {
            	Value analysisTime = bindingSet.getBinding("analysis_time").getValue();
                System.out.println("Value: " + measurement.stringValue() + " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_ID: " +deviceId.stringValue() + " || DATETIME: " + dateTime.stringValue() + " || Analysis Time: " + analysisTime.stringValue());	
            }
            else {
                System.out.println("Value: " + measurement.stringValue() + " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE: " + device.stringValue() + " || DEVICE_ID: " +deviceId.stringValue() + " || DATETIME: " + dateTime.stringValue());
            }
            IRI analysisIRI = factory.createIRI(Input.NAMESPACE, "Analysis_Heatstroke_" + fr.getLocalName());
            IRI heatstrokeIRI = factory.createIRI(Input.NAMESPACE, "Heatstroke_" + fr.getLocalName());
            //IRI property = (IRI) bindingSet.getBinding("property").getValue();
            //Value p1 = bindingSet.getBinding("property").getValue();
         
            //System.out.println(" || Value: " + measurement.stringValue() + " || Time: " + dateTime.stringValue() + " || Property: " + property.getLocalName());
            String modification=(

     		     	  "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
     		        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
     			    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
          		+ "DELETE{\r\n"
          		+ "        $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
          		+ "    }\r\n"
          		+ "    INSERT {\r\n"
          		+ "        $analysis_iri a ing:Analysis.\r\n"
          		
          		+ "        $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
          		+ "        $analysis_iri ing:hasAnalysisType \"Expert Reasoning\".\r\n"
          		+ "        $analysis_iri ing:detects $heatstroke_iri. \r\n"
          		+ "        $analysis_iri ing:hasDataSource $device_iri.\r\n"
          	
          		+ "        $heatstroke_iri a ing:Heatstroke.\r\n"
          		+ "        $heatstroke_iri a ing:PhysiologicalCondition.\r\n"
          		
          		+ "        $fr_iri ing:hasPhysiologicalCondition $heatstroke_iri.\r\n"
          		+ "    }\r\n"
          		+ "    WHERE{\r\n"
          		+ "    OPTIONAL{\r\n"
          		+ "        $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
          		+ "        }\r\n"
          		+ "    }"
  				);
            long timestamp = getCurrentDateTimeToEpochSeconds();
            String str = getDateTimeFromEpochSeconds(timestamp).toString();
            System.out.println(str);

            Literal timeLimit = factory.createLiteral(str, XSD.DATETIME);
  		executeUpdate(connection, modification, new SimpleBinding("analysis_iri", analysisIRI), new SimpleBinding("fr_iri", fr), new SimpleBinding("device_iri", device),  new SimpleBinding("heatstroke_iri", heatstrokeIRI), new SimpleBinding("timestamp", timeLimit));
          
        }
        
        // Once we are done with a particular result we need to close it
        result.close();
        
    }

//Get and Insert Dehydration Rule results
public void getAndInsertDehydration(float btLimit, float htlimit, int periodOfAverageBT, int periodOfAverageHR) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
    
    ValueFactory factory = SimpleValueFactory.getInstance();
    
    
   

    Literal periodBT = factory.createLiteral(periodOfAverageBT);
    Literal periodHR = factory.createLiteral(periodOfAverageHR);
    Literal btempLimit = factory.createLiteral(btLimit);
    Literal hrateLimit = factory.createLiteral(htlimit);
    
  
    
    TupleQueryResult result = QueryUtils.evaluateSelectQuery2(connection,
    		
    		     "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
    			+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
    			+"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
    			//+"PREFIX : <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
    			+ "select distinct ?fr ?device_bt ?device_hr ?frid ?hr_val ?bt_val ?hr_time ?bt_time ?device_hr_id ?device_bt_id ?analysis_time \r\n"
    			+ "where { \r\n"
    			+ "	?fr a ing:FR.\r\n"
    			+ "    ?fr ing:hasFrId ?frid.\r\n"
    			+ "    \r\n"
    			+ "    ?fr ing:hasVitalSign ?hr.\r\n"
    			+ "  	?hr a ing:HeartRate. \r\n"
    			+ "   	?hr ing:hasRollingAverage ?hr_avg.\r\n"
    			+ "    ?hr_avg ing:hasWindowEnd ?hr_time. \r\n"
    			+ "    ?hr_avg ing:hasWindowDuration ?hr_duration.\r\n"
    			+ "    ?hr_avg ing:hasValue ?hr_val.\r\n"
    			+ "    \r\n"
    			+ "    ?device_hr ing:makesMeasurement ?m_hr.\r\n"
    			+ "    ?m_hr ing:isMeasurementOf ?hr.\r\n"
    			+ "    ?device_hr ing:hasEquipmentId ?device_hr_id. \r\n"
    			+ "    \r\n"
    			+ "    ?fr ing:hasVitalSign ?bt.\r\n"
    			+ "    ?bt a ing:BodyTemperature.\r\n"
    			+ "    ?bt ing:hasRollingAverage ?bt_avg.\r\n"
    			+ "    ?bt_avg ing:hasWindowEnd ?bt_time.  \r\n"
    			+ "	?bt_avg ing:hasWindowDuration $bt_duration.\r\n"
    			+ "    ?bt_avg ing:hasValue ?bt_val.\r\n"
    			+ "    \r\n"
    			+ "    ?device_bt ing:makesMeasurement ?m_bt.\r\n"
    			+ "    ?m_bt ing:isMeasurementOf ?bt.\r\n"
    			+ "    ?device_bt ing:hasEquipmentId ?device_bt_id. \r\n"
    			+ "    \r\n"
    			+ "    OPTIONAL{\r\n"
    			+ "        ?analysis a ing:Analysis. \r\n"
    			+ "        ?analysis ing:detects ?dehydration.\r\n"
    			+ "        ?dehydration a ing:Dehydration. \r\n"
    			+ "        ?fr ing:hasPhysiologicalCondition ?dehydration.\r\n"
    			+ "        ?analysis ing:hasTimeStamp ?analysis_time. \r\n"
    			+ "    }\r\n"
    			+ "    \r\n"
    			+ "    FILTER(?bt_val>$bt_limit && ?hr_val>$hr_limit)\r\n"
    			+ "}"
    			    , new SimpleBinding("bt_limit", btempLimit), new SimpleBinding("hr_limit", hrateLimit), new SimpleBinding("bt_duration", periodBT), new SimpleBinding("hr_duration", periodHR)
    			    );
    //System.out.println(result.hasNext());
    
    while (result.hasNext()) {
    	
        
        BindingSet bindingSet = result.next();
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
        if (bindingSet.getBinding("analysis_time")!=null) {
        	Value analysisTime = bindingSet.getBinding("analysis_time").getValue();
            System.out.println("DEHYDRATION || Body temperature value: " + bt_measurement.stringValue() + " || Heart rate value: " + hr_measurement.stringValue() + " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_BT: " + deviceBT.stringValue() + " || DEVICE_HR: " + deviceHR.stringValue() + " || DEVICE_BT_ID: " +deviceBTId.stringValue() + " || DEVICE_HR_ID: " +deviceHRId.stringValue() + " || DATETIME BT: " + dateTimeBT.stringValue() + " || DATETIME HR: " + dateTimeHR.stringValue() + " || Analysis Time: " + analysisTime.stringValue());	
        }
        else {
            System.out.println("DEHYDRATION || Body temperature value: " + bt_measurement.stringValue() + " || Heart rate value: " + hr_measurement.stringValue() + " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_BT: " + deviceBT.stringValue() + " || DEVICE_HR: " + deviceHR.stringValue() + " || DEVICE_BT_ID: " +deviceBTId.stringValue() + " || DEVICE_HR_ID: " +deviceHRId.stringValue() + " || DATETIME BT: " + dateTimeBT.stringValue() + " || DATETIME HR: " + dateTimeHR.stringValue());	
        }
        IRI analysisIRI = factory.createIRI(Input.NAMESPACE, "Analysis_Dehydration_" + fr.getLocalName());
        IRI dehydrationIRI = factory.createIRI(Input.NAMESPACE, "Dehydration_" + fr.getLocalName());
        //IRI property = (IRI) bindingSet.getBinding("property").getValue();
        //Value p1 = bindingSet.getBinding("property").getValue();
     
        //System.out.println(" || Value: " + measurement.stringValue() + " || Time: " + dateTime.stringValue() + " || Property: " + property.getLocalName());
        String modification=(

 		     	  "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
 		        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
 			    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
      		+ "DELETE{\r\n"
      		+ "            $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
      		+ "        }\r\n"
      		+ "        INSERT {\r\n"
      		+ "            $analysis_iri a ing:Analysis.\r\n"
      		+ "            $analysis_iri ing:hasTimeStamp $timestamp.\r\n"
      		+ "            $analysis_iri ing:hasAnalysisType \"Expert Reasoning\".\r\n"
      		+ "            $analysis_iri ing:detects $dehydration_iri. \r\n"
      		+ "            $analysis_iri ing:hasDataSource $device_iri_hr. \r\n"
      		+ "            $analysis_iri ing:hasDataSource $device_iri_bt. \r\n"
      		+ "        \r\n"
      		+ "            $dehydration_iri a ing:Dehydration.\r\n"
      		+ "            $dehydration_iri a ing:PhysiologicalCondition.\r\n"
      		+ "            $fr_iri ing:hasPhysiologicalCondition $dehydration_iri. \r\n"
      		+ "        }\r\n"
      		+ "        WHERE{\r\n"
      		+ "        OPTIONAL{\r\n"
      		+ "                $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
      		+ "            }\r\n"
      		+ "        }"
				);
        long timestamp = getCurrentDateTimeToEpochSeconds();
        String str = getZonedDateTimeFromEpochSeconds(timestamp).toString();
        System.out.println("Time of Analysis: " + str);

        Literal timeLimit = factory.createLiteral(str, XSD.DATETIME);
        AlertGenerator("Alert", dehydrationIRI.getLocalName(),"event","description","In the damaged block of buildings","urgency", "severity", fr.getLocalName());
		executeUpdate(connection, modification, new SimpleBinding("analysis_iri", analysisIRI), new SimpleBinding("fr_iri", fr), new SimpleBinding("device_iri_hr", deviceBT), new SimpleBinding("device_iri_bt", deviceHR), new SimpleBinding("dehydration_iri", dehydrationIRI), new SimpleBinding("timestamp", timeLimit));
		
      
    }
    
    // Once we are done with a particular result we need to close it
    result.close();
    
}

//Get and Insert Heastroke Rule results
public void getAndInsertHeatstroke(float tempLimit, int periodOfAverage) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
    
    ValueFactory factory = SimpleValueFactory.getInstance();
    
    
   

    Literal period = factory.createLiteral(periodOfAverage);
    Literal heatstrokeLimit = factory.createLiteral(tempLimit);
  
    
    TupleQueryResult result = QueryUtils.evaluateSelectQuery2(connection,
    		
    		     "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
    			+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
    			+"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
    			//+"PREFIX : <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
    			+ "SELECT DISTINCT ?value ?frid ?fr ?device_id ?device ?time ?analysis_time\r\n"
    			+ "where { \r\n"
    			+ "    ?avg a ing:RollingAverage.\r\n"
    			+ "    ?avg ing:hasWindowEnd ?time.\r\n"
    			+ "    ?avg ing:hasWindowDuration ?time_limit.\r\n"
    			+ "    ?property ing:hasRollingAverage ?avg.  \r\n"
    			+ "    ?property a ing:BodyTemperature. \r\n"
    			+ "    \r\n"
    			+ "    ?device ing:makesMeasurement ?m.\r\n"
    			+ "    ?m ing:isMeasurementOf ?property.\r\n"
    			+ "    ?device ing:hasEquipmentId ?device_id. \r\n"
    			+ "    \r\n"
    			+ "    ?fr ing:hasVitalSign ?property.\r\n"
    			+ "    ?fr ing:hasFrId ?frid. \r\n"
    			+ "    ?avg ing:hasValue ?value.\r\n"
    			+ "    \r\n"
    			+ "    \r\n"
    			+ "    OPTIONAL{\r\n"
    			+ "        ?analysis a ing:Analysis. \r\n"
    			+ "        ?analysis ing:detects ?heatstroke.\r\n"
    			+ "        ?heatstroke a ing:Heatstroke. \r\n"
    			+ "        ?fr ing:hasPhysiologicalCondition ?heatstroke.\r\n"
    			+ "        ?analysis ing:hasTimeStamp ?analysis_time. \r\n"
    			+ "    }\r\n"
    			+ "    FILTER (?value>?heatstrokeLimit)\r\n"
    			+ "}"
    			    , new SimpleBinding("heatstrokeLimit", heatstrokeLimit), new SimpleBinding("time_limit", period)
    			    );
    //System.out.println(result.hasNext());
    
    while (result.hasNext()) {
    	
        
        BindingSet bindingSet = result.next();
        //IRI p1 = (IRI) bindingSet.getBinding("Activity").getValue();
        Value measurement = bindingSet.getBinding("value").getValue();
        Value frId = bindingSet.getBinding("frid").getValue();
        IRI fr = (IRI) bindingSet.getBinding("fr").getValue();
        Value deviceId = bindingSet.getBinding("device_id").getValue();
        IRI device = (IRI) bindingSet.getBinding("device").getValue();
        Value dateTime = bindingSet.getBinding("time").getValue();
        if (bindingSet.getBinding("analysis_time")!=null) {
        	Value analysisTime = bindingSet.getBinding("analysis_time").getValue();
            System.out.println("HEATSTROKE || Value: " + measurement.stringValue() + " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE_ID: " +deviceId.stringValue() + " || DATETIME: " + dateTime.stringValue() + " || Analysis Time: " + analysisTime.stringValue());	
        }
        else {
            System.out.println("HEATSTROKE || Value: " + measurement.stringValue() + " || FR: " + fr.stringValue() + " || FR_ID: " + frId.toString() + " || DEVICE: " + device.stringValue() + " || DEVICE_ID: " +deviceId.stringValue() + " || DATETIME: " + dateTime.stringValue());
        }
        IRI analysisIRI = factory.createIRI(Input.NAMESPACE, "Analysis_Heatstroke_" + fr.getLocalName());
        IRI heatstrokeIRI = factory.createIRI(Input.NAMESPACE, "Heatstroke_" + fr.getLocalName());
        //IRI property = (IRI) bindingSet.getBinding("property").getValue();
        //Value p1 = bindingSet.getBinding("property").getValue();
     
        //System.out.println(" || Value: " + measurement.stringValue() + " || Time: " + dateTime.stringValue() + " || Property: " + property.getLocalName());
        String modification=(

 		     	  "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
 		        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
 			    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
      		+ "DELETE{\r\n"
      		+ "        $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
      		+ "    }\r\n"
      		+ "    INSERT {\r\n"
      		+ "        $analysis_iri a ing:Analysis.\r\n"
      		
      		+ "        $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
      		+ "        $analysis_iri ing:hasAnalysisType \"Expert Reasoning\".\r\n"
      		+ "        $analysis_iri ing:detects $heatstroke_iri. \r\n"
      		+ "        $analysis_iri ing:hasDataSource $device_iri.\r\n"
      	
      		+ "        $heatstroke_iri a ing:Heatstroke.\r\n"
      		+ "        $heatstroke_iri a ing:PhysiologicalCondition.\r\n"
      		
      		+ "        $fr_iri ing:hasPhysiologicalCondition $heatstroke_iri. \r\n"
      		+ "    }\r\n"
      		+ "    WHERE{\r\n"
      		+ "    OPTIONAL{\r\n"
      		+ "        $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
      		+ "        }\r\n"
      		+ "    }"
				);
        long timestamp = getCurrentDateTimeToEpochSeconds();
        String str = getDateTimeFromEpochSeconds(timestamp).toString();
        System.out.println("Timestamp of Analysis: " + str);

        Literal timeLimit = factory.createLiteral(str, XSD.DATETIME);
        
        
        if (tempLimit>=41) {
        	AlertGenerator("Alert", heatstrokeIRI.getLocalName(),"FR suffering from severe heatstroke","description","areaDesc","Expected", "Severe", fr.getLocalName());
        }
        else {
        	AlertGenerator("Alert", heatstrokeIRI.getLocalName(),"event","description","areaDesc","Expected", "Moderate", fr.getLocalName());
        }
        
		executeUpdate(connection, modification, new SimpleBinding("analysis_iri", analysisIRI), new SimpleBinding("fr_iri", fr), new SimpleBinding("device_iri", device),  new SimpleBinding("heatstroke_iri", heatstrokeIRI), new SimpleBinding("timestamp", timeLimit));
		
      
    }
    
    // Once we are done with a particular result we need to close it
    result.close();
    
}

//Get and Insert Complex Rule results
public void getandInsertComplexRule(float htlimit, int periodOfAverageHR) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
    
    ValueFactory factory = SimpleValueFactory.getInstance();
    
    
   

    
    Literal periodHR = factory.createLiteral(periodOfAverageHR);
  
    Literal hrateLimit = factory.createLiteral(htlimit);
    
  
    
    TupleQueryResult result = QueryUtils.evaluateSelectQuery2(connection,
    		
    		     "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
    			+"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
    			+"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
    			//+"PREFIX : <http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
    			+ "select distinct ?alert ?fr  ?device_hr ?frid ?hr_val  ?hr_time  ?device_hr_id  ?analysis_time \r\n"
    			+ "where { \r\n"
    			+ "	   ?fr a ing:FR.\r\n"
    			+ "    ?fr ing:hasFrId ?frid.\r\n"
    			+ "    \r\n"
    			+ "    ?fr ing:hasVitalSign ?hr.\r\n"
    			+ "    ?fr ing:isTargetOfAlert ?alert.\r\n"
    			+ "    ?alert ing:showsEvent \"FR immobilized\".\r\n"	
    			+ "    ?hr a ing:HeartRate. \r\n"
    			+ "    ?hr ing:hasRollingAverage ?hr_avg.\r\n"
    			+ "    ?hr_avg ing:hasWindowEnd ?hr_time. \r\n"
    			+ "    ?hr_avg ing:hasWindowDuration ?hr_duration.\r\n"
    			+ "    ?hr_avg ing:hasValue ?hr_val.\r\n"
    			+ "    \r\n"
    			+ "    ?device_hr ing:makesMeasurement ?m_hr.\r\n"
    			+ "    ?m_hr ing:isMeasurementOf ?hr.\r\n"
    			+ "    ?device_hr ing:hasEquipmentId ?device_hr_id. \r\n"
    			+ "    \r\n"
    			+ "    OPTIONAL{\r\n"
    			+ "        ?analysis a ing:Analysis. \r\n"
    			+ "        ?analysis ing:detects ?complex.\r\n"
    			+ "        ?complex a ing:Complex. \r\n"
    			+ "        ?fr ing:hasPhysiologicalCondition ?complex.\r\n"
    			+ "        ?analysis ing:hasTimeStamp ?analysis_time. \r\n"
    			+ "    }\r\n"
    			+ "    \r\n"
    			+ "    FILTER(?hr_val<$hr_limit)\r\n"
    			+ "}"
    			    ,  new SimpleBinding("hr_limit", hrateLimit),  new SimpleBinding("hr_duration", periodHR)
    			    );
    //System.out.println(result.hasNext());
    
    while (result.hasNext()) {
        BindingSet bindingSet = result.next();
        Value hr_measurement = bindingSet.getBinding("hr_val").getValue();
        Value frId = bindingSet.getBinding("frid").getValue();
        IRI fr = (IRI) bindingSet.getBinding("fr").getValue();
        IRI alert = (IRI) bindingSet.getBinding("alert").getValue();
        Value deviceHRId = bindingSet.getBinding("device_hr_id").getValue();
        IRI deviceHR = (IRI) bindingSet.getBinding("device_hr").getValue();
        Value dateTimeHR = bindingSet.getBinding("hr_time").getValue();
        if (bindingSet.getBinding("analysis_time")!=null) {
        	Value analysisTime = bindingSet.getBinding("analysis_time").getValue();
            System.out.println("COMPLEX || Heart rate value: " + hr_measurement.stringValue() + " || FR: " + fr.getLocalName() + " || FR_ID: " + frId.toString() +" || DEVICE_HR: " + deviceHR.stringValue() + " || DEVICE_HR_ID: " +deviceHRId.stringValue() + " || DATETIME HR: " + dateTimeHR.stringValue() + " || Analysis Time: " + analysisTime.stringValue() + "\n Boots Alert ID: " + alert.stringValue());	
        }
        else {
            System.out.println("COMPLEX || Heart rate value: " + hr_measurement.stringValue() + " || FR: " + fr.getLocalName() + " || FR_ID: " + frId.toString() +" || DEVICE_HR: " + deviceHR.stringValue() +" || DEVICE_HR_ID: " +deviceHRId.stringValue() +" || DATETIME HR: " + dateTimeHR.stringValue() + "\n Boots Alert ID: " + alert.stringValue());	
        }
        IRI analysisIRI = factory.createIRI(Input.NAMESPACE, "Analysis_Complex_" + fr.getLocalName());
        IRI complexIRI = factory.createIRI(Input.NAMESPACE, "Complex_" + fr.getLocalName());
        //IRI property = (IRI) bindingSet.getBinding("property").getValue();
        //Value p1 = bindingSet.getBinding("property").getValue();
     
        //System.out.println(" || Value: " + measurement.stringValue() + " || Time: " + dateTime.stringValue() + " || Property: " + property.getLocalName());
        String modification=(

 		     	  "PREFIX ing:<http://www.semanticweb.org/savvas/ontologies/2020/10/untitled-ontology-10#>\r\n"
 		        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
 			    + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n"
      		+ "DELETE{\r\n"
      		+ "            $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
      		+ "        }\r\n"
      		+ "        INSERT {\r\n"
      		+ "            $analysis_iri a ing:Analysis.\r\n"
      		+ "            $analysis_iri ing:hasTimeStamp $timestamp.\r\n"
      		+ "            $analysis_iri ing:hasAnalysisType \"Expert Reasoning\".\r\n"
      		+ "            $analysis_iri ing:detects $complex_iri. \r\n"
      		+ "            $analysis_iri ing:hasDataSource $device_iri_hr. \r\n"
      		+ "			   $analysis_iri ing:hasDataSource ?alert.\r\n"
      		+ "        \r\n"
      		+ "            $complex_iri a ing:Complex.\r\n"
      		+ "            $complex_iri a ing:PhysiologicalCondition.\r\n"
      		+ "            $fr_iri ing:hasPhysiologicalCondition $complex_iri. \r\n"
      		+ "        }\r\n"
      		+ "        WHERE{\r\n"
      		+ "        OPTIONAL{\r\n"
      		+ "                $analysis_iri ing:hasTimeStamp ?timestamp.\r\n"
      		+ "            }\r\n"
      		+ "        }"
				);
        long timestamp = getCurrentDateTimeToEpochSeconds();
        String str = getZonedDateTimeFromEpochSeconds(timestamp).toString();
        System.out.println("Time of Analysis: " + str);

        Literal timeLimit = factory.createLiteral(str, XSD.DATETIME);
        String frName = fr.getLocalName(); 
        String[] split = frName.split("_");
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        //float bodytemp = Float.parseFloat(hr_measurement.stringValue());
        IRI alert_iri = factory.createIRI(Input.NAMESPACE, uuidAsString);

        AlertGenerator("Alert", alert_iri.getLocalName(),"FR Health Status","description","areaDesc","Immediate", "Extreme", split[1]);
       
        
        
		executeUpdate(connection, modification, new SimpleBinding("alert", alert),new SimpleBinding("analysis_iri", analysisIRI), new SimpleBinding("fr_iri", fr), new SimpleBinding("device_iri_hr", deviceHR), new SimpleBinding("complex_iri", complexIRI), new SimpleBinding("timestamp", timeLimit));
      
    }
    
    // Once we are done with a particular result we need to close it
    result.close();
    
}
	
	
	
	
	
	
	

	 
	 public void AlertGenerator(String msgType, String identifier, String event, String description, String areaDesc, String urgency, String severity, String source) throws IOException {
		    FileWriter writer = null;
			JSONParser parser = new JSONParser();
			Object simpleObj = null;
	 
			try {
				writer = new FileWriter(configInstance.getFilepath() + "AlertOutput.json"); // Modify path as per your need
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map<String, Object> properties = new HashMap<String, Object>(1);
			properties.put(JsonGenerator.PRETTY_PRINTING, true);
			JsonGeneratorFactory jgf = Json.createGeneratorFactory(properties);
			JsonGenerator generator = jgf.createGenerator(writer);
			
			long timestamp = getCurrentDateTimeToEpochSeconds();
		      String str = getDateTimeFromEpochSeconds(timestamp).toString();
		      UUID uuid = UUID.randomUUID();
		      String uuidAsString = uuid.toString();
		      
			 generator
	         .writeStartObject()
	         .write("identifier", identifier)
	         .write("sender", "ERE")
	         .write("sent", str)
	         .write("status", "Actual")
	         .write("msgType", msgType)
	         .write("source", source)
	         .write("scope", "Public")
	         .writeStartArray("code")
	         	//.write("to be designed")
	         	//.write("to be designed")
	         	//.write("to be designed")
	         .writeEnd()
	         .writeStartArray("info").writeStartObject()
	         	.write("category", "FR Health Status")
	         	.write("event", event)
	         	.write("urgency", urgency)
	         	.write("severity", severity)
	         	.write("certainty", "Likely")
	         	.write("description", description)
	         	.writeStartArray("area").writeStartObject()
	         		.write("areaDesc", areaDesc)
	         	.writeEnd()
	         	.writeEnd()
	         .writeEnd()
	         .writeEnd()
	         .writeEnd();
			generator.close();
			
			JsonReader reader = new JsonReader(new FileReader(configInstance.getFilepath()+ "AlertOutput.json"));
			reader.setLenient(true);
			JsonElement element = new JsonParser().parse(reader);
			System.out.println(element.toString());
			
			//Producer.sendOutputAlert();
		
	 }

	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
		
		HTTPRepository rep = new HTTPRepository(configInstance.getGraphdb() + "repositories/2");
	
		try (RepositoryConnection con = rep.getConnection()) 
		{
			//con.clear();
			con.begin();
			SemanticIntegration example = new SemanticIntegration(con);
			
			try
			{
				Consumer consumerMeas = new Consumer();
				Consumer consumerRM = new Consumer();
				Consumer consumerBA = new Consumer();
				//example.loadOntology();
				example.clearKBAndLoadOntology();
				
				//An theloume na to treksoume locally, trabame apo File (load from File methods)
				//An theloume na to treksoume plhrws mesw Kafka, kanoume ola ta loads mesw stream kai kanena Produce. 
				//An theloume na treksoume mesw kafka monoi mas, xwris synennohsh me EXUS klp, mporoume na kanoume produce monoi mas (px Producer.sendResourceMap())
				//kai meta antistoixa loadResourceMapFromStream klp. Gia na to kanoume auto, prepei na eimaste syndedemenoi sto VPN. Akoma ki etsi, mporei na exei thema o Server
				//h/kai o Kafka opote mporei na mhn treksei. Kalo einai na mhn asxoloumaste poly me auto to run, mia sto toso mono.
				
				
				
				//Producer.sendResourceMap();
				//example.loadResourceMapFromStream(consumerRM.returnConsumptionOfResourceMap());
				//Producer.sendResourceMap();
				//Producer.sendMeasurements();
				//Producer.sendBootsAlert();
				
				//example.loadResourceMapFromStream(consumerRM.returnConsumptionOfResourceMap());
				//con.commit();
				
				//If we want to run locally, we load the resources like below, once and from file. If we want to run using Kafka, we load the resource
				//map from stream, like above. Then, we proceed to the while loop, which loads the measurements and boots alerts multiple times.
				
				long t1= System.currentTimeMillis();
				long end1 = t1+600000;
				int run1 = 0;
			//	while (System.currentTimeMillis() < end1) {
					System.out.println("run no" + run1);
					example.loadResourceMapFromFile();
					example.loadMeasurementsFromFile();
					example.loadBootsAlertFromFile();
					con.commit();
					//ZOE: AUTO TO COMMIT nomizw DEN EXEI NOHMA, GIATI OI LOAD FUNCTIONS APO PANW KANOUN connection.add. An to ksesxoliasoume outwsiallws skaei
					//con.commit();
					example.calculateRollingAverage("HeartRate", durationOfOneMinute);
					example.getandInsertComplexRule(20, durationOfOneMinute);
					//example.calculateRollingAverage("BodyTemperature", durationOfOneMinute);			
					//example.getAndInsertHeatstroke(heatStrokeLimitBT, durationOfOneMinute);
					//example.loadMeasurementsFromStream(consumerMeas.returnConsumptionOfMeasurements());
					//con.commit();
					//example.loadBootsAlertFromStream(consumerBA.returnConsumptionOfBootsAlert());
					//con.commit();
					
					Thread.sleep(50000);
					example.loadMeasurementsFromFile();
					//con.commit();
					example.calculateRollingAverage("HeartRate", durationOfOneMinute);
					
			   // 	run1=run1+1;
				
				//}
				
				//example.getDateTimeToEpochSecondsFromString("2020-12-21T11:29:47+00:00");
				
				//example.calculateRollingAverage("BloodOxygen", durationOfFiveMinutes);
				//example.getAndInsertOxygen(95, durationOfFiveMinutes);
				long t= System.currentTimeMillis();
				long end = t+600000;
				int run=0;
				
				/*This while loop is needed to run the application using Kafka, so that we get multiple measurements, boots alerts etc. Then the reasoning rules are applied multiple times and alerts are
				produced, if the rules checked are realized. If we want to test locally, we don't use the while loop and only load the needed resources once. Then, we calculate
				rolling averages and check if any rule we would like to check is realized.*/
				
//				while(System.currentTimeMillis() < end) {
//					System.out.println("run no" + run);
//					//example.loadResourceMapFromFile();
//					//example.loadMeasurementsFromFile();
//					
//					example.loadMeasurementsFromStream(consumerMeas.returnConsumptionOfMeasurements());
//					example.loadBootsAlertFromStream(consumerBA.returnConsumptionOfBootsAlert());
//					
//					//KB Population ends, Reasoning Rules begin
//					
//					//con.commit();
//					
//					//example.calculateRollingAverage("BodyTemperature", durationOfOneMinute);
//					//example.getAndInsertHeatstroke(heatStrokeLimitBT, durationOfOneMinute);
//					//example.calculateRollingAverage("BodyTemperature", durationOfFiveMinutes);
//					//example.calculateRollingAverage("HeartRate", durationOfFiveMinutes);
//					//example.getAndInsertDehydration(dehydrationLimitBT, dehydrationLimitHR, durationOfFiveMinutes, durationOfFiveMinutes);
//					example.calculateRollingAverage("HeartRate", durationOfOneMinute);
//					example.getandInsertComplexRule(20, durationOfOneMinute);
//					Thread.sleep(18000);
//					run=run+1;
//				}
			}
			finally
			{
				con.close();
			}}}}