package com.vectortile.tests;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.jdbc.JDBCDataStore;
import org.junit.BeforeClass;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class TileTest {
	
	private BasicDataSource datasource;
	
	@BeforeClass
	public void setUp() throws IOException {
		datasource = new BasicDataSource();
		datasource.setDriverClassName("org.h2.Driver");
		datasource.setUrl("jdbc:h2:mem:TileTest;DB_CLOSE_DELAY=-1");
		
		URL file = getClass().getClassLoader().getResource("Building3.shp");
		
		ShapefileDataStore store = new ShapefileDataStore(file);
		
		JDBCDataStore datastore = new JDBCDataStore();
		datastore.setDataSource(datasource);
		
		Transaction transaction = new DefaultTransaction();
		FeatureWriter<SimpleFeatureType, SimpleFeature> writer = 
									datastore.getFeatureWriter(store.getFeatureSource().toString(), transaction);
		

	}
}
