package io.liveoak.vertx.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Bob McWhirter
 */
public class VertxObjectResource extends AbstractVertxResource {

    public VertxObjectResource(Resource parent, String id, Vertx vertx, JsonObject state) {
        super( parent, id, vertx );
        this.state = state;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) {
        for ( String name : state.getFieldNames() ) {
            sink.accept( name, state.getField( name ) );
        }

        sink.close();
    }

    private JsonObject state;
}