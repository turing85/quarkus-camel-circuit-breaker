package de.turing85;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.*;

import javax.enterprise.context.*;
import org.apache.camel.*;
import org.apache.camel.builder.*;

@ApplicationScoped
public class FailingRoute extends RouteBuilder {
  private int counter = 0;

  @Override
  public void configure() {
    // @formatter: off
    from(timer("foo").period(100).repeatCount(120))
        .routeId("failing-route")
        .process(this::generate)
        .log("Generated: ${body}")
        .circuitBreaker()
            .faultToleranceConfiguration()
                .requestVolumeThreshold(10)
                .failureRatio(50)
                .delay(2000)
            .end()
            .log("Before circuit breaker: ${body}")
            .process(this::transform)
            .log("After circuit breaker: ${body}")
        .end()
        .log("After processing: ${body}");
    // @formatter: on
  }

  private void generate(Exchange exchange) {
    exchange.getIn().setBody(counter++);
  }

  private void transform(Exchange exchange) {
    int value = exchange.getIn().getBody(Integer.class);
    if (value < 100) {
      throw new RuntimeException();
    }
  }
}
