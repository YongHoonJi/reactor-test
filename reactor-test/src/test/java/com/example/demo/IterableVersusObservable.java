package com.example.demo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class IterableVersusObservable {
	public void iterable() throws Exception {
		Iterable<String> nums = Arrays.asList("1","2","3+","4","5");
		for(Iterator<String> it = nums.iterator(); it.hasNext();) {
			try {
				System.out.println(Integer.parseInt(it.next()));
			} catch (Exception e) {
				throw new Exception("Fail to convert");
			}
		}
	}
	@Test
	public void Observable() throws InterruptedException {
		List<String> nums = Arrays.asList("1","2","3+","4","5");
		Flux.fromIterable(nums)
		.map(Integer::parseInt)
		.doOnError(e -> e.printStackTrace())
		.onErrorReturn(0) 	
		.subscribe(
				s -> System.out.println("convert : " + (s + 10)),
				e -> log.info(e.getMessage()),
				() -> System.out.println("complete")
				);
		
		Thread.sleep(1000);
	}
}
