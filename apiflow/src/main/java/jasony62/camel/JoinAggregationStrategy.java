package jasony62.camel;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

public class JoinAggregationStrategy implements AggregationStrategy {

  @Override
  public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
    if (oldExchange == null) {
      return newExchange;
    }

    String newResult = newExchange.getIn().getBody(String.class);
    String allResult = oldExchange.getIn().getBody(String.class);

    oldExchange.getIn().setBody(allResult + ":" + newResult);

    return oldExchange;
  }
}
