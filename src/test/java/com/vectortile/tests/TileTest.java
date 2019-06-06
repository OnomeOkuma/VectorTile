package com.vectortile.tests;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.h2.H2Dialect;
import org.geotools.data.h2.H2DialectBasic;
import org.geotools.data.h2.H2DialectPrepared;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.jdbc.BasicSQLDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.PreparedStatementSQLDialect;
import org.geotools.util.factory.Hints;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;

public class TileTest {
	
	private BasicDataSource datasource;
	
	@Before
	public void setUp() throws IOException {
		datasource = new BasicDataSource();
		datasource.setDriverClassName("org.h2.Driver");
		datasource.setUrl("jdbc:h2:mem:TileTest;DB_CLOSE_DELAY=-1");
		datasource.setUsername("sa");
		
		URL file = getClass().getClassLoader().getResource("Building3.shp");
		
		ShapefileDataStore store = new ShapefileDataStore(file);
		
		JDBCDataStore datastore = new JDBCDataStore();
		datastore.setSQLDialect(new H2Dialect(datastore));
		datastore.setDataSource(datasource);
		datastore.createSchema(store.getSchema());
		
		Transaction transaction = new DefaultTransaction();
		
		ContentFeatureStore featureStore = (ContentFeatureStore) datastore.getFeatureSource(store.getSchema().getTypeName());
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = store.getFeatureReader();
		DefaultFeatureCollection collection = new DefaultFeatureCollection();
		
		while(reader.hasNext()) {
			SimpleFeature feature = reader.next();
			collection.add(feature);
			
		}
		
		featureStore.addFeatures((FeatureCollection<SimpleFeatureType, SimpleFeature>)collection);
		transaction.commit();
	}
	
	@Test
	public void test() {
		
	}
}
