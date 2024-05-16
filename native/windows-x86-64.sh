rm -r '../library/src/jvmMain/resources/jni/windows-x86-64'
rm -r './build/Windows/x86_64'
cmake -DCMAKE_TOOLCHAIN_FILE=toolchain-Windows-x86_64.cmake -DCMAKE_BUILD_TYPE=Release -B './build/Windows/x86_64'
cmake --build './build/Windows/x86_64'
mkdir -p '../library/src/jvmMain/resources/jni/windows-x86-64/'
cp './build/Windows/x86_64/libjnifbclient.dll' '../library/src/jvmMain/resources/jni/windows-x86-64/jnifbclient.dll'