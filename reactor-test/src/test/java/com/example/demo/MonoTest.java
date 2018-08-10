package com.example.demo;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class MonoTest {

	public void mono_from_completableFuture() throws InterruptedException {
		Mono.fromCompletionStage(delayedPublish())
			.publishOn(Schedulers.single())
			.subscribeOn(Schedulers.single()).subscribe(
				s -> log.info(s),
				e -> e.printStackTrace(),
				() -> log.info("complete")
				);
		
		Thread.sleep(3000);
	}
	
	private CompletionStage<String> delayedPublish() {
		try {
			Thread.sleep(2000);
			return CompletableFuture.supplyAsync(() -> "suplements").handle( (s, f) -> s );
		} catch (InterruptedException e) {
			e.printStackTrace();
			return CompletableFuture.supplyAsync(() -> "");
		}
	}
	
	public void concat() throws InterruptedException {
		Mono<String> mono1 = Mono.just("mono1");
		Mono<String> mono2 = Mono.just("mono2");
		Mono<String> mono3 = Mono.just("mono3");
		
		Flux<String> flux1 = Flux.just("a", "b", "c");
		Flux<String> flux2 = Flux.just("1", "2", "3");
		// publishing with interval
		Flux<String> intervalFlux1 = Flux.interval(Duration.ofMillis(50)).zipWith(flux1, (s1, s2) -> s1+s2);
		Flux<String> intervalFlux2 = Flux.interval(Duration.ofMillis(70)).zipWith(flux1, (s1, s2) -> s1+s2);
		
		//intervalFlux1.subscribe(s -> log.info(s));
		
		// concat mono to flux
		Flux.concat(mono1, mono2, mono3).subscribe(s ->log.info(s));
		
		// concat flux
		Flux.concat(flux1, flux2).subscribe(s ->log.info(s));
		
		Thread.sleep(3000);
	}
	@Test
	public void concatWith() throws InterruptedException {
		Mono<String> mono1 = Mono.just("mono1");
		Mono<String> mono2 = Mono.just("mono2");
		Mono<String> mono3 = Mono.just("mono3");
		// concatwith 경우 이후 publisher를 차례로 결합. 순차적 결합
		mono1.concatWith(mono2).concatWith(mono3)
		.all(s -> s.contains("mono1"))
		.subscribe(b -> System.out.println(b));
		
		mono1.concatWith(mono2).concatWith(mono3)
		.subscribe(b -> System.out.println(b));		
		
		mono1.concatWith(mono2).concatWith(mono3)
		.subscribe(b -> System.out.println(b));		
		
/*		Flux<String> flux1 = Flux.just("a", "b", "c");
		Flux<String> flux2 = Flux.just("1", "2", "3");
		Flux<String> intervalFlux1 = Flux.interval(Duration.ofMillis(100)).zipWith(flux1, (s1, s2) -> s1+","+s2);
		Flux<String> intervalFlux2 = Flux.interval(Duration.ofMillis(70)).zipWith(flux2, (s1, s2) -> s1+s2);
		//each of {1},{2},{3},{4} emits each 700ms, then |A|,|B|,|C| emit immediately
		//intervalFlux1.concatWith(flux2).subscribe(s -> log.info(s));
		intervalFlux1.concatWith(intervalFlux2).subscribe(s -> log.info(s));*/
		
		Thread.sleep(3000);
	}
	
	public void zip() throws InterruptedException {
		// 두개의 스트림을 결합. 누락이 있을 경우 스트림에 포함이 안됨.
		Flux<String> flux1 = Flux.just("a", "b", "c");
		Flux<String> flux2 = Flux.just("1", "2", "3", "4");
		Flux.zip(flux1, flux2, (i1, i2) -> i1 +","+ i2).subscribe(s -> log.info(s));
		Thread.sleep(3000);
	}
	
	public void merge() throws InterruptedException {
		Flux<String> flux1 = Flux.just("a1", "a2", "a3");
		Flux<String> flux2 = Flux.just("b1", "b2", "b3");
		Flux<String> intervalFlux1 = Flux.interval(Duration.ofMillis(100)).zipWith(flux1, (s1, s2) -> s1+","+s2);
		Flux<String> intervalFlux2 = Flux.interval(Duration.ofMillis(70)).zipWith(flux2, (s1, s2) -> s1+","+s2);
		Flux.merge(intervalFlux1, intervalFlux2).subscribe(s -> log.info(s));
		
		// mergeWith
		intervalFlux1.mergeWith(intervalFlux2).subscribe(s -> log.info(s));
		
		Thread.sleep(3000);
	}
	
}
