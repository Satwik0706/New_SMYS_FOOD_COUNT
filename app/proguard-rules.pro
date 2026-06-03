# Keep Firebase and Data Models from being deleted or renamed
-keep class com.satwik.oodapplication.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
