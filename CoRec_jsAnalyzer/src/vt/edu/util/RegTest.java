package vt.edu.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		Pattern issue = Pattern.compile("bug|fix|error|patch|issue *\\d+");
		Matcher matcher = issue.matcher("sadsd issue234");
		System.out.println(matcher.find());
		System.out.println(matcher.start());
		System.out.println(matcher.end());
		System.out.println(getIssueId("fsafsdafdsfaissue  234. asklfladsf"));
	}
	public static int getIssueId(String message) {
		Pattern pattern = Pattern.compile("issue *\\d+");
		Matcher matcher = pattern.matcher(message.toLowerCase());
		if(matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			return Integer.valueOf(message.substring(start + 5, end).trim());
		}
		else return -1;
	}
}
