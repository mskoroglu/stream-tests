package tr.com.atmosware.academy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StreamTests {

  private static final List<Customer> CUSTOMERS = List.of(
    new Customer("Foo", List.of("Foo'nun 1. adresi", "Foo'nun 2. adresi")),
    new Customer("Bar", List.of("Bar'in 1. adresi")),
    new Customer(
      "Baz",
      List.of("Baz'in 1. adresi", "Baz'in 2. adresi", "Baz'in 3. adresi")
    )
  );

  private static Stream<Integer> range(int startInclusive, int endInclusive) {
    return IntStream.rangeClosed(startInclusive, endInclusive).boxed();
  }

  @Test
  void testFilter() {
    final Predicate<Integer> predicate = number -> number % 2 == 0;
    final var result = range(0, 9).filter(predicate);
    assertThat(result).containsExactly(0, 2, 4, 6, 8);
  }

  @Test
  void testMap() {
    final Function<Integer, String> mapper = number ->
      "%d: %d".formatted(number, number * number);
    final var result = range(1, 3).map(mapper);
    assertThat(result).containsExactly("1: 1", "2: 4", "3: 9");
  }

  @Test
  void testMapToInt() {
    final ToIntFunction<Integer> toIntFunction = number -> number * number;
    final var result = range(1, 3).mapToInt(toIntFunction);
    assertThat(result).containsExactly(1, 4, 9);
  }

  @Test
  void testFlatMap() {
    final var customerNames = CUSTOMERS.stream().map(Customer::name).toList();
    System.out.println("customerNames = " + customerNames);

    final var customerAddressesWithMap = CUSTOMERS
      .stream()
      .map(Customer::addresses)
      .toList();
    System.out.println(
      "customerAddressesWithMap = " + customerAddressesWithMap
    );

    final var customerAddressesWithFlatMap = CUSTOMERS
      .stream()
      .map(Customer::addresses)
      .flatMap(Collection::stream)
      .toList();
    System.out.println(
      "customerAddressesWithFlatMap = " + customerAddressesWithFlatMap
    );

    final Function<Integer, Stream<Integer>> findDivisors = number ->
      range(1, number).filter(it -> number % it == 0);
    final var result = range(2, 6).flatMap(findDivisors);
    assertThat(result).containsExactly(1, 2, 1, 3, 1, 2, 4, 1, 5, 1, 2, 3, 6);
  }

  @Test
  void testMapMulti() {
    final var customers = CUSTOMERS
      .stream()
      .<Customer>mapMulti((customer, consumer) -> {
        if (customer.addresses().size() >= 2) {
          consumer.accept(customer);
        }
      })
      .map(Customer::name)
      .toList();
    System.out.println("customers = " + customers);

    final var result = range(0, 5)
      .mapMulti((number, consumer) -> {
        consumer.accept(number);
        if (number % 2 == 0) {
          consumer.accept(number * number);
        }
      });
    assertThat(result).containsExactly(0, 0, 1, 2, 4, 3, 4, 16, 5);

    final var result2 = range(0, 5)
      .flatMap(number -> {
        final var list = new ArrayList<Integer>();
        list.add(number);
        if (number % 2 == 0) {
          list.add(number * number);
        }
        return list.stream();
      });
    assertThat(result2).containsExactly(0, 0, 1, 2, 4, 3, 4, 16, 5);
  }

  @Test
  void testDistinct() {
    final var result = Stream.of(1, 2, 2, 3, 4, 4, 4, 4, 5, 4, 1).distinct();
    assertThat(result).containsExactly(1, 2, 3, 4, 5);
  }

  @Test
  void testSorted() {
    final var orderedCustomers = CUSTOMERS
      .stream()
      .sorted()
      .map(Customer::name)
      .toList();
    System.out.println("orderedCustomers = " + orderedCustomers);

    final Comparator<Integer> comparator = Comparator.reverseOrder();
    final var result = Stream.of(3, 6, 1, 5).sorted(comparator);
    assertThat(result).containsExactly(6, 5, 3, 1);
  }

  @Test
  void testPeek() {
    final var total = new AtomicInteger();
    range(1, 5).peek(total::addAndGet).forEach(it -> {});
    assertThat(total.get()).isEqualTo(15);
  }

  private record Customer(String name, List<String> addresses)
    implements Comparable<Customer> {
    @Override
    public int compareTo(Customer o) {
      return name.compareTo(o.name);
    }
  }
}
