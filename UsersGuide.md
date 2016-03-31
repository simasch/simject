# POJO Injection #
## @Resource ##
Use @Resource to specify the attribute where dependency injection should occur.

In this example we want to inject EmployeeManager to EmployeeServiceImpl class:
```
//EmployeeServiceImpl.java

@Resource
private EmployeeManager employeeManager;
```

## XML Configuration ##
There is only little XML Configuration needed. The file must be placed in the META-INF directory! For example resources.xml
```
<resources xmlns="http://simject.org/xml/ns/resources"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://simject.org/xml/ns/resources 
  http://simject.org/xml/ns/resources/resources_1_0.xsd"
  version="1.0">

  <resource type="org.simject.demo.service.EmployeeService"
    target="org.simject.demo.service.impl.EmployeeServiceImpl" />
  <resource type="org.simject.demo.business.EmployeeManager" />

</resources>
```
type specifies the class or interface of the resource. If it is an interface you have to specify the implementation in the target attribute.

# The SimFactory #

To use the configured resources you need the central class in simject: the SimFactory:
```
SimFactory factory = new SimFactory("resources.xml");
EmployeeService service = factory.getResource(EmployeeService.class);
Employee employee = new Employee();
employee.setName("Simon Martinelli");
service.insertEmployee(employee);
System.out.println(service.listEmployees());
```

# JPA Injection #

To simplify the use of JPA you can specify an EntityManger as a resource:
```
//EmployeeManager.java

@Resource
private EntityManager em;
```

## XML Configuration ##
In the XML configuration you can specify the EntityManager and you also can define properties for the EntityManger:
```
<resource type="javax.persistence.EntityManager" name="org.simject.demo">
  <property name="hibernate.connection.driver_class" value="com.mysql.jdbc.Driver" />
  <property name="hibernate.connection.username" value="root" />
  <property name="hibernate.connection.password" value="root" />
  <property name="hibernate.connection.url" value="jdbc:mysql://localhost/demo" />
  <property name="hibernate.format_sql" value="true" />
  <property name="dialect" value="org.hibernate.dialect.MySQLDialect" />
  <property name="hibernate.hbm2ddl.auto" value="create-drop" />
</resource>
```

## persistence.xml ##
The persistence.xml will then look as follow:
```
<persistence version="1.0"
  xmlns="http://java.sun.com/xml/ns/persistence" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/persistence 
  http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd">
  
  <persistence-unit name="org.simject.demo" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.ejb.HibernatePersistence</provider>
    <class>org.simject.demo.model.Employee</class>
    <exclude-unlisted-classes />
  </persistence-unit>
</persistence>
```
Moving the properties to the resource config file will simplify the use of this persistence-unit in different environments.

# Remoting #

Remoting in simject is straight forwared as well.

## Client ##
On the client side you have to define the URL of the server resource:
```
<resource type="org.simject.demo.service.EmployeeService"
  target="xml:http://localhost:8080/simject-demo-server/remote/org.simject.demo.service.EmployeeService" />
```

### Protocol ###
By adding either xml: or bin: you can choose between XML or binary serialization.

After this prefix http://localhost:8080/simject-demo-server/remote defines the servlet listening to the requests and /org.simject.demo.service.EmployeeService ist simply the type of the requested resource.

## Server ##
On the server side you need org.simject.remote.server.SimServerServlet which dispatches all the requests and calls the method on the requested resource.

Here an example web.xml:
```
<web-app id="WebApp_ID" version="2.5" xmlns="http://java.sun.com/xml/ns/j2ee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee 
  http://java.sun.com/xml/ns/j2ee/web-app_2_5.xsd">

  <display-name>simject demo server</display-name>
  <servlet>
    <servlet-name>remote</servlet-name>
    <servlet-class>org.simject.remote.server.SimServerServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>remote</servlet-name>
    <url-pattern>/remote/*</url-pattern>
  </servlet-mapping>
  <context-param>
    <param-name>simjectConfig</param-name>
    <param-value>resources.xml</param-value>
  </context-param>
</web-app>
```
The context param "simjectConfig" specifies the filename of the configuration.

That's all!