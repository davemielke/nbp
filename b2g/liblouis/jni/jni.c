#include <stdlib.h>
#include <unistd.h>
#include <liblouis.h>
#include <jni.h>

#define JAVA_METHOD(object, name, type, ...) \
  JNIEXPORT type JNICALL Java_ ## object ## _ ## name ( \
    JNIEnv *env, jobject this, ## __VA_ARGS__ \
  )

JAVA_METHOD(
  org_liblouis_Louis, getVersion, jstring
) {
  return (*env)->NewStringUTF(env, lou_version());
}
