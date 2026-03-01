package io.eventstream.opensearch;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class OpenSearchConsumer {
    static Logger log = LoggerFactory.getLogger(OpenSearchConsumer.class.getName());
    public static RestHighLevelClient createOpenSearchClient(){
        {
            String connString = "http://localhost:9200";

            RestHighLevelClient restHighLevelClient;
            URI connUri = URI.create(connString);

            String userInfo = connUri.getUserInfo();

            if (userInfo == null) {
                log.info("User info is null");
                restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), "http")));
            } else {
                log.info("User info is not null");
                String[] auth = userInfo.split(":");

                CredentialsProvider cp = new BasicCredentialsProvider();
                cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(auth[0], auth[1]));

                restHighLevelClient = new RestHighLevelClient(
                        RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), connUri.getScheme()))
                                .setHttpClientConfigCallback(
                                        httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(cp)
                                                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())));


            }

            return restHighLevelClient;
        }
    }

    private static KafkaConsumer<String, String> createKafkaConsumer() {
        String groupId = "ConsumerOpensearchDemo";

        Properties properties = new Properties();
        properties.put("bootstrap.servers","127.0.0.1:9092");

        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());
        properties.put("group.id", groupId);
        properties.put("auto.offset.reset", "latest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return new KafkaConsumer<String,String>(properties);
    }

    private static String extractId(String jsonString) {
        return JsonParser.parseString(jsonString)
                .getAsJsonObject()
                .get("meta")
                .getAsJsonObject()
                .get("id")
                .getAsString();
    }

    public static void main(String[] args) throws IOException {

        RestHighLevelClient openSearchClient = createOpenSearchClient();

        KafkaConsumer<String, String> kafkaConsumer = createKafkaConsumer();

        kafkaConsumer.subscribe(Collections.singleton("wikimedia.recentchange"));

        final Thread mainThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                kafkaConsumer.wakeup();
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try(openSearchClient;kafkaConsumer){
            boolean indexExists = openSearchClient.indices().exists(new GetIndexRequest("wikimedia"), RequestOptions.DEFAULT);

            if(!indexExists){
                CreateIndexRequest createIndexRequest = new CreateIndexRequest("wikimedia");
                openSearchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                log.info("Index has been created");
            }else {
                log.info("Index is already present");
            }

            while(true){
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(3000));
                log.info("Records fetched " + records.count());

                BulkRequest bulkRequest = new BulkRequest();

                for(ConsumerRecord<String, String> record : records){
                    try{
                        String id = extractId(record.value());
                        IndexRequest indexRequest = new IndexRequest("wikimedia").source(record.value(), XContentType.JSON).id(id);
                        //IndexResponse response = openSearchClient.index(indexRequest, RequestOptions.DEFAULT);
                        //log.info("Message Inserted with ID " + response.getId());
                        bulkRequest.add(indexRequest);
                    }catch (Exception e){

                    }
                }
                if(bulkRequest.numberOfActions() > 0){
                    try{
                    BulkResponse bulkResponse = openSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                    log.info("Inserted " + bulkResponse.getItems().length + " record(s)");
                    Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    kafkaConsumer.commitSync();
                    log.info("Offsets committed");
                }
            }

        } catch (WakeupException e){
            log.info("Consumer is starting to shutdown");
        }catch (Exception e){
            log.error(e.getMessage());
            throw e;
        } finally {
            kafkaConsumer.close();
            openSearchClient.close();
            log.info("Consumer and openSearchClient shutdown gracefully");
        }
    }
}
