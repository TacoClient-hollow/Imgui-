
if(NOT "/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-subbuild/dobby-populate-prefix/src/dobby-populate-stamp/dobby-populate-gitinfo.txt" IS_NEWER_THAN "/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-subbuild/dobby-populate-prefix/src/dobby-populate-stamp/dobby-populate-gitclone-lastrun.txt")
  message(STATUS "Avoiding repeated git clone, stamp file is up to date: '/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-subbuild/dobby-populate-prefix/src/dobby-populate-stamp/dobby-populate-gitclone-lastrun.txt'")
  return()
endif()

execute_process(
  COMMAND ${CMAKE_COMMAND} -E rm -rf "/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-src"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to remove directory: '/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-src'")
endif()

# try the clone 3 times in case there is an odd git clone issue
set(error_code 1)
set(number_of_tries 0)
while(error_code AND number_of_tries LESS 3)
  execute_process(
    COMMAND "/usr/bin/git"  clone --no-checkout --config "advice.detachedHead=false" "https://github.com/chiteroman/Dobby.git" "dobby-src"
    WORKING_DIRECTORY "/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps"
    RESULT_VARIABLE error_code
    )
  math(EXPR number_of_tries "${number_of_tries} + 1")
endwhile()
if(number_of_tries GREATER 1)
  message(STATUS "Had to git clone more than once:
          ${number_of_tries} times.")
endif()
if(error_code)
  message(FATAL_ERROR "Failed to clone repository: 'https://github.com/chiteroman/Dobby.git'")
endif()

execute_process(
  COMMAND "/usr/bin/git"  checkout e793d10700ecffac6bc7ce58d218faf31cd68d35 --
  WORKING_DIRECTORY "/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-src"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to checkout tag: 'e793d10700ecffac6bc7ce58d218faf31cd68d35'")
endif()

set(init_submodules TRUE)
if(init_submodules)
  execute_process(
    COMMAND "/usr/bin/git"  submodule update --recursive --init 
    WORKING_DIRECTORY "/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-src"
    RESULT_VARIABLE error_code
    )
endif()
if(error_code)
  message(FATAL_ERROR "Failed to update submodules in: '/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-src'")
endif()

# Complete success, update the script-last-run stamp file:
#
execute_process(
  COMMAND ${CMAKE_COMMAND} -E copy
    "/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-subbuild/dobby-populate-prefix/src/dobby-populate-stamp/dobby-populate-gitinfo.txt"
    "/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-subbuild/dobby-populate-prefix/src/dobby-populate-stamp/dobby-populate-gitclone-lastrun.txt"
  RESULT_VARIABLE error_code
  )
if(error_code)
  message(FATAL_ERROR "Failed to copy script-last-run stamp file: '/home/uptightsuperlabs/Desktop/taco-client/app/.cxx/Debug/554z3q53/x86/_deps/dobby-subbuild/dobby-populate-prefix/src/dobby-populate-stamp/dobby-populate-gitclone-lastrun.txt'")
endif()

