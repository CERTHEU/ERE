package ingenious;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Kafka consumer class. O Consumer kanei subscribe se ena topic, fainetai parakatw. Etsi kanei consume o,ti yparxei sto topic ekeino (exei ginei dhladh produced sto topic ekeino)
//Gia to resource map kanoume consume apo to "ingenious-resources-test", gia ta Measurements apo to "ingenious-observations-test" kai gia ta Boots alert apo to "ingenious-events-test"
public class Consumer {

	public String returnConsumptionOfResourceMap()
	{
		// TODO Auto-generated method stub
				Logger logger = LoggerFactory.getLogger(Consumer.class.getName());
				String bootstrapServers = "192.168.30.202:14200";
				String grp_id = "resource_map";
				String topic = "resource_map";
				String group_instance ="01";
				//properties
				Properties properties = new Properties();
				properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
				properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, grp_id);
				properties.setProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, group_instance);
				
				properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
				//properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
				
				//String str = String.valueOf(Integer.MAX_VALUE);
				//properties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, str);
				
				KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
		
				consumer.subscribe(Arrays.asList(topic));
				
				while(true){  
		            ConsumerRecords<String,String> records = consumer.poll(Duration.ofMillis(100));  
		            for(ConsumerRecord<String,String> record: records){  
		                logger.info("Key: "+ record.key() + ", Value:" +record.value());  
		                logger.info("Partition:" + record.partition() + ",Offset:" + record.offset()); 
		                System.out.println(record.value());
		                consumer.close();
		                return record.value();
		               
		            }  
		        }
		
		
	}
	
	public String returnConsumptionOfMeasurements()
	{
		// TODO Auto-generated method stub
				Logger logger = LoggerFactory.getLogger(Consumer.class.getName());
				String bootstrapServers = "192.168.30.202:14200";
				String grp_id = "ingenious-obsers-test";
				String topic = "ingenious-observations-test";
				String group_instance ="02";
				//properties
				Properties properties = new Properties();
				properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
				properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, grp_id);
				properties.setProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, group_instance);
				properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
				
				//properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
				//String str = String.valueOf(Integer.MAX_VALUE);
				//properties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, str);
				
				KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
				consumer.subscribe(Arrays.asList(topic));
				
				while(true){  
		            ConsumerRecords<String,String> records = consumer.poll(Duration.ofMillis(100));  
		            for(ConsumerRecord<String,String> record: records){  
		                logger.info("Key: "+ record.key() + ", Value:" +record.value());  
		                logger.info("Partition:" + record.partition() + ",Offset:" + record.offset()); 
		                System.out.println(record.value());
		                consumer.close();
		                return record.value();
		                
		            }  
		            
		        }
				//String debug = "debug";
		
		
	}
	
	public String returnConsumptionOfBootsAlert()
	{
		// TODO Auto-generated method stub
				Logger logger = LoggerFactory.getLogger(Consumer.class.getName());
				String bootstrapServers = "192.168.30.202:14200";
				String grp_id = "test_certh_BA";
				String topic = "ingenious-events-test";
				String group_instance ="04";
				//properties
				Properties properties = new Properties();
				properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
				properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
				properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, grp_id);
				properties.setProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, group_instance);
				properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//				
//				properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
//				String str = String.valueOf(Integer.MAX_VALUE);
//				properties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, str);
				
				KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
				consumer.subscribe(Arrays.asList(topic));
				
				while(true){  
		            ConsumerRecords<String,String> records = consumer.poll(Duration.ofMillis(100));  
		            for(ConsumerRecord<String,String> record: records){  
		                logger.info("Key: "+ record.key() + ", Value:" +record.value());  
		                logger.info("Partition:" + record.partition() + ",Offset:" + record.offset()); 
		                System.out.println(record.value());
		                consumer.close();
		                return record.value();
		                
		            }  
		        }
		
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Logger logger = LoggerFactory.getLogger(Consumer.class.getName());
		String bootstrapServers = "192.168.30.202:14200";
		String grp_id = "test_consumer_certh";
		String topic = "test_resource_ere";
		
		//properties
		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, grp_id);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
		consumer.subscribe(Arrays.asList(topic));
		
		while(true){  
            ConsumerRecords<String,String> records = consumer.poll(Duration.ofMillis(1000));  
            for(ConsumerRecord<String,String> record: records){  
                logger.info("Key: "+ record.key() + ", Value:" +record.value());  
                logger.info("Partition:" + record.partition() + ",Offset:" + record.offset());
                consumer.close();
                System.out.println(record.value());
            }  
        }  
	}

}