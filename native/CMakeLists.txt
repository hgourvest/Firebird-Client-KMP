cmake_minimum_required(VERSION 3.5)

if (NOT ANDROID)
  find_package(JNI REQUIRED)
endif()

project("jnifbclient")

if (NOT ANDROID)
  include_directories(${JNI_INCLUDE_DIRS})
  include_directories(jni/${CMAKE_SYSTEM_NAME})
endif()

include_directories(include)

set(CMAKE_C_STANDARD 99)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS_RELEASE "-O3")
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -Wno-dangling-else -Wno-missing-exception-spec -Wno-unused-value")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden -Wno-dangling-else  -Wno-ignored-attributes")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fvisibility=hidden")

add_library(jnifbclient SHARED firebird-lib-jni.cpp)

if(WIN32)
   target_link_libraries(
        jnifbclient PRIVATE -static-libgcc -static-libstdc++)
else()
    target_link_libraries(jnifbclient)
endif()
