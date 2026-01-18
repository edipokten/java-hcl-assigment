package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

class LocationGatewayTest {

  @Test
  void returnsNullWhenIdentifierMissing() {
    LocationGateway gateway = new LocationGateway();

    assertNull(gateway.resolveByIdentifier(null));
    assertNull(gateway.resolveByIdentifier(" "));
  }

  @Test
  void resolvesKnownLocation() {
    LocationGateway gateway = new LocationGateway();

    Location location = gateway.resolveByIdentifier("  ZWOLLE-001  ");

    assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  void returnsNullWhenUnknownLocation() {
    LocationGateway gateway = new LocationGateway();

    assertNull(gateway.resolveByIdentifier("UNKNOWN"));
  }
}
