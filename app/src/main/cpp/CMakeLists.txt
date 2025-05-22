cmake_minimum_required(VERSION 3.22.1)
project("mc")

set(CMAKE_CXX_STANDARD 23)

# Add ImGui sources (no wildcards, explicit list recommended)
set(IMGUI_SOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/include/imgui/imgui.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/include/imgui/imgui_draw.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/include/imgui/imgui_tables.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/include/imgui/imgui_widgets.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/include/imgui/backends/imgui_impl_android.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/include/imgui/backends/imgui_impl_opengl3.cpp
)

add_library(${CMAKE_PROJECT_NAME} SHARED
    src/main.cpp
    ${IMGUI_SOURCES}
)

# Key fix: Include directories for <imgui/imgui.h>
target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/include      # Allows #include <imgui/imgui.h>
    # Backends are under include/imgui/backends, so no extra path needed
)

# Optional: Mark as system includes to suppress warnings
# target_include_directories(${CMAKE_PROJECT_NAME} SYSTEM PRIVATE
#     ${CMAKE_CURRENT_SOURCE_DIR}/include
# )

# Dependencies
include(cmake/CPM.cmake)

CPMAddPackage(
    NAME Dobby
    GIT_REPOSITORY https://github.com/chiteroman/Dobby.git
    GIT_TAG e793d10700ecffac6bc7ce58d218faf31cd68d35
)

target_link_libraries(${CMAKE_PROJECT_NAME} PRIVATE
    android
    dobby
    EGL
    GLESv3
    log
)