package com.vectortile.tests;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vectortile.EnvelopOpsUtils;

public class EnvelopOpsTests {
	
	@Test
	public void getEnvelopeFromXYZTest() {
		
		Envelope test = new Envelope(-180.0, 180.0, 85.0511287798066, -85.0511287798066);
		EnvelopOpsUtils utils = new EnvelopOpsUtils();
		
		Envelope envelope = utils.getEnvelopeFromXYZ(0, 0, 0);
		
		assertEquals(test,envelope);
	}
	
	@Test
	public void changeEnvelopeCRSTest() throws NoSuchAuthorityCodeException, FactoryException, TransformException{
		// Desired result in EPSG:3857
		Envelope test = new Envelope(-2.0037508342789244E7, 2.0037508342789244E7, -2.0037508342789255E7, 2.0037508342789244E7);
		
		//Source Envelope in EPSG:4326
		Envelope source = new Envelope(-180.0, 180.0, 85.0511287798066, -85.0511287798066);
		
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857",true);
		EnvelopOpsUtils utils = new EnvelopOpsUtils();
		
		Envelope result = utils.changeEnvelopeCRS(source, targetCRS);
		
		assertEquals(result,test);
	}
	
	@Test
	public void findAllFeaturesThatIntersectsTest() throws CQLException, IOException{
		URL file = getClass().getClassLoader().getResource("Building3.shp");
		
		ShapefileDataStore dataStore = new ShapefileDataStore(file);
		
		EnvelopOpsUtils utils = new EnvelopOpsUtils();
		
		//Source Envelope in EPSG:3857
		Envelope test = new Envelope(-2.0037508342789244E7, 2.0037508342789244E7, -2.0037508342789255E7, 2.0037508342789244E7);	
		FeatureCollection<? extends FeatureType, ? extends Feature> features = utils.findAllFeaturesThatIntersects(dataStore.getFeatureSource(), test);
		
		assertEquals(features.size(),17);
		
		//Source Envelope in EPSG:4326
		test = new Envelope(-180.0, 180.0, 85.0511287798066, -85.0511287798066);
		features = utils.findAllFeaturesThatIntersects(dataStore.getFeatureSource(), test);
		
		assertEquals(features.size(),17);
		
		//Source Envelope in EPSG:4326
		test = new Envelope(3.1628, 3.2064, 6.4689, 6.4945);
		features = utils.findAllFeaturesThatIntersects(dataStore.getFeatureSource(), test);
		
		assertEquals(features.size(),0);
		dataStore.dispose();
	}
	
	
}