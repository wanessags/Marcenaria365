plugins { id("com.android.application") }
android {
    namespace = "br.com.nexo.marcenaria365"
    compileSdk = 35
    defaultConfig {
        applicationId = "br.com.nexo.marcenaria365"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-demo"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies { testImplementation("junit:junit:4.13.2") }
