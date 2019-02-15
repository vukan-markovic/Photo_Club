#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile
#-keep class com.firebase.** { *; }
#-keep class org.apache.** { *; }
#-keepnames class com.fasterxml.jackson.** { *; }
#-keepnames class javax.servlet.** { *; }
#-keepnames class org.ietf.jgss.** { *; }
#-dontwarn org.w3c.dom.**
#-dontwarn org.joda.time.**
#-dontwarn org.shaded.apache.**
#-dontwarn org.ietf.jgss.**
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep public class * extends com.bumptech.glide.module.AppGlideModule
#-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
#  **[] $VALUES;
#  public *;
#}
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
#-keepattributes *Annotation*
#-dontwarn java.nio.file.**
#-dontwarn org.codehaus.mojo.animal_sniffer.**
#-dontwarn javax.annotation.*
#-dontwarn javax.annotation.concurrent.*
#-dontnote retrofit2.Platform
#-dontnote retrofit2.Platform$IOS$MainThreadExecutor
#-dontwarn retrofit2.Platform$Java8
#-keepattributes Signature
#-keepattributes Exceptions
#-keepclasseswithmembers class * {
#  @retrofit2.http.* <methods>;
#}
#-keepnames class vukan.com.photoclub.beans.** { *; }
#-keepresourcexmlelements manifest/application/meta-data@name=io.fabric.ApiKey
#-keepattributes *Annotation*
#-keepattributes SourceFile,LineNumberTable
#-keep public class * extends java.lang.Exception
#-keep class com.crashlytics.** { *; }
#-dontwarn com.crashlytics.**