name := "adamant-fcm-push-service"
 
version := "1.0" 
      
lazy val `adamant-fcm-push-service` = (project in file(".")).enablePlugins(PlayJava, PlayEbean, PlayEnhancer)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "sodium-bintray" at "https://dl.bintray.com/terl/lazysodium-maven/"
resolvers += "bip39-bintray" at "http://repo.spring.io/plugins-release"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq( javaJdbc , ehcache , javaWs )
libraryDependencies += "org.postgresql" % "postgresql" % "42.1.4"
// https://mvnrepository.com/artifact/com.google.firebase/firebase-admin
libraryDependencies += "com.google.firebase" % "firebase-admin" % "6.3.0"
libraryDependencies += "com.goterl.lazycode" % "lazysodium-java" % "3.3.0"
// https://mvnrepository.com/artifact/io.github.novacrypto/BIP39
// https://mvnrepository.com/artifact/io.github.novacrypto/BIP39
libraryDependencies += "io.github.novacrypto" % "BIP39" % "0.1.9"
libraryDependencies += "io.reactivex.rxjava2" % "rxjava" % "2.2.0"
// https://mvnrepository.com/artifact/com.squareup.retrofit2/retrofit
libraryDependencies += "com.squareup.retrofit2" % "retrofit" % "2.4.0"
// https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-jackson
libraryDependencies += "com.squareup.retrofit2" % "converter-jackson" % "2.4.0"
// https://mvnrepository.com/artifact/com.squareup.okhttp3/logging-interceptor
libraryDependencies += "com.squareup.okhttp3" % "logging-interceptor" % "3.11.0"
// https://mvnrepository.com/artifact/com.squareup.retrofit2/adapter-rxjava2
libraryDependencies += "com.squareup.retrofit2" % "adapter-rxjava2" % "2.4.0"


libraryDependencies += guice

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      