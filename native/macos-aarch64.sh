rm -r '../library/src/jvmMain/resources/jni/macos-aarch64/'
rm -r './build/Darwin/aarch64'
cmake -DCMAKE_TOOLCHAIN_FILE=toolchain-Darwin-aarch64.cmake -DCMAKE_BUILD_TYPE=Release -B './build/Darwin/aarch64'
cmake --build './build/Darwin/aarch64'
mkdir -p '../library/src/jvmMain/resources/jni/macos-aarch64/'
cp './build/Darwin/aarch64/libjnifbclient.dylib' '../library/src/jvmMain/resources/jni/macos-aarch64/'