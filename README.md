# swipemenu-listview
带侧滑菜单的ListView

根据[https://github.com/baoyongzhang/SwipeMenuListView](https://github.com/baoyongzhang/SwipeMenuListView)修改而来。

修改内容：

- 有条目菜单打开时，触摸任意位置关闭条目。
- 优化手势加速开启、关闭条目菜单。
- 其他一些小修改

# 使用方法
1. 因为使用了jdk8的一些特性，需要在module的build.gradle里添加如下配置：
```
//纯java的项目
android {
	compileOptions {
		sourceCompatibility JavaVersion.VERSION_1_8
		targetCompatibility JavaVersion.VERSION_1_8
	}
}

//有kotlin的项目还需要在project的build.gradle里添加
allprojects {
    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        kotlinOptions {
            jvmTarget = '1.8'
            apiVersion = '1.3'
            languageVersion = '1.3'
        }
    }
}
```

2. module的build.gradle中的添加依赖，自行修改为最新版本，同步后通常就可以用了：
```
dependencies {
	...
	implementation 'cn.wandersnail:swipemenulistview:latestVersion'
}
```

2. 如果从jcenter下载失败。在project的build.gradle里的repositories添加内容，最好两个都加上，添加完再次同步即可。
```
allprojects {
	repositories {
		...
		mavenCentral()
		maven { url 'https://dl.bintray.com/wandersnail/androidx/' }
	}
}
```

## 代码托管
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/swipemenu-listview/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/swipemenu-listview)
[![Download](https://api.bintray.com/packages/wandersnail/androidx/swipemenu-listview/images/download.svg) ](https://bintray.com/wandersnail/androidx/swipemenu-listview/_latestVersion)

## 示例效果
![image](https://github.com/wandersnail/swipemenu-listview/blob/master/screenshot/device-2018-05-27-191134.png)
![image](https://github.com/wandersnail/swipemenu-listview/blob/master/screenshot/device-2018-05-27-191220.png)
![image](https://github.com/wandersnail/swipemenu-listview/blob/master/screenshot/device-2018-05-27-191233.png)