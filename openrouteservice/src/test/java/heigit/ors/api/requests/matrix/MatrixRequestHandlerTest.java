package heigit.ors.api.requests.matrix;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.common.DistanceUnit;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.matrix.MatrixServiceSettings;
import heigit.ors.util.HelperFunctions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MatrixRequestHandlerTest {
    private MatrixRequest bareMatrixRequest = new MatrixRequest();
    private MatrixRequest matrixRequest = new MatrixRequest();
    private Coordinate[] coordinates = new Coordinate[3];
    private Double[][] bareCoordinates = new Double[3][];
    private Double[] bareCoordinate1 = new Double[2];
    private Double[] bareCoordinate2 = new Double[2];
    private Double[] bareCoordinate3 = new Double[2];
    private List<List<Double>> listOfBareCoordinatesList = new ArrayList<>();

    private Coordinate coordinate1 = new Coordinate();
    private Coordinate coordinate2 = new Coordinate();
    private Coordinate coordinate3 = new Coordinate();

    private List<List<Double>> maximumLocations;
    private List<List<Double>> minimalLocations;

    @Before
    public void setUp() {
        List<Double> bareCoordinatesList = new ArrayList<>();
        bareCoordinatesList.add(8.681495);
        bareCoordinatesList.add(49.41461);
        listOfBareCoordinatesList.add(bareCoordinatesList);
        bareCoordinatesList = new ArrayList<>();
        bareCoordinatesList.add(8.686507);
        bareCoordinatesList.add(49.41943);
        listOfBareCoordinatesList.add(bareCoordinatesList);
        bareCoordinatesList = new ArrayList<>();
        bareCoordinatesList.add(8.687872);
        bareCoordinatesList.add(49.420318);
        listOfBareCoordinatesList.add(bareCoordinatesList);

        bareCoordinate1[0] = 8.681495;
        bareCoordinate1[1] = 49.41461;
        bareCoordinate2[0] = 8.686507;
        bareCoordinate2[1] = 49.41943;
        bareCoordinate3[0] = 8.687872;
        bareCoordinate3[1] = 49.420318;
        bareCoordinates[0] = bareCoordinate1;
        bareCoordinates[1] = bareCoordinate2;
        bareCoordinates[2] = bareCoordinate3;
        coordinate1.x = 8.681495;
        coordinate1.y = 49.41461;
        coordinate2.x = 8.686507;
        coordinate2.y = 49.41943;
        coordinate3.x = 8.687872;
        coordinate3.y = 49.420318;
        coordinates[0] = coordinate1;
        coordinates[1] = coordinate2;
        coordinates[2] = coordinate3;
        matrixRequest.setResolveLocations(true);
        matrixRequest.setMetrics(MatrixMetricsType.Duration);
        matrixRequest.setSources(coordinates);
        matrixRequest.setDestinations(coordinates);
        matrixRequest.setProfileType(RoutingProfileType.CYCLING_REGULAR);
        matrixRequest.setUnits(DistanceUnit.Meters);
        bareMatrixRequest.setSources(coordinates);
        bareMatrixRequest.setDestinations(coordinates);

        // Fake locations to test maximum exceedings

        maximumLocations = HelperFunctions.fakeListLocations(MatrixServiceSettings.getMaximumLocations(false) + 1, 2);
        minimalLocations = HelperFunctions.fakeListLocations(1, 2);
    }

    @Test
    public void convertMatrixRequestTest() throws StatusCodeException {
        heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new heigit.ors.api.requests.matrix.MatrixRequest(bareCoordinates);
        springMatrixRequest.setProfile(APIEnums.MatrixProfile.DRIVING_CAR);
        List<MatrixRequest> matrixRequests = MatrixRequestHandler.convertMatrixRequest(springMatrixRequest);
        Assert.assertEquals(1, matrixRequests.size());
        Assert.assertEquals(1, matrixRequests.get(0).getProfileType());
        Assert.assertEquals(3, matrixRequests.get(0).getSources().length);
        Assert.assertEquals(3, matrixRequests.get(0).getDestinations().length);
        Assert.assertEquals(1, matrixRequests.get(0).getMetrics());
        Assert.assertNull(matrixRequests.get(0).getWeightingMethod());
        Assert.assertEquals(DistanceUnit.Meters, matrixRequests.get(0).getUnits());
        Assert.assertFalse(matrixRequests.get(0).getResolveLocations());
        Assert.assertFalse(matrixRequests.get(0).getFlexibleMode());
        Assert.assertNull(matrixRequests.get(0).getAlgorithm());
        Assert.assertNull(matrixRequests.get(0).getId());

        springMatrixRequest = new heigit.ors.api.requests.matrix.MatrixRequest(bareCoordinates);
        springMatrixRequest.setProfile(APIEnums.MatrixProfile.DRIVING_CAR);
        String[] metrics = new String[3];
        metrics[0] = "duration";
        metrics[1] = "distance";
        metrics[2] = "weight";
        springMatrixRequest.setMetrics(metrics);
        matrixRequests = MatrixRequestHandler.convertMatrixRequest(springMatrixRequest);
        Assert.assertEquals(3, matrixRequests.size());
        // Test each Request
        // 1
        Assert.assertEquals(1, matrixRequests.get(0).getProfileType());
        Assert.assertEquals(3, matrixRequests.get(0).getSources().length);
        Assert.assertEquals(3, matrixRequests.get(0).getDestinations().length);
        Assert.assertEquals(1, matrixRequests.get(0).getMetrics());
        Assert.assertNull(matrixRequests.get(0).getWeightingMethod());
        Assert.assertEquals(DistanceUnit.Meters, matrixRequests.get(0).getUnits());
        Assert.assertFalse(matrixRequests.get(0).getResolveLocations());
        Assert.assertFalse(matrixRequests.get(0).getFlexibleMode());
        Assert.assertNull(matrixRequests.get(0).getAlgorithm());
        Assert.assertNull(matrixRequests.get(0).getId());
        // 2
        Assert.assertEquals(1, matrixRequests.get(1).getProfileType());
        Assert.assertEquals(3, matrixRequests.get(1).getSources().length);
        Assert.assertEquals(3, matrixRequests.get(1).getDestinations().length);
        Assert.assertEquals(2, matrixRequests.get(1).getMetrics());
        Assert.assertNull(matrixRequests.get(1).getWeightingMethod());
        Assert.assertEquals(DistanceUnit.Meters, matrixRequests.get(1).getUnits());
        Assert.assertFalse(matrixRequests.get(1).getResolveLocations());
        Assert.assertFalse(matrixRequests.get(1).getFlexibleMode());
        Assert.assertNull(matrixRequests.get(1).getAlgorithm());
        Assert.assertNull(matrixRequests.get(1).getId());
        // 3
        Assert.assertEquals(1, matrixRequests.get(2).getProfileType());
        Assert.assertEquals(3, matrixRequests.get(2).getSources().length);
        Assert.assertEquals(3, matrixRequests.get(2).getDestinations().length);
        Assert.assertEquals(4, matrixRequests.get(2).getMetrics());
        Assert.assertNull(matrixRequests.get(2).getWeightingMethod());
        Assert.assertEquals(DistanceUnit.Meters, matrixRequests.get(2).getUnits());
        Assert.assertFalse(matrixRequests.get(2).getResolveLocations());
        Assert.assertFalse(matrixRequests.get(2).getFlexibleMode());
        Assert.assertNull(matrixRequests.get(2).getAlgorithm());
        Assert.assertNull(matrixRequests.get(2).getId());

    }

    @Test(expected = ParameterValueException.class)
    public void invalidLocationsTest() throws StatusCodeException {
        heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new heigit.ors.api.requests.matrix.MatrixRequest();
        springMatrixRequest.setProfile(APIEnums.MatrixProfile.DRIVING_CAR);
        MatrixRequestHandler.convertMatrixRequest(springMatrixRequest);
    }

    @Test(expected = ParameterValueException.class)
    public void invalidMetricsTest() throws StatusCodeException {
        heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new heigit.ors.api.requests.matrix.MatrixRequest();
        springMatrixRequest.setProfile(APIEnums.MatrixProfile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setMetrics(new String[0]);
        MatrixRequestHandler.convertMatrixRequest(springMatrixRequest);
    }

    @Test(expected = ParameterValueException.class)
    public void invalidSourceIndexTest() throws StatusCodeException {
        heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new heigit.ors.api.requests.matrix.MatrixRequest();
        springMatrixRequest.setProfile(APIEnums.MatrixProfile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setSources(new String[]{"foo"});
        MatrixRequestHandler.convertMatrixRequest(springMatrixRequest);
    }

    @Test(expected = ParameterValueException.class)
    public void invalidDestinationIndexTest() throws StatusCodeException {
        heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new heigit.ors.api.requests.matrix.MatrixRequest();
        springMatrixRequest.setProfile(APIEnums.MatrixProfile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setSources(new String[]{"all"});
        springMatrixRequest.setDestinations(new String[]{"foo"});
        MatrixRequestHandler.convertMatrixRequest(springMatrixRequest);
    }

    @Test(expected = ParameterValueException.class)
    public void invalidHasUnitsTest() throws StatusCodeException {
        heigit.ors.api.requests.matrix.MatrixRequest springMatrixRequest = new heigit.ors.api.requests.matrix.MatrixRequest();
        springMatrixRequest.setProfile(APIEnums.MatrixProfile.DRIVING_CAR);
        springMatrixRequest.setLocations(listOfBareCoordinatesList);
        springMatrixRequest.setSources(new String[]{"all"});
        springMatrixRequest.setDestinations(new String[]{"all"});
        springMatrixRequest.setUnits("foo");
        MatrixRequestHandler.convertMatrixRequest(springMatrixRequest);
    }

    @Test(expected = ParameterValueException.class)
    public void convertMetricsTest() throws ParameterValueException {
        Assert.assertEquals(1, MatrixRequestHandler.convertMetrics("Duration"));
        Assert.assertEquals(2, MatrixRequestHandler.convertMetrics("Distance"));
        Assert.assertEquals(4, MatrixRequestHandler.convertMetrics("Weight"));

        MatrixRequestHandler.convertMetrics("false-foo");
    }

    @Test(expected = ParameterValueException.class)
    public void notEnoughLocationsTest() throws ParameterValueException {
        MatrixRequestHandler.convertLocations(minimalLocations);
    }

    @Test(expected = ParameterValueException.class)
    public void maximumExceedingLocationsTest() throws ParameterValueException {
        MatrixRequestHandler.convertLocations(maximumLocations);
    }

    @Test
    public void convertLocationsTest() throws ParameterValueException {
        Coordinate[] coordinates = MatrixRequestHandler.convertLocations(listOfBareCoordinatesList);
        Assert.assertEquals(8.681495, coordinates[0].x, 0);
        Assert.assertEquals(49.41461, coordinates[0].y, 0);
        Assert.assertEquals(Double.NaN, coordinates[0].z, 0);
        Assert.assertEquals(8.686507, coordinates[1].x, 0);
        Assert.assertEquals(49.41943, coordinates[1].y, 0);
        Assert.assertEquals(Double.NaN, coordinates[1].z, 0);
        Assert.assertEquals(8.687872, coordinates[2].x, 0);
        Assert.assertEquals(49.420318, coordinates[2].y, 0);
        Assert.assertEquals(Double.NaN, coordinates[2].z, 0);
    }

    @Test
    public void convertSingleLocationCoordinateTest() throws ParameterValueException {
        List<Double> locationsList = new ArrayList<>();
        locationsList.add(8.681495);
        locationsList.add(49.41461);
        Coordinate coordinates = MatrixRequestHandler.convertSingleLocationCoordinate(locationsList);
        Assert.assertEquals(8.681495, coordinates.x, 0);
        Assert.assertEquals(49.41461, coordinates.y, 0);
        Assert.assertEquals(Double.NaN, coordinates.z, 0);
    }

    @Test(expected = ParameterValueException.class)
    public void convertWrongSingleLocationCoordinateTest() throws ParameterValueException {
        List<Double> locationsList = new ArrayList<>();
        locationsList.add(8.681495);
        locationsList.add(49.41461);
        locationsList.add(123.0);
        MatrixRequestHandler.convertSingleLocationCoordinate(locationsList);

    }

    @Test
    public void convertSourcesTest() throws ParameterValueException {
        String[] emptySources = new String[0];
        Coordinate[] convertedSources = MatrixRequestHandler.convertSources(emptySources, this.coordinates);
        Assert.assertEquals(8.681495, convertedSources[0].x, 0);
        Assert.assertEquals(49.41461, convertedSources[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[0].z, 0);
        Assert.assertEquals(8.686507, convertedSources[1].x, 0);
        Assert.assertEquals(49.41943, convertedSources[1].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[1].z, 0);
        Assert.assertEquals(8.687872, convertedSources[2].x, 0);
        Assert.assertEquals(49.420318, convertedSources[2].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[2].z, 0);

        String[] allSources = new String[]{"all"};
        convertedSources = MatrixRequestHandler.convertSources(allSources, this.coordinates);
        Assert.assertEquals(8.681495, convertedSources[0].x, 0);
        Assert.assertEquals(49.41461, convertedSources[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[0].z, 0);
        Assert.assertEquals(8.686507, convertedSources[1].x, 0);
        Assert.assertEquals(49.41943, convertedSources[1].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[1].z, 0);
        Assert.assertEquals(8.687872, convertedSources[2].x, 0);
        Assert.assertEquals(49.420318, convertedSources[2].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[2].z, 0);

        String[] secondSource = new String[]{"1"};
        convertedSources = MatrixRequestHandler.convertSources(secondSource, this.coordinates);
        Assert.assertEquals(8.686507, convertedSources[0].x, 0);
        Assert.assertEquals(49.41943, convertedSources[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedSources[0].z, 0);
    }

    @Test(expected = ParameterValueException.class)
    public void convertWrongSourcesTest() throws ParameterValueException {
        String[] wrongSource = new String[]{"foo"};
        MatrixRequestHandler.convertSources(wrongSource, this.coordinates);
    }

    @Test
    public void convertDestinationsTest() throws ParameterValueException {
        String[] emptyDestinations = new String[0];
        Coordinate[] convertedDestinations = MatrixRequestHandler.convertDestinations(emptyDestinations, this.coordinates);
        Assert.assertEquals(8.681495, convertedDestinations[0].x, 0);
        Assert.assertEquals(49.41461, convertedDestinations[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[0].z, 0);
        Assert.assertEquals(8.686507, convertedDestinations[1].x, 0);
        Assert.assertEquals(49.41943, convertedDestinations[1].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[1].z, 0);
        Assert.assertEquals(8.687872, convertedDestinations[2].x, 0);
        Assert.assertEquals(49.420318, convertedDestinations[2].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[2].z, 0);

        String[] allDestinations = new String[]{"all"};
        convertedDestinations = MatrixRequestHandler.convertDestinations(allDestinations, this.coordinates);
        Assert.assertEquals(8.681495, convertedDestinations[0].x, 0);
        Assert.assertEquals(49.41461, convertedDestinations[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[0].z, 0);
        Assert.assertEquals(8.686507, convertedDestinations[1].x, 0);
        Assert.assertEquals(49.41943, convertedDestinations[1].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[1].z, 0);
        Assert.assertEquals(8.687872, convertedDestinations[2].x, 0);
        Assert.assertEquals(49.420318, convertedDestinations[2].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[2].z, 0);

        String[] secondDestination = new String[]{"1"};
        convertedDestinations = MatrixRequestHandler.convertDestinations(secondDestination, this.coordinates);
        Assert.assertEquals(8.686507, convertedDestinations[0].x, 0);
        Assert.assertEquals(49.41943, convertedDestinations[0].y, 0);
        Assert.assertEquals(Double.NaN, convertedDestinations[0].z, 0);
    }

    @Test(expected = ParameterValueException.class)
    public void convertWrongDestinationsTest() throws ParameterValueException {
        String[] wrongDestinations = new String[]{"foo"};
        MatrixRequestHandler.convertDestinations(wrongDestinations, this.coordinates);
    }

    @Test
    public void convertIndexToLocationsTest() throws ParameterValueException {
        ArrayList<Coordinate> coordinate = MatrixRequestHandler.convertIndexToLocations(new String[]{"1"}, this.coordinates);
        Assert.assertEquals(8.686507, coordinate.get(0).x, 0);
        Assert.assertEquals(49.41943, coordinate.get(0).y, 0);
        Assert.assertEquals(Double.NaN, coordinate.get(0).z, 0);
    }

    @Test(expected = ParameterValueException.class)
    public void convertWrongIndexToLocationsTest() throws ParameterValueException {
        MatrixRequestHandler.convertIndexToLocations(new String[]{"foo"}, this.coordinates);
    }

    @Test
    public void convertUnitsTest() throws ParameterValueException {
        Assert.assertEquals(DistanceUnit.Meters, MatrixRequestHandler.convertUnits("meters"));
        Assert.assertEquals(DistanceUnit.Meters, MatrixRequestHandler.convertUnits("m"));
        Assert.assertEquals(DistanceUnit.Kilometers, MatrixRequestHandler.convertUnits("kilometers"));
        Assert.assertEquals(DistanceUnit.Kilometers, MatrixRequestHandler.convertUnits("km"));
        Assert.assertEquals(DistanceUnit.Miles, MatrixRequestHandler.convertUnits("miles"));
        Assert.assertEquals(DistanceUnit.Miles, MatrixRequestHandler.convertUnits("mi"));
    }

    @Test(expected = ParameterValueException.class)
    public void convertWrongUnitsTest() throws ParameterValueException {
        MatrixRequestHandler.convertUnits("foo");
    }

    @Test
    public void convertToMatrixProfileTypeTest() throws ParameterValueException {
        Assert.assertEquals(1, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.DRIVING_CAR));
        Assert.assertEquals(2, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.DRIVING_HGV));
        Assert.assertEquals(10, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.CYCLING_REGULAR));
        Assert.assertEquals(12, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.CYCLING_ROAD));
        Assert.assertEquals(18, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.CYCLING_SAFE));
        Assert.assertEquals(11, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.CYCLING_MOUNTAIN));
        Assert.assertEquals(19, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.CYCLING_TOUR));
        Assert.assertEquals(17, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.CYCLING_ELECTRIC));
        Assert.assertEquals(20, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.FOOT_WALKING));
        Assert.assertEquals(21, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.FOOT_HIKING));
        Assert.assertEquals(30, MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.WHEELCHAIR));
    }

    @Test(expected = ParameterValueException.class)
    public void convertToWrongMatrixProfileTypeTest() throws ParameterValueException {
        MatrixRequestHandler.convertToMatrixProfileType(APIEnums.MatrixProfile.forValue("foo"));
    }

}