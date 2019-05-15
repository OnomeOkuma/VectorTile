package com.vectortile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
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

public class VectorTile {

	private FeatureSource<?, ?> featureStore;
	private VectorTileEncoder encoder;

	public VectorTile(FeatureSource<?, ?> featureStore) {
		this.featureStore = featureStore;
		this.encoder = new VectorTileEncoder();
	}

	@SuppressWarnings("unchecked")
	public byte[] getVectorTile(int zoomLevel, int tileXCoord, int tileYCoord) throws Exception {

		/*
		 * Calculate the Bounds of the Envelope from the zoomlevel, x-coordinate and
		 * y-coordinate of the tile. This Envelope is in EPSG:4326
		 * CoordinateReferenceSystem.
		 */
		Envelope envelope = EnvelopOps.getEnvelopeFromXYZ(zoomLevel, tileXCoord, tileYCoord);

		// Convert it to the CoordinateReferenceSystem of the FeatureStore.
		Envelope targetCRSEnvelope = EnvelopOps.changeEnvelopeCRS(envelope, this.featureStore.getSchema().getCoordinateReferenceSystem());

		// Get all the features that intersects with the bounding box.
		FeatureCollection<? extends FeatureType, ? extends Feature> features = EnvelopOps.findAllFeaturesThatIntersects(featureStore, targetCRSEnvelope);
		
		features = new ReprojectingFeatureCollection((FeatureCollection<SimpleFeatureType, SimpleFeature>) features, CRS.decode("EPSG:3857",true));
		FeatureIterator<?> iterator = features.features();
		
		envelope = EnvelopOps.changeEnvelopeCRS(envelope, CRS.decode("EPSG:3857",true));

		while (iterator.hasNext()) {
			Feature feature = iterator.next();
			Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
			Map<String, String> attributes = new HashMap<String, String>();
			
			// Transform the CRS of the geometry to pixel coordinates.
	        AffineTransformation transform = new AffineTransformation();
	        double xOffset = -envelope.getMinX();
	        double yOffset = -envelope.getMinY();

	        transform.translate(xOffset, yOffset);
	        transform.scale(1d / (envelope.getWidth() / (double) 256),
	                -1d / (envelope.getHeight() / (double) 256));
	        transform.translate(0d, (double) 256);
	        
	        geometry = transform.transform(geometry);
	        
			Collection<Property> properties = feature.getProperties();
			for (Property property : properties) {
				if (property.getName().getLocalPart() != null && property.getValue() != null) {
					attributes.put(property.getName().getLocalPart(), property.getValue().toString());
				}
			}
			
			this.encoder.addFeature(this.featureStore.getName().getLocalPart(), attributes, geometry);
			
		}
		
		return this.encoder.encode();
	}
	

}
