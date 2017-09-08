#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_choch_michaeldicioccio_myapplication_OldMainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
