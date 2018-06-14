# n2n_vLTS

[README](README.md) | [中文文档](README_zh.md)

N2N is a VPN project that supports p2p. It was originally developed and open sourced by `Luca Deri` <deri@ntop.org>, `Richard Andrews` <andrews@ntop.org> of ntop.org, and `Meyerd` <https://github.com/meyerd> continues to do optimization work. Our goal is to continuously optimize n2n based on several masters and provide `mobile version` support.

## Hin2n <img height="24" src="doc/pic/logo.png">

[![gradle](https://img.shields.io/badge/gradle-2.14.1-green.svg?style=plastic)](https://docs.gradle.org/2.14.1/userguide/userguide.html)
[![API](https://img.shields.io/badge/API-15%2B-green.svg?style=plastic)](https://android-arsenal.com/api?level=15)
[![license](https://img.shields.io/github/license/switch-iot/n2n_vLTS.svg?style=plastic)](https://www.gnu.org/licenses/gpl-3.0)
[![GitHub release](https://img.shields.io/github/release/switch-iot/n2n_vLTS.svg?style=plastic)](https://github.com/switch-iot/n2n_vLTS/releases)
[![Github All Releases](https://img.shields.io/github/downloads/switch-iot/n2n_vLTS/total.svg?style=plastic)](https://github.com/switch-iot/n2n_vLTS/releases)
[![Travis branch](https://img.shields.io/travis/switch-iot/n2n_vLTS/master.svg?style=plastic)](https://travis-ci.org/switch-iot/n2n_vLTS)

The original n2n supports many platforms, including windows, linux, osx, bsd, openwrt, raspberry pie, etc., except for mobile phones(non-root). Therefore, we have developed the Hin2n project.

### What is Hin2n
- Hin2n is a mobile VPN app that supports the n2n protocol
- Hin2n does not need a rooted phone
- Hin2n only supports Android phones for now, IPhone version will be developed in the future
- Hin2n is currently in continuous development and will gradually provide more complete versions
- Hin2n only supports [n2n_v2s](#about-v2s-version) protocol now, other versions of n2n protocol are under development

### Hin2n latest version [CHANGELOG](An2n/CHANGELOG)
The latest version of Hin2n is available for download at [release link](https://github.com/switch-iot/n2n_vLTS/releases).

### Hin2n Development Plan
View the development plan at [`Projects`](https://github.com/switch-iot/n2n_vLTS/projects).
If you have new features and ideas, you can submit them in [`issues`](https://github.com/switch-iot/n2n_vLTS/issues), and we will arrange development plans as appropriate. Your concern is our motivation.

### Technical principle
- VPNService
> Hin2n is based on Android's native VPNService. It builds a tun virtual network card through VPNService and communicates with supernode and edges.
- tun2tap
> Android only supports tun virtual network card, only support network layer, and n2n  requires tap virtual network card, which needs data link layer support. So we simulated the data link layer and ARP protocol.
- n2n protocol
> Hin2n supports the n2n protocol by using the native method of jni to reuse the code of the original n2n project as far as possible.

## N2N protocol version
There are three popular versions of the n2n project
- Version v1 developed by the great masters of ntop.org. Project address：https://github.com/meyerd/n2n.git(n2n_v1)
- Version v2 developed by the great masters of ntop.org. Project address：https://github.com/ntop/n2n.git
- Version [v2s](#about-v2s-version) developed by the master Meyerd. Project address：https://github.com/meyerd/n2n.git(n2n_v2)

### About v2s version
The v2s is the renaming of the v2 (also known as v2.1) developed by master Meyerd in the QQ group(256572040), that is, the v2 upgrade version. The v2s version is not compatible with the v2 version developed by the ntop.org masters. To avoid confusion, the QQ group friends named the project separately.

## Development and compilation
### Hin2n
- git clone https://github.com/switch-iot/n2n_vLTS.git `--recurse-submodules`
- The windows environment needs to execute `link.bat` in the project folder to replace symbolic links under Linux.
- An2n directory is the Hin2n project Android source directory
- Execute `gradlew assemble` in An2n directory to compile Hin2n(For historical reasons, the original project was called an2n)
- The gradle version of the An2n project is 2.14.1. If you want to upgrade the gradle version to 4.4, copy the file under the branch `dev_android_gradle4.4` to the branch `marster`/`dev_android` to overwrite the corresponding files.

### About open source agreement
The project is open sourced under the [`GPLv3`](LICENSE) agreement, and is consistent with the original open source agreement of n2n. We also hope that everyone will support and comply with the open source agreement of this project.

## Contribute to Hin2n
Hin2n is a free and open source n2n project, and we welcome anyone to contribute to it.
- Any problems in use can be fed back through ['issues'](https://github.com/switch-iot/n2n_vLTS/issues)
- Bug fixes can submit `Pull Request` to `android_dev` branch
- If you want to add a new feature, please create an [`issues`](https://github.com/switch-iot/n2n_vLTS/issues) first to describe the new feature, as well as the implementation approach. Once a proposal is accepted, create an implementation of the new features and submit it as a pull request.
- Sorry for my poor english and improvement for this document is welcome even some typo fix.
- Welcome to pay attention to the project and give the project a `Star`

### Contributors
- [`lucktu`](https://github.com/lucktu) is the initiator of the Hin2n project and plays a crucial role in the project. We thank [`lucktu`](https://github.com/lucktu) for organizing, promoting and testing for the project.
- [`zhangbz`](https://github.com/zhangbz) is mainly responsible for the development of the Android level, and has given strong support in the most difficult time of the project. The participation of [`zhangbz`](https://github.com/zhangbz) has enabled the project to continue.
- At the same time, thank all the friends for their support

## QQ group
- Hin2n QQ group： 769731491
- N2N QQ group： 256572040
