package com.mople.dto.request.weather;

import java.math.BigDecimal;

public record CoordinateRequest(BigDecimal longitude, BigDecimal latitude) {
}
