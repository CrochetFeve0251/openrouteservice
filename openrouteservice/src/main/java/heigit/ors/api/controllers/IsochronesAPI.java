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
import heigit.ors.api.errors.CommonResponseEntityExceptionHandler;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.isochrones.IsochronesRequest;
import heigit.ors.api.requests.isochrones.IsochronesRequestHandler;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects.GeoJSONIsochronesResponse;
import heigit.ors.exceptions.*;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.isochrones.IsochronesErrorCodes;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@Api(value = "/v2/isochrones", description = "Get an Isochrone Calculation")
@RequestMapping("/v2/isochrones")
public class IsochronesAPI {
    final static CommonResponseEntityExceptionHandler errorHandler = new CommonResponseEntityExceptionHandler(IsochronesErrorCodes.BASE);

    // generic catch methods - when extra info is provided in the url, the other methods are accessed.
    @GetMapping
    @ApiOperation(value = "", hidden = true)
    public void getGetMapping() throws MissingParameterException {
        throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "profile");
    }

    @PostMapping
    @ApiOperation(value = "", hidden = true)
    public String getPostMapping(@RequestBody RouteRequest request) throws MissingParameterException {
        throw new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, "profile");
    }

    // Matches any response type that has not been defined
    @PostMapping(value="/{profile}/*")
    public void getInvalidResponseType() throws StatusCodeException {
        throw new StatusCodeException(HttpServletResponse.SC_NOT_ACCEPTABLE, IsochronesErrorCodes.UNSUPPORTED_EXPORT_FORMAT, "This response format is not supported");
    }

    // Functional request methods

    @PostMapping(value = "/{profile}/geojson", produces = "application/geo+json;charset=UTF-8")
    @ApiOperation(value = "Get isochrones from the specified profile", httpMethod = "POST", consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GeoJSON Response", response = GeoJSONIsochronesResponse.class)
    })
    public GeoJSONIsochronesResponse getGeoJsonMime(
            @ApiParam(value = "Specifies the route profile.", required = true) @PathVariable APIEnums.Profile profile,
            @ApiParam(value = "The request payload", required = true) @RequestBody IsochronesRequest request) throws Exception {
        request.setProfile(profile);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);

        IsochronesRequestHandler handler = new IsochronesRequestHandler();
        handler.generateIsochronesFromRequest(request);
        IsochroneMapCollection isoMaps = handler.getIsoMaps();
        return new GeoJSONIsochronesResponse(request, isoMaps);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(final MissingServletRequestParameterException e) {
        return errorHandler.handleStatusCodeException(new MissingParameterException(IsochronesErrorCodes.MISSING_PARAMETER, e.getParameterName()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class})
    public ResponseEntity<Object> handleReadingBodyException(final Exception e) {
        final Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException) {
            return errorHandler.handleUnknownParameterException(new UnknownParameterException(IsochronesErrorCodes.UNKNOWN_PARAMETER, ((UnrecognizedPropertyException) cause).getPropertyName()));
        } else if (cause instanceof InvalidFormatException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, ((InvalidFormatException) cause).getValue().toString()));
        } else if (cause instanceof InvalidDefinitionException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, ((InvalidDefinitionException) cause).getPath().get(0).getFieldName()));
        } else if (cause instanceof MismatchedInputException) {
            return errorHandler.handleStatusCodeException(new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, ((MismatchedInputException) cause).getPath().get(0).getFieldName()));
        } else {
            // Check if we are missing the body as a whole
            if (e.getLocalizedMessage().startsWith("Required request body is missing")) {
                return errorHandler.handleStatusCodeException(new EmptyElementException(IsochronesErrorCodes.MISSING_PARAMETER, "Reuqest body could not be read"));
            }
            return errorHandler.handleGenericException(e);
        }
    }

    @ExceptionHandler(StatusCodeException.class)
    public ResponseEntity<Object> handleException(final StatusCodeException e) {
        return errorHandler.handleStatusCodeException(e);
    }
}
