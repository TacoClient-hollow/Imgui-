cmake_minimum_required(VERSION 3.10)

# Project name
project(taco_client)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)

# Specify the native library
add_library(mc SHARED 
    mc-init.cpp
    imgui/imgui.cpp
    imgui/imgui_draw.cpp
    imgui/imgui_widgets.cpp
    imgui/imgui_tables.cpp
    backends/imgui_impl_opengl3.cpp
    backends/imgui_impl_android.cpp
)

# Include directories
include_directories(
    dobby
    imgui
    backends
)

# Link the Dobby library
add_library(dobby STATIC IMPORTED)
set_target_properties(dobby PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/dobby/libraries/${ANDROID_ABI}/libdobby.a
)

# Link the libraries
target_link_libraries(mc
    dobby
    android
    log
    EGL
    GLESv3
)
