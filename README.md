# Project Name

DbAssist offers fix for unexpected date time shift, occuring in case of JVM and DB set up in different time zones.
Project also introduces ConditionsBuilder class which enables the user to easily create complex logical combinations of conditions in the SQL query.

## Installation

* Install Microsoft JDBC Driver 4.0 from link https://www.microsoft.com/en-us/download/details.aspx?id=11774 (tested on versions: TODO)

Installation can be done by changing the .jar name to sqljdbc4-4.0.jar and copying that file into Maven local repository \.m2\repository\com\microsoft\sqlserver\sqljdbc4\4.0\ 

* Install the fix by adding the following dependency into your project's .pom file:

### For JPA Annotations:
```xml
<dependency>
    <groupId>com.montrosesoftware</groupId>
    <artifactId>DbAssist-jpa</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### For HBM files:
```xml
<dependency>
    <groupId>com.montrosesoftware</groupId>
    <artifactId>DbAssist-hbm</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Compatibility

### For JPA Annotations: 
From Hibernate 4.3.11.Final to 5.2.2.Final

### For .hbm files:
TODO

## Usage

```java
ConditionsBuilder cb = new ConditionsBuilder();

//prepare conditions
HierarchyCondition c1 = cb.lessThan("id", 15);
HierarchyCondition c2 = cb.equal("name", "Mont");
...
HierarchyCondition c5 = ...

//construct logical expression
HierarchyCondition hc = or(
        and(c1, c2),
        or(c3, and(c4, c5))
);

//apply the conditions hierarchy to the conditions builder
cb.apply(hc);

List<User> users = uRepo.find(cb);
```

Result:
```sql
WHERE (c1 AND c2) OR c3 OR (c4 AND c5)
```

More examples and the tutorial for DbAssist library is available on the wiki page: TODO

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## Credits

Montrose Software 2016

## License

TODO
