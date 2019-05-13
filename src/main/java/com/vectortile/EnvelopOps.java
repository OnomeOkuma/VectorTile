package com.vectortile;

import java.io.IOException;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class EnvelopOps {

	public static Envelope getEnvelopeFromXYZ(int zoomLevel, int tileXcoord, int tileYcoord) {

		double newTileXminCoord = (tileXcoord / Math.pow(2, zoomLevel) * 360 - 180);
		double newTileXmaxCoord = ((tileXcoord + 1) / Math.pow(2, zoomLevel) * 360 - 180);

		double radiansToDegree = 180 / Math.PI;
		double value = Math.PI - 2 * Math.PI * tileYcoord / Math.pow(2, zoomLevel);
		double newTileYminCoord = radiansToDegree * Math.atan(0.5 * (Math.exp(value) - Math.exp(-value)));

		value = Math.PI - 2 * Math.PI * (tileYcoord + 1) / Math.pow(2, zoomLevel);
		double newTileYmaxCoord = radiansToDegree * Math.atan(0.5 * (Math.exp(value) - Math.exp(-value)));

		return new Envelope(newTileXminCoord, newTileXmaxCoord, newTileYminCoord, newTileYmaxCoord);

	}

	public static Envelope changeEnvelopeCRS(Envelope envelope, CoordinateReferenceSystem targetCRS)
			throws NoSuchAuthorityCodeException, FactoryException, TransformException {
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326", true);
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

		Envelope result = JTS.transform(envelope, transform);
		return result;
	}

	public static FeatureCollection<? extends FeatureType, ? extends Feature> findAllFeaturesThatIntersects(FeatureSource<?, ?> featureSource,
			Envelope envelop) throws CQLException, IOException {

		String geometricAttribute = featureSource.getSchema().getGeometryDescriptor().getLocalName();

		String query = "BBOX(" + geometricAttribute + ", " + envelop.getMinX() + ", " + envelop.getMinY() + ", "
				+ envelop.getMaxX() + ", " + envelop.getMaxY() + ")";

		Filter filter = CQL.toFilter(query);
		return featureSource.getFeatures(filter);
	}

}
