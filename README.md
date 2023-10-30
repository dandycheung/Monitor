# Monitor

Monitor 是一个适用于 OkHttp 和 Retrofit 的可视化抓包工具

![](https://github.com/leavesCZY/Monitor/assets/30774063/f95347c1-20a7-4ec9-9f36-230a78fb4a3e)

## 导入依赖

[![Maven Central](https://img.shields.io/maven-central/v/io.github.leavesczy/monitor.svg)](https://central.sonatype.com/artifact/io.github.leavesczy/monitor)

从 v1.4.0 版本开始，Monitor 的发布地址从 jitpack 迁移到了 Maven Central，因此依赖库地址和依赖名均有所变化

```kotlin
## v1.4.0 版本开始
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

dependencies {
    val latestVersion = "x.x.x"
    debugImplementation("io.github.leavesczy:monitor:${latestVersion}")
    releaseImplementation("io.github.leavesczy:monitor-no-op:${latestVersion}")
}


## v1.4.0 版本之前
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
    }
}

dependencies {
    val latestVersion = "x.x.x"
    debugImplementation("com.github.leavesCZY.Monitor:monitor:${latestVersion}")
    releaseImplementation("com.github.leavesCZY.Monitor:monitor-no-op:${latestVersion}")
}
```

同时引入 debug 和 release 版本的依赖库

- debug 依赖用于日常的开发阶段
- release 依赖用于最终的上线阶段，此模式下的 MonitorInterceptor 不包含任何依赖，且也不会执行任何操作

## 使用

只需为 OkHttpClient 添加 MonitorInterceptor，就会自动记录并缓存所有的网络请求信息，并提供可视化界面进行查看

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(MonitorInterceptor(context = context))
    .build()
```