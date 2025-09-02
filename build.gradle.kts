plugins {
    id("com.android.library") version "8.1.1" apply false
    kotlin("android") version "1.9.0" apply false
}

tasks.register("make") {
    dependsOn(":AnimesRollProvider:make")
}
