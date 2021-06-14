package wekaPre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import vt.edu.sql.SqliteManager;

public class TPMap {

	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		System.out.println(getMap("afcfData1_meteor").size());

	}
	
	public static Map<String, HashSet<String>> getMap(String dataTable) throws SQLException {
		Map<String, HashSet<String>> map = new HashMap<>();
		Connection conn = SqliteManager.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + dataTable);
		rs.next();
		int totalNum = rs.getInt(1);
		System.out.println(totalNum);
		
		stmt.close();
		conn.close();
		
		for (int offset = 0; offset < totalNum; offset++) {
			conn = SqliteManager.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + dataTable + " LIMIT 1 OFFSET " + offset);
			String afSig = rs.getString(1), cf1Sig = rs.getString(2), candiSig = rs.getString(3),
					changed = rs.getString(4);
//			System.out.println(offset + "/" + totalNum);
			if (changed.equals("true")) {
				String key = afSig + cf1Sig;
				if (!map.containsKey(key)) map.put(key, new HashSet<String>());
				map.get(key).add(candiSig);
			}
			rs.close();
			stmt.close();
			conn.close();
	    }
		
		return map;
	}
}
