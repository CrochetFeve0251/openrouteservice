/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.api.requests.isochrones;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.isochrones.IsochronesErrorCodes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "IsochronesRequest", description = "The GeoJSON body request sent to the isochrones service which defines options and parameters regarding the isochrones to generate.")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)

public class IsochronesRequest {

    @ApiModelProperty(value = "Arbitrary identification string of the request reflected in the meta information.")
    private String id;
    private boolean hasId = false;

    @ApiModelProperty(name = "locations", value = "The locations to use for the route as an array of longitude/latitude pairs", example = "[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]]")
    @JsonProperty("locations")
    private List<List<Double>> locations;

    @ApiModelProperty(hidden = true)
    private APIEnums.RoutingProfile profile;

    @ApiModelProperty(hidden = true)
    private APIEnums.RouteResponseType responseType = APIEnums.RouteResponseType.GEOJSON;

    // units only valid for range_type distance, will be ignored for range_time time
    @ApiModelProperty(name = "units",
            value = "Specifies the distance unit.\n" +
                    "Default: m.")
    @JsonProperty(value = "units", defaultValue = "m")
    private APIEnums.Units units = APIEnums.Units.METRES;

    @ApiModelProperty(name = "area_units",
            value = "Specifies the area unit.\n" +
                    "Default: m.")
    @JsonProperty(value = "area_units", defaultValue = "m")
    private APIEnums.Units areaUnits = APIEnums.Units.METRES;

    @ApiModelProperty(name = "attributes", value = "List of isochrones attributes")
    @JsonProperty("attributes")
    private IsochronesRequestEnums.Attributes[] attributes;
    @JsonIgnore
    private boolean hasAttributes = false;

    @ApiModelProperty(name = "options",
            value = "Additional options for the isochrones request")
    @JsonProperty("options")
    private IsochronesRequestOptions isochronesOptions;
    @JsonIgnore
    private boolean hasIsochronesOptions = false;

    @ApiModelProperty(name = "range_type",
            value = "Specifies the isochrones reachability type")
    @JsonProperty(value = "range_type", defaultValue = "time")
    private IsochronesRequestEnums.RangeType rangeType = IsochronesRequestEnums.RangeType.TIME;

    @ApiModelProperty(name = "location_type",
            value = "Start treats the location(s) as starting point, destination as goal. " +
                    "Has an influence in mountainous areas"
    )
    @JsonProperty(value = "location_type", defaultValue = "start")
    private IsochronesRequestEnums.LocationType locationType = IsochronesRequestEnums.LocationType.START;

    @ApiModelProperty(name = "range", value = "Maximum range value of the analysis in seconds for time and meters for distance." +
            "Alternatively a comma separated list of specific single range values if more than one location is set.",
            example = "[ 300, 200 ]"
    )
    @JsonProperty("range")
    private List<Double> range;

    @ApiModelProperty(name = "interval", value = "Interval of isochrones or equidistants for one range value. " +
            "value in seconds for time and meters for distance.",
            example = "30"
    )
    @JsonProperty("interval")
    private Double interval;

    @ApiModelProperty(name = "intersections",
            value = "Specifies whether to return intersecting polygons")
    @JsonProperty(value = "intersections", defaultValue = "false")
    private Boolean intersections = false;

    @ApiModelProperty(name = "smoothing",
            value = "Applies a level of generalisation to the isochrone polygons generated as a smoothing_factor between 0 and 1.0.\n" +
                    "Generalisation is produced by determining a maximum length of a connecting line between two points found on the outside of a containing polygon.\n" +
                    "If the distance is larger than a threshold value, the line between the two points is removed and a smaller connecting line between other points is used.\n" +
                    "The threshold value is determined as (smoothing_factor * maximum_radius_of_isochrone) / 10.\n" +
                    "Therefore, a value closer to 1 will result in a more generalised shape.\n" +
                    "The polygon generation algorithm is based on Duckham and al. (2008) \"Efficient generation of simple polygons for characterizing the shape of a set of points in the plane.\"")
    @JsonProperty(value = "intersections", defaultValue = "false")
    private Double smoothing;
    private boolean hasSmoothing = false;

    @JsonCreator
    public IsochronesRequest(
            @JsonProperty(value = "locations", required = true) List<List<Double>> locations) {
        this.locations = locations;
    }

    public IsochronesRequest(Double[][] locations) throws ParameterValueException {
        if (locations.length < 2)
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "locations");

        this.locations = new ArrayList<>();
        for (Double[] coordPair : locations) {
            if (coordPair.length != 2)
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "locations");
            List<Double> coordPairList = new ArrayList<>();
            coordPairList.add(coordPair[0]);
            coordPairList.add(coordPair[1]);
            this.locations.add(coordPairList);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.hasId = true;
    }

    public boolean hasId() {
        return hasId;
    }

    public List<List<Double>> getLocations() {
        return locations;
    }

    public void setLocations(List<List<Double>> locations) {
        this.locations = locations;
    }

    public APIEnums.RoutingProfile getProfile() {
        return profile;
    }

    public void setProfile(APIEnums.RoutingProfile profile) {
        this.profile = profile;
    }

    public APIEnums.Units getUnits() {
        return units;
    }

    public void setUnits(APIEnums.Units units) {
        this.units = units;
    }

    public APIEnums.Units getAreaUnits() {
        return areaUnits;
    }

    public void setAreaUnits(APIEnums.Units units) {
        this.areaUnits = units;
    }

    public IsochronesRequestEnums.RangeType getRangeType() {
        return rangeType;
    }

    public void setRangeType(IsochronesRequestEnums.RangeType rangeType) {
        this.rangeType = rangeType;
    }

    public IsochronesRequestEnums.LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(IsochronesRequestEnums.LocationType locationType) {
        this.locationType = locationType;
    }

    public List<Double> getRange() {
        return range;
    }

    public void setRange(List<Double> range) {
        this.range = range;
    }

    public Double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
    }

    public Double getSmoothing() {
        return smoothing;
    }

    public void setSmoothing(Double smoothing) {
        this.smoothing = smoothing;
    }

    public boolean hasSmoothing() {
        return hasSmoothing;
    }

    public IsochronesRequestEnums.Attributes[] getAttributes() {
        return attributes;
    }

    public void setAttributes(IsochronesRequestEnums.Attributes[] attributes) {
        this.attributes = attributes;
        this.hasAttributes = true;
    }

    public APIEnums.RouteResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(APIEnums.RouteResponseType responseType) {
        this.responseType = responseType;
    }

    public IsochronesRequestOptions getIsochronesOptions() {
        return isochronesOptions;
    }

    public void setIsochronesOptions(IsochronesRequestOptions isochronesOptions) {
        this.isochronesOptions = isochronesOptions;
        this.hasIsochronesOptions = true;
    }

    public Boolean getIntersections() {
        return intersections;
    }

    public void setIntersection(Boolean intersections) {
        this.intersections = intersections;
    }

    public boolean hasAttributes() {
        return hasAttributes;
    }

    public boolean hasIsochronesOptions() {
        return hasIsochronesOptions;
    }

}

