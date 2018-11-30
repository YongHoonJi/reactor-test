package com.example.demo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import lombok.AllArgsConstructor;
import lombok.Data;

public class BakcPressure {
	@Test
	public void test() throws InterruptedException {
		BpPublisher pub = new BpPublisher();
		pub.subscribe(new BpSubsriber());
		Thread.sleep(10000);
	}
}

@Data
@AllArgsConstructor
class LargeData {
	private String content;
	private Integer size;
}

class Repository {
	List<LargeData> findAll() {
		return Arrays.asList(new LargeData("Huge image", 5000), new LargeData("Huge vector", 4000),
				new LargeData("Huge byte", 3000));
	}
}

class BpPublisher implements Publisher<LargeData> {
	@Override
	public void subscribe(Subscriber<? super LargeData> subscriber) {
		final List<LargeData> list = new Repository().findAll();
		ConcurrentLinkedQueue<LargeData> queue = new ConcurrentLinkedQueue<LargeData>(list);
		subscriber.onSubscribe(new Subscription() {
			@Override
			public void request(long n) {
				long streamSize = (n >= queue.size()) ? queue.size() : n;
				for (int i = 0; i < streamSize; i++) {
					subscriber.onNext(queue.poll());
				}
				if (queue.isEmpty()) {
					subscriber.onComplete();
				}
			}
			@Override
			public void cancel() {
			}
		});
	}
}

class BpSubsriber implements Subscriber<LargeData> {
	private Subscription subscription;

	@Override
	public void onSubscribe(Subscription s) {
		this.subscription = s;
		 //subscription.request(Long.MAX_VALUE);
		//subscription.request(1);
	}

	@Override
	public void onNext(LargeData data) {
		System.out.println("request processing - " + data.toString());
		this.process(data);
	}

	@Override
	public void onError(Throwable t) {
		System.out.println(t.getMessage());
	}

	@Override
	public void onComplete() {
		System.out.println("completed!");
	}

	private void process(LargeData data) {
		Runnable run = () -> {
			System.out.println("Image processiong - " + data.getContent());
			try {
				Thread.sleep(data.getSize());
				subscription.request(1);
			} catch (Exception e) {
				subscription.request(1);
			}
			System.out.println("End of Image processiong - " + data.getContent());
		};
		Thread th = new Thread(run);
		th.start();
	}
}
