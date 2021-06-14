package jstest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class jsToJson {
	String f1;
	
	public static void main(String[] args) throws IOException {
		jsToJson jJ = new jsToJson("/Users/zijianjiang/Documents/esprima/jsonScript.js");

		jJ.cmdForEs();
	}
//	
	public jsToJson(String f1){
		this.f1 = f1;
	}
	public String cmdForEs() throws IOException{
		String command = "/usr/local/bin/node /Users/zijianjiang/Documents/esprima/jsonModule.js " + f1;
		String command2 = "/usr/local/bin/node /Users/zijianjiang/Documents/esprima/jsonScript.js " + f1;
		StringBuilder sb = new StringBuilder();
//		System.out.println(sb.toString() == null);
		Process proc = null;
		proc = Runtime.getRuntime().exec(command);
		
		
		BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String output = null;
		while ((output = br.readLine()) != null) {
			sb.append(output);
			sb.append("\n");
		}
		br.close();
		String s = sb.toString();
//		System.out.println(s);
		if (s.length() == 0) {
			proc = Runtime.getRuntime().exec(command2);
			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((output = br.readLine()) != null) {
				sb.append(output);
				sb.append("\n");
			}
			br.close();
			s = sb.toString();
//			System.out.println(s);
		}
		return s;
	}
	
	
}

