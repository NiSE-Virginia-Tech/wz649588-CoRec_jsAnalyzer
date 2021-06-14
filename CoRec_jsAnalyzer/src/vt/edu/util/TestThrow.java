package vt.edu.util;

import java.util.ArrayList;
import java.util.List;

public class TestThrow {
	List<Integer> list = new ArrayList<>();
	public static void main(String[] args) {
		int[] myNumbers = {1, 2, 3};
		for(int i = 0; i < 10; i++){
//		test();
			try {
				test();
			} catch (Exception e) {
				System.out.println("No problen");
			}
		}
		System.out.println("dafdsfs");
	}
	public static void test() {
		int[] myNumbers = {1,2,3,4,5};
		for(int i = 0; i < 3; i++){
			System.out.println(myNumbers[10]);
			System.out.println("dafd");
		}
	}
	public void doSomething() {
		list.add(5);
		list.add(6);
	}
}
