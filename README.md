# Project Name

DbAssist offers fix for Hibernate date time shift, occuring in case of JVM and DB set up in different time zones.
Project also introduces ConditionsBuilder class which enables the user to easily create complex logical combinations of conditions in the SQL query.

## Installation

To simply install the fix, add following dependency into Maven .pom file:

For JPA Annotations:
<dependency>
    <groupId>com.montrosesoftware</groupId>
    <artifactId>DbAssist-jpa</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

For HBM files:
<dependency>
    <groupId>com.montrosesoftware</groupId>
    <artifactId>DbAssist-hbm</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

## Compatibility

For JPA Annotations: from Hibernate 4.3.11.Final to 5.2.2.Final
For .hbm files: TODO

## Usage

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

Result:
WHERE (c1 AND c2) OR c3 OR (c4 AND c5)

More examples and tutorial for DbAssist library is available on the wiki page: TODO

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
