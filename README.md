# DbAssist

DbAssist provides the fix for the unexpected date time shift, occuring in case of JVM and DB set up in different time zones.
Project also introduces `ConditionsBuilder` class which enables the user to easily create complex logical combinations of conditions in the SQL query.

## Installation

* Install Microsoft JDBC Driver 4.0 from link `https://www.microsoft.com/en-us/download/details.aspx?id=11774`

Installation can be done by changing the .jar name to `sqljdbc4-4.0.jar` and copying that file into Maven local repository `\.m2\repository\com\microsoft\sqlserver\sqljdbc4\4.0\`

* Install the fix by adding the following dependency into your project's .pom file (for correct versions look up the table in the Compatibility section)

### For JPA Annotations:
```xml
<dependency>
    <groupId>com.montrosesoftware</groupId>
    <artifactId>DbAssist-5.2.2</artifactId>
    <version>1.0-RELEASE</version>
</dependency>
```

### For HBM files:
```xml
<dependency>
    <groupId>com.montrosesoftware</groupId>
    <artifactId>DbAssist-hbm-3.6.10</artifactId>
    <version>1.0-RELEASE</version>
</dependency>
```

## Compatibility

### Hibernate

The table shows what version of fix to use depending on the Hibernate version we use and the entity mapping method (.hbm files or JPA annotations):

| Hibernate version   | HBM                  | JPA    |
| :------------------ |:--------------------:| :-----:|
| `3.3.2.GA`          | `DbAssist-hbm-3.3.2` | N/A |
| `3.6.10.Final`      | `DbAssist-hbm-3.6.10`| N/A |
| `4.2.21.Final`      | `DbAssist-4.2.21`    | `DbAssist-4.2.21`|
| `4.3.11.Final`      | `DbAssist-4.3.11`    | `DbAssist-4.3.11`|
| `5.0.10.Final`      | `DbAssist-5.0.10`    | `DbAssist-5.0.10`|
| `5.1.1.Final`       | `DbAssist-5.1.1`     | `DbAssist-5.1.1` |
| `5.2.2.Final`       | `DbAssist-5.2.2`     | `DbAssist-5.2.2` |

### JDBC SQL Driver
* `4.0`
* `4.1`
* `4.2`

### Spring Boot: 
* `1.2.2.RELEASE`
* `1.4.0.RELEASE`

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

## License

Copyright 2016 Montrose Software

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

`http://www.apache.org/licenses/LICENSE-2.0`

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
