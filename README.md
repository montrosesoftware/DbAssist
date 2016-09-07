# DbAssist

DbAssist provides the fix for the unexpected date time shift, occuring in case of JVM and DB set up in a time zone other than UTC0.
The project also introduces `ConditionsBuilder` class which enables the user to easily create complex logical combinations of conditions in the SQL query.

## Installation of the fix

### Add the dependency

In order to fix the issue with date shift, you need to determine first if you want to use JPA annotations or .hbm files to map your entities. Depending on your choice, add the following dependency to your project's pom file and pick the correct version from the table in Compatibility section.

```xml
<dependency>
    <groupId>com.montrosesoftware</groupId>
    <artifactId>DbAssist-5.2.2</artifactId>
    <version>1.0-RELEASE</version>
</dependency>
```

### Apply the fix

The fix is slightly different for both entity mapping methods:

#### For HBM case:

You do **not** modify the `java.util.Date` type of dates fields in your entity class. However, you need to change the way how they are mapped in the `.hbm` file of your entities. You can do it by using our custom type, `UtcDateType`:

`ExampleEntity.hbm.xml`
```xml
<property name="createdAtUtc" type="UtcDateType" column="created_at_utc"/>
```

`ExampleEntity.java` (not modified)
```java
public class ExampleEntity {

    private int id;
    private String name;
    private Date createdAtUtc;
   
    //setters and getters
}
```

#### For JPA case:

In case of JPA Annotations, the fix works instantly after adding the correct fix dependency.

The exception is when we are using Hibernate's `Specification` class to specify `WHERE` conditions. In order to fix it we have two options, which are described in details on the [wiki page](https://github.com/montrosesoftware/DbAssist/wiki)

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
Condition c1 = cb.lessThan("id", 15);
Condition c2 = cb.equal("name", "Mont");
...
Condition c5 = ...

//construct logical expression
Condition hc =
or(
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

More examples and the tutorial for DbAssist library is available on the [wiki page](https://github.com/montrosesoftware/DbAssist/wiki)

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
