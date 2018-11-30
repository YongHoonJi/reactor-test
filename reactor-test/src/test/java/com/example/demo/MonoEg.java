package com.example.demo;

import java.io.IOException;
import java.util.Optional;

import org.assertj.core.util.Strings;
import org.junit.Test;
import org.reactivestreams.Subscription;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Slf4j
public class MonoEg {
	public void generate() {
		Mono<String> mono = Mono.just("Mono1");
		mono.subscribe(
				s -> log.info(s),
				e -> e.printStackTrace(),
				() -> log.info("completed")
				);
	}
	
	public void create() {
		Mono.create(sink -> {
			sink.success("suc");
		}).map(s -> "[ " + s + " ]")
		.subscribe(System.out::println);
	}
	
	public void defer() throws InterruptedException {
		Mono<String> d1 = Mono.just("not defered1");
		Mono<String> d2 = Mono.just("not defered2");
		log.info("prepared defered monos");
		Flux<String> flux = d1.concatWith(d2);
		flux.subscribe(
				s -> log.info(s),
				e -> e.printStackTrace(),
				() -> log.info("completed")
				);
		log.info("prepared defered monos from network");
		Mono<String> network1 = getFromNetwork("network1");
		Mono<String> network2 = getFromNetwork("network2");
		Flux<String> flux2 = network1.concatWith(network2);
		log.info("start subscribing");
		flux2.subscribe(
				s -> log.info("sucess :" + s),
				e -> e.printStackTrace(),
				() -> log.info("completed")
				);
		Thread.sleep(10000);
	}
	
	private Mono<String> getFromNetwork(String s) {
		return Mono.defer(() -> {
			try {
				log.info("networking..." + s);
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return Mono.just(s);
		});
	}
	
	public void handleException() {
		Mono<String> m1 = Mono.just("");
		m1.flatMap(s -> check(s))
		.flatMap(s2 -> convert(s2))
		.subscribe(
				s -> System.out.println(s),
				e -> log.info(e.getMessage()),
				() -> log.info("completed")
				);
		
	}
	
	private static Mono<String> check(String s) {
		if(Strings.isNullOrEmpty(s)) {
			return Mono.error(new IllegalArgumentException("sting is empty."));
		} else {
			return Mono.just(s);
		}
	}
	
	private static Mono<Integer> convert(String s) {
		try {
			return Mono.just(Integer.parseInt(s));
		} catch (Exception e) {
			return Mono.error(e);
		}
	}
	
	
	public void handleCache() {
		Mono.create(sink -> {
			Optional<String> cache = getCache("cache");
			if(cache.isPresent()) {
				// 동기적인 방출
				sink.success(cache.get());
			} else {
				// 비동기적 방출
				getAsynFromNetwork().subscribe(
						s -> sink.success(s.get()),
						e -> sink.error(new IOException("network error"))
						);
				
			}
		})
		.subscribe(
				s -> System.out.println(s)
				);
	}
	
	private Mono<Optional<String>> getAsynFromNetwork() {
		return Mono.defer(() -> Mono.just(Optional.of("cache from network")));
		
	}
	
	private Optional<String> getCache(String key) {
		if(key.equals("")) return Optional.empty();
		return Optional.of("Cache - " + key);
	}
	
	@Data
	@AllArgsConstructor
	class Tweet {
		private String nickName;
		private Integer age;
	}
	
	public void subscribe() throws InterruptedException {
		//publisher
		Mono<Tweet> tweetPublisher = Mono.just(new Tweet("Jack", 28));
		
		//subscriber
		BaseSubscriber<Tweet> twsub1 =  new BaseSubscriber<Tweet>() {
			public void hookOnSubscribe(Subscription subscription) {
				log.info("start sub1 subscribe");
				request(1);
		    }
			
			public void hookOnNext(Tweet value) {
				log.info(value.toString());
			}
		};
		
		BaseSubscriber<Tweet> twsub2 =  new BaseSubscriber<Tweet>() {
			public void hookOnSubscribe(Subscription subscription) {
				log.info("start sub2 subscribe");
				request(1);
		    }
			
			public void hookOnNext(Tweet value) {
				log.info(value.toString());
			}
		};
		
		// subscrition
		tweetPublisher.subscribe(
				s -> log.info(s.toString()),
				e -> e.printStackTrace(),
				() -> log.info("completed")
				);
		tweetPublisher.subscribe(twsub1);
		tweetPublisher.subscribe(twsub2);
		
		
		Thread.sleep(1000);
	}
	
	
	public void exceptionTest() {
		Mono<String> mono1 = getMono();
		mono1
		.flatMap(m -> map1(m))
		.flatMap(m2 -> map2(m2))
		.subscribe(
				s -> {
					log.info(s);
				},
				e -> {
					e.printStackTrace();
				},
				() -> log.info("completed")
		);
	}
	
	private Mono<String> getMono() {
		return Mono.defer(() -> {
			log.info("mono");
			return Mono.just("mono");
		});
	}
	
	private Mono<String> map1(String s) {
		return Mono.defer(() -> {
			log.info("map1");
			return Mono.just(s + " map1");
		});
	}
	
	private Mono<String> map2(String s) {
		return Mono.defer(() -> {
			//return Mono.error(new Exception("error from map1"));
			return Mono.just(s + " map2");
		});
	}		
	
	@Test
	public void merge_zip() {
		Mono<String> mono1 = Mono.defer(() -> {
			return Mono.just("mono1");
		});
		Mono<String> mono2 = Mono.defer(() -> {
			return Mono.just("mono2");
		});
		
		Flux<String> f = mono1.mergeWith(mono2);
		f.subscribe(System.out::println);
		
		mono1.zipWith(mono2)
		.subscribe(tu -> {
			System.out.println("t1 - " + tu.getT1());
			System.out.println("t2 - " + tu.getT2());
		});
	}
	
}
