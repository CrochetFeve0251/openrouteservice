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

package heigit.ors.api.controllers;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import heigit.ors.api.errors.RoutingResponseEntityExceptionHandler;
import heigit.ors.api.requests.routing.APIRoutingEnums;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXRouteResponse;
import heigit.ors.api.responses.routing.GeoJSONRouteResponseObjects.GeoJSONRouteResponse;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONRouteResponse;
import heigit.ors.exceptions.*;
import heigit.ors.api.requests.routing.RouteRequestHandler;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingErrorCodes;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value="/v2/directions", description = "Get a route")
@RequestMapping("/v2/directions")
public class RoutingAPI {

    @PostMapping
    @ApiOperation(value = "", hidden = true)
    public String getPostMapping(@RequestBody RouteRequest request) throws MissingParameterException {
        throw new MissingParameterException(RoutingErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping(value = "/{profile}")
    public JSONRouteResponse getDefault( @ApiParam(value = "Specifies the route profile.") @PathVariable APIRoutingEnums.RoutingProfile profile,
                                         @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        return getJsonMime(profile, request);
    }

    @PostMapping(value = "/{profile}/json", produces = {"application/json;charset=UTF-8"})
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "JSON Response", response = JSONRouteResponse.class)
    })
    public JSONRouteResponse getJsonMime(
            @ApiParam(value = "Specifies the route profile.", required = true) @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws StatusCodeException {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.JSON);

        RouteResult result = RouteRequestHandler.generateRouteFromRequest(request);

        return new JSONRouteResponse(new RouteResult[] { result }, request);
    }

    @PostMapping(value = "/{profile}/gpx", produces = "application/gpx+xml;charset=UTF-8")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GPX Response", response = GPXRouteResponse.class)
    })
    public GPXRouteResponse getGPXMime(
            @ApiParam(value = "Specifies the route profile.", required = true) @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.GPX);

        RouteResult result = RouteRequestHandler.generateRouteFromRequest(request);

        return new GPXRouteResponse(new RouteResult[] { result }, request);

    }

    @PostMapping(value = "/{profile}/geojson", produces = "application/geo+json;charset=UTF-8")
    @ApiOperation(value = "Get a route from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GeoJSON Response", response = GeoJSONRouteResponse.class)
    })
    public GeoJSONRouteResponse getGeoJsonMime(
            @ApiParam(value = "Specifies the route profile.", required = true) @PathVariable APIRoutingEnums.RoutingProfile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody RouteRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIRoutingEnums.RouteResponseType.GEOJSON);

        RouteResult result = RouteRequestHandler.generateRouteFromRequest(request);

        return new GeoJSONRouteResponse(new RouteResult[] { result }, request);
    }

    // Errors generated from the reading of the request (before entering the routing system). Normally these are where
    // parameters have been entered incorrectly in the request
    @ExceptionHandler
    public ResponseEntity<Object> handleError(final HttpMessageNotReadableException e) {
        final Throwable cause = e.getCause();
        final RoutingResponseEntityExceptionHandler h = new RoutingResponseEntityExceptionHandler();
        if(cause instanceof UnrecognizedPropertyException) {
            return h.handleUnknownParameterException(new UnknownParameterException(RoutingErrorCodes.UNKNOWN_PARAMETER, ((UnrecognizedPropertyException)cause).getPropertyName()));
        } else if(cause instanceof InvalidFormatException) {
            return h.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, ((InvalidFormatException)cause).getValue().toString()));
        } else if(cause instanceof InvalidDefinitionException) {
            return h.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, ((InvalidDefinitionException)cause).getPath().get(0).getFieldName()));
        } else if(cause instanceof MismatchedInputException) {
            return h.handleStatusCodeException(new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, ((MismatchedInputException)cause).getPath().get(0).getFieldName()));
        } else {
            return h.handleGenericException(e);
        }
    }
}
