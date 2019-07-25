package com.vectortile.tests;

import static org.junit.Assert.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.h2.H2DialectBasic;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.jdbc.JDBCDataStore;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.vectortile.Tile;

public class TileTest {

	private BasicDataSource datasource;

	@Before
	public void setUp() throws IOException, SchemaException {
		datasource = new BasicDataSource();
		datasource.setDriverClassName("org.h2.Driver");
		datasource.setUrl("jdbc:h2:mem:Til;DB_CLOSE_DELAY=-1");
		datasource.setUsername("sa");
		JDBCDataStore datastore = new JDBCDataStore();
		datastore.setDataSource(datasource);
		H2DialectBasic dialect = new H2DialectBasic(datastore);
		datastore.setSQLDialect(dialect);
		
		final SimpleFeatureType TYPE = DataUtilities.createType("Location",
				"the_geom:Point:srid=4326," + "name:String," + "number:Integer");

		datastore.createSchema(TYPE);
		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		double initialLongitude = 3.567;
		double initialLatitude = 6.543;

		// Create 10 features.
		for (int i = 0; i < 10; i++) {
			double latitude = initialLatitude + 0.2;
			double longitude = initialLongitude + 0.2;
			initialLongitude = longitude;
			initialLatitude = latitude;
			Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

			featureBuilder.add(point);
			featureBuilder.add("one point");
			featureBuilder.add(initialLongitude);
			SimpleFeature feature = featureBuilder.buildFeature(null);
			features.add(feature);
		}

		Transaction transaction = new DefaultTransaction("create");

		String typeName = datastore.getTypeNames()[0];
		SimpleFeatureSource featureSource = datastore.getFeatureSource(typeName);

		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(collection);
				transaction.commit();	
			} catch (Exception problem) {
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
		}
	}

	@Test
	public void test() throws NoSuchAuthorityCodeException, CQLException, FactoryException, TransformException, IOException{		
		
		Tile tile = new Tile(datasource, "Location");
		byte[] encodedResult = tile.getVectorTile(0, 0, 0);
		assertNotEquals(encodedResult.length, 0);
	
	}
}
