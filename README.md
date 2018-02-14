# ECR Scan

To release:
```
mvn release:clean release:prepare release:perform \
    -Dcsstudio.composite.repo=//home/user/git/org.csstudio.frib.product/p2repo -Dcs-studio=false \
    -Darguments="-Dcsstudio.composite.repo=//home/user/git/org.csstudio.frib.product/p2repo -Dcs-studio=false" \
    -Dgoals=deploy
```
