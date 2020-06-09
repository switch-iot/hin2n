project(Hin2n)
cmake_minimum_required(VERSION 3.4.1)

# N2n information
set(N2N_VERSION 2.1.0)
set(N2N_OSNAME ${CMAKE_SYSTEM})
set(N2N_MODIFY_VERSION v2s_0.1.0)
set(N2N_MODIFY_AUTHOR "switchwang(https://github.com/switch-st) zhangbz(https://github.com/zhangbz)")

INCLUDE(TestBigEndian)
TEST_BIG_ENDIAN(UIP_CONF_BYTE_ORDER)
if(${UIP_CONF_BYTE_ORDER} EQUAL 1)
set(UIP_CONF_BYTE_ORDER UIP_BIG_ENDIAN)
else(${UIP_CONF_BYTE_ORDER} EQUAL 1)
set(UIP_CONF_BYTE_ORDER UIP_LITTLE_ENDIAN)
endif(${UIP_CONF_BYTE_ORDER} EQUAL 1)

# @TODO set N2N_OPTION_AES for openssl, add_definitions(-DN2N_HAVE_AES)

if(CMAKE_BUILD_TYPE)
if(NOT ${CMAKE_BUILD_TYPE} STREQUAL Debug)
set(CMAKE_BUILD_TYPE Release)
endif(NOT ${CMAKE_BUILD_TYPE} STREQUAL Debug)
else(CMAKE_BUILD_TYPE)
set(CMAKE_BUILD_TYPE Release)
endif(CMAKE_BUILD_TYPE)


add_definitions(-DN2N_VERSION=\"${N2N_VERSION}\" -DN2N_OSNAME=\"${N2N_OSNAME}\" -DN2N_MODIFY_VERSION=\"${N2N_MODIFY_VERSION}\" -DN2N_MODIFY_AUTHOR=\"${N2N_MODIFY_AUTHOR}\" -DUIP_CONF_BYTE_ORDER=\"${UIP_CONF_BYTE_ORDER}\")
include_directories(src/main/cpp src/main/cpp/edge_jni src/main/cpp/slog src/main/cpp/tun2tap src/main/cpp/uip)

add_library(edge_jni SHARED
                src/main/cpp/edge_jni/edge_jni.c
            )
target_link_libraries(edge_jni edge_v2s)
target_link_libraries(edge_jni edge_v2)
target_link_libraries(edge_jni edge_v1)
target_link_libraries(edge_jni slog)

add_library(edge_v2s SHARED
                src/main/cpp/n2n_v2s/edge.c
            )
target_link_libraries(edge_v2s n2n_v2s)
target_link_libraries(edge_v2s slog)

add_library(edge_v2 SHARED
                src/main/cpp/n2n_v2/android/edge_android.c
                src/main/cpp/n2n_v2/edge_utils.c

                src/main/cpp/n2n_v2/n2n.c
                src/main/cpp/n2n_v2/n2n_keyfile.c
                src/main/cpp/n2n_v2/wire.c
                src/main/cpp/n2n_v2/minilzo.c
                src/main/cpp/n2n_v2/twofish.c
                src/main/cpp/n2n_v2/transform_null.c
                src/main/cpp/n2n_v2/transform_tf.c
                src/main/cpp/n2n_v2/transform_aes.c
                src/main/cpp/n2n_v2/android/tuntap_android.c
                src/main/cpp/n2n_v2/version.c
            )
target_link_libraries(edge_v2 uip)
target_link_libraries(edge_v2 slog)

add_library(edge_v1 SHARED
                src/main/cpp/n2n_v1/edge.c
            )
target_link_libraries(edge_v1 n2n_v1)
target_link_libraries(edge_v1 slog)

add_library(n2n_v2s SHARED
                src/main/cpp/n2n_v2s/n2n.c
                src/main/cpp/n2n_v2s/n2n_keyfile.c
                src/main/cpp/n2n_v2s/wire.c
                src/main/cpp/n2n_v2s/minilzo.c
                src/main/cpp/n2n_v2s/twofish.c
                src/main/cpp/n2n_v2s/transform_null.c
                src/main/cpp/n2n_v2s/transform_tf.c
                src/main/cpp/n2n_v2s/transform_aes.c
                src/main/cpp/n2n_v2s/android/tuntap_android.c
                src/main/cpp/n2n_v2s/version.c
            )
target_link_libraries(n2n_v2s uip)
target_link_libraries(n2n_v2s slog)

add_library(n2n_v1 SHARED
                src/main/cpp/n2n_v1/n2n.c
                src/main/cpp/n2n_v1/minilzo.c
                src/main/cpp/n2n_v1/twofish.c
                src/main/cpp/n2n_v1/android/tuntap_android.c
                src/main/cpp/n2n_v1/version.c
            )
target_link_libraries(n2n_v1 uip)
target_link_libraries(n2n_v1 slog)

add_library(uip SHARED
                src/main/cpp/uip/uip.c
                src/main/cpp/uip/uip_arp.c
                src/main/cpp/tun2tap/tun2tap.c
            )

add_library(slog SHARED
                src/main/cpp/slog/slog.c
            )
target_link_libraries(slog log)
