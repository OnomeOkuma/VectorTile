package com.vectortile.tests;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vectortile.EnvelopOps;

public class EnvelopOpsTests {
	
	@Test
	public void getEnvelopeFromXYZTest() {
		
		Envelope test = new Envelope(-180.0, 180.0, 85.0511287798066, -85.0511287798066);
		
		Envelope envelope = EnvelopOps.getEnvelopeFromXYZ(0, 0, 0);
		
		assertTrue(test.equals(envelope));
	}
	
	@Test
	public void changeEnvelopeCRSTest() throws Exception{
		// Desired result in EPSG:3857
		Envelope test = new Envelope(-2.0037508342789244E7, 2.0037508342789244E7, -2.0037508342789255E7, 2.0037508342789244E7);
		
		//Source Envelope in EPSG:4326
		Envelope source = new Envelope(-180.0, 180.0, 85.0511287798066, -85.0511287798066);
		
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857",true);
		Envelope result = EnvelopOps.changeEnvelopeCRS(source, targetCRS);
		
		assertTrue(result.equals(test));
	}
	
	@Test
	public void findAllFeaturesThatIntersectsTest() throws Exception{
		URL file = getClass().getClassLoader().getResource("Building3.shp");
		
		ShapefileDataStore dataStore = new ShapefileDataStore(file);
		
		//Source Envelope in EPSG:3857
		Envelope test = new Envelope(-2.0037508342789244E7, 2.0037508342789244E7, -2.0037508342789255E7, 2.0037508342789244E7);	
		FeatureCollection<? extends FeatureType, ? extends Feature> features = EnvelopOps.findAllFeaturesThatIntersects(dataStore.getFeatureSource(), test);
		
		assertTrue(features.size() == 17);
		
		//Source Envelope in EPSG:4326
		test = new Envelope(-180.0, 180.0, 85.0511287798066, -85.0511287798066);
		features = EnvelopOps.findAllFeaturesThatIntersects(dataStore.getFeatureSource(), test);
		
		assertTrue(features.size() == 17);
		
		test = new Envelope(3.1628, 3.2064, 6.4689, 6.4945);
		features = EnvelopOps.findAllFeaturesThatIntersects(dataStore.getFeatureSource(), test);
		
		assertTrue(features.size() == 0);
		
		dataStore.dispose();
	}
	
	
}