package com.example.demo;

import java.time.Duration;
import java.util.Arrays;

import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class FluxEg {
	
	public void create() {
		Flux<String> f1 = Flux.fromIterable(Arrays.asList("A", "B", "C", "D"));
		f1.subscribe(s -> System.out.println(s), e -> System.out.println(e.getMessage()),
				() -> System.out.println("complete"));

		Flux<String> f2 = Flux.create(sink -> {
			sink.next("a");
			sink.next("b");
			sink.next("c");
			sink.next("d");
			sink.complete();
		});

		f2.subscribe(s -> System.out.println(s), e -> System.out.println(e.getMessage()),
				() -> System.out.println("complete"));
	}

	public void generate() {
		Flux<String> flux = Flux.generate(() -> 0, (state, sink) -> {
			sink.next("3 x " + state + " = " + 3 * state);
			if (state == 10)
				sink.complete();
			return state + 1;
		});

		flux.subscribe(System.out::println);
	}
	public void defer() {
		Flux<String> d1 = Flux.defer(() -> {
			System.out.println("start d1");
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return Flux.fromIterable(Arrays.asList("A", "B", "C", "D"));
		});

		Flux<String> d2 = Flux.defer(() -> {
			System.out.println("start d2");
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return Flux.fromIterable(Arrays.asList("E", "F", "G", "H"));
		});

		// concat
		d1.concatWith(d2).subscribe(System.out::println);
	}
	
	public void infinite() throws InterruptedException {
		// interval
		Flux.interval(Duration.ofSeconds(1)).subscribe(System.out::println);
		Thread.sleep(5000);
	}
	
	public void flatMap1() {
		Flux<CarPhoto> cars = Flux.fromIterable(
				Arrays.asList(new CarPhoto("BMW", ""), new CarPhoto("KIA", ""), new CarPhoto("ZEEP", "")));
		Flux<Flux<LicensePlate>> nestedLicenses = cars.map(car -> recognize(car));
		Flux<LicensePlate> licenses = cars.flatMap(car -> recognize(car));
	}

	public void flatMap2() throws InterruptedException {
		int limitConcurrnecy = 5;
		Flux.just("Lorem", "ipsum", "dolor", "sit", "amet", "consetetur", "adipiscing", "elit")
				.subscribeOn(Schedulers.parallel()).flatMap(w -> timer(w), limitConcurrnecy)
				.subscribe(System.out::println);

		Thread.sleep(30000);
	}
	
	public void concatMap() throws InterruptedException {
		Flux.just("Lorem", "ipsum", "dolor", "sit", "amet", "consetetur", "adipiscing", "elit")
				.subscribeOn(Schedulers.parallel()).concatMap(w -> timer(w)).subscribe(System.out::println);

		Thread.sleep(30000);
	}

	private Flux<String> timer(String s) {
		System.out.println("start timer - " + s);
		return Flux.just(s).delayElements(Duration.ofSeconds(s.length()));
	}

	private Flux<LicensePlate> recognize(CarPhoto car) {
		System.out.println("recognizing..." + car.toString());
		return Flux.just(new LicensePlate(car.getType() + "1234"));
	}

	@Data
	@AllArgsConstructor
	class LicensePlate {
		private String licenseNumber;
	}

	@Data
	@AllArgsConstructor
	class CarPhoto {
		private String type;
		private String image;
	}
	
	public void mergeEvent() throws InterruptedException {
		CarPhoto photo = new CarPhoto("BMW", "");
		Flux<LicensePlate> all = Flux.merge(fastAlgo(photo), preciseAlgo(photo), experimentalAlgo(photo));
		all.subscribe(System.out::println);
		Thread.sleep(10000);
	}
	
	@Test
	public void concatMapEvent() throws InterruptedException {
		CarPhoto photo = new CarPhoto("BMW", "");
		Flux<LicensePlate> all = Flux.concat(fastAlgo(photo), preciseAlgo(photo), experimentalAlgo(photo));
		all.subscribe(System.out::println);
		Thread.sleep(10000);
	}	

	Mono<LicensePlate> fastAlgo(CarPhoto photo) {
		// 빠르지만 낮은 품질
		return Mono.just(new LicensePlate(photo.getType() + " = fastAlgo")).delayElement(Duration.ofSeconds(1));
	}

	Mono<LicensePlate> preciseAlgo(CarPhoto photo) {
		// 정확하지만 비용이 높은
		return Mono.just(new LicensePlate(photo.getType() + " = preciseAlgo")).delayElement(Duration.ofSeconds(4));
	}

	Mono<LicensePlate> experimentalAlgo(CarPhoto photo) {
		// 예측이 어렵지만 실행됨
		return Mono.just(new LicensePlate(photo.getType() + " = experimentalAlgo")).delayElement(Duration.ofSeconds(3));
	}

}
