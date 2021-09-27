# semantic-graphs-sbt-plugin

SBT plugin for autoloading Semantic Graphs plugin for scalac

## Usage

```
sbt ^publishSigned
sbt sonatypeBundleRelease
```

## Usage in project

In `project/graphbuddy.sbt` file:

```scala
addSbtPlugin("com.virtuslab" % "semantic-graphs-sbt-plugin" % "0.2.10")
```
