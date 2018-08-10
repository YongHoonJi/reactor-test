package com.example.demo;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log
public class FluxTest {
	private static List<String> words = Arrays.asList("the", "quick", "brown", "fox", "umped", "over", "the", "lazy",
			"dog");

	public void test() {
		Flux<String> fw = Flux.just("hi", "jj");
		Flux<String> mw = Flux.fromIterable(words);

		mw.log().subscribe(s -> log.info(s));
	}

	public void flatmap() {
		Flux<String> ml = Flux.fromIterable(words).flatMap(w -> Flux.fromArray(w.split(""))).distinct().sort()
				.zipWith(Flux.range(1, Integer.MAX_VALUE), (s, c) -> s + "," + c);
		ml.subscribe(s -> log.info(s));
	}

	@Test
	public void concatWith() {
		Mono<String> missing = Mono.just("s");
		Flux<String> allLetters = Flux
				.fromIterable(words)
				.flatMap(word -> Flux.fromArray(word.split("")))
				.concatWith(missing) // 글자로 추출한 요소에 "s" 를 추가
				.distinct()
				.sort()
				.zipWith(Flux.range(1, Integer.MAX_VALUE), (string, count) -> String.format("%2d. %s", count, string));

		allLetters.subscribe(System.out::println);
	}

}
