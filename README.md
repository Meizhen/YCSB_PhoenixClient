YCSB_PhoenixClient
==================

Yahoo cloud service benchmark (YCSB) with extended client to Phoenix

mainly modified:

CoreWorkload:
YCSB_PhoenixClient / core / src / main / java / com / yahoo / ycsb / workloads / CoreWorkload.java 
Modified to take schema and workload from external XML files, build prepared statement and workload accordingly and execute workload over HBase on server

Phoenix Client:
YCSB_PhoenixClient / hbase / src / main / java / com / yahoo / ycsb / db /phoenixClient
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

use schema and workload XMLs in Phoenix_Test:

phoenixTest/src/


