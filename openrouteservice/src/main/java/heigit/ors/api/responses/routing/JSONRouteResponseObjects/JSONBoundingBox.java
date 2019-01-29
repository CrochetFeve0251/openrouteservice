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

package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.graphhopper.util.shapes.BBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBox;
import heigit.ors.api.responses.routing.BoundingBox.BoundingBoxBase;
import heigit.ors.util.FormatUtility;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"minLat", "minLon", "maxLat", "maxLon"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JSONBoundingBox extends BoundingBoxBase implements BoundingBox {
    private final int COORDINATE_DECIMAL_PLACES = 6;

    public JSONBoundingBox(BBox bounding) {
        super(bounding);
    }

    @Override
    public double getMinLat() {
        return FormatUtility.roundToDecimals(minLat, COORDINATE_DECIMAL_PLACES);
    }

    @Override
    public double getMinLon() {
        return FormatUtility.roundToDecimals(minLon, COORDINATE_DECIMAL_PLACES);
    }

    @Override
    public double getMaxLat() {
        return FormatUtility.roundToDecimals(maxLat, COORDINATE_DECIMAL_PLACES);
    }

    @Override
    public double getMaxLon() {
        return FormatUtility.roundToDecimals(maxLon, COORDINATE_DECIMAL_PLACES);
    }
}
