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

package heigit.ors.api.responses.routing.BoundingBox;

import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.api.responses.routing.GPXRouteResponseObjects.GPXBounds;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSON3DBoundingBox;
import heigit.ors.api.responses.routing.JSONRouteResponseObjects.JSONBoundingBox;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.routing.RoutingErrorCodes;

public class BoundingBoxFactory {
    public static BoundingBox constructBoundingBox(BBox bounds, RouteRequest request) throws ParameterValueException {
        switch(request.getResponseType()) {
            case GEOJSON:
            case JSON:
                if(request.hasReturnElevationForPoints() && request.getReturnElevationForPoints())
                    return new JSON3DBoundingBox(bounds);
                return new JSONBoundingBox(bounds);
            case GPX:
                return new GPXBounds(bounds);
            default:
                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "format", request.getResponseType().toString());
        }
    }
}
