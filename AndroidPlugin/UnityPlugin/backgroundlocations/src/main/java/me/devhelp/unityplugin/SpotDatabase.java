package me.devhelp.unityplugin;

// Base Stitch Packages
import android.content.Context;
import android.location.Location;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;

// Packages needed to interact with MongoDB and Stitch
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

// Necessary component for working with MongoDB Mobile
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService;

import org.bson.Document;

public class SpotDatabase {
    private MongoCollection<Document> spotsCollection;

    public SpotDatabase(Context context) {
        Stitch.initialize(context);
        StitchAppClient client = Stitch.initializeDefaultAppClient("marble");
        MongoClient mobileClient = client.getServiceClient(LocalMongoDbService.clientFactory);
        spotsCollection = mobileClient.getDatabase("marble").getCollection("spots");
        spotsCollection.createIndex(Indexes.geo2dsphere("geometry"));
    }

    public final void insert(String json) {
        Document doc = Document.parse(json);
        spotsCollection.insertOne(doc);
    }

    public final void upsert(String json) {
        Document doc = Document.parse(json);
        String _id = doc.getString("_id");
        spotsCollection.replaceOne(eq("_id", _id), doc, new ReplaceOptions().upsert(true));
    }

    public final Document getSpotNear(Location location, double distance) {
        Point refPoint = new Point(new Position(location.getLongitude(), location.getLatitude()));
        Document spot = spotsCollection
                .find(nearSphere("geometry", refPoint, distance, 0.0))
                .sort(ascending("geometry"))
                .first();
        return spot;
    }

}
