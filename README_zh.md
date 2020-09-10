# Hin2n <img height="24" src="doc/pic/logo.png">

[README](README.md) | [中文文档](README_zh.md)

n2n是一个支持内网穿透p2p的VPN项目，最初由ntop.org大神`Luca Deri` <deri@ntop.org>, `Richard Andrews` <andrews@ntop.org>开发并开源的项目，后由`meyerd`大神 <https://github.com/meyerd>继续做优化工作。我们的目的是为n2n提供`手机版本`的支持。

[![gradle](https://img.shields.io/badge/gradle-2.14.1-green.svg?style=plastic)](https://docs.gradle.org/2.14.1/userguide/userguide.html)
[![API](https://img.shields.io/badge/API-15%2B-green.svg?style=plastic)](https://android-arsenal.com/api?level=15)
[![license](https://img.shields.io/github/license/switch-iot/hin2n.svg?style=plastic)](https://www.gnu.org/licenses/gpl-3.0)
[![GitHub release](https://img.shields.io/github/release/switch-iot/hin2n/all.svg?style=plastic)](https://github.com/switch-iot/hin2n/releases)
[![Github All Releases](https://img.shields.io/github/downloads/switch-iot/hin2n/total.svg?style=plastic)](https://github.com/switch-iot/hin2n/releases)
[![Travis branch](https://img.shields.io/travis/switch-iot/hin2n/dev_android.svg?style=plastic)](https://travis-ci.org/switch-iot/hin2n)

原版的n2n支持很多平台，包括windows，linux，osx，bsd，openwrt，raspberry pie等，唯独缺少对手机(非root)的支持。因此，我们开发了hin2n项目。

### Hin2n是什么
- Hin2n是支持n2n协议的手机VPN软件
- 该APP不需要root手机
- 该APP暂时只支持安卓手机，后续会发开IPhone版本
- 该项目现处于持续开发阶段，后续会提供更完善的功能
- 该项目现已支持全部v1/v2/v2s协议

### Hin2n最新版本 [CHANGELOG](Hin2n_android/CHANGELOG_zh)
Hin2n最新版本可在[release地址](https://github.com/switch-iot/hin2n/releases)查看下载。

### Hin2n开发计划
详细开发计划请见[`Projects`](https://github.com/switch-iot/hin2n/projects)。
大家如果有新需求和想法，任何意见建议均可提交在[`issues`](https://github.com/switch-iot/hin2n/issues)中，我们将会酌情安排开发计划。您的关注就是我们的动力。

### 技术原理
- VPNService
> Hin2n基于安卓原生提供的VPNService，通过VPNService建立tun虚拟网卡，与supernode和edge通讯。
- tun2tap
> 安卓上层仅支持建立tun虚拟网卡，仅是TCP/IP网络层，而n2n协议依赖tap虚拟网卡，需要对数据链路层的支持，因此我们模拟了数据链路层，并实现了ARP协议。
- n2n protocol
> Hin2n对n2n协议的支持是采用jni的方式，native方法可以尽量复用原n2n项目的代码。

## n2n协议版本
n2n项目现有三个主流版本
- ntop.org大神们维护的 v1 版本，停止更新，项目地址：https://github.com/meyerd/n2n.git
- meyerd大神维护的 v2s 版本，停止更新，项目地址：https://github.com/meyerd/n2n.git
- ntop.org大神们维护的 v2 版本，正在更新，项目地址：https://github.com/ntop/n2n.git

### 关于v2s版本
v2s版本是N2N交流QQ群(5804301)中对meyerd大神维护的v2版本(又称v2.1)的命名，即v2升级版，该版本与ntop.org大神们维护的v2版本并不互通，为避免混淆，群友们对该项目另行命名。

## 项目开发/编译说明
### Hin2n 在 linux 系统下的编译方法
下面以 ubuntu 为例子进行说明（需要先安装好 java 和 sdk）
- `cd /opt`
- `git clone https://github.com/switch-iot/hin2n.git --recurse-submodules` # 下载源码
- `cd hin2n/Hin2n_android` # Hin2n_android 目录即是 hin2n 项目安卓源码目录
- `./gradlew assemble` # 开始编译（你也可以使用`./gradlew assembleNormalAllarchDebug`来编译其中一个文件）。如果你要使用 android studio 来编译，那么请选择 Import Project，指向 Hin2n_android 目录，然后再选择建立 app。编译好的文件在这里 hin2n/Hin2n_android/app/build/outputs/apk/
- 切换分支时，需要执行`git submodule update`来同步 submodule 的代码

### Windows 系统下的编译方法
Windows环境下需要设置git兼容符号链接。
- 打开`gpedit.msc`，将当前账户加入设置`计算机配置/Windows 设置/安全设置/本地策略/用户权限分配/创建符号链接`中。
- 或以管理员用户运行git-cmd，并执行下述命令
- git clone -c `core.symlinks=true` https://github.com/switch-iot/hin2n.git `--recurse-submodules` && cd hin2n && `link.bat`

### 关于开源协议
该项目以[`GPLv3`](LICENSE)协议进行开源，与n2n原有开源协议保持一致，也希望大家支持并遵守本项目的开源协议。

## 为hin2n做贡献
Hin2n是一个免费且开源的n2n项目，我们欢迎任何人为其开发和进步贡献力量。
- 在使用过程中出现任何问题，可以通过[`issues`](https://github.com/switch-iot/hin2n/issues) 来反馈
- Bug的修复可以直接提交`Pull Request`到`android_dev`分支
- 如果是增加新的功能特性，请先创建一个[`issues`](https://github.com/switch-iot/hin2n/issues)并做简单描述以及大致的实现方法，提议被采纳后，就可以创建一个实现新特性的 Pull Request
- 欢迎对说明文档做出改善，帮助更多的人使用`hin2n`，特别是英文文档
- 如果您觉得hin2n对您有帮助，欢迎您关注该项目，并给项目点个`Star`

### 贡献者
- [`lucktu`](https://github.com/lucktu)是hin2n项目的发起人，并做了一些推广、测试的工作。
- [`switch`](https://github.com/switch-iot)是项目的总指挥，项目架构由他独立完成。没有 switch，就没有 hin2n。
- [`zhangbz`](https://github.com/zhangbz)主要负责Android层面的开发，在项目最困难的时候，给予了强有力的支持。
- [`emanuele-f`](https://github.com/emanuele-f)提供了最新的 hin2n 源码，并对 ntop n2n 的发展也做出了巨大的贡献。 
- 感谢他们的无私奉献，同时也感谢广大网友对 hin2n 项目的支持。

## 交流群
- Hin2n交流群： 769731491(QQ群号)
- N2N交流群： 5804301(QQ群号)

