package ingenious;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import ingenious.utils.ConfigsLoader;

//Kafka Producer class. O Producer, kanei get kapoio .json (me ta get methods) kai me to antistoixo send method stelnei 
//to .json sto topic pou theloume, px ProducerRecord<String, String> record = new ProducerRecord<String, String>("resource_map", getResourceMap()); stelnei sto "resource_map"

public class Producer {

	public static void main(String[] args) {
		sendResourceMap();
		sendMeasurements();
	}
	
	static ConfigsLoader configInstance;
	
	static {
		configInstance = ConfigsLoader.getInstance();
		configInstance.loadProperties();
	}
	
	static void sendResourceMap() {
		
		String bootstrapServers = "192.168.30.202:14200";
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		
		KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
		ProducerRecord<String, String> record = new ProducerRecord<String, String>("resource_map", getResourceMap());
		//COPResourcesTopic
		//SOCIAL_MEDIA_APP_TOPIC
		producer.send(record, new Callback() {
			public void onCompletion(RecordMetadata recordMetadata, Exception e) {  
		        Logger logger = LoggerFactory.getLogger(Producer.class);  
		        if (e== null) {  
		            logger.info("Successfully received the details as: \n" +  
			                    "Topic:" + recordMetadata.topic() + "\n" +  
			                    "Partition:" + recordMetadata.partition() + "\n" +  
			                    "Offset" + recordMetadata.offset() + "\n" +  
			                    "Timestamp" + recordMetadata.timestamp());  
		         } else {  
		            logger.error("Can't produce,getting error",e);  
		         }  
		    }  
		});
		producer.flush();
		producer.close();
	}
	
static void sendMeasurements() {
		
		String bootstrapServers = "192.168.30.202:14200";
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		
		KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
		ProducerRecord<String, String> record = new ProducerRecord<String, String>("ingenious-observations-test", getMeasurements());
		//COPResourcesTopic
		//SOCIAL_MEDIA_APP_TOPIC
		producer.send(record, new Callback() {
			public void onCompletion(RecordMetadata recordMetadata, Exception e) {  
		        Logger logger = LoggerFactory.getLogger(Producer.class);  
		        if (e== null) {  
		            logger.info("Successfully received the details as: \n" +  
			                    "Topic:" + recordMetadata.topic() + "\n" +  
			                    "Partition:" + recordMetadata.partition() + "\n" +  
			                    "Offset" + recordMetadata.offset() + "\n" +  
			                    "Timestamp" + recordMetadata.timestamp());  
		         } else {  
		            logger.error("Can't produce,getting error",e);  
		         }  
		    }  
		});
		producer.flush();
		producer.close();
	}

static void sendBootsAlert() {
	
	String bootstrapServers = "192.168.30.202:14200";
	Properties properties = new Properties();
	properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
	properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
	
	
	KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
	ProducerRecord<String, String> record = new ProducerRecord<String, String>("ingenious-events-test", getBootsAlert());
	//COPResourcesTopic
	//SOCIAL_MEDIA_APP_TOPIC
	producer.send(record, new Callback() {
		public void onCompletion(RecordMetadata recordMetadata, Exception e) {  
	        Logger logger = LoggerFactory.getLogger(Producer.class);  
	        if (e== null) {  
	            logger.info("Successfully received the details as: \n" +  
		                    "Topic:" + recordMetadata.topic() + "\n" +  
		                    "Partition:" + recordMetadata.partition() + "\n" +  
		                    "Offset" + recordMetadata.offset() + "\n" +  
		                    "Timestamp" + recordMetadata.timestamp());  
	         } else {  
	            logger.error("Can't produce,getting error",e);  
	         }  
	    }  
	});
	producer.flush();
	producer.close();
}

static void sendOutputAlert() {
	
	String bootstrapServers = "192.168.30.202:14200";
	Properties properties = new Properties();
	properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
	properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
	
	
	KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
	ProducerRecord<String, String> record = new ProducerRecord<String, String>("ingenious-alerts", getOutputAlert());
	//COPResourcesTopic
	//SOCIAL_MEDIA_APP_TOPIC
	producer.send(record, new Callback() {
		public void onCompletion(RecordMetadata recordMetadata, Exception e) {  
	        Logger logger = LoggerFactory.getLogger(Producer.class);  
	        if (e== null) {  
	            logger.info("Successfully received the details as: \n" +  
		                    "Topic:" + recordMetadata.topic() + "\n" +  
		                    "Partition:" + recordMetadata.partition() + "\n" +  
		                    "Offset" + recordMetadata.offset() + "\n" +  
		                    "Timestamp" + recordMetadata.timestamp());  
	         } else {  
	            logger.error("Can't produce,getting error",e);  
	         }  
	    }  
	});
	producer.flush();
	producer.close();
}
	
	private static String getResourceMap() {
		//MongoClient mongoClient = new MongoClient();
		//MongoDatabase db = mongoClient.getDatabase("ingenious_events");
		//MongoCollection<Document> collection = db.getCollection("events");
		
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(configInstance.getFilepath() +  "ResourceMap.json"));
			reader.setLenient(true);
			JsonElement element = new JsonParser().parse(reader);
			return element.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = "problem";
		return str;
		//Document doc = element.toString();
		
		
	}
	
	private static String getMeasurements() {
		//MongoClient mongoClient = new MongoClient();
		//MongoDatabase db = mongoClient.getDatabase("ingenious_events");
		//MongoCollection<Document> collection = db.getCollection("events");
		
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(configInstance.getFilepath() + "Measurements.json"));
			reader.setLenient(true);
			JsonElement element = new JsonParser().parse(reader);
			return element.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = "problem";
		return str;
		//Document doc = element.toString();
		
		
	}
	
	private static String getBootsAlert() {
		//MongoClient mongoClient = new MongoClient();
		//MongoDatabase db = mongoClient.getDatabase("ingenious_events");
		//MongoCollection<Document> collection = db.getCollection("events");
		
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(configInstance.getFilepath() + "BootsAlert.json"));
			reader.setLenient(true);
			JsonElement element = new JsonParser().parse(reader);
			return element.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = "problem";
		return str;
		//Document doc = element.toString();
	}
	
	private static String getOutputAlert() {
		//MongoClient mongoClient = new MongoClient();
		//MongoDatabase db = mongoClient.getDatabase("ingenious_events");
		//MongoCollection<Document> collection = db.getCollection("events");
		
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(configInstance.getFilepath() +  "AlertOutput.json"));
			reader.setLenient(true);
			JsonElement element = new JsonParser().parse(reader);
			return element.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = "problem";
		return str;
		//Document doc = element.toString();
	}
}