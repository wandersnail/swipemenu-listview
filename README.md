# swipemenulistview
带侧滑菜单的ListView

根据[https://github.com/baoyongzhang/SwipeMenuListView](https://github.com/baoyongzhang/SwipeMenuListView)修改而来。

修改内容：

- 有条目菜单打开时，触摸任意位置关闭条目。
- 优化手势加速开启、关闭条目菜单。
- 其他一些小修改

# 使用方法

1. 在project的build.gradle里的repositories添加内容，最好两个都加上，有时jitpack会抽风，同步不下来。
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
		maven { url 'https://dl.bintray.com/wandersnail/android/' }
	}
}
```
2. module的build.gradle中的添加依赖：
```
dependencies {
	...
	implementation 'com.github.wandersnail:treeadapter:1.0.0'
}
```

## 代码托管
[![](https://jitpack.io/v/wandersnail/swipemenulistview.svg)](https://jitpack.io/#wandersnail/swipemenulistview)
[![Download](https://api.bintray.com/packages/wandersnail/android/swipemenulistview/images/download.svg) ](https://bintray.com/wandersnail/android/swipemenulistview/_latestVersion)

## 示例效果
![image](https://github.com/wandersnail/swipemenulistview/blob/master/screenshot/device-2018-05-27-191134.png)
![image](https://github.com/wandersnail/swipemenulistview/blob/master/screenshot/device-2018-05-27-191220.png)
![image](https://github.com/wandersnail/swipemenulistview/blob/master/screenshot/device-2018-05-27-191233.png)