package vt.edu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class StringTest {

	public static void main(String[] args) throws IOException {
		System.out.println(true + "");
////		// TODO Auto-generated method stub
		System.out.println("\\\"Hello World\\\"".replace("\\\"",""));
		String path = "lib++buildDeps.js";
		String[] str1 = path.split("\\.");
		System.out.println(str1[1]);
		String[] str = path.split("\\+\\+");
		String fileName = str[str.length - 1];
		
		System.out.println(fileName.substring(0, fileName.length() - 3));
		
		System.out.println("@electron/internal/a/b/c".replace("@electron/internal", "lib"));
		
		System.out.println("@electron/internal/a/b/c".replace("@electron/internal", "lib").replace("/", "++"));
		
		StringBuilder sb = new StringBuilder();
		sb.append("a");
		sb.append("/");
		sb.append("..");
		sb.append("/");
		sb.append("b");
		sb.append("/");
		sb.delete(sb.length() - 1, sb.length());
		System.out.println("./" + sb.toString());
		
		List<String> list = new ArrayList<String>();
		list.add("dsadf");
		list.add(".");
		list.add("ioi");
		
		System.out.println(list.indexOf("."));
		
		String output = "v1.2.3-456-345";
		System.out.println(output.split("\\s*").length);
		
		String x = "\"a/b/c";
		System.out.println(x.replace("/", "-"));
//		File a = new File("/Users/zijianjiang/Documents/R2C/meteor/dsg.tgz");
//		a.delete();
//		copyFileUsingStream(new File("/Users/zijianjiang/Documents/R2C/meteor/package.json"), new File(
//				"/Users/zijianjiang/Documents/R2C/package.json"));
		String xx = "/b/a/..";
		System.out.println(xx.split("/")[1]);
		String name = "upper++realName";
		String upper = "", realName = name;
		if (name.contains("++")) {
			String[] names = name.split("\\+\\+");
			upper = names[0];
			realName = names[1];
		}
		System.out.println(upper + " " + realName);
		System.out.println(null + " class");
		String fullName = "Human++age";
		String shortSig = fullName;
		if (fullName.contains("++")) {
			shortSig = "a++b++Human" + fullName.substring(name.indexOf("++"));
		}
		System.out.println(shortSig);
		TestThrow TT = new TestThrow();
		TT.doSomething();
		System.out.println(TT.list.size());
		TestThrow TT2 = new TestThrow();
		System.out.println(TT2.list.size());
		
		int xxx = 5, yyy = 4;
		switch(xxx) {
			case 4 : {
				
			}
			case 5 : {
				if  (yyy > 3) {
					System.out.println(yyy);
//					break;
				}
				System.out.println("can it go here?");
				break;
			}
		}
	
	}
	private static void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}

}


