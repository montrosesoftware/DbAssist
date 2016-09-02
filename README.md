# DbAssist

DbAssist provides the fix for the unexpected date time shift, occuring in case of JVM and DB set up in different time zones.
Project also introduces `ConditionsBuilder` class which enables the user to easily create complex logical combinations of conditions in the SQL query.

## Installation

### Add dependency

In order to fix the issue with date shift, you need to determine first if you want to use JPA annotations or .hbm files. Depending on your choice, use one of the following:

#### For JPA Annotations:

```xml
<dependency>
    <groupId>com.montrosesoftware</groupId>
    <artifactId>DbAssist-5.2.2</artifactId>
    <version>1.0-RELEASE</version>
</dependency>
```

#### For HBM files:

```xml
<dependency>
    <groupId>com.montrosesoftware</groupId>
    <artifactId>DbAssist-hbm-3.6.10</artifactId>
    <version>1.0-RELEASE</version>
</dependency>
```
## Compatibility

### Hibernate
The list of supported Hibernate versions and their fix counterparts is in the table below:

| Hibernate version | HBM                  | JPA    |
| :---------------- |:--------------------:| :-----:|
| 3.3.2.GA          | `DbAssist-hbm-3.3.2` | N/A |
| 3.6.10.Final      | `DbAssist-hbm-3.6.10`| N/A |
| 4.2.21.Final      | `DbAssist-4.2.21`    | `DbAssist-4.2.21`|
| 4.3.11.Final      | `DbAssist-4.3.11`    | `DbAssist-4.3.11`|
| 5.0.10.Final      | `DbAssist-5.0.10`    | `DbAssist-5.0.10`|
| 5.1.1.Final       | `DbAssist-5.1.1`     | `DbAssist-5.1.1` |
| 5.2.2.Final       | `DbAssist-5.2.2`     | `DbAssist-5.2.2` |

### JDBC SQL Driver
* `4.0`
* `4.1`
* `4.2`

### Spring Boot: 
* `1.2.2.RELEASE`
* `1.4.0.RELEASE`

## Usage of `DbAssist-jpa-commons` library

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

The MIT License (MIT)
Copyright (c) 2016 Montrose Software

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.