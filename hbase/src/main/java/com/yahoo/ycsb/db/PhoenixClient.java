package com.yahoo.ycsb.db;

import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.StringByteIterator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import com.yahoo.ycsb.DB;

public class PhoenixClient extends DB{
	  private Properties props;
	  private Properties prop=new Properties();
	  private Connection con;
	  @Override
	  public void init() throws DBException {
		  props=getProperties();
		  try{
			  Class.forName("com.salesforce.phoenix.jdbc.PhoenixDriver");
			  con = DriverManager.getConnection("jdbc:phoenix:yahoo029.nicl.cs.duke.edu,yahoo030.nicl.cs.duke.edu,yahoo031.nicl.cs.duke.edu:2181",prop);
		      System.out.println("Finish connection to phoenix database");
		  }
		  catch (ClassNotFoundException e) {
			  System.err.println("Error in initializing the JDBS driver: " + e);
			  throw new DBException(e);
		  } 
		  catch (SQLException e) {
			  System.err.println("Error in database operation: " + e);
	      throw new DBException(e);
	      } 
		  catch (NumberFormatException e) {
	      System.err.println("Invalid value for fieldcount property. " + e);
	      throw new DBException(e);
	    }
		  // create a table in phoenix for benchmark
		  try{
		  String create=props.getProperty("create");
		  PreparedStatement createSt=con.prepareStatement(create);
		  System.out.println("Finish preparing statement");
		  createSt.executeUpdate();
		  //con.commit();
		  System.out.println("Finish creating table in phoenix");
		  }
		  catch(SQLException e){
			  System.out.println("Error in database operation of creating table in phoenix");
		  }		  		  
	  }
	  @Override
		public void cleanup() throws DBException {
		  try {
	      con.close();
	      } 
		  catch (SQLException e) {
	      System.err.println("Error in closing the connection. " + e);
	      throw new DBException(e);
	      }
	  }

	  @Override
		public int read(String tableName, String key, Set<String> fields,
				HashMap<String, ByteIterator> result) {
		  String read=props.getProperty("read");
		  try{
		  PreparedStatement readStatement=con.prepareStatement(read);
		  readStatement.setString(1, key);
	      ResultSet resultSet = readStatement.executeQuery();
	      if (!resultSet.next()) {
	          resultSet.close();
	          return 1;
	        }//what if readallfields???
	        if (result != null && fields != null) {
	          for (String field : fields) {
	            String value = resultSet.getString(field);
	            result.put(field, new StringByteIterator(value));
	          }
	        }
	        resultSet.close();
	        return 0;
		  }
		  catch (SQLException e){
			  System.err.println("Error in processing read of table " + tableName + ": "+e);
			  return -2;
		  }		 		  
	  }
	  
	  @Override
		public int scan(String tableName, String startKey, int recordcount,
				Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
		  if (startKey == null) {
		      return -1;
		    }
		  String scan=props.getProperty("scan");
		  try{
		  PreparedStatement scanStatement=con.prepareStatement(scan);
		  scanStatement.setString(1, startKey);
		  scanStatement.setInt(2,recordcount);
	      ResultSet resultSet = scanStatement.executeQuery();
	      if (!resultSet.next()) {
	          resultSet.close();
	          return 1;
	        }//what if readallfields???
	      for (int i = 0; i < recordcount && resultSet.next(); i++) {
	          if (result != null && fields != null) {
	            HashMap<String, ByteIterator> values = new HashMap<String, ByteIterator>();
	            for (String field : fields) {
	              String value = resultSet.getString(field);
	              values.put(field, new StringByteIterator(value));
	            }
	            result.add(values);
	          }
	        }
	        resultSet.close();
	        return 0;
		  }
		  catch (SQLException e){
			  System.err.println("Error in processing read of table " + tableName + ": "+e);
			  return -2;
		  }		 		  
	  }	  

		@Override
		public int insert(String tableName, String key, HashMap<String, ByteIterator> values) {
		  if (tableName == null) {
		    return -1;
		  }
		  if (key == null) {
		    return -1;
		  }
		  try {
		    //int numFields = values.size();
		    String insert=props.getProperty("insert");
		    PreparedStatement insertStatement=con.prepareStatement(insert);
	        insertStatement.setString(1, key);
	        int index = 2;
	        for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
	          String field = entry.getValue().toString();
	          insertStatement.setString(index++, field);
	        }
	        int result = insertStatement.executeUpdate();
	        if (result == 1) return 0;
	        else return 1;
	    } catch (SQLException e) {
	      System.err.println("Error in processing insert to table: " + tableName + e);
	      return -1;
	    }
		}
		
		@Override
		public int update(String tableName, String key, HashMap<String, ByteIterator> values) {
			  if (tableName == null) {
		      return -1;
		    }
		    if (key == null) {
		      return -1;
		    }
		    try {
		      //int numFields = values.size();
		      String insert=props.getProperty("insert");
			  PreparedStatement updateStatement=con.prepareStatement(insert);
		      updateStatement.setString(1, key);
		      int index = 2;     
		      for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
		        updateStatement.setString(index++, entry.getValue().toString());
		      }
		      updateStatement.setString(index, key);
		      int result = updateStatement.executeUpdate();
		      if (result == 1) return 0;
		      else return 1;
		    } catch (SQLException e) {
		      System.err.println("Error in processing update to table: " + tableName + e);
		      return -1;
		    }
			}
		
		@Override
		public int delete(String tableName, String key) {
		  if (tableName == null) {
	      return -1;
	    }
	    if (key == null) {
	      return -1;
	    }
	    try {
	      String delete=props.getProperty("delete");
	      PreparedStatement deleteStatement=con.prepareStatement(delete);
	      deleteStatement.setString(1, key);
	      int result = deleteStatement.executeUpdate();
	      if (result == 1) return 0;
	      else return 1;
	    } catch (SQLException e) {
	      System.err.println("Error in processing delete to table: " + tableName + e);
	      return -1;
	    }
		}
		

}
