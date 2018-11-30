package com.example.demo;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import lombok.AllArgsConstructor;
import lombok.Data;
public class PubSub {
	@Test
	public void test() throws InterruptedException {
		EgPublisher pub = new EgPublisher();
		EgSubscriber sub = new EgSubscriber();
		pub.subscribe(sub);
		Thread.sleep(2000);
	}

}

@Data
@AllArgsConstructor
class Person {
	private String name;
	private Integer age;
}

class PeopleRepository {
	List<Person> findAll() {
		return Arrays.asList(new Person("John", 35), new Person("Mika", 40), new Person("Tylor", 23));
	}
}

class EgPublisher implements Publisher<Person> {
	// 구독이 일어날 때 호출
	@Override
	public void subscribe(Subscriber<? super Person> sub) {
		// DB로부 People 정보를 조회
		final List<Person> people = new PeopleRepository().findAll();
		final LinkedList<Person> linkedList = new LinkedList<>(people); 
		// 구독자에 대한 구독 정보 설정
		sub.onSubscribe(new Subscription() {
			@Override
			public void request(long n) {
				if(linkedList.isEmpty()) {
					// 스트림의 마지막임을 알림
					sub.onComplete();
				} else {
					System.out.println("request size - " + n);
					long streamSize = (n >= linkedList.size())?linkedList.size():n;
					
					int i=0;
					if(linkedList.isEmpty()) {
						sub.onComplete();
					} else {
						while(i < streamSize) {
							// 요청된 스트림의 수만큼 onNext로 subscriber에 전달
							//sub.onError(new IllegalStateException("Error occured!"));
							sub.onNext(linkedList.poll());
							i++;
						}
					}
					
					sub.onError(new Exception("error"));
				}
			}
			@Override
			public void cancel() {
				System.out.println("Subscriber cancel subscription");
			}
		});
	}
}


class EgSubscriber implements Subscriber<Person> {
	Subscription subscription;
	@Override
	public void onSubscribe(Subscription s) {
		System.out.println("initialized");
		// 모두 요청
		//s.request(Long.MAX_VALUE);
		// 특정 수 요청
		subscription = s;
		subscription.request(1);
	}

	@Override
	public void onNext(Person t) {
		// 스트림의 단위요소인 element가 전달될 경우 호출
		System.out.println(t.toString());
		System.out.println("request an item.");
		subscription.request(1);
	}

	@Override
	public void onError(Throwable t) {
		// 스트림 처리 중 오류가 발생할 경우
		System.out.println(t.getMessage());
		
	}

	@Override
	public void onComplete() {
		// 스트림 처리가 완료됨
		System.out.println("Complete");
	}
	
}
