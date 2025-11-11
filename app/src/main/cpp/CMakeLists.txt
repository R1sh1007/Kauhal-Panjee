cmake_minimum_required(VERSION 3.18.1)


project("secure-keys")

add_library(
        secure-keys
        SHARED
        secure-keys.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        secure-keys
        ${log-lib}
)