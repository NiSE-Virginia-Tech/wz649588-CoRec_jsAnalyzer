package vt.edu.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

public class GsonTest {
	public static void main(String[] args) throws FileNotFoundException{
		HashMap<String, List<String>> testMap = new HashMap<>();
		testMap.put("a", new ArrayList<String>());
		testMap.put("b", new ArrayList<String>());
		testMap.get("a").add("aa");
		testMap.get("a").add("aaa");
		testMap.get("b").add("bb");
		Gson gson = new Gson();
		String json = gson.toJson(testMap);
		System.out.println(json);
		File saveFile = new File("/Users/zijianjiang/Documents/json/test.json");
		try (PrintStream out = new PrintStream(new FileOutputStream(saveFile))) {
		    out.print(json);
		}
	}
	
	
}
