hin2n bundles prebuilt OpenSSL 1.1.1g libraries. These are necessary to enable the AES encryption in the ntop n2n.

The following commands can be used to generate them:

```bash
# Reference
# https://wiki.openssl.org/index.php/Android#Build_the_OpenSSL_Library_2
# https://github.com/leenjewel/openssl_for_ios_and_android/blob/master/tools/build-android-openssl.sh

# Toolchain paths (REPLACE THESE BASED ON YOUR ENVIRONMENT)
export PATH="$PATH:/home/emanuele/android-sdk/ndk-bundle/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin"
export PATH="$PATH:/home/emanuele/android-sdk/ndk-bundle/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64/bin"
export PATH="$PATH:/home/emanuele/android-sdk/ndk-bundle/toolchains/x86-4.9/prebuilt/linux-x86_64/bin"
export PATH="$PATH:/home/emanuele/android-sdk/ndk-bundle/toolchains/x86_64-4.9/prebuilt/linux-x86_64/bin"

# Preparation
mkdir android-openssl
cd android-openssl
wget https://wiki.openssl.org/images/7/70/Setenv-android.sh
wget https://www.openssl.org/source/openssl-1.1.1g.tar.gz
tar -xf openssl-1.1.1g.tar.gz
tr -d '^M' < Setenv-android.sh > Setenv-android.sh
source ./Setenv-android.sh
cd openssl-1.0.1g

# Build ARM target
make clean
./Configure android-arm --prefix="${PWD}/../openssl-armeabi-v7a"
make -j`nproc` SHLIB_VERSION_NUMBER= SHLIB_EXT=.so
make install_sw SHLIB_VERSION_NUMBER= SHLIB_EXT=.so

# Build ARM64 target
make clean
./Configure android-arm64 --prefix="${PWD}/../openssl-arm64-v8a"
make -j`nproc` SHLIB_VERSION_NUMBER= SHLIB_EXT=.so
make install_sw SHLIB_VERSION_NUMBER= SHLIB_EXT=.so

# Build x86 target
make clean
./Configure android-x86 --prefix="${PWD}/../openssl-x86"
make -j`nproc` SHLIB_VERSION_NUMBER= SHLIB_EXT=.so
make install_sw SHLIB_VERSION_NUMBER= SHLIB_EXT=.so

# Build x86_64 target
make clean
./Configure android-x86_64 --prefix="${PWD}/../openssl-x86_64"
make -j`nproc` SHLIB_VERSION_NUMBER= SHLIB_EXT=.so
make install_sw SHLIB_VERSION_NUMBER= SHLIB_EXT=.so
```

The commands above will create header and .so files for the 4 supported archs. Such files are then copied to the `Hin2n_android/app/src/main/jniLibs` directory to make them available for the apk build.
