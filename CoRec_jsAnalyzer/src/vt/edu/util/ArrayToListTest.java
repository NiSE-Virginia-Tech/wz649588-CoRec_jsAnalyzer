package vt.edu.util;

import java.io.IOException;
import java.util.Arrays;

public class ArrayToListTest {
	public static void main(String[] args){
		String[] str = {"dsaf","dsafd","redagf.js"};
		System.out.println(Arrays.asList(str).contains("redagf.js"));
	}
}
