package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

public class LocationGatewayTest {

  @Test
  void testWhenResolveExistingLocationShouldReturn() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  void testWhenResolveUnknownLocationShouldReturnNull() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("UNKNOWN-999");

    // then
    assertNull(location);
  }

  @Test
  void testWhenResolveNullOrBlankIdentifierShouldReturnNull() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // then
    assertNull(locationGateway.resolveByIdentifier(null));
    assertNull(locationGateway.resolveByIdentifier(""));
    assertNull(locationGateway.resolveByIdentifier("   "));
  }

  @Test
  void testWhenResolveIdentifierWithWhitespaceShouldStillResolve() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("  AMSTERDAM-001  ");

    // then
    assertNotNull(location);
    assertEquals("AMSTERDAM-001", location.identification);
  }
}
