package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ProductResourceErrorMapperTest {

  @Test
  void mapsRuntimeExceptionToServerError() throws Exception {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    setObjectMapper(mapper);

    Response response = mapper.toResponse(new RuntimeException("boom"));
    ObjectNode entity = (ObjectNode) response.getEntity();

    assertEquals(500, response.getStatus());
    assertEquals("java.lang.RuntimeException", entity.get("exceptionType").asText());
    assertEquals(500, entity.get("code").asInt());
    assertEquals("boom", entity.get("error").asText());
  }

  @Test
  void mapsWebApplicationExceptionStatus() throws Exception {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    setObjectMapper(mapper);

    WebApplicationException exception = new WebApplicationException("bad", 400);
    Response response = mapper.toResponse(exception);
    ObjectNode entity = (ObjectNode) response.getEntity();

    assertEquals(400, response.getStatus());
    assertEquals("jakarta.ws.rs.WebApplicationException", entity.get("exceptionType").asText());
    assertEquals(400, entity.get("code").asInt());
    assertEquals("bad", entity.get("error").asText());
  }

  private void setObjectMapper(ProductResource.ErrorMapper mapper) throws Exception {
    Field field = ProductResource.ErrorMapper.class.getDeclaredField("objectMapper");
    field.setAccessible(true);
    field.set(mapper, new ObjectMapper());
  }
}
