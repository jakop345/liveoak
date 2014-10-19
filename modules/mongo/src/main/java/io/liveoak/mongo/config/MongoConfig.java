package io.liveoak.mongo.config;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.PropertyException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoConfig extends EmbeddedConfigResource {

    public static final String DATABASE = "db";
    public static final String DATASTORE = "datastore";

    private String databaseName;
    private String datastoreName;
    private MongoDatastoreResource dataStore;

    private MongoDatastoresRegistry mongoDatastoresResource;

    public MongoConfig(Resource parent, MongoDatastoresRegistry mongoSystemConfigResource, ResourceState resourceState) throws Exception {
        super(parent);
        this.mongoDatastoresResource = mongoSystemConfigResource;
        generateDataStore(resourceState, true);
    }

    @Override
    public void properties(ResourceState configState) throws Exception {
        generateDataStore(configState, false);
    }

    public void generateDataStore(ResourceState resourceState, Boolean init) throws Exception {
        String database = resourceState.getProperty(DATABASE, true, String.class);
        String datastore = resourceState.getProperty(DATASTORE, false, String.class);

        this.databaseName = database;

        if (resourceState.getPropertyNames().contains(DATASTORE) && resourceState.getProperty(DATASTORE) == null) {
            throw new PropertyException("A datastore cannot accept a null value.");
        }

        if (datastore != null && mongoDatastoresResource != null) {
            if (mongoDatastoresResource.getDataStore(datastore) != null || init) {
                this.datastoreName = datastore;
            } else {
                throw new PropertyException("No datastore named '" + datastore + "' exists.");
            }
        } else if (datastore != null && mongoDatastoresResource == null) {
            throw new PropertyException("DataStores not supported for this resource.");
        } else {
            this.dataStore = new MongoDatastoreResource(this, resourceState);
        }
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map properties = new HashMap<>();

        if (datastoreName != null) {
            properties.put(DATASTORE, datastoreName);
        } else {
           properties = dataStore.properties(ctx);
        }

        properties.put(DATABASE, this.databaseName);

        return properties;
    }

    public DB getDB() {
        if (datastoreName != null && mongoDatastoresResource != null) {
            return mongoDatastoresResource.getDataStore(datastoreName).mongoClient.getDB(databaseName);
        } else if (dataStore != null){
            return dataStore.mongoClient.getDB(databaseName);
        } else {
            return null;
        }
    }

    public void close() {
        if (datastoreName == null && dataStore != null) {
            dataStore.mongoClient.close();
        }
    }
}
