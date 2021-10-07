# semantic-graphs-scala2-support
Semantic Graphs generator for Scala 2 based projects


To publish the plugin locally, please run
```
sbt +publishM2
```

## Publishing to bintray repo

Below command will publish separate artifact for each scala version defined in `build.sbt` file.
```
sbt +publishSigned
sbt sonatypeBundleRelease
```

__Note: you need to have `~/.bintray/.credentails` configured__
