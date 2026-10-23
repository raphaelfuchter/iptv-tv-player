# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.rf.iptvtv.** {
    *** Companion;
}
-keepclasseswithmembers class com.rf.iptvtv.** {
    kotlinx.serialization.KSerializer serializer(...);
}
