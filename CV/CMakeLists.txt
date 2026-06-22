cmake_minimum_required(VERSION 3.10)
project(CV)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find OpenCV package on your system
find_package(OpenCV REQUIRED)

# Include OpenCV headers
include_directories(${OpenCV_INCLUDE_DIRS})

# Define the executable name and source file
add_executable(app 
	src/recognize.cpp)

# Link the OpenCV libraries to your executable
target_link_libraries(app ${OpenCV_LIBS})

