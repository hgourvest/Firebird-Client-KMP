rm -r '../library/src/jvmMain/resources/jni/linux-arm/'
rm -r './build/Linux/arm'
cmake -DCMAKE_TOOLCHAIN_FILE=toolchain-Linux-arm.cmake -DCMAKE_BUILD_TYPE=Release -B './build/Linux/arm'
cmake --build './build/Linux/arm'
mkdir -p '../library/src/jvmMain/resources/jni/linux-arm/'
cp './build/Linux/arm/libjnifbclient.so' '../library/src/jvmMain/resources/jni/linux-arm/'