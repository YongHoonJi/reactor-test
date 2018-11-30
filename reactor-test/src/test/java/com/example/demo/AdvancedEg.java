package com.example.demo;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class AdvancedEg {
	
	public void zip() throws InterruptedException {
		LaWeatherStation station = new LaWeatherStation();
		Mono<Temperature> temperatureMeasurements = station.temperature();
		Mono<Wind> windMeasurements = station.wind();
		// 이벤트가 시작되면 zipWith는 Wind를 기다린다(블로킹은 아님!)
		temperatureMeasurements.zipWith(windMeasurements, (temp, wind) -> new Weather(temp, wind))
				.subscribe(s -> log.info(s.toString()));

		Thread.sleep(50000);
	}
	
	public void odd() {
		Flux<Boolean> trueFalse = Flux.just(true, false).repeat();
		Flux<Integer> upstream = Flux.range(1, 10);
		Flux<Integer> downstream = upstream.zipWith(trueFalse, Pair::of)
				.filter(Pair::getRight)
				.map(Pair::getLeft);
		downstream.subscribe(s -> log.info(s + ""));
	}
	
	
	public void compose() {
		AtomicInteger ai = new AtomicInteger();
		Function<Flux<String>, Flux<String>> filterAndMap = f -> {
			if (ai.incrementAndGet() == 1) {
				System.out.println("ai = " + ai.get());
				return f.filter(color -> !color.equals("orange")).map(String::toUpperCase);
			}
			return f.filter(color -> !color.equals("purple")).map(String::toUpperCase);
		};

		Flux<String> composedFlux = Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
				.doOnNext(System.out::println).compose(filterAndMap);

		composedFlux.subscribe(d -> System.out.println("Subscriber 1 to Composed MapAndFilter :" + d));
		composedFlux.subscribe(d -> System.out.println("Subscriber 2 to Composed MapAndFilter: " + d));
	}
	
	public void takeWhile() throws InterruptedException {
		// interval
		Flux.interval(Duration.ofSeconds(1))
		.takeWhile(x -> x != 5)
		.subscribe(System.out::println);
		Thread.sleep(10000);
	}
	
	public void refactoringBefore() {
		Customer c = new Customer("JJ");
	    Book book;
	    try {    
	        book = recommend(c);
	    } catch(Exception e) {
	        book = bookSeller();
	    }
	    display(book.getTitle());
	}
	
	public void refactoringAfter() {
		Customer c = new Customer("KK");
		rxRecommend(c)
		.onErrorReturn(bookSeller())
        .map(Book::getTitle)
        .subscribe(this::display);
	}
	
	public void sink() {
		Flux<String> flux1 = Flux.generate(
			    () -> 0, 
			    (state, sink) -> {
			      sink.next("3 x " + state + " = " + 3*state); 
			      if (state == 10) sink.complete(); 
			      return state + 1; 
			    });
		
		Flux<String> flux2 = Flux.generate(
			    AtomicLong::new, 
			    (state, sink) -> {
			      long i = state.getAndIncrement(); 
			      sink.next("3 x " + i + " = " + 3*i);
			      if (i == 10) sink.complete();
			      return state; 
			    });
		
		flux2.subscribe(System.out::println);
	}

	@Test
	public void publishOn() throws InterruptedException {
		log.info("start");
		Flux.range(1, 10)
		.publishOn(Schedulers.newSingle("publisher"))
		.log()
		.subscribeOn(Schedulers.newSingle("subscriber"))
		.subscribe(s -> System.out.println(s+""));
		
		Thread.sleep(1000);
	}
	
	public void subscribeOn() throws InterruptedException {
		Scheduler s = Schedulers.newParallel("parallel-scheduler", 4); 

		final Flux<String> flux = Flux
		    .range(1, 10)
		    .map(i -> {
		    	return 10 + i;
		    })  
		    .subscribeOn(s)  
		    .map(i -> "value " + i)
		.log();

		new Thread(() -> flux.subscribe(i -> log.info(i))).run();  
		Thread.sleep(10000);
	}
	
	private void display(String title) {
		System.out.println("Your book is "+title);
	}	
	
	private Book recommend(Customer p) {
		return new Book("recommended book");
	}
	
	private Book bookSeller() {
		return new Book("bestseller book");
	}	
	
	private Mono<Book> rxRecommend(Customer p) {
		return Mono.defer(() -> {
			return Mono.just(new Book("recommended book"));
			//return Mono.error(new IllegalStateException("error"));
		});
	}
	
	// 조급함과 느긋함의 차이
	private Flux<Customer> listPeople() {    
	    final List<Customer> people = query("SELECT * FROM PEOPLE");
	    return Flux.fromIterable(people);
	}

	private Flux<Customer> rxlistPeople() {
	    return Flux.defer(() -> Flux.fromIterable(query("SELECT * FROM PEOPLE")));
	}	
	
	private List<Customer> query(String sql) {
		return Arrays.asList(new Customer("glue"), new Customer("marker"));
	}

}

@Data
@AllArgsConstructor
class Book {
	private String title;
}

@Data
@AllArgsConstructor
class Customer {
	private String name;
	
}

class Temperature { }

class Wind {
}

interface WeatherStation {
	Mono<Temperature> temperature();

	Mono<Wind> wind();
}

class LaWeatherStation implements WeatherStation {

	@Override
	public Mono<Temperature> temperature() {
		System.out.println("get temp");
		return Mono.just(new Temperature()).delayElement(Duration.ofSeconds(1));
	}

	@Override
	public Mono<Wind> wind() {
		System.out.println("get wind");
		return Mono.just(new Wind()).delayElement(Duration.ofSeconds(4));
	}
}

class Weather {
	public Weather(Temperature temperature, Wind wind) {
		// ...
	}
}
