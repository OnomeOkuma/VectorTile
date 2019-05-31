package com.vectortile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import no.ecc.vectortile.VectorTileEncoder;

public class Tile {

	private FeatureSource<?, ?> featureStore;
	
	private final DataStore datastore;
	
	public Tile(FeatureSource<?, ?> featureStore) {
		this.featureStore = featureStore;
		this.datastore = null;
	}
	
	public Tile(DataSource datasource) {
		JDBCDataStore datastore = new JDBCDataStore();
		datastore.setDataSource(datasource);
		this.datastore = datastore;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized byte[] getVectorTile(int zoomLevel, int tileXCoord, int tileYCoord) throws Exception {
		
		final VectorTileEncoder encoder = new VectorTileEncoder();
		final EnvelopOpsUtils utils = new EnvelopOpsUtils();
		
		/*
		 * Calculate the Bounds of the Envelope from the zoomlevel, x-coordinate and
		 * y-coordinate of the tile. This Envelope is in EPSG:4326
		 * CoordinateReferenceSystem.
		 */
		Envelope envelope = utils.getEnvelopeFromXYZ(zoomLevel, tileXCoord, tileYCoord);

		// Convert it to the CoordinateReferenceSystem of the FeatureStore.
		Envelope targetCRSEnvelope = utils.changeEnvelopeCRS(envelope, this.featureStore.getSchema().getCoordinateReferenceSystem());

		// Get all the features that intersects with the bounding box.
		FeatureCollection<? extends FeatureType, ? extends Feature> features = utils.findAllFeaturesThatIntersects(featureStore, targetCRSEnvelope);
		
		features = new ReprojectingFeatureCollection((FeatureCollection<SimpleFeatureType, SimpleFeature>) features, CRS.decode("EPSG:3857",true));
		FeatureIterator<?> iterator = features.features();
		
		envelope = utils.changeEnvelopeCRS(envelope, CRS.decode("EPSG:3857",true));
		
		// Transform the CRS of the geometry to pixel coordinates.
        final AffineTransformation transform = new AffineTransformation();
        final double xOffset = -envelope.getMinX();
        final double yOffset = -envelope.getMinY();

        transform.translate(xOffset, yOffset);
        transform.scale(1d / (envelope.getWidth() / (double) 256),
                -1d / (envelope.getHeight() / (double) 256));
        transform.translate(0d, (double) 256);
        
		while (iterator.hasNext()) {
			Feature feature = iterator.next();
			Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
			Map<String, String> attributes = new HashMap<String, String>();
			
	        geometry = transform.transform(geometry);
	        
			Collection<Property> properties = feature.getProperties();
			for (Property property : properties) {
				if (property.getName().getLocalPart() != null && property.getValue() != null) {
					attributes.put(property.getName().getLocalPart(), property.getValue().toString());
				}
			}
			
			encoder.addFeature(this.featureStore.getName().getLocalPart(), attributes, geometry);
			
		}
		
		iterator.close();
		return encoder.encode();
	}
	

}
