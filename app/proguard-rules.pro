# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Firebase UI Auth uses deprecated Credentials API (Smart Lock for Passwords)
# These classes are no longer available but FirebaseUI can work without them
-dontwarn com.google.android.gms.auth.api.credentials.Credential$Builder
-dontwarn com.google.android.gms.auth.api.credentials.Credential
-dontwarn com.google.android.gms.auth.api.credentials.CredentialRequest$Builder
-dontwarn com.google.android.gms.auth.api.credentials.CredentialRequest
-dontwarn com.google.android.gms.auth.api.credentials.CredentialRequestResponse
-dontwarn com.google.android.gms.auth.api.credentials.Credentials
-dontwarn com.google.android.gms.auth.api.credentials.CredentialsClient
-dontwarn com.google.android.gms.auth.api.credentials.CredentialsOptions$Builder
-dontwarn com.google.android.gms.auth.api.credentials.CredentialsOptions
-dontwarn com.google.android.gms.auth.api.credentials.HintRequest$Builder
-dontwarn com.google.android.gms.auth.api.credentials.HintRequest

# Keep Firebase Authentication classes
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }

# Keep Firestore classes
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class com.google.firebase.firestore.** { *; }

# Keep Firebase Database classes
-keep class com.google.firebase.database.** { *; }
-keepclassmembers class com.google.firebase.database.** { *; }

# Keep model classes used with Firebase (prevents field name obfuscation)
-keep class com.android.sample.model.** { *; }
-keepclassmembers class com.android.sample.model.** { *; }

# Keep authentication repository
-keep class com.android.sample.model.authentication.** { *; }

# Firebase UI
-keep class com.firebase.ui.auth.** { *; }
-keepclassmembers class com.firebase.ui.auth.** { *; }

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep attributes for Firebase serialization
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# R8 Full Mode
-allowaccessmodification

# Keep FirebaseAuth getInstance method
-keepclassmembers class com.google.firebase.auth.FirebaseAuth {
    public static *** getInstance();
}

# Keep Firestore getInstance method
-keepclassmembers class com.google.firebase.firestore.FirebaseFirestore {
    public static *** getInstance();
}

# Keep serialization for Firestore models
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# Prevent obfuscation of enum classes used with Firebase
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep data classes used with Firebase
-keep class com.android.sample.model.user.Profile { *; }
-keep class com.android.sample.model.user.** { *; }
-keep class com.android.sample.model.map.** { *; }

# Google Play Services - Additional rules
-keep class com.google.android.gms.tasks.** { *; }
-keep class com.google.android.gms.internal.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

