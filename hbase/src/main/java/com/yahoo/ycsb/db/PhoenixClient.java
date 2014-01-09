/**
 * This class extends the Yahoo! Cloud Servicing Benchmark (YCSB),
 * to benchmark the performance of HBase database wrapped by a Phoenix interface. 
 * It create the table schema and perform workload against the database.
 * <br>This class translates simple requests (read(), scan(), insert(), update(), delete()) 
 * from the client threads into calls against the database.
 * 
 * This class extends com.yahoo.ycsb.DB and implements the database interface used by YCSB client.
 * <br>This class parse the schema information from a schema.xml document and create the table schema in the database accordingly.
 * This class parse the workload information from a workload.xml document and build the prepare statement accordingly
 * <br> This interface expects a schema <key> <field1> <field2> <field3> ...
 * All attributes are of type VARCHAR. 
 *  
 * @author Meizhen Shi
 *
 */

package com.yahoo.ycsb.db;

import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.StringByteIterator;
import com.yahoo.ycsb.generator.CounterGenerator;
import com.yahoo.ycsb.generator.DiscreteGenerator;
import com.yahoo.ycsb.generator.ExponentialGenerator;
import com.yahoo.ycsb.generator.Generator;
import com.yahoo.ycsb.generator.ConstantIntegerGenerator;
import com.yahoo.ycsb.generator.HotspotIntegerGenerator;
import com.yahoo.ycsb.generator.HistogramGenerator;
import com.yahoo.ycsb.generator.IntegerGenerator;
import com.yahoo.ycsb.generator.ScrambledZipfianGenerator;
import com.yahoo.ycsb.generator.SkewedLatestGenerator;
import com.yahoo.ycsb.generator.UniformIntegerGenerator;
import com.yahoo.ycsb.generator.ZipfianGenerator;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.yahoo.ycsb.DB;

public class PhoenixClient extends DB{
	  private Properties props;
	  private Properties prop=new Properties();
	  private Connection con;
	  private HashMap<String,PreparedStatement> cachedPreparedStatement;
	  PreparedStatement readStatement;
	  PreparedStatement insertStatement;
	  PreparedStatement scanStatement;
	  PreparedStatement updateStatement;
	  PreparedStatement deleteStatement;
	  IntegerGenerator col;
	  PreparedStatement createSt;
	  String create;
	 
	  /**
	     * Initialize any state for this DB.
	     * Called once per DB instance; there is one DB instance per client thread.
	     */
	  @Override
	  public void init() throws DBException {
		  props=getProperties();
		  try{
			  Class.forName("com.salesforce.phoenix.jdbc.PhoenixDriver");
			  con = DriverManager.getConnection("jdbc:phoenix:yahoo029.nicl.cs.duke.edu,yahoo030.nicl.cs.duke.edu,yahoo031.nicl.cs.duke.edu:2181",props);
		      System.out.println("Finish connection to phoenix database");
		      //con.setAutoCommit(true);
		      System.out.println(con.getAutoCommit());
		      //insert
		    //parse an xml document to get the schema information, then build the prepareStatement according to this
				File fXmlFile = new File("/home/meizhen/workspace/phoenixTest/src/Schema2.xml");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				NodeList nList = doc.getElementsByTagName("table");
				System.out.println(nList.getLength());
		 
				for (int temp = 0; temp < nList.getLength(); temp++) {
		 
					Node nNode = nList.item(temp);
		 
					System.out.println("\nCurrent Element :" + nNode.getNodeName());
		 
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
						Element eElement = (Element) nNode;
		 
						System.out.println("Staff id : " + eElement.getAttribute("name"));
						create= eElement.getElementsByTagName("statement").item(0).getTextContent();
						createSt=con.prepareStatement(create);
						//System.out.println("proportion : " + eElement.getElementsByTagName("proportion").item(0).getTextContent());
						NodeList binding=eElement.getElementsByTagName("binding");
						if (binding!=null){
						//NodeList children=binding.getChildNodes();
						int len= binding.getLength();
						System.out.println(len);
						String[] bindings=new String[len];
						for (int i = 0; i < len; i++) {
							Node textChild = binding.item(i);
							Element e=(Element)textChild;
							String type=e.getAttribute("type");						
							bindings[i]=textChild.getTextContent();						
							System.out.print(bindings[i]);
							if (type.equals("Bytes")){
								createSt.setBytes(i+1,bindings[i].getBytes());
							}
							if (type.equals("Integer")){
								createSt.setInt(i+1,Integer.parseInt(bindings[i]));							
							}
							if (type.equals("String")){
								createSt.setString(i+1,bindings[i]);							
							}
							if (type.equals("Double")){
								createSt.setDouble(i+1,Double.parseDouble(bindings[i]));							
							}
						}					
					//System.out.println(bind.getTextContent());										
					//System.out.println("binding : " + eElement.getElementsByTagName("binds").item(1).getTextContent());
					/*for (String s:bindings){
						System.out.println(s);
					}*/
						}
				createSt.executeUpdate();
				}
			}
		      //
		  }
		  catch (ClassNotFoundException e) {
			  System.err.println("Error in initializing the Phoenix driver: " + e);
			  throw new DBException(e);
		  } 
		  catch (ParserConfigurationException e){
				  System.err.println("Error in Parser: " + e);
			 }
		  catch (SAXException e){
				  System.err.println("Error in SAX: " + e);
			 }
		  catch (SQLException e) {
			  System.err.println("Error in database operation: " + e);
	      throw new DBException(e);
	      } 
		  catch (IOException e){
			  System.err.println("Error in IO: " + e);
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
		  con.commit();
		  System.out.println("Finish creating table in phoenix");
		  }
		  catch(SQLException e){
			  System.out.println("Error in database operation of creating table in phoenix");
		  }		  		  
		  cachedPreparedStatement=new HashMap<String, PreparedStatement>();
		  try{
			  col=new UniformIntegerGenerator(1,200);
			  String read=props.getProperty("read");
			  readStatement=con.prepareStatement(read);
			  String insert=props.getProperty("insert");
			  insertStatement=con.prepareStatement(insert);
			  String scan=props.getProperty("scan");
              scanStatement=con.prepareStatement(scan);
			  //cachedPreparedStatement.put(read,readStatement);
              //insert: initialize statement:
            //parse an xml document to get the workload information, then build the prepareStatement according to this
              File fXmlFile = new File("/home/meizhen/workspace/phoenixTest/src/workload.xml");
  			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
  			Document doc = dBuilder.parse(fXmlFile);
  			NodeList nList = doc.getElementsByTagName("query");
  			System.out.println(nList.getLength());
  			PreparedStatement selectSt;
  			String select;
  	 
  			for (int temp = 0; temp < nList.getLength(); temp++) {
  	 
  				Node nNode = nList.item(temp);
  	 
  				System.out.println("\nCurrent Element :" + ((Element)nNode).getAttribute("name"));
  	 
  				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
  	 
  					Element eElement = (Element) nNode;
  					
  	 
  					System.out.println("Staff id : " + eElement.getAttribute("name"));
  					String name=eElement.getAttribute("name");
  					select= eElement.getElementsByTagName("statement").item(0).getTextContent();
  					//selectSt=conn.prepareStatement(select);
  					//System.out.println("proportion : " + eElement.getElementsByTagName("proportion").item(0).getTextContent());
  					//String proportion=eElement.getElementsByTagName("proportion").item(0).getTextContent();
  					if (name.equals("read")) {
  						readStatement=con.prepareStatement(select);
  					}
  					if (name.equals("scan")) {
  						scanStatement=con.prepareStatement(select);
  					}
  					if (name.equals("update")) {
  						updateStatement=con.prepareStatement(select);
  					}
  					if (name.equals("insert")) {
  						insertStatement=con.prepareStatement(select);
  					}
  					if (name.equals("delete")) {
  						deleteStatement=con.prepareStatement(select);
  					}
              //
  				}
  			}
		  }
		  catch (SQLException e){
			  System.err.println("Error in preparing reading statement "+e);
		  }
		  catch (SAXException e){
			  System.err.println("Error in SAX: " + e);
		 }	 
		  catch (IOException e){
		  System.err.println("Error in IO: " + e);
		  }
		  catch (ParserConfigurationException e){
			  System.err.println("Error in Parser: " + e);
		 }
	  }
	  
	  /**
	     * Cleanup any state for this DB.
	     * Called once per DB instance; there is one DB instance per client thread.
	     */
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

	  /**
	     * Read a record from the database. Each field/value pair from the result will be stored in a HashMap.
	     *
	     * @param table The name of the table
	     * @param key The record key of the record to read.
	     * @param fields The list of fields to read, or null for all of them
	     * @param result A HashMap of field/value pairs for the result
	     * @return Zero on success, a non-zero error code on error
	     */
	  @Override
		public int read(String tableName, String key, Set<String> fields,
				HashMap<String, ByteIterator> result) {
		  //String read=props.getProperty("read");
		  try{
		  //PreparedStatement readStatement=con.prepareStatement(read);
		  readStatement.setString(1, key);
	      ResultSet resultSet = readStatement.executeQuery();
	      if (!resultSet.next()) {
	          resultSet.close();
	          return 1;
	        }
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
	  
	  /**
	     * Perform a range scan for a set of records in the database. Each field/value pair from the result will be stored in a HashMap.
	     *
	     * @param table The name of the table
	     * @param startkey The record key of the first record to read.
	     * @param recordcount The number of records to read
	     * @param fields The list of fields to read, or null for all of them
	     * @param result A Vector of HashMaps, where each HashMap is a set field/value pairs for one record
	     * @return Zero on success, a non-zero error code on error
	     */
	  @Override
		public int scan(String tableName, String startKey, int recordcount,
				Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
		  if (startKey == null) {
		      return -1;
		    }
		  ///String scan=props.getProperty("scan");
		  try{
		  //PreparedStatement scanStatement=con.prepareStatement(scan);
		  scanStatement.setString(1, startKey);
		  scanStatement.setInt(2,recordcount);
	      //ResultSet resultSet = scanStatement.executeQuery();
		  scanStatement.executeQuery();
		  /* if (!resultSet.next()) {
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
	        }*/
	        //resultSet.close();
	        return 0;
		  }
		  catch (SQLException e){
			  System.err.println("Error in processing read of table " + tableName + ": "+e);
			  return -2;
		  }		 		  
	  }
	  //overload
	  public int scan(String tableName, String startKey, int recordcount, HashMap<String, ByteIterator> values,HashMap<String, ByteIterator> result){
		  
		  return 1;
	  }

	  
	  /**
	     * Insert a record in the database. Any field/value pairs in the specified values HashMap will be written into the record with the specified
	     * record key.
	     *
	     * @param table The name of the table
	     * @param key The record key of the record to insert.
	     * @param values A HashMap of field/value pairs to insert in the record
	     * @return Zero on success, a non-zero error code on error
	     */
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
		    
	        insertStatement.setString(1, key);
	        int index = 2;
	        for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
	       //for (int i=0; i<10; i++) {
	          String field = entry.getValue().toString();
	          insertStatement.setString(index++, entry.getValue().toString());
	        	//insertStatement.setString(index++, "aaaaaaaaaaaaaaa");
	        }
	        insertStatement.executeUpdate();
	        //con.commit();
	        //if (result == 1) return 0;
	        //else return 1;
	        //insertStatement.close();
	        
	    } catch (SQLException e) {
	      System.err.println("Error in processing insert to table: " + tableName + e);
	      return -1;
	    }
		  return 0;
		}
	
		 /**
	     * Update a record in the database. Any field/value pairs in the specified values HashMap will be written into the record with the specified
	     * record key, overwriting any existing values with the same field name.
	     *
	     * @param table The name of the table
	     * @param key The record key of the record to write
	     * @param values A HashMap of field/value pairs to update in the record
	     * @return Zero on success, a non-zero error code on error
	     */
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
		      String update=props.getProperty("update");
			  PreparedStatement updateStatement=con.prepareStatement(update);
		      updateStatement.setString(1, key);
		      int index = 2;     
		      for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
		    	//updateStatement.setString(1, entry.getKey().toString());
		        //updateStatement.setString(2, entry.getValue().toString());
		        updateStatement.setString(index, Integer.toString(col.nextInt()));
		        index++;
		      }
		      //}
		      //updateStatement.setString(index, key);
		      int result = updateStatement.executeUpdate();
		      if (result == 1) return 0;
		      else return 1;
		    } catch (SQLException e) {
		    	System.err.println("Error in processing update to table: " + tableName + e);
		      return -1;
		    }
			}
		
		 /**
	     * Delete a record from the database.
	     * @param table The name of the table
	     * @param key The record key of the record to delete.
	     * @return Zero on success, a non-zero error code on error
	     */
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
