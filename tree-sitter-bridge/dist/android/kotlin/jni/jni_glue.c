#include <jni.h>
#include <stdint.h>
#include <stdbool.h>
#include <limits.h>
#include <string.h>
#include <stdlib.h>
#if defined(__ANDROID__)
#include <pthread.h>
#endif

#include "tree-sitter-bridge.h"
static void boltffi_jni_throw_runtime(JNIEnv *env, const char *message) {
    jclass exception_class = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (exception_class == NULL) {
        return;
    }
    (*env)->ThrowNew(env, exception_class, message);
    (*env)->DeleteLocalRef(env, exception_class);
}

static void boltffi_jni_throw_illegal_argument(JNIEnv *env, const char *message) {
    jclass exception_class = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (exception_class == NULL) {
        return;
    }
    (*env)->ThrowNew(env, exception_class, message);
    (*env)->DeleteLocalRef(env, exception_class);
}

static void boltffi_jni_throw_error_buffer(JNIEnv *env, FfiBuf_u8 buffer) {
    if (buffer.len > ((uintptr_t)INT32_MAX)) {
        boltffi_free_buf(buffer);
        boltffi_jni_throw_runtime(env, "BoltFFI error buffer was too large");
        return;
    }
    jbyteArray bytes = (*env)->NewByteArray(env, (jsize)buffer.len);
    if (bytes != NULL && buffer.len != 0) {
        (*env)->SetByteArrayRegion(env, bytes, 0, (jsize)buffer.len, (const jbyte *)buffer.ptr);
    }
    boltffi_free_buf(buffer);
    if (bytes == NULL || (*env)->ExceptionCheck(env)) {
        return;
    }
    jclass exception_class = (*env)->FindClass(env, "starmage27/shumide/BoltFfiErrorBufferException");
    if (exception_class == NULL) {
        (*env)->DeleteLocalRef(env, bytes);
        return;
    }
    jmethodID constructor = (*env)->GetMethodID(env, exception_class, "<init>", "([B)V");
    if (constructor == NULL) {
        (*env)->DeleteLocalRef(env, exception_class);
        (*env)->DeleteLocalRef(env, bytes);
        return;
    }
    jthrowable exception = (jthrowable)(*env)->NewObject(env, exception_class, constructor, bytes);
    if (exception != NULL) {
        (*env)->Throw(env, exception);
        (*env)->DeleteLocalRef(env, exception);
    }
    (*env)->DeleteLocalRef(env, exception_class);
    (*env)->DeleteLocalRef(env, bytes);
}

static jbyteArray boltffi_jni_buffer_to_byte_array(JNIEnv *env, FfiBuf_u8 buffer) {
    if (buffer.ptr == NULL) {
        if (buffer.len != 0) {
            boltffi_jni_throw_runtime(env, "BoltFFI buffer pointer was null with non-zero length");
            return NULL;
        }
        return (*env)->NewByteArray(env, 0);
    }
    if (buffer.len > (uintptr_t)INT32_MAX) {
        boltffi_free_buf(buffer);
        boltffi_jni_throw_runtime(env, "BoltFFI buffer too large for Java byte array");
        return NULL;
    }
    jbyteArray array = (*env)->NewByteArray(env, (jsize)buffer.len);
    if (array == NULL) {
        boltffi_free_buf(buffer);
        return NULL;
    }
    (*env)->SetByteArrayRegion(env, array, 0, (jsize)buffer.len, (const jbyte *)buffer.ptr);
    boltffi_free_buf(buffer);
    if ((*env)->ExceptionCheck(env)) {
        (*env)->DeleteLocalRef(env, array);
        return NULL;
    }
    return array;
}

static inline jbyteArray boltffi_jni_bytes_to_byte_array(JNIEnv *env, const uint8_t *bytes, uintptr_t len) {
    if (bytes == NULL && len != 0) {
        boltffi_jni_throw_runtime(env, "BoltFFI byte slice pointer was null with non-zero length");
        return NULL;
    }
    if (len > (uintptr_t)INT32_MAX) {
        boltffi_jni_throw_runtime(env, "BoltFFI byte slice too large for Java byte array");
        return NULL;
    }
    jbyteArray array = (*env)->NewByteArray(env, (jsize)len);
    if (array == NULL) {
        return NULL;
    }
    if (len != 0) {
        (*env)->SetByteArrayRegion(env, array, 0, (jsize)len, (const jbyte *)bytes);
    }
    return array;
}

static inline FfiBuf_u8 boltffi_jni_byte_array_to_buffer(JNIEnv *env, jbyteArray array) {
    FfiBuf_u8 empty = {0};
    if (array == NULL) {
        boltffi_jni_throw_runtime(env, "BoltFFI byte array return was null");
        return empty;
    }
    jsize len = (*env)->GetArrayLength(env, array);
    if (len == 0) {
        return empty;
    }
    FfiBuf_u8 buffer = boltffi_buf_with_len((uintptr_t)len);
    if (buffer.ptr == NULL) {
        boltffi_jni_throw_runtime(env, "failed to allocate BoltFFI byte array return");
        return empty;
    }
    (*env)->GetByteArrayRegion(env, array, 0, len, (jbyte *)buffer.ptr);
    return buffer;
}

static bool boltffi_jni_direct_buffer_address(JNIEnv *env, jobject buffer, jlong required_capacity, void **address) {
    if (buffer == NULL) {
        boltffi_jni_throw_illegal_argument(env, "BoltFFI direct buffer argument was null");
        return false;
    }
    if (required_capacity < 0) {
        boltffi_jni_throw_illegal_argument(env, "BoltFFI direct buffer length was negative");
        return false;
    }
    jlong capacity = (*env)->GetDirectBufferCapacity(env, buffer);
    if (capacity < 0) {
        boltffi_jni_throw_illegal_argument(env, "BoltFFI argument was not a direct buffer");
        return false;
    }
    if (capacity < required_capacity) {
        boltffi_jni_throw_illegal_argument(env, "BoltFFI direct buffer capacity was too small");
        return false;
    }
    *address = (*env)->GetDirectBufferAddress(env, buffer);
    if (*address == NULL && required_capacity != 0) {
        boltffi_jni_throw_illegal_argument(env, "BoltFFI direct buffer address was unavailable");
        return false;
    }
    return true;
}

JNIEXPORT void JNICALL Java_starmage27_shumide_Native_boltffi_1release_1class_1tree_1sitter_1bridge_1ts_1bridge(JNIEnv *env, jclass cls, jlong handle) {
    (void)cls;

    (void)env;
    boltffi_release_class_tree_sitter_bridge_ts_bridge(handle);

    return;
}

JNIEXPORT jlong JNICALL Java_starmage27_shumide_Native_boltffi_1init_1class_1tree_1sitter_1bridge_1ts_1bridge_1new(JNIEnv *env, jclass cls) {
    (void)cls;

    (void)env;
    uint64_t __boltffi_result = boltffi_init_class_tree_sitter_bridge_ts_bridge_new();

    return (jlong)__boltffi_result;
}

JNIEXPORT void JNICALL Java_starmage27_shumide_Native_boltffi_1method_1class_1tree_1sitter_1bridge_1ts_1bridge_1set_1language(JNIEnv *env, jclass cls, jlong receiver, jint lang) {
    (void)cls;

    FfiBuf_u8 error = boltffi_method_class_tree_sitter_bridge_ts_bridge_set_language(receiver, (___TSLang)lang);

    if (error.ptr != NULL || error.len != 0) {
        boltffi_jni_throw_error_buffer(env, error);
        return;
    }

    return;
}

JNIEXPORT jbyteArray JNICALL Java_starmage27_shumide_Native_boltffi_1method_1class_1tree_1sitter_1bridge_1ts_1bridge_1get_1kinds_1for_1selected_1language(JNIEnv *env, jclass cls, jlong receiver) {
    (void)cls;

    FfiBuf_u8 __boltffi_return = (FfiBuf_u8){0};

    FfiBuf_u8 error = boltffi_method_class_tree_sitter_bridge_ts_bridge_get_kinds_for_selected_language(receiver, &__boltffi_return);

    if (error.ptr != NULL || error.len != 0) {
        boltffi_jni_throw_error_buffer(env, error);
        return NULL;
    }

    return boltffi_jni_buffer_to_byte_array(env, __boltffi_return);
}

JNIEXPORT void JNICALL Java_starmage27_shumide_Native_boltffi_1method_1class_1tree_1sitter_1bridge_1ts_1bridge_1set_1initial_1string(JNIEnv *env, jclass cls, jlong receiver, jobject source_string, jint __boltffi_source_string_len) {
    (void)cls;

    void *__boltffi_source_string_ptr = NULL;

    if (!boltffi_jni_direct_buffer_address(env, source_string, (jlong)__boltffi_source_string_len, &__boltffi_source_string_ptr)) {
        goto __boltffi_error;
    }

    FfiBuf_u8 error = boltffi_method_class_tree_sitter_bridge_ts_bridge_set_initial_string(receiver, (const uint8_t *)__boltffi_source_string_ptr, (uintptr_t)__boltffi_source_string_len);

    if (error.ptr != NULL || error.len != 0) {
        boltffi_jni_throw_error_buffer(env, error);
        return;
    }

    return;
__boltffi_error:
    return;
}

JNIEXPORT jbyteArray JNICALL Java_starmage27_shumide_Native_boltffi_1method_1class_1tree_1sitter_1bridge_1ts_1bridge_1get_1previous_1parse(JNIEnv *env, jclass cls, jlong receiver) {
    (void)cls;

    FfiBuf_u8 __boltffi_return = (FfiBuf_u8){0};

    FfiBuf_u8 error = boltffi_method_class_tree_sitter_bridge_ts_bridge_get_previous_parse(receiver, &__boltffi_return);

    if (error.ptr != NULL || error.len != 0) {
        boltffi_jni_throw_error_buffer(env, error);
        return NULL;
    }

    return boltffi_jni_buffer_to_byte_array(env, __boltffi_return);
}

JNIEXPORT jbyteArray JNICALL Java_starmage27_shumide_Native_boltffi_1method_1class_1tree_1sitter_1bridge_1ts_1bridge_1parse_1everything(JNIEnv *env, jclass cls, jlong receiver, jobject source_string, jint __boltffi_source_string_len) {
    (void)cls;

    void *__boltffi_source_string_ptr = NULL;
    FfiBuf_u8 __boltffi_return = (FfiBuf_u8){0};

    if (!boltffi_jni_direct_buffer_address(env, source_string, (jlong)__boltffi_source_string_len, &__boltffi_source_string_ptr)) {
        goto __boltffi_error;
    }

    FfiBuf_u8 error = boltffi_method_class_tree_sitter_bridge_ts_bridge_parse_everything(receiver, (const uint8_t *)__boltffi_source_string_ptr, (uintptr_t)__boltffi_source_string_len, &__boltffi_return);

    if (error.ptr != NULL || error.len != 0) {
        boltffi_jni_throw_error_buffer(env, error);
        return NULL;
    }

    return boltffi_jni_buffer_to_byte_array(env, __boltffi_return);
__boltffi_error:
    return NULL;
}

JNIEXPORT jbyteArray JNICALL Java_starmage27_shumide_Native_boltffi_1method_1class_1tree_1sitter_1bridge_1ts_1bridge_1parse_1changes(JNIEnv *env, jclass cls, jlong receiver, jobject changed_part, jint __boltffi_changed_part_len, jobject diff_range) {
    (void)cls;

    void *__boltffi_changed_part_ptr = NULL;
    void *__boltffi_diff_range_ptr = NULL;
    ___DiffRange __boltffi_diff_range_value;
    FfiBuf_u8 __boltffi_return = (FfiBuf_u8){0};

    if (!boltffi_jni_direct_buffer_address(env, changed_part, (jlong)__boltffi_changed_part_len, &__boltffi_changed_part_ptr)) {
        goto __boltffi_error;
    }

    if (!boltffi_jni_direct_buffer_address(env, diff_range, (jlong)sizeof(___DiffRange), &__boltffi_diff_range_ptr)) {
        goto __boltffi_error;
    }
    memcpy(&__boltffi_diff_range_value, __boltffi_diff_range_ptr, sizeof(___DiffRange));

    FfiBuf_u8 error = boltffi_method_class_tree_sitter_bridge_ts_bridge_parse_changes(receiver, (const uint8_t *)__boltffi_changed_part_ptr, (uintptr_t)__boltffi_changed_part_len, __boltffi_diff_range_value, &__boltffi_return);

    if (error.ptr != NULL || error.len != 0) {
        boltffi_jni_throw_error_buffer(env, error);
        return NULL;
    }

    return boltffi_jni_buffer_to_byte_array(env, __boltffi_return);
__boltffi_error:
    return NULL;
}
